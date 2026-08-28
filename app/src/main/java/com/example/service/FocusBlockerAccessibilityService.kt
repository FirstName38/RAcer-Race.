package com.example.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.MainActivity
import com.example.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FocusBlockerAccessibilityService : AccessibilityService() {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var blockedPackageSet = setOf<String>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Load blocked apps from database
        scope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            val blockedList = db.appUsageDao().getBlockedAppsSync()
            blockedPackageSet = blockedList.map { it.packageName }.toSet()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Check if timer is running and app is in blocked set
            val isTimerRunning = FocusTimerService.timerState.value.isRunning && !FocusTimerService.timerState.value.isPaused

            if (isTimerRunning && (blockedPackageSet.contains(packageName) || isDistractingDefault(packageName))) {
                if (packageName != applicationContext.packageName) {
                    // Redirect user back to RAcer Focus zone
                    val bringToFront = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                        putExtra("BLOCKED_APP_ALERT", packageName)
                    }
                    startActivity(bringToFront)
                }
            }
        }
    }

    private fun isDistractingDefault(pkg: String): Boolean {
        // Fallback check if user hasn't customized yet
        val known = listOf("com.instagram.android", "com.zhiliaoapp.musically", "com.twitter.android", "com.facebook.katana")
        return known.contains(pkg)
    }

    override fun onInterrupt() {
        // Accessibility service interrupted
    }
}
