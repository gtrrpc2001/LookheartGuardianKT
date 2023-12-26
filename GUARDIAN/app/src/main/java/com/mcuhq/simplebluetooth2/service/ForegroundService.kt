package com.mcuhq.simplebluetooth2.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mcuhq.simplebluetooth2.R
import com.mcuhq.simplebluetooth2.activity.Activity_Main
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class ForegroundService : Service() {
    private val executor: Executor = Executors.newSingleThreadExecutor() // 백그라운드에서 코드를 실행할 Executor

    @Volatile
    private var isRunning = true // 서비스가 실행 중인지 확인하는 플래그
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startBackgroundTask()
        initializeNotification() // 포그라운드 생성
        return START_NOT_STICKY
    }

    private fun startBackgroundTask() {
        executor.execute {
            while (isRunning) {
                try {
                    Thread.sleep(1000)
                } catch (ex: InterruptedException) {
                    // Thread interrupted
                }
            }
        }
    }

    fun initializeNotification() {
        val channelId = "LookHeartGuardianFS" // 채널 ID 정의
        val channelName = "Foreground Service" // 채널 이름 정의
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)

        // Notification 생성
        val builder = NotificationCompat.Builder(this, channelId)
        builder.setSmallIcon(R.mipmap.ic_launcher_round)
        builder.setContentTitle("LOOKHEART GUARDIAN")
        builder.setContentText(resources.getString(R.string.serviceRunning))
        builder.setOngoing(true)
        builder.setWhen(0)
        builder.setShowWhen(false)

        // 클릭 시 이동할 액티비티 설정
        val notificationIntent = Intent(this, Activity_Main::class.java)
        // 현재 작업 스택에 있는 액티비티 인스턴스를 가져오거나, 없으면 새로 만듭니다.
        notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.setContentIntent(pendingIntent)

        // 포그라운드 서비스 시작
        val notification = builder.build()
        startForeground(777, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false // 백그라운드 작업 중지
        stopSelf()
    }
}