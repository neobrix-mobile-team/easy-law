package com.easylaw.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("FCM_TEST", "메시지 수신 성공! 보낸 이: ${message.from}")

        // 1. 알림 데이터 추출 (Title, Body)
        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]

        // 2. 중요: 상세 페이지 이동을 위한 postId 추출
        val postId = message.data["postId"]

        Log.d("FCM_TEST", "추출된 데이터 - 제목: $title, 내용: $body, postId: $postId")

        if (title != null || body != null) {
            sendNotification(title ?: "알림", body ?: "", postId)
        }
    }

    private fun sendNotification(
        title: String,
        body: String,
        postId: String?,
    ) {
        val channelId = "default_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. 알림 채널 생성 (Android O 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    channelId,
                    "기본 알림",
                    NotificationManager.IMPORTANCE_HIGH,
                )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. 알림 클릭 시 실행될 Intent 설정
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("postId", postId) // MainActivity로 postId 전달
            }

        // 3. PendingIntent 생성 (태블릿 및 최신 기기 대응을 위해 FLAG_IMMUTABLE 필수)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(), // 요청 코드 고유화
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        // 4. 알림 빌드
        val notificationBuilder =
            NotificationCompat
                .Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // 기본 아이콘 사용 (변경 가능)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true) // 클릭 시 알림 제거
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent) // 👈 이 부분이 있어야 클릭 시 앱이 열립니다!

        // 5. 알림 띄우기 (고유 ID를 사용하여 알림이 겹치지 않게 함)
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TEST", "새로운 토큰 생성됨: $token")
    }
}
