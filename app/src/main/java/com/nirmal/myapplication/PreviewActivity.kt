package com.nirmal.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.canhub.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject

class PreviewActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private lateinit var cropButton: Button
    private lateinit var saveButton: Button
    private var croppedBitmap: Bitmap? = null

    private lateinit var serverUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        // Load server URL from SharedPreferences
        val sharedPreferences = getSharedPreferences("AppSettings", MODE_PRIVATE)
        serverUrl = sharedPreferences.getString("upload_url", "http://192.168.42.37:5000/upload") ?: "http://192.168.42.37:5000/upload"

        // Initialize views
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        cropImageView = findViewById(R.id.cropImageView)
        cropButton = findViewById(R.id.cropButton)
        saveButton = findViewById(R.id.uploadButton)

        // Set the Toolbar as the ActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true) // Enable back button
            setHomeAsUpIndicator(android.R.drawable.ic_menu_revert) // Optional: Custom back arrow icon
        }

        // Handle back navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the activity and go back
            }
        })

        // Get the image URI passed from MainActivity
        val imageUriString = intent.getStringExtra("image_uri")
        if (!imageUriString.isNullOrEmpty()) {
            val imageUri = Uri.parse(imageUriString)
            cropImageView.setImageUriAsync(imageUri) // Load the image into CropImageView
        } else {
            Toast.makeText(this, "No image to display", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Set up the crop button
        cropButton.setOnClickListener {
            croppedBitmap = cropImageView.croppedImage // Get the cropped image
            if (croppedBitmap != null) {
                cropImageView.setImageBitmap(croppedBitmap) // Update the view with the cropped image
                Toast.makeText(this, "Image cropped successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to crop image", Toast.LENGTH_SHORT).show()
            }
        }

        // Set up the save button
        saveButton.setOnClickListener {
            if (croppedBitmap != null) {
                // Save the cropped image and get its URI
                val imageUri = saveCroppedImage(croppedBitmap!!)

                if (imageUri != null) {
                    // Get the absolute file path of the saved image
                    val imagePath = imageUri.path

                    if (imagePath != null) {
                        // Upload the image to the Flask server
                        uploadImageToServer(imagePath)
                    } else {
                        Toast.makeText(this, "Failed to get image path", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please crop the image first", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Handle the back arrow click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // Navigate back when the back arrow is clicked
        return true
    }

    private fun saveCroppedImage(bitmap: Bitmap): Uri? {
        // Get the output directory
        val outputDir = externalMediaDirs.firstOrNull()?.let {
            // Use the directory named after your app (app_name) under the media storage, then add the "Cropped" folder
            File(it, "${resources.getString(R.string.app_name)}/Cropped").apply { mkdirs() }
        }
        val fileName = "Cropped_${System.currentTimeMillis()}.jpg"
        val file = File(outputDir, fileName)

        return try {
            // Compress the cropped image and save it
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
            Uri.fromFile(file) // Return the file's URI
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadImageToServer(imagePath: String) {
        val client = OkHttpClient()

        val file = File(imagePath)
        val mediaType = "image/jpeg".toMediaTypeOrNull()
        val requestBody = file.asRequestBody(mediaType)

        // Create a multipart request body
        val multipartBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", file.name, requestBody)
            .build()

        val request = Request.Builder()
            .url(serverUrl)
            .post(multipartBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@PreviewActivity,
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: "{}"  // Ensure responseBody is not null

                    val jsonResponse = try {
                        JSONObject(responseBody)
                    } catch (e: Exception) {
                        JSONObject() // Handle invalid JSON gracefully
                    }

                    val predictedLabel = jsonResponse.optString("name", "Unknown label")
                    val characteristics = jsonResponse.optString("characteristics", "Unknown label")
                    val symptoms = jsonResponse.optString("symptoms", "Unknown label")
                    val chemicalTreatment = jsonResponse.optString("chemtreatment", "Unknown label")
                    val remedies = jsonResponse.optString("remedies", "Unknown label")
                    val prevention = jsonResponse.optString("prevention", "Unknown label")
                    val filePath = jsonResponse.optString("file_path", "")

                    // Pass data to ResultActivity
                    val intent = Intent(this@PreviewActivity, ResultActivity::class.java).apply {
                        putExtra("cropped_image_path", imagePath) // Use local image path
                        putExtra("predicted_label", predictedLabel)
                        putExtra("characteristics", characteristics)
                        putExtra("symptoms", symptoms)
                        putExtra("chemical_treatment", chemicalTreatment)
                        putExtra("remedies", remedies)
                        putExtra("prevention", prevention)
                    }

                    startActivity(intent)
                }
                else {
                    runOnUiThread {
                        Toast.makeText(
                            this@PreviewActivity,
                            "Upload failed: ${response.code}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }


}
