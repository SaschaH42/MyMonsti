package com.sascha.xdriptreatment

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.eveningoutpost.dexdrip.services.broadcastservice.models.Settings
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.sascha.xdriptreatment.adapter.ViewPagerAdapter
import com.sascha.xdriptreatment.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        viewPager.adapter = ViewPagerAdapter(this)

        registerWithXdrip()
    }

    private fun getReceiverAction(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("broadcast_receiver_action", ACTION_WATCH_COMMUNICATION_RECEIVER_DEFAULT) ?: ACTION_WATCH_COMMUNICATION_RECEIVER_DEFAULT
    }

    private fun getXdripPackageName(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        return prefs.getString("xdrip_package_name", XDRIP_PACKAGE_NAME_DEFAULT) ?: XDRIP_PACKAGE_NAME_DEFAULT
    }

    private fun registerWithXdrip() {
        val settings = Settings()
        settings.apkName = packageName
        settings.setDisplayGraph(true)
        settings.setGraphStart(4 * 60 * 60 * 1000L) // 4 hours
        settings.setGraphEnd(0)

        val serviceIntent = Intent(getReceiverAction())
        serviceIntent.putExtra(INTENT_PACKAGE_KEY, packageName)
        serviceIntent.putExtra(INTENT_FUNCTION_KEY, CMD_SET_SETTINGS)
        serviceIntent.putExtra(INTENT_SETTINGS, settings)
        serviceIntent.setPackage(getXdripPackageName())
        logIntent("Registration Intent Sent", serviceIntent)
        sendBroadcast(serviceIntent)
    }

    fun requestXdripData() {
        val settings = Settings()
        settings.apkName = packageName
        settings.setDisplayGraph(true)
        settings.setGraphStart(4 * 60 * 60 * 1000L) // 4 hours
        settings.setGraphEnd(0)

        val serviceIntent = Intent(getReceiverAction())
        serviceIntent.putExtra(INTENT_PACKAGE_KEY, packageName)
        serviceIntent.putExtra(INTENT_FUNCTION_KEY, CMD_UPDATE_BG_FORCE)
        serviceIntent.putExtra(INTENT_SETTINGS, settings)
        serviceIntent.setPackage(getXdripPackageName())
        logIntent("Refresh Intent Sent", serviceIntent)
        sendBroadcast(serviceIntent)
    }

    fun sendTreatment(carbs: Double, insulin: Double) {
        val serviceIntent = Intent(getReceiverAction())
        serviceIntent.putExtra(INTENT_PACKAGE_KEY, packageName)
        serviceIntent.putExtra(INTENT_FUNCTION_KEY, CMD_ADD_TREATMENT)
        serviceIntent.putExtra("carbs", carbs)
        serviceIntent.putExtra("insulin", insulin)
        serviceIntent.putExtra("timeStamp", System.currentTimeMillis())
        serviceIntent.setPackage(getXdripPackageName())
        logIntent("Treatment Intent Sent", serviceIntent)
        sendBroadcast(serviceIntent)
    }

    private fun logIntent(logTitle: String, intent: Intent) {
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
        if (currentFragment is FirstFragment) {
            currentFragment.logIntent(logTitle, intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                val viewPager: ViewPager2 = findViewById(R.id.view_pager)
                val currentFragment = supportFragmentManager.findFragmentByTag("f" + viewPager.currentItem)
                if (currentFragment is SecondFragment) {
                    currentFragment.refresh()
                } else {
                    requestXdripData()
                }
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_info -> {
                val buildTime = Date(BuildConfig.BUILD_TIME)
                val buildDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(buildTime)
                MaterialAlertDialogBuilder(this)
                    .setTitle("Info")
                    .setMessage("MyMonsti by Sascha Jan 2026\n\nBuild: $buildDate")
                    .setPositiveButton("OK", null)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val XDRIP_PACKAGE_NAME_DEFAULT = "com.eveningoutpost.dexdrip"
        const val ACTION_WATCH_COMMUNICATION_RECEIVER_DEFAULT = "com.eveningoutpost.dexdrip.watch.wearintegration.BROADCAST_SERVICE_RECEIVER"
        const val ACTION_WATCH_COMMUNICATION_SENDER_DEFAULT = "com.eveningoutpost.dexdrip.watch.wearintegration.BROADCAST_SERVICE_SENDER"
        const val INTENT_PACKAGE_KEY = "PACKAGE"
        const val INTENT_FUNCTION_KEY = "FUNCTION"
        const val INTENT_SETTINGS = "SETTINGS"
        const val CMD_SET_SETTINGS = "set_settings"
        const val CMD_UPDATE_BG_FORCE = "update_bg_force"
        const val CMD_ADD_TREATMENT = "add_treatment"
        const val CMD_REPLY_MSG = "reply_msg"
        const val CMD_UPDATE_BG = "update_bg"
    }
}