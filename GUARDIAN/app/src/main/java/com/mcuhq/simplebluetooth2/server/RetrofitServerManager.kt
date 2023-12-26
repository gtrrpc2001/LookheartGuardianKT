package com.mcuhq.simplebluetooth2.server

import android.util.Log
import com.mcuhq.simplebluetooth2.server.model.UserProfile
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.Builder
//import okhttp3.OkHttpClient.Builder.build
//import okhttp3.OkHttpClient.Builder.connectTimeout
//import okhttp3.OkHttpClient.Builder.readTimeout
//import okhttp3.OkHttpClient.Builder.retryOnConnectionFailure
//import okhttp3.OkHttpClient.Builder.writeTimeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitServerManager {


    var okHttpClient: OkHttpClient = Builder()
        .connectTimeout(300, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun initializeApiService() {
        if (apiService == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiService = retrofit.create(
                RetrofitService::class.java
            )
        }
    }

    interface APICallback<T> {
        fun onSuccess(result: T)
        fun onFailure(e: Exception?)
    }

    interface ServerTaskCallback {
        fun onSuccess(result: String?)
        fun onFailure(e: Exception?)
    }

    interface UserDataCallback {
        fun userData(userProfile: UserProfile?)
        fun onFailure(e: Exception?)
    }

    interface HourlyDataCallback {
        fun hourlyData(data: List<*>?)
        fun onFailure(e: Exception?)
    }

    interface DataCallback {
        fun getData(data: List<Map<String, String>>?)
        fun onFailure(e: Exception?)
    }

    interface ArrDataCallback {
        fun getData(dataList: List<String>?)
        fun onFailure(e: Exception?)
    }

    interface RealBpmCallback {
        fun getBpm(bpm: String?)
        fun onFailure(e: Exception?)
    }

    fun loginTask(
        email: String,
        pw: String,
        phone: String,
        token: String?,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email
            mapParam["password"] = pw
            mapParam["phone"] = phone

            // API 호출
            loginTaskFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun loginTaskFromAPI(loginData: Map<String, Any>, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.checkLogin(
            loginData["eq"].toString(),
            loginData["password"].toString(),
            loginData["phone"].toString()
        )
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun tokenTask(
        email: String,
        pw: String,
        phone: String,
        token: String,
        callback: ServerTaskCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email
            mapParam["password"] = pw
            mapParam["phone"] = phone
            mapParam["token"] = token

            // API 호출
            tokenTaskFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun tokenTaskFromAPI(loginData: Map<String, Any>, callback: ServerTaskCallback) {
        initializeApiService()
        val call = apiService!!.setToken(
            loginData["eq"].toString(),
            loginData["password"].toString(),
            loginData["phone"].toString(),
            loginData["token"].toString()
        )
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                callback.onSuccess(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun getProfile(email: String, callback: UserDataCallback) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email

            // API 호출
            getProfileFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun getProfileFromAPI(mapParam: Map<String, Any>, callback: UserDataCallback) {
        initializeApiService()
        val call = apiService!!.getProfileData(mapParam["eq"].toString())
        executeCall(call!!, object : APICallback<List<UserProfile?>?> {
            override fun onSuccess(result: List<UserProfile?>?) {
                Log.e("getProfileFromAPI", result?.size.toString())
                try {
                    if (result?.isEmpty() == false) {
                        val profile = result[0]
                        callback.userData(profile) // 콜백 호출
                    }
                } catch (ignored: Exception) {
                    callback.onFailure(ignored)
                }
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }



    fun getRealBPM(email: String, callback: RealBpmCallback) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["eq"] = email

            // API 호출
            getRealBPMFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun getRealBPMFromAPI(mapParam: Map<String, Any>, callback: RealBpmCallback) {
        initializeApiService()
        val call = apiService!!.getRealBPM(mapParam["eq"].toString())
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                callback.getBpm(result)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun getBpmData(
        kind: String,
        email: String,
        startDate: String,
        endDate: String,
        callback: DataCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = kind
            mapParam["eq"] = email
            mapParam["startDate"] = startDate
            mapParam["endDate"] = endDate

            // API 호출
            getBPMDataFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun getBPMDataFromAPI(mapParam: Map<String, Any>, callback: DataCallback) {
        initializeApiService()
        val call = apiService!!.getBpmData(
            mapParam["eq"].toString(),
            mapParam["startDate"].toString(),
            mapParam["endDate"].toString()
        )
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                val lines =
                    result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val dataList: MutableList<Map<String, String>> = ArrayList()
                for (line in lines) {
                    val segments = line.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 파이프(|) 구분
                    if (segments.size >= 6) {
                        val data: MutableMap<String, String> = HashMap()
                        data["email"] = segments[1]
                        data["time"] = segments[2]
                        data["utcOffset"] = segments[3]
                        data["bpm"] = segments[4]
                        data["temp"] = segments[5]
                        data["hrv"] = segments[6]
                        dataList.add(data)
                    }
                }
                callback.getData(dataList)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun getHourlyData(
        kind: String,
        email: String,
        startDate: String,
        endDate: String,
        callback: HourlyDataCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = kind
            mapParam["eq"] = email
            mapParam["startDate"] = startDate
            mapParam["endDate"] = endDate

            // API 호출
            getHourlyDataFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e)
        }
    }

    fun getHourlyDataFromAPI(mapParam: Map<String, Any>, callback: HourlyDataCallback) {
        initializeApiService()
        val call = apiService!!.getHourlyData(
            mapParam["eq"].toString(),
            mapParam["startDate"].toString(),
            mapParam["endDate"].toString()
        )
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                val lines =
                    result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val dataList: MutableList<String> = ArrayList()
                for (line in lines) {
                    val segments = line.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray() // 파이프(|) 구분
                    if (segments.size > 11 && segments[5] != "datahour") {
                        dataList.add(segments[1] + "," + segments[2] + "," + segments[6] + "," + segments[7] + "," + segments[8] + "," + segments[9] + "," + segments[10] + "," + segments[11])
                    }
                }
                callback.hourlyData(dataList) // 콜백 호출
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    fun getArrData(
        kind: String,
        idx: String,
        email: String,
        startDate: String,
        endDate: String,
        callback: ArrDataCallback
    ) {
        try {
            val mapParam: MutableMap<String, Any> = HashMap()
            mapParam["kind"] = kind
            mapParam["idx"] = idx
            mapParam["eq"] = email
            mapParam["startDate"] = startDate
            mapParam["endDate"] = endDate

            // API 호출
            getArrDataFromAPI(mapParam, callback)
        } catch (e: Exception) {
            callback.onFailure(e) // 콜백 호출
        }
    }

    fun getArrDataFromAPI(mapParam: Map<String, Any>, callback: ArrDataCallback) {
        initializeApiService()
        val call = apiService!!.getArrData(
            mapParam["idx"].toString(),
            mapParam["eq"].toString(),
            mapParam["startDate"].toString(),
            mapParam["endDate"].toString()
        )
        executeCall(call!!, object : APICallback<String> {
            override fun onSuccess(result: String) {
                val lines =
                    result.split("\\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val dataList: MutableList<String> = ArrayList()
                for (line in lines) {
                    if (line.length > 3) {
                        if (!line.contains("|null")) // Android
                            dataList.add(line) else {  // IOS
                            if (line.length > 10 && line.contains("|null")) {
                                line.replace("|null", "")
                                dataList.add(line)
                            }
                        }
                    }
                }
                callback.getData(dataList)
            }

            override fun onFailure(e: Exception?) {
                callback.onFailure(e)
            }
        })
    }

    @JvmName("callFromList")
    private fun executeCall(call: Call<List<UserProfile?>?>, callback: APICallback<List<UserProfile?>?>) {
        call.enqueue(object : Callback<List<UserProfile?>?> {
            override fun onResponse(call: Call<List<UserProfile?>?>, response: Response<List<UserProfile?>?>) {
                if (response.isSuccessful && response.body() != null) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onFailure(Exception("API call not successful"))
                }
            }

            override fun onFailure(call: Call<List<UserProfile?>?>, t: Throwable) {
                callback.onFailure(Exception(t))
            }
        })
    }

    @JvmName("callFromT")
    private fun <T> executeCall(call: Call<T?>, callback: APICallback<T>) {
        call.enqueue(object : Callback<T?> {
            override fun onResponse(call: Call<T?>, response: Response<T?>) {
                if (response.isSuccessful && response.body() != null) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onFailure(Exception("API call not successful"))
                }
            }

            override fun onFailure(call: Call<T?>, t: Throwable) {
                callback.onFailure(Exception(t))
            }
        })
    }

    companion object {
        private const val BASE_URL = "http://121.152.22.85:40081/" // TEST Address

        //    private static final String BASE_URL = "http://121.152.22.85:40080/"; // Real Address
        private var apiService: RetrofitService? = null
    }
}