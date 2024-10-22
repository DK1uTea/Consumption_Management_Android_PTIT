package com.map.mobileapp.demo2.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.Gravity
import android.view.MotionEvent
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.map.mobileapp.demo2.R
import com.map.mobileapp.demo2.dao.DailyStatDAO
import com.map.mobileapp.demo2.dao.TransactionDAO
import com.map.mobileapp.demo2.model.DailyStat
import java.text.SimpleDateFormat
import java.util.*

class CalendarActivity : AppCompatActivity() {

    private lateinit var tvCurrentMonth: TextView
    private lateinit var gridCalendar: GridLayout
    private lateinit var dailyStatDAO: DailyStatDAO
    private lateinit var transactionDAO: TransactionDAO
    private val calendar = Calendar.getInstance()

    private lateinit var gestureDetector: GestureDetector

    companion object {
        private const val SWIPE_THRESHOLD = 100
        private const val SWIPE_VELOCITY_THRESHOLD = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        try {
            // Initialize TransactionDAO
            transactionDAO = TransactionDAO(this)

            // Initialize DAO for statistics
            dailyStatDAO = DailyStatDAO(transactionDAO)

            // Assign TextView and GridLayout from layout
            tvCurrentMonth = findViewById(R.id.tvCurrentMonth)
            gridCalendar = findViewById(R.id.gridCalendar)

            // Initialize GestureDetector
            gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 != null && e2 != null) {
                        val deltaX = e2.x - e1.x
                        val deltaY = e2.y - e1.y
                        if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (deltaX > 0) {
                                // Swipe right - go to previous month
                                calendar.add(Calendar.MONTH, -1)
                            } else {
                                // Swipe left - go to next month
                                calendar.add(Calendar.MONTH, 1)
                            }
                            updateMonthDisplay(calendar)
                            return true
                        }
                    }
                    return false
                }
            })

            // Display the current month
            updateMonthDisplay(calendar)

            // Set up GestureDetector
            gridCalendar.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        } catch (e: Exception) {
            Log.e("CalendarActivity", "Error initializing activity", e)
        }
    }

    private fun updateMonthDisplay(calendar: Calendar) {
        val monthFormat = SimpleDateFormat("MM/yyyy", Locale.getDefault())
        tvCurrentMonth.text = monthFormat.format(calendar.time)

        gridCalendar.removeAllViews()

        val dayNames = arrayOf("CN", "T2", "T3", "T4", "T5", "T6", "T7")
        for (dayName in dayNames) {
            val headerView = TextView(this)
            headerView.text = dayName
            headerView.gravity = Gravity.CENTER
            headerView.layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
            }
            gridCalendar.addView(headerView)
        }

        val monthStats: List<DailyStat> = dailyStatDAO.getMonth(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)

        // Calculate the number of rows needed
        val totalCells = daysInMonth + firstDayOfMonth - 1
        val numRows = (totalCells / 7) + if (totalCells % 7 == 0) 0 else 1

        gridCalendar.rowCount = numRows + 1 // +1 for the header row

        for (day in 1..daysInMonth) {
            val stat = monthStats.find { it.day == day } ?: DailyStat(day, 0.0, 0.0)
            val dayView = layoutInflater.inflate(R.layout.item_day, gridCalendar, false)

            val tvDay = dayView.findViewById<TextView>(R.id.tvDay)
            val tvIncome = dayView.findViewById<TextView>(R.id.tvIncome)
            val tvExpense = dayView.findViewById<TextView>(R.id.tvExpense)

            tvDay.text = day.toString()
            tvIncome.text = formatAmount(stat.income)
            tvExpense.text = formatAmount(stat.expense)

            val params = GridLayout.LayoutParams().apply {
                width = 0
                height = GridLayout.LayoutParams.WRAP_CONTENT
                rowSpec = GridLayout.spec((day + firstDayOfMonth - 2) / 7 + 1)
                columnSpec = GridLayout.spec((day + firstDayOfMonth - 2) % 7, 1f)
            }
            dayView.layoutParams = params

            dayView.setOnClickListener {
                if(stat.income > 0 || stat.expense > 0){
                    val intent = Intent(this, HomeAct::class.java)
                    intent.putExtra("day", day)
                    intent.putExtra("month", calendar.get(Calendar.MONTH))
                    intent.putExtra("year", calendar.get(Calendar.YEAR))
                    startActivity(intent)
                }
            }

            gridCalendar.addView(dayView)
        }
    }




    private fun formatAmount(amount: Double): String {
        return when {
            amount < 100 -> "${amount.toInt()} đ"
            amount < 1_000_000 -> "${(amount / 1000).format(1)} K"
            amount < 1_000_000_000 -> "${(amount / 1_000_000).format(1)} M"
            else -> "${(amount / 1_000_000_000).format(1)} B"
        }
    }

    // Extension function to format doubles with fixed decimal places
    private fun Double.format(digits: Int) = "%.${digits}f".format(this)


}
