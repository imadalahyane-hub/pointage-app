package com.pointage.app.database

import androidx.room.*
import com.pointage.app.models.Pointage

@Dao
interface PointageDao {
    @Insert
    suspend fun insert(pointage: Pointage): Long

    @Query("SELECT * FROM pointages WHERE synced = 0")
    suspend fun getUnsynced(): List<Pointage>

    @Query("UPDATE pointages SET synced = 1, serverId = :serverId WHERE localId = :localId")
    suspend fun markSynced(localId: Int, serverId: Int)

    @Query("SELECT * FROM pointages ORDER BY timestamp DESC LIMIT 50")
    suspend fun getRecent(): List<Pointage>

    @Query("""
        SELECT * FROM pointages
        WHERE employeeId = :employeeId
        AND timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp DESC LIMIT 1
    """)
    suspend fun getLastForEmployee(employeeId: Int, startOfDay: Long, endOfDay: Long): Pointage?
}
