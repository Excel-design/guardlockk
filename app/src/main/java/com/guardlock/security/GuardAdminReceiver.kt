package com.guardlock.security

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build

class GuardAdminReceiver : DeviceAdminReceiver() {
    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val failedAttempts = dpm.currentFailedPasswordAttempts
        val prefs = context.getSharedPreferences("guard_prefs", Context.MODE_PRIVATE)
        val maxThreshold = prefs.getInt("max_attempts", 1)
        if (failedAttempts >= maxThreshold) {
            val serviceIntent = Intent(context, IntruderCaptureService::class.java).apply {
                putExtra("FAILED_COUNT", failedAttempts)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
