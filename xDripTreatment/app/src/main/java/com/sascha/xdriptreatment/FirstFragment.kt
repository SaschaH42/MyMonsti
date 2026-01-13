package com.sascha.xdriptreatment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sascha.xdriptreatment.databinding.FragmentFirstBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private val xDripReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            logIntent("Intent Received", intent)
            when (intent.getStringExtra(MainActivity.INTENT_FUNCTION_KEY)) {
                MainActivity.CMD_UPDATE_BG, MainActivity.CMD_UPDATE_BG_FORCE -> {
                    _binding?.let {
                        val bgValue = intent.getDoubleExtra("bg.valueMgdl", 0.0)
                        if (bgValue > 0) {
                            val deltaValue = intent.getDoubleExtra("bg.deltaValueMgdl", 0.0)
                            val timestamp = intent.getLongExtra("bg.timeStamp", 0L)
                            val deltaName = intent.getStringExtra("bg.deltaName")

                            val roundedBg = bgValue.roundToInt()
                            val roundedDelta = deltaValue.roundToInt()
                            val timeDiff = if (timestamp > 0) (System.currentTimeMillis() - timestamp) / 60000 else 0

                            val deltaSymbol = when {
                                deltaName?.contains("Up") == true -> "▲"
                                deltaName?.contains("Down") == true -> "▼"
                                deltaName == "Flat" -> "→"
                                else -> ""
                            }

                            val deltaSign = if (roundedDelta >= 0) "+" else ""

                            // Part 1: Full size
                            val fullSizePart = "$roundedBg $deltaSymbol"
                            // Part 2: Half size
                            val halfSizePart = "$deltaSign$roundedDelta ($timeDiff min ago)"

                            val displayString = fullSizePart + halfSizePart
                            val spannable = SpannableString(displayString)

                            // Apply the half-size span to the second part of the string
                            spannable.setSpan(
                                RelativeSizeSpan(0.5f),
                                fullSizePart.length, // Start index of the half-size part
                                displayString.length,   // End index
                                0
                            )

                            it.textviewGlucose.text = spannable

                            logDebug("BG updated: $displayString")
                        }
                    }
                }
                MainActivity.CMD_REPLY_MSG -> handleTreatmentResponse(intent)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonAddTreatment.setOnClickListener {
            showAddTreatmentDialog()
        }

        binding.buttonClear.setOnClickListener {
            binding.edittextCarbs.text.clear()
            binding.edittextInsulin.text.clear()
        }
    }

    private fun showAddTreatmentDialog() {
        val carbsText = binding.edittextCarbs.text.toString()
        val insulinText = binding.edittextInsulin.text.toString()
        val glucoseText = binding.textviewGlucose.text.toString()

        val carbs = carbsText.toDoubleOrNull() ?: 0.0
        val insulin = insulinText.toDoubleOrNull() ?: 0.0

        if (carbs == 0.0 && insulin == 0.0) {
            Toast.makeText(requireContext(), "Please enter carbs or insulin", Toast.LENGTH_SHORT).show()
            return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Treatment")
            .setMessage("Glucose: $glucoseText\nCarbs: $carbs\nInsulin: $insulin")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK") { _, _ ->
                (activity as? MainActivity)?.sendTreatment(carbs, insulin)
            }
            .show()
    }

    private fun handleTreatmentResponse(intent: Intent) {
        val replyCode = intent.getStringExtra("REPLY_CODE")
        val replyMsg = intent.getStringExtra("REPLY_MSG")

        if (replyCode == "OK") {
            Toast.makeText(requireContext(), "Treatment added successfully", Toast.LENGTH_LONG).show()
        } else {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Treatment Failed")
                .setMessage(replyMsg ?: "Unknown error")
                .setPositiveButton("OK", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val senderAction = prefs.getString("broadcast_sender_action", MainActivity.ACTION_WATCH_COMMUNICATION_SENDER_DEFAULT) ?: MainActivity.ACTION_WATCH_COMMUNICATION_SENDER_DEFAULT
        
        val filter = IntentFilter(senderAction)
        ContextCompat.registerReceiver(
            requireActivity(),
            xDripReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        logDebug("Receiver registered with action: $senderAction")
        (activity as? MainActivity)?.requestXdripData()

        binding.scrollViewDebug.visibility = if (prefs.getBoolean("show_debug", false)) View.VISIBLE else View.GONE
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(xDripReceiver)
        logDebug("Receiver unregistered.")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun logDebug(message: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (prefs.getBoolean("show_debug", false)) {
            val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val currentText = binding.textviewDebug.text.toString()
            binding.textviewDebug.text = "$timestamp: $message\n$currentText"
        }
    }

    fun logIntent(logTitle: String, intent: Intent) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        if (prefs.getBoolean("show_debug", false)) {
            val bundle = intent.extras
            val stringBuilder = StringBuilder("$logTitle:\n")
            stringBuilder.append("  Action: ${intent.action}\n")
            stringBuilder.append("  Flags: ${intent.flags}\n")
            stringBuilder.append("  Package: ${intent.getPackage()}\n")
            if (bundle != null) {
                stringBuilder.append("  Extras:\n")
                for (key in bundle.keySet()) {
                    val value = bundle.get(key)
                    val valueType = value?.javaClass?.name ?: "null"
                    stringBuilder.append("    - $key: $value ($valueType)\n")
                }
            } else {
                stringBuilder.append("  Extras: null\n")
            }
            logDebug(stringBuilder.toString())
        }
    }
}