package com.mcuhq.simplebluetooth2.firebase

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager
import com.mcuhq.simplebluetooth2.server.RetrofitServerManager.ServerTaskCallback
import java.util.Locale

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (isFirstToken(applicationContext)) setTokenAsReceived(applicationContext) else {
            val context = applicationContext
            fetchFCMToken(token, context)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val intent = Intent("arr-event")
        intent.putExtra("message", remoteMessage.notification!!.title)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        //수신한 메시지를 처리
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        var builder: NotificationCompat.Builder? = null
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            //채널 생성
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 중요도
            )
            channel.description = "This channel is used for important notifications." // 채널 설명
            channel.setSound(
                Uri.parse("android.resource://" + packageName + "/" + R.raw.arrsound),
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
            )
            notificationManager.createNotificationChannel(channel)
        }
        builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        val title = remoteMessage.notification!!.title
        val body = remoteMessage.notification!!.body
        builder.setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX) // 최대 우선순위
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE) // 소리와 진동 설정
            .setSmallIcon(R.mipmap.ic_launcher_round)
        val notification = builder.build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationManager.notify(1610, notification)
    }

    fun sendToken(context: Context) {
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                fetchFCMToken(token, context)
            })
    }

    private fun fetchFCMToken(token: String, context: Context) {
        val retrofitServerManager = RetrofitServerManager()
        val emailSharedPreferences = context.getSharedPreferences("User", MODE_PRIVATE)
        val email = emailSharedPreferences.getString("email", null)
        val userSp = context.getSharedPreferences(email, MODE_PRIVATE)
        val pw = userSp.getString("password", null)
        val guardian = userSp.getString("guardian", null)
        retrofitServerManager.tokenTask(email!!, pw!!, guardian!!, token, object : ServerTaskCallback {
            override fun onSuccess(result: String?) {
                if (result?.lowercase(Locale.getDefault())?.contains("true")!!) Log.i(
                    "fetchFCMToken",
                    "토큰 전송 성공"
                ) else Log.i("fetchFCMToken", "토큰 전송 실패")
            }

            override fun onFailure(e: Exception?) {
                e?.printStackTrace()
                Log.e("fetchFCMToken", "서버 응답 없음")
            }
        })
    }

    private fun isFirstToken(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
        return sharedPreferences.getBoolean("isFirstToken", true)
    }

    private fun setTokenAsReceived(context: Context) {
        val sharedPreferences = context.getSharedPreferences("FCM_TOKEN", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isFirstToken", false)
        editor.apply()
    }

    companion object {
        private const val CHANNEL_ID = "high_importance_channel"
        private val CHANNEL_NAME: CharSequence = "FIREBASE"
    }
}