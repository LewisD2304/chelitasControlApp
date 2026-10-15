package com.dlcode.chelasapp.repository

import com.dlcode.chelasapp.data.BusinessDao
import com.dlcode.chelasapp.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class BusinessRepository(private val dao: BusinessDao) {

    val config: Flow<BusinessConfig?> = dao.getConfig()
    val activeJornada: Flow<Jornada?> = dao.getActiveJornada()
    val allJornadas: Flow<List<Jornada>> = dao.getAllJornadas()
    val allSales: Flow<List<Venta>> = dao.getAllSales()
    val allPurchases: Flow<List<Compra>> = dao.getAllPurchases()
    val allExpenses: Flow<List<Gasto>> = dao.getAllExpenses()
    val allAbonos: Flow<List<Abono>> = dao.getAllAbonos()
    val allNotes: Flow<List<Apunte>> = dao.getAllNotes()

    suspend fun saveConfig(config: BusinessConfig) {
        dao.saveConfig(config)
    }

    suspend fun openJornada(date: String, openingCash: Long, initialStock: Int) {
        val jornada = Jornada(
            date = date,
            openingCash = openingCash,
            openingTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
            initialStock = initialStock
        )
        dao.upsertJornada(jornada)
    }

    suspend fun getJornadaByDate(date: String): Jornada? = dao.getJornadaByDate(date)

    suspend fun registerSale(venta: Venta) {
        dao.upsertSale(venta)
    }

    suspend fun updateSaleWithAudit(venta: Venta, audit: SaleAudit) {
        dao.upsertSale(venta)
        dao.insertAudit(audit)
    }

    suspend fun registerPurchase(compra: Compra) {
        dao.insertPurchase(compra)
    }

    suspend fun registerExpense(gasto: Gasto) {
        dao.insertExpense(gasto)
    }

    suspend fun registerAbono(abono: Abono) {
        dao.insertAbono(abono)
    }

    suspend fun registerNote(apunte: Apunte) {
        dao.upsertNote(apunte)
    }

    suspend fun closeJornada(
        jornada: Jornada,
        closingTime: String,
        countedCash: Long,
        boxes: Int,
        units: Int,
        note: String,
        salesTotal: Long,
        costTotal: Long,
        expensesTotal: Long,
        purchasesTotal: Long,
        abonosTotal: Long
    ) {
        val closedJornada = jornada.copy(
            closingTime = closingTime,
            closingCashCounted = countedCash,
            closingStockBoxes = boxes,
            closingStockUnits = units,
            closingNote = note,
            isClosed = true,
            snapshotSalesTotal = salesTotal,
            snapshotCostTotal = costTotal,
            snapshotExpensesTotal = expensesTotal,
            snapshotPurchasesTotal = purchasesTotal,
            snapshotAbonosTotal = abonosTotal
        )
        dao.upsertJornada(closedJornada)
    }

    fun getSalesByDate(date: String): Flow<List<Venta>> = dao.getSalesByDate(date)
    
    fun getAbonosForSale(saleId: Long): Flow<List<Abono>> = dao.getAbonosForSale(saleId)

    suspend fun getSaleById(id: Long): Venta? = dao.getSaleById(id)

    fun getAuditsForSale(saleId: Long): Flow<List<SaleAudit>> = dao.getAuditsForSale(saleId)
}
