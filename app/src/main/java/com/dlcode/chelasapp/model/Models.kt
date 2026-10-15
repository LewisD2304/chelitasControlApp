package com.dlcode.chelasapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_config")
data class BusinessConfig(
    @PrimaryKey val id: Int = 1,
    val bottlesPerPack: Int = 0,
    val boxPrice: Long = 8000, // in cents
    val unitPrice: Long = 700, // in cents
    val promoN: Int = 3,
    val promoPrice: Long = 2000, // in cents
)

@Entity(tableName = "jornadas")
data class Jornada(
    @PrimaryKey val date: String, // YYYY-MM-DD
    val openingCash: Long,
    val openingTime: String,
    val initialStock: Int,
    val closingTime: String? = null,
    val closingCashCounted: Long? = null,
    val closingStockBoxes: Int? = null,
    val closingStockUnits: Int? = null,
    val closingNote: String? = null,
    val isClosed: Boolean = false,
    val snapshotSalesTotal: Long = 0,
    val snapshotCostTotal: Long = 0,
    val snapshotExpensesTotal: Long = 0,
    val snapshotPurchasesTotal: Long = 0,
    val snapshotAbonosTotal: Long = 0
)

@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val boxes: Int,
    val bottles: Int,
    val units: Int,
    val subtotal: Long,
    val discount: Long,
    val total: Long,
    val cost: Long,
    val method: String,
    val client: String?,
    val cashReceived: Long = 0,
    val change: Long = 0,
    val creditAmount: Long = 0,
    val appliedPackSize: Int,
    val appliedBoxPrice: Long,
    val appliedUnitPrice: Long,
    val appliedPromoN: Int,
    val appliedPromoPrice: Long,
    val payEfectivo: Long = 0,
    val payYape: Long = 0,
    val payPlin: Long = 0,
    val payTarjeta: Long = 0,
    val status: String = "active", // active, void, deleted
    val revisionCount: Int = 0,
    val lastModifiedDate: String? = null,
    val lastModifiedTime: String? = null
)

@Entity(tableName = "sale_audits")
data class SaleAudit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val action: String, // Edición, Anulación, Eliminación
    val date: String,
    val time: String,
    val reason: String,
    val beforeJson: String,
    val afterJson: String?
)

@Entity(tableName = "compras")
data class Compra(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val boxes: Int,
    val bottles: Int,
    val units: Int,
    val amount: Long // total cost
)

@Entity(tableName = "gastos")
data class Gasto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val time: String,
    val concept: String,
    val amount: Long
)

@Entity(tableName = "abonos")
data class Abono(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val date: String,
    val time: String,
    val amount: Long,
    val method: String
)

@Entity(tableName = "apuntes")
data class Apunte(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,
    val category: String, // operacion, compra, cliente, recordatorio
    val text: String
)

data class InventoryState(
    val stock: Int = 0,
    val totalValue: Long = 0,
    val averageCost: Double = 0.0
)
