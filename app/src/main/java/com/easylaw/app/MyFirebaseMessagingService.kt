package com.easylaw.app

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    // 1. 메시지를 받았을 때 실행되는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_TEST", "메시지 수신 성공!")
        Log.d("FCM_TEST", "보낸 이: ${message.from}")

        if (message.data.isNotEmpty()) {
            Log.d("FCM_TEST", "데이터 메시지 내용: ${message.data}")
        }

        message.notification?.let {
            Log.d("FCM_TEST", "알림 제목: ${it.title}")
            Log.d("FCM_TEST", "알림 내용: ${it.body}")
        }

        // 1. notification 객체에서 가져오기
        var title = message.notification?.title
        var body = message.notification?.body

        // 2. 만약 notification이 비어있다면 data 객체에서 가져오기 (커스텀 전송 시 유용)
        if (title == null && message.data.isNotEmpty()) {
            title = message.data["title"]
            body = message.data["body"]
        }

        // 최종 확인 후 알림 생성
        if (title != null || body != null) {
            sendNotification(title ?: "알림", body ?: "")
        }
    }

    // 2. 화면에 알림을 띄워주는 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendNotification(
        title: String,
        body: String,
    ) {
        Log.d("FCM_TEST", "화면에 알림 띄우기 시도: $title / $body")

        val channelId = "default_channel" // 파이어베이스 콘솔과 일치해야 함
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 안드로이드 8.0 이상은 채널 설정이 필수
        val channel = NotificationChannel(channelId, "기본 알림", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification =
            NotificationCompat
                .Builder(this, channelId)
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
