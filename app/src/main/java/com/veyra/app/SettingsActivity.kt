package com.veyra.app

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class SettingsActivity : AppCompatActivity() {

    private val GITHUB_USERNAME = "nidashay" // ⚠️ REPLACE THIS!
    private val REPO_NAME = "Veyra"

    private lateinit var downloadContainer: LinearLayout
    private lateinit var downloadProgressBar: ProgressBar
    private lateinit var downloadStatusText: TextView
    private lateinit var downloadPercentageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val currentVersionText = findViewById<TextView>(R.id.currentVersionText)
        currentVersionText.text = "Current Version: v${BuildConfig.VERSION_NAME}"

        val checkUpdatesBtn = findViewById<Button>(R.id.checkUpdatesBtn)
        downloadContainer = findViewById(R.id.downloadContainer)
        downloadProgressBar = findViewById(R.id.downloadProgressBar)
        downloadStatusText = findViewById(R.id.downloadStatusText)
        downloadPercentageText = findViewById(R.id.downloadPercentageText)

        checkUpdatesBtn.setOnClickListener {
            checkUpdatesBtn.isEnabled = false
            checkUpdatesBtn.text = "Checking..."
            
            checkForUpdates { isUpdateAvailable, newVersion, downloadUrl ->
                runOnUiThread {
                    checkUpdatesBtn.isEnabled = true
                    checkUpdatesBtn.text = "Check For Updates"
                    
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
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                
                val jsonResult = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(jsonResult)
                
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
            .setMessage("Veyra v$newVersion is ready to download and install. Proceed?")
            .setPositiveButton("Download & Install") { _, _ ->
                startInAppDownload(downloadUrl)
            }
            .setNegativeButton("Later", null)
            .show()
    }

    private fun startInAppDownload(downloadUrl: String) {
        // Check if app has permission to install unknown apps (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Veyra needs permission to install updates. Please enable 'Install unknown apps' for Veyra in the next screen.")
                    .setPositiveButton("Grant Permission") { _, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
                return
            }
        }

        downloadContainer.visibility = View.VISIBLE
        downloadStatusText.text = "Connecting..."
        downloadProgressBar.progress = 0
        downloadPercentageText.text = "0%"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL(downloadUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                
                val fileLength = connection.contentLength
                val downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadDir, "veyra-update.apk")

                connection.inputStream.use { input ->
                    FileOutputStream(file).use { output ->
                        val data = ByteArray(8192) // 8KB buffer (very RAM friendly)
                        var total: Long = 0
                        var count: Int
                        
                        downloadStatusText.text = "Downloading..."
                        
                        while (input.read(data).also { count = it } != -1) {
                            total += count
                            val progress = (total * 100 / fileLength).toInt()
                            
                            withContext(Dispatchers.Main) {
                                downloadProgressBar.progress = progress
                                downloadPercentageText.text = "$progress%"
                            }
                            output.write(data, 0, count)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    downloadStatusText.text = "Download complete! Installing..."
                    installApk(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    downloadStatusText.text = "Download failed. Tap to retry."
                    downloadContainer.setOnClickListener { startInAppDownload(downloadUrl) }
                }
            }
        }
    }

    private fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this, "com.veyra.app.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
        
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}