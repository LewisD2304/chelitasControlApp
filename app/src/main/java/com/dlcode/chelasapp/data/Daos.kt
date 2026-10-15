package com.dlcode.chelasapp.data

import androidx.room.*
import com.dlcode.chelasapp.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Query("SELECT * FROM business_config WHERE id = 1")
    fun getConfig(): Flow<BusinessConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: BusinessConfig)

    @Query("SELECT * FROM jornadas ORDER BY date DESC")
    fun getAllJornadas(): Flow<List<Jornada>>

    @Query("SELECT * FROM jornadas WHERE date = :date LIMIT 1")
    suspend fun getJornadaByDate(date: String): Jornada?

    @Query("SELECT * FROM jornadas WHERE isClosed = 0 LIMIT 1")
    fun getActiveJornada(): Flow<Jornada?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJornada(jornada: Jornada)

    @Query("SELECT * FROM ventas ORDER BY id DESC")
    fun getAllSales(): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE date = :date ORDER BY id DESC")
    fun getSalesByDate(date: String): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun getSaleById(id: Long): Venta?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSale(venta: Venta)

    @Query("SELECT * FROM compras ORDER BY id DESC")
    fun getAllPurchases(): Flow<List<Compra>>

    @Insert
    suspend fun insertPurchase(compra: Compra)

    @Query("SELECT * FROM gastos ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<Gasto>>

    @Insert
    suspend fun insertExpense(gasto: Gasto)

    @Query("SELECT * FROM abonos ORDER BY id DESC")
    fun getAllAbonos(): Flow<List<Abono>>

    @Query("SELECT * FROM abonos WHERE saleId = :saleId")
    fun getAbonosForSale(saleId: Long): Flow<List<Abono>>

    @Insert
    suspend fun insertAbono(abono: Abono)

    @Query("SELECT * FROM apuntes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<Apunte>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(apunte: Apunte)

    @Insert
    suspend fun insertAudit(audit: SaleAudit)

    @Query("SELECT * FROM sale_audits WHERE saleId = :saleId ORDER BY id DESC")
    fun getAuditsForSale(saleId: Long): Flow<List<SaleAudit>>
}

@Database(entities = [BusinessConfig::class, Jornada::class, Venta::class, Compra::class, Gasto::class, Abono::class, Apunte::class, SaleAudit::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
}
