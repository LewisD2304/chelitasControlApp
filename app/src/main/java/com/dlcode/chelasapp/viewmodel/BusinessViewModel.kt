package com.dlcode.chelasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlcode.chelasapp.model.*
import com.dlcode.chelasapp.repository.BusinessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.google.gson.Gson

class BusinessViewModel(private val repository: BusinessRepository) : ViewModel() {

    private val gson = Gson()

    val config = repository.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessConfig())
    val activeJornada = repository.activeJornada.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allSales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPurchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAbonos = repository.allAbonos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allJornadas = repository.allJornadas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state for "Vender" screen draft
    private val _saleDraft = MutableStateFlow(VentaDraft())
    val saleDraft = _saleDraft.asStateFlow()

    data class VentaDraft(
        val boxes: Int = 0,
        val bottles: Int = 0,
        val discount: Long = 0,
        val method: String = "Efectivo",
        val client: String = "",
        val payEfectivo: Long = 0,
        val payYape: Long = 0,
        val payPlin: Long = 0,
        val payTarjeta: Long = 0,
        val cashReceived: Long = 0
    )

    fun updateDraft(update: (VentaDraft) -> VentaDraft) {
        _saleDraft.update(update)
    }

    fun clearDraft() {
        _saleDraft.value = VentaDraft()
    }

    val inventoryState = combine(allPurchases, allSales) { purchases, sales ->
        var totalUnits = 0
        var totalCost = 0L
        
        purchases.sortedBy { it.id }.forEach {
            totalUnits += it.units
            totalCost += it.amount
        }
        
        // Only active sales subtract from inventory
        val soldUnits = sales.filter { it.status == "active" }.sumOf { it.units }
        val remainingUnits = totalUnits - soldUnits
        
        val avgCost = if (totalUnits > 0) totalCost.toDouble() / totalUnits else 0.0
        
        InventoryState(
            stock = remainingUnits,
            totalValue = if (totalUnits > 0) (totalCost * remainingUnits / totalUnits) else 0L,
            averageCost = avgCost
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InventoryState())

    fun saveConfig(newConfig: BusinessConfig) {
        viewModelScope.launch {
            repository.saveConfig(newConfig)
        }
    }

    fun openJornada(date: String, openingCash: Long) {
        viewModelScope.launch {
            repository.openJornada(date, openingCash, inventoryState.value.stock)
        }
    }

    fun registerSale(draft: VentaDraft) {
        val currentConfig = config.value ?: BusinessConfig()
        val jornada = activeJornada.value ?: return
        
        val promoGroups = draft.bottles / currentConfig.promoN
        val remainderUnits = draft.bottles % currentConfig.promoN
        val subtotal = (draft.boxes * currentConfig.boxPrice) + 
                       (promoGroups * currentConfig.promoPrice) + 
                       (remainderUnits * currentConfig.unitPrice)
        val total = subtotal - draft.discount
        val units = (draft.boxes * currentConfig.bottlesPerPack) + draft.bottles
        
        val avgCostVal = inventoryState.value.averageCost

        val venta = Venta(
            date = jornada.date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            boxes = draft.boxes,
            bottles = draft.bottles,
            units = units,
            subtotal = subtotal,
            discount = draft.discount,
            total = total,
            cost = (units * avgCostVal).toLong(),
            method = draft.method,
            client = if (draft.client.isBlank()) null else draft.client,
            cashReceived = draft.cashReceived,
            change = if (draft.method == "Efectivo") draft.cashReceived - total else 0,
            creditAmount = if (draft.method == "Fiado") total else 0,
            appliedPackSize = currentConfig.bottlesPerPack,
            appliedBoxPrice = currentConfig.boxPrice,
            appliedUnitPrice = currentConfig.unitPrice,
            appliedPromoN = currentConfig.promoN,
            appliedPromoPrice = currentConfig.promoPrice,
            payEfectivo = if (draft.method == "Efectivo") total else draft.payEfectivo,
            payYape = if (draft.method == "Yape") total else draft.payYape,
            payPlin = if (draft.method == "Plin") total else draft.payPlin,
            payTarjeta = if (draft.method == "Tarjeta") total else draft.payTarjeta
        )
        
        viewModelScope.launch {
            repository.registerSale(venta)
            clearDraft()
        }
    }

    fun updateSale(saleId: Long, draft: VentaDraft, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val oldSale = repository.getSaleById(saleId) ?: return@launch onResult(false, "Venta no encontrada")
            val jornada = repository.getJornadaByDate(oldSale.date)
            
            if (jornada == null || jornada.isClosed) {
                return@launch onResult(false, "La jornada original de esta venta está cerrada.")
            }

            // Check if abonos exist and if the new total is compatible
            val abonos = repository.getAbonosForSale(saleId).first()
            val abonosTotal = abonos.sumOf { it.amount }
            
            val promoGroups = draft.bottles / oldSale.appliedPromoN
            val remainderUnits = draft.bottles % oldSale.appliedPromoN
            val subtotal = (draft.boxes * oldSale.appliedBoxPrice) + 
                           (promoGroups * oldSale.appliedPromoPrice) + 
                           (remainderUnits * oldSale.appliedUnitPrice)
            val total = subtotal - draft.discount
            
            if (draft.method != "Fiado" && abonosTotal > 0) {
                return@launch onResult(false, "No se puede cambiar a contado si ya existen abonos.")
            }
            
            if (draft.method == "Fiado" && total < abonosTotal) {
                return@launch onResult(false, "El nuevo total no puede ser menor a lo ya abonado (S/ ${abonosTotal/100.0}).")
            }

            val units = (draft.boxes * oldSale.appliedPackSize) + draft.bottles
            val newSale = oldSale.copy(
                boxes = draft.boxes,
                bottles = draft.bottles,
                units = units,
                subtotal = subtotal,
                discount = draft.discount,
                total = total,
                cost = (units * (if (oldSale.units > 0) oldSale.cost / oldSale.units else 0)),
                method = draft.method,
                client = if (draft.client.isBlank()) null else draft.client,
                cashReceived = draft.cashReceived,
                change = if (draft.method == "Efectivo") draft.cashReceived - total else 0,
                creditAmount = if (draft.method == "Fiado") total else 0,
                payEfectivo = if (draft.method == "Efectivo") total else draft.payEfectivo,
                payYape = if (draft.method == "Yape") total else draft.payYape,
                payPlin = if (draft.method == "Plin") total else draft.payPlin,
                payTarjeta = if (draft.method == "Tarjeta") total else draft.payTarjeta,
                revisionCount = oldSale.revisionCount + 1,
                lastModifiedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                lastModifiedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )

            val audit = SaleAudit(
                saleId = saleId,
                action = "Edición",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                reason = reason,
                beforeJson = gson.toJson(oldSale),
                afterJson = gson.toJson(newSale)
            )

            repository.updateSaleWithAudit(newSale, audit)
            onResult(true, "Venta actualizada correctamente")
        }
    }

    fun revertSale(saleId: Long, newStatus: String, reason: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val oldSale = repository.getSaleById(saleId) ?: return@launch onResult(false, "Venta no encontrada")
            val jornada = repository.getJornadaByDate(oldSale.date)
            
            if (jornada == null || jornada.isClosed) {
                return@launch onResult(false, "La jornada original de esta venta está cerrada.")
            }

            val abonos = repository.getAbonosForSale(saleId).first()
            if (abonos.isNotEmpty()) {
                return@launch onResult(false, "No se puede revertir una venta que tiene abonos registrados.")
            }

            val newSale = oldSale.copy(
                status = newStatus,
                lastModifiedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                lastModifiedTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )

            val audit = SaleAudit(
                saleId = saleId,
                action = if (newStatus == "void") "Anulación" else "Eliminación",
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                reason = reason,
                beforeJson = gson.toJson(oldSale),
                afterJson = gson.toJson(newSale)
            )

            repository.updateSaleWithAudit(newSale, audit)
            onResult(true, "Venta ${if(newStatus=="void") "anulada" else "eliminada"} correctamente")
        }
    }

    fun registerPurchase(boxes: Int, bottles: Int, amount: Long) {
        val jornada = activeJornada.value ?: return
        val currentConfig = config.value ?: BusinessConfig()
        val units = (boxes * currentConfig.bottlesPerPack) + bottles
        
        val compra = Compra(
            date = jornada.date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            boxes = boxes,
            bottles = bottles,
            units = units,
            amount = amount
        )
        
        viewModelScope.launch {
            repository.registerPurchase(compra)
        }
    }

    fun registerExpense(concept: String, amount: Long) {
        val jornada = activeJornada.value ?: return
        val gasto = Gasto(
            date = jornada.date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            concept = concept,
            amount = amount
        )
        viewModelScope.launch {
            repository.registerExpense(gasto)
        }
    }

    fun registerAbono(saleId: Long, amount: Long, method: String) {
        val jornada = activeJornada.value ?: return
        val abono = Abono(
            saleId = saleId,
            date = jornada.date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            amount = amount,
            method = method
        )
        viewModelScope.launch {
            repository.registerAbono(abono)
        }
    }

    fun registerNote(category: String, text: String) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            repository.registerNote(Apunte(date = date, category = category, text = text))
        }
    }

    fun closeJornada(countedCash: Long, boxes: Int, units: Int, note: String) {
        val jornada = activeJornada.value ?: return
        val date = jornada.date
        
        viewModelScope.launch {
            val sales = repository.getSalesByDate(date).first().filter { it.status == "active" }
            val purchases = allPurchases.value.filter { it.date == date }
            val expenses = allExpenses.value.filter { it.date == date }
            val abonos = allAbonos.value.filter { it.date == date }
            
            val salesTotal = sales.sumOf { it.total }
            val costTotal = sales.sumOf { it.cost }
            val expensesTotal = expenses.sumOf { it.amount }
            val purchasesTotal = purchases.sumOf { it.amount }
            val abonosTotal = abonos.sumOf { it.amount }
            
            repository.closeJornada(
                jornada = jornada,
                closingTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                countedCash = countedCash,
                boxes = boxes,
                units = units,
                note = note,
                salesTotal = salesTotal,
                costTotal = costTotal,
                expensesTotal = expensesTotal,
                purchasesTotal = purchasesTotal,
                abonosTotal = abonosTotal
            )
        }
    }
}
