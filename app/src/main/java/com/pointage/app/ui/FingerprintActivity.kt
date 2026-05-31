package com.pointage.app.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pointage.app.R
import com.pointage.app.biometric.BiometricHelper
import com.pointage.app.database.AppDatabase
import com.pointage.app.databinding.ActivityFingerprintBinding
import com.pointage.app.models.Pointage
import com.pointage.app.models.PointageType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FingerprintActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFingerprintBinding
    private var employeeId = 0
    private var employeeName = ""
    private lateinit var pointageType: PointageType
    private var attempts = 0
    private val maxAttempts = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFingerprintBinding.inflate(layoutInflater)
        setContentView(binding.root)

        employeeId = intent.getIntExtra("employee_id", 0)
        employeeName = intent.getStringExtra("employee_name") ?: ""
        pointageType = PointageType.valueOf(
            intent.getStringExtra("pointage_type") ?: PointageType.ENTREE.name
        )

        binding.tvName.text = employeeName
        binding.tvInstruction.text = "Posez votre doigt sur le capteur"

        val action = if (pointageType == PointageType.ENTREE) "Entrée" else "Sortie"
        binding.tvAction.text = action

        binding.btnCancel.setOnClickListener { finish() }

        // Lancer le scan automatiquement
        lifecycleScope.launch {
            delay(500)
            startBiometric()
        }
    }

    private fun startBiometric() {
        BiometricHelper.showPrompt(
            activity = this,
            title = "Confirmation d'identité",
            subtitle = "$employeeName — ${if (pointageType == PointageType.ENTREE) "Entrée" else "Sortie"}",
            onSuccess = { handleSuccess() },
            onError = { msg -> handleError(msg) },
            onFailed = { handleFailed() }
        )
    }

    private fun handleSuccess() {
        binding.ivFingerprint.setImageResource(R.drawable.ic_fingerprint_success)
        binding.tvStatus.text = "✓ Empreinte reconnue"
        binding.tvStatus.setTextColor(getColor(R.color.green))

        lifecycleScope.launch {
            savePointage()
            delay(300)
            goToConfirmation()
        }
    }

    private fun handleFailed() {
        attempts++
        binding.ivFingerprint.setImageResource(R.drawable.ic_fingerprint_error)
        binding.tvStatus.text = "✗ Empreinte non reconnue (${attempts}/$maxAttempts)"
        binding.tvStatus.setTextColor(getColor(R.color.red))

        if (attempts >= maxAttempts) {
            binding.tvStatus.text = "Trop de tentatives — réessayez"
            lifecycleScope.launch {
                delay(2000)
                finish()
            }
        } else {
            lifecycleScope.launch {
                delay(1000)
                binding.ivFingerprint.setImageResource(R.drawable.ic_fingerprint)
                binding.tvStatus.text = ""
                startBiometric()
            }
        }
    }

    private fun handleError(msg: String) {
        binding.tvStatus.text = msg
        binding.tvStatus.setTextColor(getColor(R.color.red))
        lifecycleScope.launch {
            delay(2000)
            finish()
        }
    }

    private suspend fun savePointage() {
        val db = AppDatabase.getInstance(this)
        val pointage = Pointage(
            employeeId = employeeId,
            type = pointageType,
            timestamp = System.currentTimeMillis(),
            synced = false
        )
        db.pointageDao().insert(pointage)
    }

    private fun goToConfirmation() {
        val timeFormat = SimpleDateFormat("HH'h'mm", Locale.FRENCH)
        val time = timeFormat.format(Date())

        val intent = Intent(this, ConfirmationActivity::class.java).apply {
            putExtra("employee_name", employeeName)
            putExtra("time", time)
            putExtra("pointage_type", pointageType.name)
        }
        startActivity(intent)
        finish()
    }
}
