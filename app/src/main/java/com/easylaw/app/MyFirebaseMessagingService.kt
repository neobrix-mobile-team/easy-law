package com.easylaw.app

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // 1. 메시지를 받았을 때 실행되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // 알림 제목과 내용 가져오기
        val title = message.notification?.title ?: "알림"
        val body = message.notification?.body ?: "메시지가 도착했습니다."

        sendNotification(title, body)
    }

    // 2. 화면에 알림을 띄워주는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(title: String, body: String) {
        val channelId = "default_channel" // 파이어베이스 콘솔과 일치해야 함
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0 이상은 채널 설정이 필수
        val channel = NotificationChannel(channelId, "기본 알림", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_dialog_info) // 아이콘 설정
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)
    }

    // 3. 토큰이 갱신될 때 실행 (앱을 새로 깔거나 할 때)
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // 여기서 새로운 토큰을 Supabase DB에 업데이트하는 로직을 넣으면 완벽합니다!
    }
}