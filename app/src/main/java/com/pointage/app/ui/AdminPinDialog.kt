package com.pointage.app.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.pointage.app.R

class AdminPinDialog(
    private val onResult: (Boolean) -> Unit
) : DialogFragment() {

    // PIN admin par défaut — à changer dans les préférences admin
    private val ADMIN_PIN = "123456"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_admin_pin, null)
        val etPin = view.findViewById<EditText>(R.id.etPin)

        return AlertDialog.Builder(requireContext())
            .setTitle("Accès Administrateur")
            .setView(view)
            .setPositiveButton("Valider") { _, _ ->
                onResult(etPin.text.toString() == ADMIN_PIN)
            }
            .setNegativeButton("Annuler") { _, _ ->
                onResult(false)
            }
            .create()
    }
}
