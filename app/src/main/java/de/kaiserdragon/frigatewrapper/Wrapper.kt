@file:Suppress("OVERRIDE_DEPRECATION")

package de.kaiserdragon.frigatewrapper

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebView
import android.view.Window
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var browserView: WebView
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove the title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FrigateApp", MODE_PRIVATE)

        // Check if the URL is already set in SharedPreferences
        val url = sharedPreferences.getString("url", null)

        if (url == null) {
            // If URL is not set, start SetupActivity to get the URL
            val setupIntent = Intent(this, SetupActivity::class.java)
            startActivity(setupIntent)
            finish()  // Close MainActivity, since we are redirecting to SetupActivity
        } else {
            // If the URL is set, load it in the WebView
            setContentView(R.layout.activity_wrapper)
            browserView = findViewById(R.id.webkit)

            // Enable JavaScript
            browserView.settings.javaScriptEnabled = true
            browserView.isVerticalScrollBarEnabled = false
            browserView.isHorizontalScrollBarEnabled = false

            // Load the URL into the WebView
            browserView.loadUrl(url)
        }
    }

    // Handle back button press to navigate within WebView history
    override fun onBackPressed() {
        if (browserView.canGoBack()) {
            // If the WebView has a history, navigate back
            browserView.goBack()
        } else {
            // Otherwise, perform the default back press behavior (exit the activity)
            super.onBackPressed()
        }
    }
    // Inflate the options menu to show action bar buttons
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Handle the action bar button clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_reload -> {
                // Reload the WebView
                browserView.reload()
                true
            }
            R.id.action_settings -> {
                // Open SetupActivity to change the URL
                val setupIntent = Intent(this, SetupActivity::class.java)
                startActivity(setupIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

class SetupActivity : AppCompatActivity() {

    private lateinit var urlInput: EditText
    private lateinit var submitButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var tempWebView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FrigateApp", MODE_PRIVATE)

        urlInput = findViewById(R.id.url_input)
        submitButton = findViewById(R.id.submit_url_button)
        urlInput.setText(sharedPreferences.getString("url", ""))
        // Initialize the temporary WebView for title validation
        tempWebView = WebView(this)
        tempWebView.settings.javaScriptEnabled = true

        submitButton.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty()) {
                // Load the URL in the temporary WebView to check the title
                tempWebView.loadUrl(url)
                tempWebView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // After the page loads, check the title
                        val pageTitle = tempWebView.title
                        if (pageTitle == "Frigate") {
                            // Title is valid, save the URL to SharedPreferences
                            val editor = sharedPreferences.edit()
                            editor.putString("url", url)
                            editor.apply()

                            // Start MainActivity and pass the URL to it
                            val intent = Intent(this@SetupActivity, MainActivity::class.java)
                            intent.putExtra("url", url)
                            startActivity(intent)

                            // Close the SetupActivity
                            finish()
                        } else {
                            // Show error message if the title is not "Frigate"
                            Toast.makeText(this@SetupActivity,R.string.invalid_url_message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                // Handle empty URL input
                Toast.makeText(this, R.string.setup_url_error_message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}