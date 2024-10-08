package com.map.mobileapp.demo2.activity

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.map.mobileapp.demo2.R
import com.map.mobileapp.demo2.dao.DatabaseHelper
import com.map.mobileapp.demo2.dao.TransactionDAO
import com.map.mobileapp.demo2.dao.CategoryDAO
import com.map.mobileapp.demo2.model.Category
import com.map.mobileapp.demo2.model.CatInOut
import com.map.mobileapp.demo2.model.InOut
import com.map.mobileapp.demo2.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class AddTransaction : AppCompatActivity() {

    private lateinit var spinnerCategory: Spinner
    private lateinit var radioGroup: RadioGroup
    private lateinit var radioButtonIncome: RadioButton
    private lateinit var radioButtonExpense: RadioButton
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etName: EditText
    private lateinit var etAmount: EditText
    private lateinit var etDate: EditText
    private lateinit var etNote: EditText
    private lateinit var btnAdd: Button
    private lateinit var transactionDAO: TransactionDAO
    private lateinit var categoryDAO: CategoryDAO
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedCategory: Category? = null  // Change from Category to CatInOut

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_transaction)

        dbHelper = DatabaseHelper(this)
        transactionDAO = TransactionDAO(this)
        categoryDAO = CategoryDAO(this)

        // Initialize views
        etName = findViewById(R.id.etName)
        etDate = findViewById(R.id.etDate)
        etAmount = findViewById(R.id.etAmount)
        etNote = findViewById(R.id.etNote)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        radioGroup = findViewById(R.id.radioGroup)
        radioButtonIncome = findViewById(R.id.radioButtonIncome)
        radioButtonExpense = findViewById(R.id.radioButtonExpense)
        btnAdd = findViewById(R.id.btnAdd)

        // Set up the date picker
        etDate.setOnClickListener {
            showDatePickerDialog(etDate)
        }

        // Handle income/expense selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonIncome -> loadCategories(true)  // Load income categories
                R.id.radioButtonExpense -> loadCategories(false) // Load expense categories
            }
        }

        // Set default selection to Income
        radioButtonIncome.isChecked = true

        // Handle Add button click
        btnAdd.setOnClickListener {
            addTransaction()
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                editText.setText(selectedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun loadCategories(isIncome: Boolean) {
        // Fetch categories based on the selected type (Income or Expense)
        val categories = categoryDAO.searchByInOut(isIncome)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set listener to capture the selected category
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                selectedCategory = categories[position] // Vẫn giữ selectedCategory là Category
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }

    }

    private fun addTransaction() {
        // Gather input values
        val name = etName.text.toString()
        val amountStr = etAmount.text.toString()
        val dateStr = etDate.text.toString()
        val note = etNote.text.toString()

        // Validate inputs
        if (name.isEmpty() || amountStr.isEmpty() || dateStr.isEmpty() || selectedCategory == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse amount
        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        // Parse date
        val date = dateFormat.parse(dateStr)
        if (date == null) {
            Toast.makeText(this, "Invalid date", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine InOut based on selected radio button
        val inOut = if (radioButtonIncome.isChecked) {
            InOut(id = 1, name = "Income") // Assuming id=1 for Income
        } else {
            InOut(id = 2, name = "Expense") // Assuming id=2 for Expense
        }

        // Create CatInOut object
        val catInOut = CatInOut(
            id = selectedCategory!!.getId(),
            category = selectedCategory!!,
            inOut = inOut
        )

        // Create Transaction object with CatInOut
        val transaction = Transaction(
            id = null,  // Assuming you're inserting a new transaction
            name = name,
            catInOut = catInOut,  // Pass the created CatInOut object
            amount = amount,
            date = date,
            note = note
        )
        Log.d("Debug date: ", "${transaction.getDate()}")

        // Insert transaction into database
        val success = transactionDAO.add(transaction)
        if (success) {
            Toast.makeText(this, "Transaction added successfully", Toast.LENGTH_SHORT).show()
            // Navigate to home screen
            startActivity(Intent(this, HomeAct::class.java))
            finish()
        } else {
            Toast.makeText(this, "Failed to add transaction", Toast.LENGTH_SHORT).show()
        }
    }


}
