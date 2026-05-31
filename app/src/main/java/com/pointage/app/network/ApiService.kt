package com.pointage.app.network

import com.pointage.app.models.Employee
import retrofit2.Response
import retrofit2.http.*

data class PointageRequest(
    val employeeId: Int,
    val type: String,
    val timestamp: Long,
    val deviceId: String
)

data class PointageResponse(val id: Int, val message: String)

data class SyncRequest(val pointages: List<PointageRequest>)
data class SyncResponse(val synced: Int, val errors: Int)

interface ApiService {
    @GET("employees")
    suspend fun getEmployees(): Response<List<Employee>>

    @POST("employees")
    suspend fun createEmployee(@Body employee: Employee): Response<Employee>

    @POST("pointages")
    suspend fun recordPointage(@Body request: PointageRequest): Response<PointageResponse>

    @POST("sync")
    suspend fun syncBatch(@Body request: SyncRequest): Response<SyncResponse>
}
