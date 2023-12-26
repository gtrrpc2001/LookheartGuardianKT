package com.mcuhq.simplebluetooth2.server.model

import com.google.gson.annotations.SerializedName

class UserProfile {
    // getters
    // Gson 응답 파싱
    @SerializedName("eq")
    @get:JvmName("Id")
    val id: String? = null

    @SerializedName("eqname")
    @get:JvmName("Name")
    val name: String? = null

    @SerializedName("email")
    @get:JvmName("Email")
    val email: String? = null

    @SerializedName("userphone")
    @get:JvmName("Phone")
    val phone: String? = null

    @SerializedName("sex")
    @get:JvmName("Gender")
    val gender: String? = null

    @SerializedName("height")
    @get:JvmName("Height")
    val height: String? = null

    @SerializedName("weight")
    @get:JvmName("Weight")
    val weight: String? = null

    @SerializedName("age")
    @get:JvmName("Age")
    val age: String? = null

    @SerializedName("birth")
    @get:JvmName("Birthday")
    val birthday: String? = null

    @SerializedName("signupdate")
    @get:JvmName("JoinDate")
    val joinDate: String? = null

    @SerializedName("sleeptime")
    @get:JvmName("SleepStart")
    val sleepStart: String? = null

    @SerializedName("uptime")
    @get:JvmName("SleepEnd")
    val sleepEnd: String? = null

    @SerializedName("bpm")
    @get:JvmName("ActivityBPM")
    val activityBPM: String? = null

    @SerializedName("step")
    @get:JvmName("DailyStep")
    val dailyStep: String? = null

    @SerializedName("distanceKM")
    @get:JvmName("DailyDistance")
    val dailyDistance: String? = null

    @SerializedName("calexe")
    @get:JvmName("DailyActivityCalorie")
    val dailyActivityCalorie: String? = null

    @SerializedName("cal")
    @get:JvmName("DailyCalorie")
    val dailyCalorie: String? = null

    @SerializedName("alarm_sms")
    private val smsNotification: String? = null

    @SerializedName("differtime")
    private val timeDifference: String? = null

    @SerializedName("phone")
    @get:JvmName("Guardian")
    val guardian: String? = null

    // getters
    fun getId(): String? {
        return id
    }

    fun getName(): String? {
        return name
    }

    fun getEmail(): String? {
        return email
    }

    fun getPhone(): String? {
        return phone
    }

    fun getGender(): String? {
        return gender
    }

    fun getHeight(): String? {
        return height
    }

    fun getWeight(): String? {
        return weight
    }

    fun getAge(): String? {
        return age
    }

    fun getBirthday(): String? {
        return birthday
    }

    fun getJoinDate(): String? {
        return joinDate
    }

    fun getSleepStart(): String? {
        return sleepStart
    }

    fun getSleepEnd(): String? {
        return sleepEnd
    }

    fun getActivityBPM(): String? {
        return activityBPM
    }

    fun getDailyStep(): String? {
        return dailyStep
    }

    fun getDailyDistance(): String? {
        return dailyDistance
    }

    fun getDailyCalorie(): String? {
        return dailyCalorie
    }

    fun getDailyActivityCalorie(): String? {
        return dailyActivityCalorie
    }

    fun getGuardian(): String? {
        return guardian
    }
}