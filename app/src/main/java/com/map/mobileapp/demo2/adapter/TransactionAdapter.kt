package com.map.mobileapp.demo2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.map.mobileapp.demo2.model.Transaction
import java.text.SimpleDateFormat
import java.util.Locale
import com.map.mobileapp.demo2.R

class TransactionAdapter(private val context: Context, private val transactions: List<Transaction>) : BaseAdapter() {

    override fun getCount(): Int {
        return transactions.size
    }

    override fun getItem(position: Int): Any {
        return transactions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)

        val cardView: CardView = view.findViewById(R.id.cardView)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvNote: TextView = view.findViewById(R.id.tvNote)

        val transaction = transactions[position]
        tvCategory.text = transaction.getCatInOut().getCategory().getName() // Display the category name
        tvAmount.text = transaction.getAmount().toString()

        // Format the date
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvDate.text = dateFormat.format(transaction.getDate())

        tvNote.text = transaction.getNote()
        tvName.text = transaction.getName()

        // Set background color based on transaction type
        if (transaction.getCatInOut().getInOut().getName() == "Income") {
            cardView.setCardBackgroundColor(context.getColor(R.color.incomeColor))
        } else {
            cardView.setCardBackgroundColor(context.getColor(R.color.expenseColor))
        }

        return view
    }
}
