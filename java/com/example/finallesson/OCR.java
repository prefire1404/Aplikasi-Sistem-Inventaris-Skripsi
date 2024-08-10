package com.example.finallesson;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class OCR extends AppCompatActivity {

    EditText etOcrResult;
    Button btnScan, btnAddItem, btnFotoAset;
    TextRecognizer textRecognizer;
    ImageView ivSelectedImage;
    Uri selectedImageUri;
    ActivityResultLauncher<Intent> cameraLauncher, imagePickerLauncher;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    public static int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[s1.length()][s2.length()];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocractivity);


        etOcrResult = findViewById(R.id.etOcrResult);
        btnScan = findViewById(R.id.btnScan);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnFotoAset = findViewById(R.id.btnFotoAset);
        ivSelectedImage = findViewById(R.id.ivSelectedImage);

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        initializeLaunchers();
        setupButtonListeners();

    }

    private void initializeLaunchers() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
                            if (imageBitmap != null) {
                                processImage(imageBitmap, 0);
                            }
                        }
                    }
                });

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getData() != null) {
                            selectedImageUri = data.getData();
                            ivSelectedImage.setImageURI(selectedImageUri);
                        }
                    }
                });
    }

    private void setupButtonListeners() {
        btnScan.setOnClickListener(view -> {
            launchCameraForScanning();
        });

        btnAddItem.setOnClickListener(view -> {
            String extractedText = etOcrResult.getText().toString();
            String id = extractField(extractedText, "ID:");
            String nama = extractField(extractedText, "Nama:");
            String kategori = extractField(extractedText, "Kategori:");
            String kondisi = extractField(extractedText, "Kondisi:");
            String jumlah = extractField(extractedText, "Jumlah:");
            String dateAdded = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());


            if (id.isEmpty() || nama.isEmpty() || kategori.isEmpty() || kondisi.isEmpty() || jumlah.isEmpty()) {
                Toast.makeText(OCR.this, "Please ensure all required fields are extracted", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {

                GroundTruthItem groundTruthItem = readGroundTruthFromFile("ground_truth.txt", 0);
                if (groundTruthItem != null) {
                    String Id = extractField(extractedText, "ID:");
                    String Nama = extractField(extractedText, "Nama:");
                    String Kategori = extractField(extractedText, "Kategori:");
                    String Kondisi = extractField(extractedText, "Kondisi:");
                    String Jumlah = extractField(extractedText, "Jumlah:");

                    int idDistance = levenshteinDistance(groundTruthItem.id, Id);
                    int namaDistance = levenshteinDistance(groundTruthItem.nama, Nama);
                    int kategoriDistance = levenshteinDistance(groundTruthItem.kategori, Kategori);
                    int kondisiDistance = levenshteinDistance(groundTruthItem.kondisi, Kondisi);
                    int jumlahDistance = levenshteinDistance(groundTruthItem.jumlah, Jumlah);

                    double overallAccuracy = calculateOverallAccuracy(idDistance, namaDistance, kategoriDistance, kondisiDistance, jumlahDistance);

                    storeOcrAccuracy(overallAccuracy, user.getUid());
                }
                addItemToFirestore(id, nama, kategori, kondisi, jumlah, dateAdded, user.getUid(), selectedImageUri);
            } else {
                Toast.makeText(OCR.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        btnFotoAset.setOnClickListener(view -> showImagePickerDialog());
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Gambar")
                .setItems(new String[]{"Kamera", "Galeri"}, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void addItemToFirestore(String itemId, String itemName, String itemCategory, String itemCondition,
                                    String itemQuantity, String dateAdded, String userId, Uri imageUri) {
        if (imageUri != null) {
            byte[] compressedImage = compressImage(imageUri);
            if (compressedImage != null) {
                StorageReference storageRef = storage.getReference().child("image/" + UUID.randomUUID().toString());
                storageRef.putBytes(compressedImage)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            saveItemDataToFirestore(itemId, itemName, itemCategory, itemCondition, itemQuantity, dateAdded, userId, imageUrl);
                        }))
                        .addOnFailureListener(e -> Toast.makeText(OCR.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(OCR.this, "Failed to compress image", Toast.LENGTH_SHORT).show();
            }
        } else {
            saveItemDataToFirestore(itemId, itemName, itemCategory, itemCondition, itemQuantity, dateAdded, userId, null);
        }
    }

    private void saveItemDataToFirestore(String itemId, String itemName, String itemCategory, String itemCondition,
                                         String itemQuantity, String dateAdded, String userId, String imageUrl) {
        Map<String, Object> itemData = new HashMap<>();
        itemData.put("id", itemId);
        itemData.put("name", itemName);
        itemData.put("category", itemCategory);
        itemData.put("condition", itemCondition);
        itemData.put("quantity", itemQuantity);
        itemData.put("dateAdded", dateAdded);
        if (imageUrl != null) {
            itemData.put("imageUrl", imageUrl);
        }

        db.collection("Users").document(userId).collection("Items")
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(OCR.this, "Data dari OCR berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                    etOcrResult.setText("");
                    ivSelectedImage.setImageResource(R.drawable.ic_add_photo); // Ganti dengan default image Anda
                    selectedImageUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OCR.this, "Failed to add item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private byte[] compressImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Adjust compression quality (0-100)
            return baos.toByteArray();
        } catch (IOException e) {
            Log.e("UploadGambar", "Error compressing image", e);
            return null;
        }
    }

    private void launchCameraForScanning() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private void processImage(Bitmap imageBitmap, int imageIndex) {
        InputImage image = InputImage.fromBitmap(imageBitmap, 0);
        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String extractedText = visionText.getText();
                    if (extractedText.isEmpty()) {
                        etOcrResult.setText("No text detected");
                        return;
                    }

                    String formattedResult = formatExtractedText(extractedText);
                    etOcrResult.setText(formattedResult);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OCR.this, "Failed to extract text: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String extractField(String text, String keyword) {
        int startIndex = text.toLowerCase().indexOf(keyword.toLowerCase()); // Case-insensitive search
        if (startIndex != -1) {
            startIndex += keyword.length();
            int endIndex = text.indexOf("\n", startIndex);
            if (endIndex == -1) {
                endIndex = text.length();
            }
            String extractedValue = text.substring(startIndex, endIndex).trim();
            return extractedValue.replace(":", "").trim(); // Combined cleanup
        }
        return "";
    }

    private GroundTruthItem readGroundTruthFromFile(String filename, int index) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int currentLine = 0;
            while ((line = reader.readLine()) != null) {
                if (currentLine == index) {
                    String[] fields = line.split(","); // Assuming fields are comma-separated
                    if (fields.length == 5) {
                        return new GroundTruthItem(fields[0], fields[1], fields[2], fields[3], fields[4]);
                    } else {

                        return null;
                    }
                }
                currentLine++;
            }
        } catch (IOException e) {

            return null;
        }

        return null;
    }

    private double calculateOverallAccuracy(int idDistance, int namaDistance, int kategoriDistance, int kondisiDistance, int jumlahDistance) {
        double totalDistance = idDistance + namaDistance + kategoriDistance + kondisiDistance + jumlahDistance;
        double averageDistance = totalDistance / 5.0;
        return 1 - (averageDistance / 5.0);
    }

    private String formatExtractedText(String extractedText) {
        String id = extractField(extractedText, "ID:");
        String nama = extractField(extractedText, "Nama:");
        String kategori = extractField(extractedText, "Kategori:");
        String kondisi = extractField(extractedText, "Kondisi:");
        String jumlah = extractField(extractedText, "Jumlah:");
        return "ID: " + id + "\nNama: " + nama + "\nKategori: " + kategori + "\nKondisi: " + kondisi + "\nJumlah: " + jumlah;
    }

    private void storeOcrAccuracy(double overallAccuracy, String userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("scan_accuracy", overallAccuracy); // Changed key to "scan_accuracy"
        db.collection("Users").document(userId).collection("OCR")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "OCR Accuracy stored", Toast.LENGTH_SHORT).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error storing OCR accuracy", Toast.LENGTH_SHORT).show();
                });
    }
}
