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

    @Query("SELECT * FROM jornadas WHERE isClosed = 0 LIMIT 1")
    fun getActiveJornada(): Flow<Jornada?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJornada(jornada: Jornada)

    @Query("SELECT * FROM ventas ORDER BY id DESC")
    fun getAllSales(): Flow<List<Venta>>

    @Query("SELECT * FROM ventas WHERE date = :date ORDER BY id DESC")
    fun getSalesByDate(date: String): Flow<List<Venta>>

    @Insert
    suspend fun insertSale(venta: Venta)

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

    @Insert
    suspend fun insertNote(apunte: Apunte)
}

@Database(entities = [BusinessConfig::class, Jornada::class, Venta::class, Compra::class, Gasto::class, Abono::class, Apunte::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessDao(): BusinessDao
}
