package com.example.firebasesample

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessageService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("kidcherish", "fcm token..........$p0")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d("kidcherish", "fcm message..........${p0.notification}")
        Log.d("kidcherish", "fcm message..........${p0.data}")
    }
}
