package com.sascha.xdriptreatment.data

import com.google.gson.annotations.SerializedName

// Using a sealed class to represent the different types of items in our list
sealed class TreatmentListItem {
    data class Treatment(val treatment: TreatmentData) : TreatmentListItem()
    data class DateSeparator(val date: String, val carbSum: Double, val insulinSum: Double) : TreatmentListItem()
}

data class TreatmentData(
    @SerializedName("_id") val id: String,
    @SerializedName("created_at") val createdAt: Long,
    val eventType: String?,
    val enteredBy: String?,
    val carbs: Double?,
    val insulin: Double?,
    val notes: String?,
    var glucoseValue: String = "---", // Value of the closest glucose reading
    var glucoseAge: String = ""       // Age/delta of that reading
)
