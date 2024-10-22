package com.map.mobileapp.demo2.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.map.mobileapp.demo2.R
import com.map.mobileapp.demo2.adapter.TransactionAdapter
import com.map.mobileapp.demo2.dao.TransactionDAO
import com.map.mobileapp.demo2.model.Transaction
import java.text.SimpleDateFormat
import java.util.*

class HomeAct : AppCompatActivity() {

    private lateinit var transactionDAO: TransactionDAO
    private lateinit var tvCurrentDate: TextView
    private lateinit var tvTotalIncomeExpense: TextView
    private lateinit var lvIncomeExpenseList: ListView
    private val CALENDAR_REQUEST_CODE = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        transactionDAO = TransactionDAO(this)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        tvTotalIncomeExpense = findViewById(R.id.tvTotalIncomeExpense)
        lvIncomeExpenseList = findViewById(R.id.lvIncomeExpenseList)

        // Hiển thị ngày hiện tại
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvCurrentDate.text = currentDate

        // Lấy danh sách giao dịch từ TransactionDAO
        val transactions: List<Transaction> = transactionDAO.getAllTransactions()

        // Hiển thị tổng thu và tổng chi
        calculateAndDisplayTotal(transactions)

        // Cài đặt Adapter cho ListView
        val adapter = TransactionAdapter(this, transactions)
        lvIncomeExpenseList.adapter = adapter

        // Nút thêm mới giao dịch
        val btnAddNew = findViewById<Button>(R.id.btnAddNew)
        btnAddNew.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
        }

        // Nút mở CalendarActivity để chọn ngày
        val btnChooseDate = findViewById<Button>(R.id.btnChooseDate)
        btnChooseDate.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivityForResult(intent, CALENDAR_REQUEST_CODE)
        }

        // Kiểm tra xem có dữ liệu ngày được truyền từ CalendarActivity không
        val day = intent.getIntExtra("day", -1)
        val month = intent.getIntExtra("month", -1)
        val year = intent.getIntExtra("year", -1)

        if (day != -1 && month != -1 && year != -1) {
            val selectedDate = Calendar.getInstance().apply {
                set(year, month, day)
            }.time
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            tvCurrentDate.text = dateFormat.format(selectedDate)

            // Lấy và hiển thị danh sách giao dịch cho ngày đã chọn
            val transactionsByDate = transactionDAO.getTransactionsByDate(day, month, year)
            displayTransactions(transactionsByDate)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CALENDAR_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.getLongExtra("selectedDay", 0L)?.let { selectedTime ->
                val selectedDate = Date(selectedTime)
                val calendar = Calendar.getInstance().apply { time = selectedDate }
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                val year = calendar.get(Calendar.YEAR)

                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                tvCurrentDate.text = dateFormat.format(selectedDate)

                // Lấy và hiển thị danh sách giao dịch cho ngày đã chọn
                val transactionsByDate = transactionDAO.getTransactionsByDate(day, month, year)
                displayTransactions(transactionsByDate)

                Toast.makeText(this, "Ngày được chọn: ${tvCurrentDate.text}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayTransactions(transactions: List<Transaction>) {
        // Cài đặt Adapter cho ListView với danh sách giao dịch mới
        val adapter = TransactionAdapter(this, transactions)
        lvIncomeExpenseList.adapter = adapter

        // Tính toán và hiển thị tổng thu và tổng chi
        calculateAndDisplayTotal(transactions)
    }

    private fun calculateAndDisplayTotal(transactions: List<Transaction>) {
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (transaction in transactions) {
            if (transaction.getName() == "Income") {
                totalIncome += transaction.getAmount()
            } else if (transaction.getName() == "Expense") {
                totalExpense += transaction.getAmount()
            }
        }
        tvTotalIncomeExpense.text = "Tổng thu: $totalIncome - Tổng chi: $totalExpense"
    }
}
