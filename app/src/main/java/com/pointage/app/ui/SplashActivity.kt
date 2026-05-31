package com.pointage.app.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pointage.app.biometric.BiometricHelper
import com.pointage.app.databinding.ActivitySplashBinding
import com.pointage.app.sync.SyncWorker

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Démarrer la sync en arrière-plan
        SyncWorker.schedule(this)

        // Vérifier disponibilité biométrique
        if (!BiometricHelper.isAvailable(this)) {
            binding.tvStatus.text = "⚠️ Capteur d'empreinte non disponible"
        } else {
            binding.tvStatus.text = "✓ Système prêt"
        }

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }, 2000)
    }
}
