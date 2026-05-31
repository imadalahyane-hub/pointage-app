package com.pointage.app.sync

import android.content.Context
import androidx.work.*
import com.pointage.app.database.AppDatabase
import com.pointage.app.network.PointageRequest
import com.pointage.app.network.RetrofitClient
import com.pointage.app.network.SyncRequest
import java.util.concurrent.TimeUnit

class SyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val unsynced = db.pointageDao().getUnsynced()

        if (unsynced.isEmpty()) return Result.success()

        return try {
            val requests = unsynced.map { p ->
                PointageRequest(
                    employeeId = p.employeeId,
                    type = p.type.name,
                    timestamp = p.timestamp,
                    deviceId = p.deviceId
                )
            }

            val response = RetrofitClient.api.syncBatch(SyncRequest(requests))
            if (response.isSuccessful) {
                // Marquer tous comme synchronisés
                unsynced.forEachIndexed { index, pointage ->
                    db.pointageDao().markSynced(pointage.localId, index + 1000)
                }
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "sync_pointages",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
