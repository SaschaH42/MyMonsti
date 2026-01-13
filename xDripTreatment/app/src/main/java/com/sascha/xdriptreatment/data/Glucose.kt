package com.sascha.xdriptreatment.data

import com.google.gson.annotations.SerializedName

data class Glucose(
    @SerializedName("_id") val id: String?,
    val device: String?,
    val dateString: String?,
    val sysTime: String?,
    val date: Long,
    val sgv: Int?,
    val delta: Double?,
    val direction: String?,
    val noise: Int?,
    val filtered: Double?,
    val unfiltered: Double?,
    val rssi: Int?,
    val type: String?,
    val units_hint: String?
)
