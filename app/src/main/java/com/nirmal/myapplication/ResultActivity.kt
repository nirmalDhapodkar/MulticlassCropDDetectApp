package com.nirmal.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import java.io.File
import android.net.Uri

class ResultActivity : AppCompatActivity() {

    private lateinit var resultImageView: ImageView
    private lateinit var resultLabel: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        resultImageView = findViewById(R.id.resultImageView)
        resultLabel = findViewById(R.id.resultLabel)

        val croppedImagePath = intent.getStringExtra("cropped_image_path")
        val predictedLabel = intent.getStringExtra("predicted_label")

        Log.d("ResultActivity", "Received Image URL: $croppedImagePath")
        Log.d("ResultActivity", "Received Predicted Label: $predictedLabel")

        if (croppedImagePath != null && predictedLabel != null) {
            // Use Glide to load the image from the URL
            Glide.with(this)
                .load(croppedImagePath) // URL to the image served by Flask
                .into(resultImageView)

            // Display the predicted label
            resultLabel.text = "Prediction: $predictedLabel"
        } else {
            Toast.makeText(this, "Error displaying results", Toast.LENGTH_SHORT).show()
        }
    }
}
