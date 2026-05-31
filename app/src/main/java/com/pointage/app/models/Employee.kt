package com.pointage.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey val id: Int = 0,
    val nom: String,
    val prenom: String,
    val poste: String,
    val photoUrl: String? = null,
    val actif: Boolean = true,
    val createdAt: String = ""
) {
    val fullName: String get() = "$prenom $nom"
}
