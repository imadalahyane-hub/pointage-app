package com.pointage.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointage.app.R
import com.pointage.app.database.AppDatabase
import com.pointage.app.databinding.ActivityEmployeeSelectBinding
import com.pointage.app.models.Employee
import com.pointage.app.models.PointageType
import kotlinx.coroutines.launch

class EmployeeSelectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmployeeSelectBinding
    private lateinit var adapter: EmployeeAdapter
    private var allEmployees = listOf<Employee>()
    private lateinit var pointageType: PointageType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEmployeeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pointageType = PointageType.valueOf(
            intent.getStringExtra("pointage_type") ?: PointageType.ENTREE.name
        )

        val title = if (pointageType == PointageType.ENTREE) "Entrée — Sélectionnez votre nom" else "Sortie — Sélectionnez votre nom"
        binding.tvTitle.text = title
        if (pointageType == PointageType.ENTREE) {
            binding.root.setBackgroundColor(getColor(R.color.green_light))
        } else {
            binding.root.setBackgroundColor(getColor(R.color.red_light))
        }

        setupRecyclerView()
        setupSearch()
        loadEmployees()

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = EmployeeAdapter { employee ->
            val intent = Intent(this, FingerprintActivity::class.java).apply {
                putExtra("employee_id", employee.id)
                putExtra("employee_name", employee.fullName)
                putExtra("pointage_type", pointageType.name)
            }
            startActivity(intent)
            finish()
        }
        binding.rvEmployees.layoutManager = GridLayoutManager(this, 3)
        binding.rvEmployees.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().lowercase()
                val filtered = allEmployees.filter {
                    it.fullName.lowercase().contains(query)
                }
                adapter.submitList(filtered)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadEmployees() {
        lifecycleScope.launch {
            val db = AppDatabase.getInstance(this@EmployeeSelectActivity)
            allEmployees = db.employeeDao().getAllActiveOnce()
            adapter.submitList(allEmployees)
        }
    }
}

class EmployeeAdapter(
    private val onClick: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.VH>() {

    private var list = listOf<Employee>()

    fun submitList(newList: List<Employee>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_employee, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount() = list.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val tvName: TextView = view.findViewById(R.id.tvEmployeeName)
        private val tvPoste: TextView = view.findViewById(R.id.tvPoste)

        fun bind(employee: Employee) {
            tvName.text = employee.fullName
            tvPoste.text = employee.poste
            itemView.setOnClickListener { onClick(employee) }
        }
    }
}
