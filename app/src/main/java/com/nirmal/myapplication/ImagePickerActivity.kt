package com.nirmal.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException
import okhttp3.MultipartBody
import android.database.Cursor
import android.provider.OpenableColumns
import android.util.Log
import org.json.JSONObject

class ImagePickerActivity : AppCompatActivity() {
    private var imageView: ImageView? = null
    private var changeImageButton: Button? = null
    private var uploadButton: Button? = null
    private var selectedImageUri: Uri? = null
    private lateinit var serverUrl: String

    private val imagePickerLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            Glide.with(this).load(selectedImageUri).into(imageView!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        // Load server URL from SharedPreferences
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        serverUrl = sharedPreferences.getString("upload_url", "http://192.168.42.37:5000/upload") ?: "http://192.168.42.37:5000/upload"
        Log.d("ServerURL", serverUrl)


        imageView = findViewById(R.id.imagePreview)
        changeImageButton = findViewById<Button>(R.id.changeImageButton)
        uploadButton = findViewById(R.id.uploadImageButton)

        selectedImageUri = Uri.parse(intent.getStringExtra("selected_image_uri"))
        selectedImageUri?.let { uri ->
            imageView?.let { Glide.with(this).load(uri).into(it) }
        }


        changeImageButton?.setOnClickListener { openImagePicker() }
        uploadButton?.setOnClickListener { uploadImage() }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.setType("image/*")
        imagePickerLauncher.launch(intent)
    }

    private fun uploadImage() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
            return
        }

        val imageFile = getFileFromUri(selectedImageUri!!) ?: run {
            Toast.makeText(this, "Failed to get file", Toast.LENGTH_SHORT).show()
            return
        }

        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestBody = imageFile.asRequestBody(mediaType)

        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, requestBody)
            .build()

        val client = OkHttpClient()
        val request = Request.Builder()
            .url(serverUrl)
            .post(multipartBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@ImagePickerActivity,
                        "Upload failed: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"
                    Log.d("ImagePickerActivity", "Server Response: $responseBody")

                    val jsonResponse = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        Log.e("ImagePickerActivity", "Invalid JSON", e)
                        JSONObject()
                    }

                    val predictedLabel = jsonResponse.optString("name", "Unknown label")
                    val characteristics = jsonResponse.optString("characteristics", "Unknown")
                    val symptoms = jsonResponse.optString("symptoms", "Unknown")
                    val chemicalTreatment = jsonResponse.optString("chemtreatment", "Unknown")
                    val remedies = jsonResponse.optString("remedies", "Unknown")
                    val prevention = jsonResponse.optString("prevention", "Unknown")

                    val intent = Intent(this@ImagePickerActivity, ResultActivity::class.java).apply {
                        putExtra("cropped_image_path", selectedImageUri.toString()) // ✅ Use the same uploaded image
                        putExtra("predicted_label", predictedLabel)
                        putExtra("characteristics", characteristics)
                        putExtra("symptoms", symptoms)
                        putExtra("chemical_treatment", chemicalTreatment)
                        putExtra("remedies", remedies)
                        putExtra("prevention", prevention)
                    }

                    startActivity(intent)
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this@ImagePickerActivity,
                            "Upload failed: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }



    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = contentResolver
        val fileName = getFileName(uri)

        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val file = File(cacheDir, fileName)

        inputStream.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return file
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_file"
        val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

}