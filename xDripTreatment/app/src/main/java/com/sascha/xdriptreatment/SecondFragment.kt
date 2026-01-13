package com.sascha.xdriptreatment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sascha.xdriptreatment.adapter.TreatmentAdapter
import com.sascha.xdriptreatment.data.Glucose
import com.sascha.xdriptreatment.data.TreatmentData
import com.sascha.xdriptreatment.data.TreatmentListItem
import com.sascha.xdriptreatment.databinding.FragmentSecondBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerViewTreatments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = TreatmentAdapter(emptyList()) // Set an empty adapter initially
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    fun refresh() {
        if (!isAdded) return

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Fetch treatments and glucose data concurrently
                val treatmentsDeferred = async { fetchTreatmentsData() }
                val glucoseDeferred = async { fetchGlucoseData() }

                val treatments = treatmentsDeferred.await()
                val glucoseValues = glucoseDeferred.await()

                if (treatments != null && glucoseValues != null) {
                    val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                    val glucoseMatchWindow = prefs.getString("glucose_match_window", "20")?.toLongOrNull() ?: 20L

                    treatments.forEach { treatment ->
                        val (glucoseValue, glucoseAge) = findClosestGlucose(treatment.createdAt, glucoseValues, glucoseMatchWindow)
                        treatment.glucoseValue = glucoseValue
                        treatment.glucoseAge = glucoseAge
                    }
                    val processedList = processTreatments(treatments)

                    withContext(Dispatchers.Main) {
                        if (view?.isLaidOut == true) {
                            _binding?.recyclerViewTreatments?.adapter = TreatmentAdapter(processedList)
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private suspend fun fetchTreatmentsData(): List<TreatmentData>? {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val endpoint = prefs.getString("api_endpoint", "http://127.0.0.1:17580")
                val count = prefs.getString("treatments_count", "100")
                val url = URL("$endpoint/treatments.json?count=$count")
                val response = readUrl(url)
                val type = object : TypeToken<List<TreatmentData>>() {}.type
                Gson().fromJson(response, type)
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun fetchGlucoseData(): List<Glucose>? {
        return withContext(Dispatchers.IO) {
            try {
                val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
                val endpoint = prefs.getString("api_endpoint", "http://127.0.0.1:17580")
                val count = prefs.getString("glucose_count", "600")
                val url = URL("$endpoint/sgv.json?count=$count")
                val response = readUrl(url)
                val type = object : TypeToken<List<Glucose>>() {}.type
                Gson().fromJson(response, type)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun readUrl(url: URL): String {
        val connection = url.openConnection() as HttpURLConnection
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = reader.readText()
        reader.close()
        connection.disconnect()
        return response
    }

    private fun findClosestGlucose(treatmentTime: Long, glucoseValues: List<Glucose>, matchWindow: Long): Pair<String, String> {
        var closestGlucose: Glucose? = null
        var minAbsTimeDiff = Long.MAX_VALUE

        for (glucose in glucoseValues) {
            val timeDiff = abs(treatmentTime - glucose.date)
            if (timeDiff < minAbsTimeDiff) {
                minAbsTimeDiff = timeDiff
                closestGlucose = glucose
            }
        }

        if (closestGlucose?.sgv != null && minAbsTimeDiff / 60000 <= matchWindow) {
            val signedMinutesDiff = (treatmentTime - closestGlucose.date) / 60000
            val sign = if (signedMinutesDiff >= 0) "+" else ""
            return Pair("${closestGlucose.sgv}", "($sign${signedMinutesDiff}min)")
        }

        return Pair("---", "")
    }

    private fun processTreatments(treatments: List<TreatmentData>): List<TreatmentListItem> {
        if (treatments.isEmpty()) return emptyList()

        val items = mutableListOf<TreatmentListItem>()
        val groupedByDay = treatments.groupBy {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.createdAt
            cal.get(Calendar.DAY_OF_YEAR) to cal.get(Calendar.YEAR)
        }

        val sortedKeys = groupedByDay.keys.sortedWith(compareByDescending { it: Pair<Int, Int> -> it.second }.thenByDescending { it.first })

        for (key in sortedKeys) {
            val dayTreatments = groupedByDay[key]!!
            val firstTreatment = dayTreatments.first()
            val cal = Calendar.getInstance().apply { timeInMillis = firstTreatment.createdAt }
            val dateString = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(cal.time)

            val totalCarbs = dayTreatments.sumOf { it.carbs ?: 0.0 }
            val totalInsulin = dayTreatments.sumOf { it.insulin ?: 0.0 }

            items.add(TreatmentListItem.DateSeparator(dateString, totalCarbs, totalInsulin))
            dayTreatments.forEach { items.add(TreatmentListItem.Treatment(it)) }
        }
        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}