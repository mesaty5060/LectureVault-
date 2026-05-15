package com.lecturevault.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.lecturevault.app.data.SettingsRepository
import com.lecturevault.app.navigation.LectureVaultNavHost
import com.lecturevault.app.ui.theme.LectureVaultTheme
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensureNotificationChannel()

        val settings = SettingsRepository(applicationContext)
        setContent {
            val themeMode by settings.theme.collectAsState(initial = "system")
            val onboarded by settings.onboarded.collectAsState(initial = true)
            LectureVaultTheme(themeMode = themeMode) {
                LectureVaultNavHost(startWithOnboarding = !onboarded)
            }
        }
    }

    private fun ensureNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                "reminders", "Reminders", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "LectureVault reminders" }
            mgr.createNotificationChannel(ch)
        }
    }
}
