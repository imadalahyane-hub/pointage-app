package com.pointage.app.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.pointage.app.R
import com.pointage.app.databinding.ActivityConfirmationBinding
import com.pointage.app.models.PointageType

class ConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("employee_name") ?: ""
        val time = intent.getStringExtra("time") ?: ""
        val type = PointageType.valueOf(
            intent.getStringExtra("pointage_type") ?: PointageType.ENTREE.name
        )

        if (type == PointageType.ENTREE) {
            binding.tvMessage.text = "Bonjour $name !"
            binding.tvTime.text = "Entrée enregistrée — $time"
            binding.ivIcon.setImageResource(R.drawable.ic_check_green)
            binding.root.setBackgroundColor(getColor(R.color.green_light))
        } else {
            binding.tvMessage.text = "Au revoir $name !"
            binding.tvTime.text = "Sortie enregistrée — $time"
            binding.ivIcon.setImageResource(R.drawable.ic_check_blue)
            binding.root.setBackgroundColor(getColor(R.color.blue_light))
        }

        // Retour auto à l'accueil après 3 secondes
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000)
    }
}
