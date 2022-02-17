package com.example.mynotesformsc2students.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mynotesformsc2students.Activity.MainActivity
import com.example.mynotesformsc2students.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //generate the notification
    //attach the notification created with custom layout
    //show the notification

    val channelId =  "notification_channel"
    val channelName = "com.example.mynotesformsc2students.service"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.notification != null){
            generateNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }
    }

    fun generateNotification(title:String,message:String){
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT)

        var builder:NotificationCompat.Builder = NotificationCompat.Builder(applicationContext,channelId)
            .setSmallIcon(R.drawable.ic_message_24)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContent(getRemoteView(title,message))

        val notificationManager:NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId,channelName,NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0,builder.build())


    }

    private fun getRemoteView(title: String, message: String): RemoteViews? {

        val remoteViews =
            RemoteViews("com.example.mynotesformsc2students.service", R.layout.notification_layout)

        remoteViews.setTextViewText(R.id.txtTitleNotification, title)
        remoteViews.setTextViewText(R.id.txtMessageNotification, message)
        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.ic_message_24)

        return remoteViews

    }

    }
