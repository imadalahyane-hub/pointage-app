package com.pointage.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.pointage.app.R
import com.pointage.app.database.AppDatabase
import com.pointage.app.databinding.ActivityAdminBinding
import com.pointage.app.models.Employee
import com.pointage.app.network.RetrofitClient
import kotlinx.coroutines.launch

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val db by lazy { AppDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddEmployee.setOnClickListener { showAddEmployeeDialog() }
        binding.btnSyncNow.setOnClickListener { syncEmployees() }

        loadEmployees()
    }

    private fun loadEmployees() {
        lifecycleScope.launch {
            val employees = db.employeeDao().getAllActiveOnce()
            setupList(employees)
            binding.tvCount.text = "${employees.size} employé(s)"
        }
    }

    private fun setupList(employees: List<Employee>) {
        val adapter = AdminEmployeeAdapter(employees) { emp ->
            showEmployeeOptions(emp)
        }
        binding.rvEmployees.layoutManager = LinearLayoutManager(this)
        binding.rvEmployees.adapter = adapter
    }

    private fun showAddEmployeeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_employee, null)
        AlertDialog.Builder(this)
            .setTitle("Ajouter un employé")
            .setView(dialogView)
            .setPositiveButton("Ajouter") { _, _ ->
                val nom = dialogView.findViewById<TextInputEditText>(R.id.etNom).text.toString()
                val prenom = dialogView.findViewById<TextInputEditText>(R.id.etPrenom).text.toString()
                val poste = dialogView.findViewById<TextInputEditText>(R.id.etPoste).text.toString()

                if (nom.isNotBlank() && prenom.isNotBlank()) {
                    addEmployee(nom, prenom, poste)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun addEmployee(nom: String, prenom: String, poste: String) {
        lifecycleScope.launch {
            val employee = Employee(
                nom = nom,
                prenom = prenom,
                poste = poste
            )
            db.employeeDao().insert(employee)
            // Envoyer au backend si connecté
            try {
                RetrofitClient.api.createEmployee(employee)
            } catch (e: Exception) {
                // Sera synchronisé plus tard
            }
            loadEmployees()
        }
    }

    private fun showEmployeeOptions(employee: Employee) {
        AlertDialog.Builder(this)
            .setTitle(employee.fullName)
            .setItems(arrayOf("Désactiver", "Annuler")) { _, which ->
                if (which == 0) {
                    lifecycleScope.launch {
                        db.employeeDao().deactivate(employee.id)
                        loadEmployees()
                    }
                }
            }
            .show()
    }

    private fun syncEmployees() {
        binding.btnSyncNow.isEnabled = false
        binding.btnSyncNow.text = "Synchronisation..."
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.getEmployees()
                if (response.isSuccessful) {
                    response.body()?.let { employees ->
                        db.employeeDao().insertAll(employees)
                        binding.tvSyncStatus.text = "✓ ${employees.size} employés synchronisés"
                    }
                } else {
                    binding.tvSyncStatus.text = "Erreur de synchronisation"
                }
            } catch (e: Exception) {
                binding.tvSyncStatus.text = "Hors ligne — sync impossible"
            }
            binding.btnSyncNow.isEnabled = true
            binding.btnSyncNow.text = "Synchroniser"
            loadEmployees()
        }
    }
}

class AdminEmployeeAdapter(
    private val employees: List<Employee>,
    private val onLongClick: (Employee) -> Unit
) : RecyclerView.Adapter<AdminEmployeeAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_employee, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(employees[position])
    }

    override fun getItemCount() = employees.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvName)
        private val tvPoste: TextView = view.findViewById(R.id.tvPoste)

        fun bind(employee: Employee) {
            tvName.text = employee.fullName
            tvPoste.text = employee.poste
            itemView.setOnLongClickListener {
                onLongClick(employee)
                true
            }
        }
    }
}
