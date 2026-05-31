package com.pointage.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pointage.app.R
import com.pointage.app.databinding.ActivityHomeBinding
import com.pointage.app.models.PointageType
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val dateFormat = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRENCH)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.FRENCH)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        updateClock()
        setupButtons()
        setupAdminAccess()
    }

    override fun onResume() {
        super.onResume()
        updateClock()
    }

    private fun updateClock() {
        val now = Date()
        binding.tvDate.text = dateFormat.format(now).replaceFirstChar { it.uppercase() }
        binding.tvTime.text = timeFormat.format(now)
    }

    private fun setupButtons() {
        binding.btnEntree.setOnClickListener {
            val intent = Intent(this, EmployeeSelectActivity::class.java)
            intent.putExtra("pointage_type", PointageType.ENTREE.name)
            startActivity(intent)
        }

        binding.btnSortie.setOnClickListener {
            val intent = Intent(this, EmployeeSelectActivity::class.java)
            intent.putExtra("pointage_type", PointageType.SORTIE.name)
            startActivity(intent)
        }
    }

    private fun setupAdminAccess() {
        // Appui long sur le logo pour accéder à l'admin
        binding.ivLogo.setOnLongClickListener {
            showAdminPinDialog()
            true
        }
    }

    private fun showAdminPinDialog() {
        val dialog = AdminPinDialog { pinCorrect ->
            if (pinCorrect) {
                startActivity(Intent(this, AdminActivity::class.java))
            }
        }
        dialog.show(supportFragmentManager, "admin_pin")
    }
}
