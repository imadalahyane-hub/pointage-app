package com.pointage.app.database

import android.content.Context
import androidx.room.*
import com.pointage.app.models.Employee
import com.pointage.app.models.Pointage
import com.pointage.app.models.PointageType

// Type converter pour Room
class Converters {
    @TypeConverter
    fun fromPointageType(value: PointageType): String = value.name

    @TypeConverter
    fun toPointageType(value: String): PointageType = PointageType.valueOf(value)
}

@Database(entities = [Employee::class, Pointage::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun pointageDao(): PointageDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pointage_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
