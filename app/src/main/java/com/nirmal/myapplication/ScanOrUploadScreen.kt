package com.nirmal.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.io.File
import java.io.FileOutputStream

class ScanOrUploadScreen : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                selectedImageUri = result.data!!.data
                selectedImageUri?.let {
                    val intent = Intent(this, ImagePickerActivity::class.java)
                    intent.putExtra("selected_image_uri", it.toString())
                    startActivity(intent)
                } ?: run {
                    Toast.makeText(this, "Failed to get image", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_or_upload_screen)

        val scanCard: CardView = findViewById(R.id.scanCard)
        val uploadCard: CardView = findViewById(R.id.uploadCard)
        val logoCard: CardView = findViewById(R.id.logoCard)

        scanCard.setOnClickListener { goToNextActivity() }
        uploadCard.setOnClickListener { openImagePicker() }
        logoCard.setOnClickListener{goToSettingsActivity()}
    }

    private fun goToNextActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun goToSettingsActivity() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }
}
