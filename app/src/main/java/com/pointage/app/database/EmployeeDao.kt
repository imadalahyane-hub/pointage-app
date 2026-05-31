package com.pointage.app.database

import androidx.room.*
import com.pointage.app.models.Employee
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees WHERE actif = 1 ORDER BY prenom ASC")
    fun getAllActive(): Flow<List<Employee>>

    @Query("SELECT * FROM employees WHERE actif = 1 ORDER BY prenom ASC")
    suspend fun getAllActiveOnce(): List<Employee>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getById(id: Int): Employee?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employees: List<Employee>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: Employee)

    @Update
    suspend fun update(employee: Employee)

    @Query("UPDATE employees SET actif = 0 WHERE id = :id")
    suspend fun deactivate(id: Int)
}
