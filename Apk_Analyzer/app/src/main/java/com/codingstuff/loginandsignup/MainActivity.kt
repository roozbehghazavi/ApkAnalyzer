package com.codingstuff.loginandsignup

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.MediaScannerConnection.scanFile
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import android.content.pm.PackageInfo
import java.nio.Buffer
import kotlin.math.log

private const val API_KEY = "de90b189350c6f9b1d3ae502562855427bd998e8cb6969ddff5c2443f6652200"
private const val PICK_FILE_REQUEST_CODE = 123

class MainActivity : AppCompatActivity() {

    private lateinit var chosenFileUri: Uri

    // Inside your MainActivity
    private lateinit var statusTextView: TextView
    private var fileName: String = ""
    private var hash: String = ""
    private var scanType: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize statusTextView
        statusTextView = findViewById(R.id.statusTextView)

        // Find and set click listener for the uploadButton
        val uploadButton: Button = findViewById(R.id.chooseFileButton)
        val scanButton: Button = findViewById(R.id.beginScanButton)
        val downloadButton: Button = findViewById(R.id.begindownloadButton)


        uploadButton.setOnClickListener {
            chooseFile()
            statusTextView.text = "File uploading..."
        }
        scanButton.setOnClickListener {
            scanFile(fileName, hash, scanType)
            statusTextView.text = "Start Scanning..."
        }
        downloadButton.setOnClickListener {
            downloadPdf(hash)
        }
    }


    private fun chooseFile() {
        requestFilePermission()
        if (isFilePermissionGranted()) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*" // Set the MIME type to allow any file type
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        val chooserIntent = Intent.createChooser(intent, "Choose File")
        startActivityForResult(chooserIntent, PICK_FILE_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { fileUri ->
                chosenFileUri = fileUri
                val file = getFileFromUri(chosenFileUri)
                if (file != null) {
                    uploadFile(file)
                } else {
                    Log.e("MainActivity", "Failed to get file from Uri")
                }
            }
        }
    }
    fun parseApk(file: File) {
        // Check if a file is selected
        if (chosenFileUri != null) {
            // Get the APK file path from the chosen file URI
            val apkFilePath = getFileFromUri(chosenFileUri)

            // Parse APK metadata
            val packageInfo = packageManager.getPackageArchiveInfo(file.path, PackageManager.GET_META_DATA)
            packageInfo?.applicationInfo?.let { appInfo ->
                val packageName = appInfo.packageName
                val versionName = packageInfo.versionName
                val versionCode = packageInfo.versionCode
                val targetSdkVersion = appInfo.targetSdkVersion

                // Get the list of permissions
                val permissions = packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)?.requestedPermissions

                // Display the parsed metadata
                val metadataString = "Package Name: $packageName\n" +
                        "Version Name: $versionName\n" +
                        "Version Code: $versionCode\n" +
                        "Target SDK Version: $targetSdkVersion\n" +
                        "Permissions: ${permissions?.joinToString()}"
                Log.d("MainActivity", "Parsed Metadata: $metadataString")

            }
        } else {
            // No file selected, show an error message or handle the case appropriately
            Log.d("MainActivity", "No file selected for parsing.")
        }
    }
    private fun getFileFromUri(uri: Uri): File? {
        val filePath: String? = uri.path
        if (filePath != null && filePath.isNotEmpty()) {
            return File(filePath)
        }
        return null
    }


    private fun uploadFile(file: File) {
        val client = OkHttpClient()

        val mediaType = "application/octet-stream".toMediaTypeOrNull()
        val requestBody = RequestBody.create(mediaType,file)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", file.name, requestBody)
            .build()


        val request = Request.Builder()
            .url("http://10.0.2.2:8000/api/v1/upload")
            .post(multipartBody)
            .header("Authorization", API_KEY)
            .build()

        runOnUiThread {
            statusTextView.text = "Uploading file..."
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // File upload failed due to network or other issues
                runOnUiThread {
                    statusTextView.text = "Upload failed!"
                    statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.upload_failure))
                }
                Log.e("MainActivity", "File Upload failed. Exception: ${e.message}")
            }
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // File upload successful
                    runOnUiThread {
                        statusTextView.text = "Upload successful"
                        statusTextView.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.upload_success
                            )
                        )
                    }
                    val responseBody = response.body?.string()
                    // Process the response body as needed
                    Log.d("MainActivity", "File upload successful. Response: $responseBody")
                    val responseString = response.body?.string()
                    responseString?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            fileName = jsonObject.getString("file_name")
                            hash = jsonObject.getString("hash")
                            scanType = jsonObject.getString("scan_type")

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }
                } else {
                    // File upload failed
                    runOnUiThread {
                        statusTextView.text = "Upload failed"
                        statusTextView.setTextColor(
                            ContextCompat.getColor(
                                this@MainActivity,
                                R.color.upload_failure
                            )
                        )
                    }
                    Log.e("MainActivity", "File upload failed. Response code: ${response.code}")
                }
            }
        })
    }
            private fun scanFile(fileName: String, hash: String, scanType: String) {
                // Create a request to the "/api/v1/scan" endpoint
                val client = OkHttpClient()
                val requestBody = FormBody.Builder()
                    .add("file_name", fileName)
                    .add("hash", hash)
                    .add("scan_type", scanType)
                    .build()

                val request = Request.Builder()
                    .url("http://10.0.2.2:8000/api/v1/scan")
                    .post(requestBody)
                    .build()

                runOnUiThread {
                    statusTextView.text = "Scanning file..."
                }

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        // File upload failed due to network or other issues
                        runOnUiThread {
                            statusTextView.text = "Scan failed!"
                            statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.upload_failure))
                        }
                        Log.e("MainActivity", "File Scan failed. Exception: ${e.message}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        // Handle the response
                        if (response.isSuccessful) {
                            runOnUiThread {
                                statusTextView.text = "Scan Completed"
                                statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.upload_success))
                            }
                            // File scan success
                        } else {
                            runOnUiThread {
                                statusTextView.text = "Scan failed"
                                statusTextView.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.upload_failure))
                            }
                            // File scan failed
                        }
                    }
                })
            }



    private fun downloadPdf(hash: String) {
        val url = "http://10.0.2.2:8000/api/v1/download_pdf"

        // Create a request body with the hash parameter
        val requestBody = FormBody.Builder()
            .add("hash", hash)
            .build()

        // Create a request with the URL and request body
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Create a client and execute the request
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()

                    // Process the response body as needed (e.g., save the downloaded file)

                    // Show a toast message indicating successful download
                    runOnUiThread {
                        Toast.makeText(applicationContext, "PDF download successful", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Show a toast message indicating download failure
                    runOnUiThread {
                        Toast.makeText(applicationContext, "PDF download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // Show a toast message indicating network failure
                runOnUiThread {
                    Toast.makeText(applicationContext, "Network error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
    private val PERMISSION_REQUEST_CODE = 123
    // Function to check if file permission is granted
    private fun isFilePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Function to request file permission
    private fun requestFilePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    // Override onRequestPermissionsResult to handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // File permission granted, proceed with file selection logic
            } else {
                // File permission denied, handle the case when permission is not granted
                // You can show a message or take any other action here
            }
        }
        val changeActivityText: TextView = findViewById(R.id.changeActivityText)
        changeActivityText.setOnClickListener {
            // Perform the action to change the activity
            val intent = Intent(this, ApkDownload::class.java)
            startActivity(intent)
        }
    }
}

