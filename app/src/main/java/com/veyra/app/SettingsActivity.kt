package com.veyra.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class SettingsActivity : AppCompatActivity() {

    // ⚠️ REPLACE THIS WITH YOUR ACTUAL GITHUB USERNAME
    private val GITHUB_USERNAME = "nidashay" 
    private val REPO_NAME = "Veyra"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val currentVersionText = findViewById<TextView>(R.id.currentVersionText)
        currentVersionText.text = "Current Version: v${BuildConfig.VERSION_NAME}"

        val checkUpdatesBtn = findViewById<Button>(R.id.checkUpdatesBtn)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        checkUpdatesBtn.setOnClickListener {
            checkUpdatesBtn.isEnabled = false
            progressBar.visibility = View.VISIBLE
            
            checkForUpdates { isUpdateAvailable, newVersion, downloadUrl ->
                runOnUiThread {
                    checkUpdatesBtn.isEnabled = true
                    progressBar.visibility = View.GONE
                    
                    if (isUpdateAvailable) {
                        showUpdateDialog(newVersion, downloadUrl)
                    } else {
                        Toast.makeText(this, "You are on the latest version!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun checkForUpdates(callback: (Boolean, String, String) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/$GITHUB_USERNAME/$REPO_NAME/releases/latest")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val jsonResult = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonResult)
                
                // GitHub tags often look like "v1.0.0", so we strip the 'v' to compare with "1.0.0"
                val tagName = json.getString("tag_name").removePrefix("v")
                val currentVersion = BuildConfig.VERSION_NAME
                
                if (tagName != currentVersion) {
                    val assets = json.getJSONArray("assets")
                    if (assets.length() > 0) {
                        val downloadUrl = assets.getJSONObject(0).getString("browser_download_url")
                        callback(true, tagName, downloadUrl)
                    } else {
                        callback(false, "", "")
                    }
                } else {
                    callback(false, "", "")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Failed to check for updates.", Toast.LENGTH_SHORT).show()
                }
                callback(false, "", "")
            }
        }
    }

    private fun showUpdateDialog(newVersion: String, downloadUrl: String) {
        AlertDialog.Builder(this)
            .setTitle("Update Available!")
            .setMessage("Veyra v$newVersion is available. Would you like to download it?")
            .setPositiveButton("Download") { _, _ ->
                // Opens the GitHub release page in the browser to download the APK
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                startActivity(intent)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}