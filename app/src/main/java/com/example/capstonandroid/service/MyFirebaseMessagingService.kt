package com.example.capstonandroid.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import androidx.core.app.NotificationCompat
import com.example.capstonandroid.R
import com.example.capstonandroid.activity.IntroActivity
import com.example.capstonandroid.activity.PostActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * FirebaseInstanceIdService is deprecated.
     * this is new on firebase-messaging:17.1.0
     */
    companion object {
        private const val CHANNEL_NAME = "FCM_NOTIFICATION_CHANNEL_NAME"
        private const val CHANNEL_ID = "FCM_NOTIFICATION_CHANNEL_ID"
    }

    // token 이 새로 갱신됐을 경우.... 서버의 token 을 갱신해 줘야함
    override fun onNewToken(token: String) {
        println("(Firebase) new Token: $token")
    }

    /**
     * this method will be triggered every time there is new FCM Message.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title
        val body = remoteMessage.notification?.body
        val data = remoteMessage.data
        println("(Firebase) Title: $title")
        println("(Firebase) Body: $body")
        println("(Firebase) data: $data")
        sendNotification(title!!, body!!, data)
    }

    private fun sendNotification(title: String, body: String, data: Map<String, String>) {
        createNotificationChannel()

        // notification type 에 따라 분기처리
        val intent = when (data["type"]!!) {
            "follow" -> {
                Intent(this, IntroActivity::class.java).apply {
//                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    putExtra("postId", data["postId"]!!.toInt())
                }
            }
            else -> {
                Intent(this, PostActivity::class.java).apply {
                    putExtra("postId", data["postId"]!!.toInt())
                }
            }
        }

        var pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        var notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_main)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(notificationSound)
            .setContentIntent(pendingIntent)

        var notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }

    // notification channel 생성
    @SuppressLint("NewApi")
    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        channel.enableLights(true)
        channel.enableVibration(true)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
    }
}