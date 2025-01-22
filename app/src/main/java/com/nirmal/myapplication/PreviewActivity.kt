package com.nirmal.myapplication

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.canhub.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class PreviewActivity : AppCompatActivity() {

    private lateinit var cropImageView: CropImageView
    private lateinit var cropButton: Button
    private lateinit var saveButton: Button
    private var croppedBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        // Enable the Up button (back arrow) in the Action Bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.ic_menu_revert)  // Set back arrow icon

        // Initialize views
        cropImageView = findViewById(R.id.cropImageView)
        cropButton = findViewById(R.id.cropButton)
        saveButton = findViewById(R.id.saveButton)

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
                val savedUri = saveCroppedImage(croppedBitmap!!)
                if (savedUri != null) {
                    Toast.makeText(this, "Cropped image saved at: $savedUri", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save cropped image", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please crop the image first before saving", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Handle the back arrow click
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()  // This will call the onBackPressed method to navigate back to MainActivity
        return true
    }

    private fun saveCroppedImage(bitmap: Bitmap): Uri? {
        // Get the output directory
        val outputDir = getExternalFilesDir(null) // Use external files directory
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
}
