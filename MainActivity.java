package com.bluflames.ocrextractor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView imageView;
    private TextView textView;
    private Bitmap selectedImage;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        Button selectImageButton = findViewById(R.id.selectImageButton);
        Button extractTextButton = findViewById(R.id.extractTextButton);

        // Initialize the ActivityResultLauncher for image selection
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                            imageView.setImageBitmap(selectedImage);
                        } catch (IOException e) {
                            Log.e(TAG, "Error loading image: ", e);
                            textView.setText("Failed to load image");
                        }
                    }
                });

        // Set up button click listeners
        selectImageButton.setOnClickListener(v -> openGallery());
        extractTextButton.setOnClickListener(v -> extractTextFromImage());
    }

    // Opens the image picker
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // Extracts text from the selected image using ML Kit
    @SuppressLint("SetTextI18n")
    private void extractTextFromImage() {
        if (selectedImage == null) {
            textView.setText("Please select an image first.");
            return;
        }

        InputImage image = InputImage.fromBitmap(selectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(image)
                .addOnSuccessListener(result -> {
                    String extractedText = result.getText();
                    textView.setText(extractedText.isEmpty() ? "No text found" : extractedText);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Text extraction failed: ", e);
                    textView.setText("Failed to extract text");
                });
    }
}
