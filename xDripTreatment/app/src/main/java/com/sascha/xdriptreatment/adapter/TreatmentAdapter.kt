package com.sascha.xdriptreatment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sascha.xdriptreatment.R
import com.sascha.xdriptreatment.data.TreatmentData
import com.sascha.xdriptreatment.data.TreatmentListItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TreatmentAdapter(private val items: List<TreatmentListItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TREATMENT = 0
        private const val TYPE_DATE_SEPARATOR = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TreatmentListItem.Treatment -> TYPE_TREATMENT
            is TreatmentListItem.DateSeparator -> TYPE_DATE_SEPARATOR
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_TREATMENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_treatment_row, parent, false)
            TreatmentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_date_separator, parent, false)
            DateSeparatorViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is TreatmentListItem.Treatment -> (holder as TreatmentViewHolder).bind(item.treatment)
            is TreatmentListItem.DateSeparator -> (holder as DateSeparatorViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    class TreatmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val time: TextView = itemView.findViewById(R.id.tv_time)
        private val age: TextView = itemView.findViewById(R.id.tv_age)
        private val glucoseValue: TextView = itemView.findViewById(R.id.tv_glucose_value)
        private val glucoseAge: TextView = itemView.findViewById(R.id.tv_glucose_age)
        private val carbs: TextView = itemView.findViewById(R.id.tv_carbs)
        private val insulin: TextView = itemView.findViewById(R.id.tv_insulin)
        private val notes: TextView = itemView.findViewById(R.id.tv_notes)

        fun bind(treatment: TreatmentData) {
            time.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(treatment.createdAt))
            glucoseValue.text = treatment.glucoseValue
            glucoseAge.text = treatment.glucoseAge
            carbs.text = treatment.carbs?.toString() ?: ""
            insulin.text = treatment.insulin?.toString() ?: ""
            notes.text = treatment.notes ?: ""

            val treatmentCal = Calendar.getInstance().apply { timeInMillis = treatment.createdAt }
            val nowCal = Calendar.getInstance()

            if (treatmentCal.get(Calendar.DAY_OF_YEAR) == nowCal.get(Calendar.DAY_OF_YEAR) &&
                treatmentCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR)) {

                val diff = nowCal.timeInMillis - treatmentCal.timeInMillis
                val hours = diff / (60 * 60 * 1000)
                val minutes = (diff / (60 * 1000)) % 60

                age.text = String.format("(%02dh%02d)", hours, minutes)
                age.visibility = View.VISIBLE
            } else {
                age.visibility = View.GONE
            }
        }
    }

    class DateSeparatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val date: TextView = itemView.findViewById(R.id.tv_date)
        private val carbSum: TextView = itemView.findViewById(R.id.tv_carb_sum)
        private val insulinSum: TextView = itemView.findViewById(R.id.tv_insulin_sum)

        fun bind(separator: TreatmentListItem.DateSeparator) {
            date.text = separator.date
            carbSum.text = String.format(Locale.US, "Σ Carbs: %.2f", separator.carbSum)
            insulinSum.text = String.format(Locale.US, "Σ Insulin: %.2f", separator.insulinSum)
        }
    }
}
