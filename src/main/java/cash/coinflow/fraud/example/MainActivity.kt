package cash.coinflow.fraud.example

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import cash.coinflow.fraud.CoinflowFraud
import cash.coinflow.fraud.CoinflowFraudEnv
import cash.coinflow.fraud.ProtectionHeader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    private companion object {
        const val TAG = "CoinflowFraudExample"

        // Gives initialize time to start the signal SDKs before the autorun collects.
        const val AUTORUN_DELAY_MILLIS = 5_000L

        val ENV = if (BuildConfig.ENV == "prod") CoinflowFraudEnv.PROD else CoinflowFraudEnv.SANDBOX
    }

    private lateinit var getHeaderButton: Button
    private lateinit var sendRequestButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<TextView>(R.id.subtitle).text =
            "merchant ${BuildConfig.MERCHANT_ID} · env ${BuildConfig.ENV}"
        getHeaderButton = findViewById(R.id.getHeader)
        sendRequestButton = findViewById(R.id.sendRequest)

        lifecycleScope.launch {
            CoinflowFraud.initialize(this@MainActivity, BuildConfig.MERCHANT_ID, ENV)
        }

        getHeaderButton.setOnClickListener { lifecycleScope.launch { onGetHeader() } }
        if (BuildConfig.CHARGE_URL.isNotEmpty()) {
            sendRequestButton.visibility = View.VISIBLE
            sendRequestButton.setOnClickListener { lifecycleScope.launch { onSendRequest() } }
        }

        if (BuildConfig.AUTORUN) lifecycleScope.launch { autorun() }
    }

    private suspend fun requestHeaders(): Map<String, String> =
        mapOf("Content-Type" to "application/json") + CoinflowFraud.purchaseHeaders()

    private suspend fun onGetHeader() {
        setBusy(true)
        try {
            showHeaders(requestHeaders())
            findViewById<View>(R.id.responseSection).visibility = View.GONE
        } finally {
            setBusy(false)
        }
    }

    private suspend fun onSendRequest() {
        setBusy(true)
        try {
            val headers = requestHeaders()
            showHeaders(headers)
            showResponse(post(url = BuildConfig.CHARGE_URL, headers = headers))
        } finally {
            setBusy(false)
        }
    }

    private suspend fun autorun() {
        delay(AUTORUN_DELAY_MILLIS)
        val headers = requestHeaders()
        showHeaders(headers)
        Log.i(TAG, "COINFLOW_FRAUD_RESULT ${json(headers, indent = 0)}")
    }

    private suspend fun post(url: String, headers: Map<String, String>): String =
        withContext(Dispatchers.IO) {
            runCatching {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                headers.forEach { (name, value) -> connection.setRequestProperty(name, value) }
                connection.outputStream.use { it.write("{}".toByteArray()) }

                val status = connection.responseCode
                val stream = if (status < 400) connection.inputStream else connection.errorStream
                "$status\n${stream?.bufferedReader()?.use { it.readText() }.orEmpty()}"
            }.getOrElse { it.toString() }
        }

    private fun showHeaders(headers: Map<String, String>) {
        findViewById<TextView>(R.id.deviceId).text =
            headers[ProtectionHeader.NAME] ?: "(empty — no signal was available)"
        findViewById<TextView>(R.id.requestHeaders).text = json(headers, indent = 2)
        findViewById<View>(R.id.results).visibility = View.VISIBLE
    }

    private fun showResponse(response: String) {
        findViewById<TextView>(R.id.response).text = response
        findViewById<View>(R.id.responseSection).visibility = View.VISIBLE
    }

    // org.json escapes "/" as "\/", which is valid JSON but noisy in a demo.
    private fun json(headers: Map<String, String>, indent: Int): String {
        val json = JSONObject(headers)
        val text = if (indent > 0) json.toString(indent) else json.toString()
        return text.replace("\\/", "/")
    }

    private fun setBusy(busy: Boolean) {
        getHeaderButton.isEnabled = !busy
        getHeaderButton.text = if (busy) "Collecting…" else "Get x-device-id header"
        sendRequestButton.isEnabled = !busy
    }
}
