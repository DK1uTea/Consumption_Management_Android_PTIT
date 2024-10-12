package com.map.mobileapp.demo2.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.map.mobileapp.demo2.R
import com.map.mobileapp.demo2.dao.CategoryDAO
import com.map.mobileapp.demo2.model.Category
import com.map.mobileapp.demo2.model.InOut

class AddCategory : AppCompatActivity() {
    private lateinit var spnType: Spinner
    private lateinit var edtIcon: EditText
    private lateinit var edtNote: EditText
    private lateinit var spnParent: Spinner
    private lateinit var edtName: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnReset: Button
    private lateinit var parentCategories: List<Category>  // Define parentCategories

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_category)

        // Initialize views
        spnType = findViewById(R.id.spnType)
        spnParent = findViewById(R.id.spnParent)
        edtName = findViewById(R.id.edtName)
        btnAdd = findViewById(R.id.btnAdd)
        btnReset = findViewById(R.id.btnReset)
        edtIcon = findViewById(R.id.edtIcon)
        edtNote = findViewById(R.id.edtNote)

        // Setup the spinners and button actions
        setupTypeSpinner()
        setupParentSpinner()

        // Handle button click to add a category
        btnAdd.setOnClickListener {
            addCategory()
        }

        // Handle reset button click
        btnReset.setOnClickListener {
            resetFields()
        }
    }

    // Setup Type spinner (Income/Expense)
    private fun setupTypeSpinner() {
        val types = listOf(InOut(1, "Income"), InOut(2, "Expense"))
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types.map { it.getName() })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnType.adapter = adapter
    }

    // Setup Parent Category spinner
    private fun setupParentSpinner() {
        val categoryDAO = CategoryDAO(this)

        // Fetch parent categories from the database
        parentCategories = categoryDAO.getAllCategories()

        // If no parent category is available, show "None"
        val categories = if (parentCategories.isEmpty()) {
            listOf("None")
        } else {
            parentCategories.map { it.getName() }
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spnParent.adapter = adapter
    }

    // Method to add a category
    private fun addCategory() {
        // Retrieve data from form
        val name = edtName.text.toString()
        val type = spnType.selectedItem.toString()
        val parent = spnParent.selectedItem.toString()
        val icon = edtIcon.text.toString()
        val note = edtNote.text.toString()

        // Check if the category name is valid
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
            return
        }

        // Determine if it's Income or Expense
        val isIncome = type == "Income"

        // Fetch selected parent category ID (if any)
        val categoryDAO = CategoryDAO(this)
        val parentCategoryId = if (parent == "None") null else {
            val parentCategory = parentCategories.find { it.getName() == parent }
            parentCategory?.getId()
        }

        // Create Category object without id
        val category = Category(
            name = name,
            idParent = parentCategoryId ?: 0,  // Default to 0 if no parent
            icon = icon,
            note = note
        )
        Log.d("Debug new object cate: ", "$category")

        // Add category to the database and get the new category ID
        val newCategoryId = categoryDAO.addCategoryAndGetId(category)

        if (newCategoryId != -1L) {
            // Link the new category with InOut type
            val inOutId = if (isIncome) 1 else 2
            val catInOutResult = categoryDAO.addCatInOut(newCategoryId.toInt(), inOutId)

            if (catInOutResult) {
                Toast.makeText(this, "Category added successfully", Toast.LENGTH_SHORT).show()
                resetFields()

                // Navigate back to AddTransaction activity
                val intent = Intent(this, AddTransaction::class.java)
                intent.putExtra("refreshCategories", true)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Failed to link category with InOut", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Failed to add category", Toast.LENGTH_SHORT).show()
        }
    }


    // Reset the fields to their default state
    private fun resetFields() {
        edtName.text.clear()
        spnType.setSelection(0)
        spnParent.setSelection(0)
        edtIcon.text.clear()
        edtNote.text.clear()
    }
}
