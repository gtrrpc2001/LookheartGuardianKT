package com.mcuhq.simplebluetooth2.server

import com.mcuhq.simplebluetooth2.server.model.UserProfile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface RetrofitService {
    // checkLogin
    @GET("msl/CheckLogin")
    fun checkLogin(
        @Query("empid") empid: String?,
        @Query("pw") pw: String?,
        @Query("phone") phone: String?
    ): Call<String?>?

    @GET("msl/CheckLogin")
    fun setToken(
        @Query("empid") empid: String?,
        @Query("pw") pw: String?,
        @Query("phone") phone: String?,
        @Query("token") token: String?
    ): Call<String?>?

    // Real-Time Bpm
    @GET("mslLast/Last")
    fun getRealBPM(@Query("eq") eq: String?): Call<String?>?

    // BpmData
    @GET("mslbpm/api_getdata")
    fun getBpmData(
        @Query("eq") eq: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Call<String?>?

    // Arr
    @GET("mslecgarr/test")
    fun getArrData(
        @Query("idx") idx: String?,
        @Query("eq") eq: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Call<String?>?

    // CalandDistance
    @GET("mslecgday/day")
    fun getHourlyData(
        @Query("eq") eq: String?,
        @Query("startDate") startDate: String?,
        @Query("endDate") endDate: String?
    ): Call<String?>?

    // Profile
    @GET("msl/Profile")
    fun getProfileData(@Query("empid") empid: String?): Call<List<UserProfile?>?>?
}