package com.pointage.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PointageType { ENTREE, SORTIE }

@Entity(tableName = "pointages")
data class Pointage(
    @PrimaryKey(autoGenerate = true) val localId: Int = 0,
    val serverId: Int? = null,
    val employeeId: Int,
    val type: PointageType,
    val timestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false,
    val deviceId: String = android.os.Build.SERIAL
)
