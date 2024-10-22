package com.map.mobileapp.demo2.activity

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
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
    private lateinit var btnReset: Button
    private lateinit var imageView: ImageView
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
        etDate = findViewById(R.id.etDate)
        etAmount = findViewById(R.id.etAmount)
        etNote = findViewById(R.id.etNote)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        radioGroup = findViewById(R.id.radioGroup)
        radioButtonIncome = findViewById(R.id.radioButtonIncome)
        radioButtonExpense = findViewById(R.id.radioButtonExpense)
        btnAdd = findViewById(R.id.btnAdd)
        btnReset = findViewById(R.id.btnReset)
        imageView = findViewById(R.id.imageView_menu)

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

        // Thiết lập sự kiện click cho ImageView để hiển thị PopupMenu
        imageView.setOnClickListener { view: View ->
            showPopupMenu(view)
        }

        // Handle Add button click
        btnAdd.setOnClickListener {
            addTransaction()
        }

        // Handle reset button click
        btnReset.setOnClickListener {
            resetFields()
        }

        // Check if we need to refresh categories
        if (intent.getBooleanExtra("refreshCategories", false)) {
            // Determine which categories to load based on the selected radio button
            if (radioButtonIncome.isChecked) {
                loadCategories(true)
            } else {
                loadCategories(false)
            }
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
        // Fetch parent categories based on the selected type (Income or Expense)
        val parentCategories = categoryDAO.searchByInOut(isIncome).filter { it.getIdParent() == 0 }
        Log.d("Parent Categories", parentCategories.toString())

        // Create a custom ArrayAdapter to display only the category names
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, parentCategories.map { it.getName() })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set listener to capture the selected category
        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedParentCategory = parentCategories[position]
                val categoryId = selectedParentCategory.getId()
                if (categoryId != null && categoryDAO.hasChildren(categoryId)) {
                    showSubCategoryDialog(selectedParentCategory)
                } else {
                    // Chỉ cập nhật selectedCategory nếu chưa chọn subcategory trước đó
                    if (selectedCategory == null || selectedCategory!!.getIdParent() == 0) {
                        // Set selectedCategory là danh mục cha nếu không có subcategory
                        selectedCategory = selectedParentCategory
                        Log.d("Selected cate parent", selectedCategory.toString())
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }

    }

    private fun showSubCategoryDialog(parentCategory: Category) {
        val parentId = parentCategory.getId()
        if (parentId != null) {
            val subCategories = categoryDAO.getChildCategories(parentId)
            val subCategoryNames = subCategories.map { it.getName() }.toTypedArray()

            AlertDialog.Builder(this)
                .setTitle("Chọn danh mục con")
                .setItems(subCategoryNames) { _, which ->
                    // Khi người dùng chọn subcategory, cập nhật selectedCategory
                    selectedCategory = subCategories[which]
                    Log.d("Selected cate child", selectedCategory.toString())

                    // Update spinner để hiển thị danh mục con đã chọn
                    updateSpinnerWithSelectedCategory()
                }
                .setNegativeButton("Hủy") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Toast.makeText(this, "Danh mục cha không hợp lệ", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateSpinnerWithSelectedCategory() {
        if (selectedCategory != null) {
            // If a category has been selected, we can set the spinner's text to that category's name
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf(selectedCategory!!.getName()))
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerCategory.adapter = adapter

            // Optionally, set the spinner to the selected category
            spinnerCategory.setSelection(0) // Update this based on your use case
        }
    }


    private fun showPopupMenu(view: View) {
        // Tạo PopupMenu
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.menu_add_category, popupMenu.menu)

        // Xử lý sự kiện khi chọn item trong menu
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.menu_add -> {
                    // Chuyển sang AddCategory khi chọn "Add"
                    val intent = Intent(this, AddCategory::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Hiển thị menu
        popupMenu.show()
    }

    private fun addTransaction() {
        // Gather input values
        val amountStr = etAmount.text.toString()
        val dateStr = etDate.text.toString()
        val note = etNote.text.toString()

        // Validate inputs
        if (amountStr.isEmpty() || dateStr.isEmpty() || selectedCategory == null) {
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

        // Set name based on InOut
        val name = if (radioButtonIncome.isChecked) "Income" else "Expense"

        if (selectedCategory == null) {
            Toast.makeText(this, "Vui lòng chọn danh mục", Toast.LENGTH_SHORT).show()
            return
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

    // Reset the fields to their default state
    private fun resetFields() {
        etAmount.text.clear()
        etDate.text.clear()
        etNote.text.clear()
        spinnerCategory.setSelection(0)
    }
}
