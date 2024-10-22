package com.map.mobileapp.demo2.dao

import com.map.mobileapp.demo2.model.DailyStat
import com.map.mobileapp.demo2.model.Transaction
import java.util.Calendar

class DailyStatDAO (private val transactionDAO: TransactionDAO){
    // Hàm lấy thống kê cho một ngày cụ thể
    fun getDayStat(day: Int, month: Int, year: Int): DailyStat? {
        val transactions: List<Transaction> = transactionDAO.getAllTransactions()
        var totalIncome = 0.0
        var totalExpense = 0.0

        // Duyệt qua tất cả các giao dịch và tính toán thu nhập và chi tiêu của ngày đó
        transactions.forEach { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.getDate() }
            if (calendar.get(Calendar.DAY_OF_MONTH) == day && calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year) {
                when (transaction.getCatInOut().getInOut().getName()) {
                    "Income" -> totalIncome += transaction.getAmount()
                    "Expense" -> totalExpense += transaction.getAmount()
                }
            }
        }

        // Nếu có giao dịch trong ngày đó, trả về DailyStat, ngược lại trả về null
        return if (totalIncome > 0 || totalExpense > 0) {
            DailyStat(day, totalIncome, totalExpense)
        } else {
            null
        }
    }

    // Hàm lấy thống kê cho cả tháng
    fun getMonth(month: Int, year: Int): List<DailyStat> {
        val transactions: List<Transaction> = transactionDAO.getAllTransactions()
        val dailyStats = mutableMapOf<Int, DailyStat>()

        // Duyệt qua tất cả các giao dịch và tính toán thu nhập và chi tiêu của từng ngày trong tháng
        transactions.forEach { transaction ->
            val calendar = Calendar.getInstance().apply { time = transaction.getDate() }
            if (calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year) {
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val currentStat = dailyStats[day] ?: DailyStat(day, 0.0, 0.0)

                when (transaction.getCatInOut().getInOut().getName()) {
                    "Income" -> currentStat.income += transaction.getAmount()
                    "Expense" -> currentStat.expense += transaction.getAmount()
                }

                dailyStats[day] = currentStat
            }
        }

        // Trả về danh sách DailyStat cho từng ngày có giao dịch trong tháng
        return dailyStats.values.toList()
    }
}