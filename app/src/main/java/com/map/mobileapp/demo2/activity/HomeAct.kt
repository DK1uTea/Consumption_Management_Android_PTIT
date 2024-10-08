package com.map.mobileapp.demo2.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // Khởi tạo TransactionDAO
        transactionDAO = TransactionDAO(this)

        // Khởi tạo các TextView và ListView từ giao diện
        tvCurrentDate = findViewById(R.id.tvCurrentDate)
        tvTotalIncomeExpense = findViewById(R.id.tvTotalIncomeExpense)
        lvIncomeExpenseList = findViewById(R.id.lvIncomeExpenseList)

        // Hiển thị ngày hiện tại
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        tvCurrentDate.text = currentDate

        // Lấy danh sách giao dịch từ TransactionDAO
        val transactions: List<Transaction> = transactionDAO.getAllTransactions()

        // Tính tổng thu và tổng chi
        var totalIncome = 0.0
        var totalExpense = 0.0
        for (transaction in transactions) {
            if (transaction.getName() == "Income") {
                totalIncome += transaction.getAmount()
            } else if (transaction.getName() == "Expense") {
                totalExpense += transaction.getAmount()
            }
        }

        // Hiển thị tổng thu và tổng chi
        val incomeExpenseText = "Tổng thu: $totalIncome - Tổng chi: $totalExpense"
        tvTotalIncomeExpense.text = incomeExpenseText

        // Cài đặt Adapter cho ListView để hiển thị danh sách giao dịch
        val adapter = TransactionAdapter(this, transactions)
        lvIncomeExpenseList.adapter = adapter

        // Nút thêm mới giao dịch
        val btnAddNew = findViewById<Button>(R.id.btnAddNew)
        btnAddNew.setOnClickListener {
            val intent = Intent(this, AddTransaction::class.java)
            startActivity(intent)
        }
    }
}
