package com.map.mobileapp.demo2.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.map.mobileapp.demo2.model.Transaction
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
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)

        val transaction = transactions[position]
        tvName.text = transaction.getName()
        tvAmount.text = formatAmount(transaction.getAmount())

        // Set background color based on transaction type
        if (transaction.getCatInOut().getInOut().getName() == "Income") {
            cardView.setCardBackgroundColor(context.getColor(R.color.incomeColor))
        } else {
            cardView.setCardBackgroundColor(context.getColor(R.color.expenseColor))
        }

        return view
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
