package com.dlcode.chelasapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dlcode.chelasapp.model.*
import com.dlcode.chelasapp.repository.BusinessRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class BusinessViewModel(private val repository: BusinessRepository) : ViewModel() {

    val config = repository.config.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BusinessConfig())
    val activeJornada = repository.activeJornada.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allSales = repository.allSales.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allPurchases = repository.allPurchases.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allExpenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allAbonos = repository.allAbonos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allNotes = repository.allNotes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val allJornadas = repository.allJornadas.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val inventoryState = combine(allPurchases, allSales) { purchases, sales ->
        var totalBottles = 0
        var totalCost = 0L
        
        purchases.sortedBy { it.id }.forEach {
            totalBottles += it.units
            totalCost += it.amount
        }
        
        val soldUnits = sales.sumOf { it.units }
        val remainingBottles = totalBottles - soldUnits
        
        val avgCost = if (totalBottles > 0) totalCost.toDouble() / totalBottles else 0.0
        
        InventoryState(remainingBottles, avgCost)
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

    fun registerSale(
        boxes: Int,
        bottles: Int,
        discount: Long,
        method: String,
        client: String?,
        pay: Map<String, Long>,
        received: Long
    ) {
        val currentConfig = config.value ?: BusinessConfig()
        val jornada = activeJornada.value ?: return
        
        val promoGroups = bottles / currentConfig.promoN
        val remainderUnits = bottles % currentConfig.promoN
        val subtotal = (boxes * currentConfig.boxPrice) + 
                       (promoGroups * currentConfig.promoPrice) + 
                       (remainderUnits * currentConfig.unitPrice)
        val total = subtotal - discount
        val units = (boxes * currentConfig.bottlesPerPack) + bottles
        
        val venta = Venta(
            date = jornada.date,
            time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            boxes = boxes,
            bottles = bottles,
            units = units,
            subtotal = subtotal,
            discount = discount,
            total = total,
            cost = (units * inventoryState.value.averageCost).toLong(),
            method = method,
            client = client,
            cashReceived = received,
            change = if (method == "Efectivo") received - total else 0,
            creditAmount = if (method == "Fiado") total else 0,
            appliedPackSize = currentConfig.bottlesPerPack,
            appliedBoxPrice = currentConfig.boxPrice,
            appliedUnitPrice = currentConfig.unitPrice,
            appliedPromoN = currentConfig.promoN,
            appliedPromoPrice = currentConfig.promoPrice,
            payEfectivo = pay["Efectivo"] ?: 0,
            payYape = pay["Yape"] ?: 0,
            payPlin = pay["Plin"] ?: 0,
            payTarjeta = pay["Tarjeta"] ?: 0
        )
        
        viewModelScope.launch {
            repository.registerSale(venta)
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
            val sales = repository.getSalesByDate(date).first()
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
