package com.example.finallesson;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TambahBarang extends AppCompatActivity {

    private static final int REQUEST_IMAGE = 1;
    private ImageView itemImageView;
    private FloatingActionButton fab, sendfab;
    private Uri imageUri;
    private TextInputEditText etId, etName, etCategory, etCondition, etQuantity, etDateAdded;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tambah_barang);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etId = findViewById(R.id.etId);
        etName = findViewById(R.id.etName);
        etCategory = findViewById(R.id.etCategory);
        etCondition = findViewById(R.id.etCondition);
        etQuantity = findViewById(R.id.etQuantity);
        etDateAdded = findViewById(R.id.etDateAdded);
        itemImageView = findViewById(R.id.itemImageView);
        fab = findViewById(R.id.fab);
        sendfab = findViewById(R.id.sendfab);

        // Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        itemImageView.setOnClickListener(view ->
                showImagePickerDialog());
        fab.setOnClickListener(view ->
                showImagePickerDialog());

        etDateAdded.setOnClickListener(view -> {
            showDatePickerDialog();
        });

        sendfab.setOnClickListener(view -> {
            String id = etId.getText().toString();
            String name = etName.getText().toString();
            String category = etCategory.getText().toString();
            String condition = etCondition.getText().toString();
            String quantity = etQuantity.getText().toString();
            String dateAdded = etDateAdded.getText().toString();

            if (name.isEmpty() || category.isEmpty() || condition.isEmpty() || quantity.isEmpty() || dateAdded.isEmpty()) {
                Toast.makeText(TambahBarang.this, "Tolong isi semua data", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                addItemToFirestore(id, name, category, condition, quantity, dateAdded, user.getUid());
            } else {
                Toast.makeText(TambahBarang.this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String dateString = year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                    etDateAdded.setText(dateString);
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void showImagePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Cara Ambil Gambar")
                .setItems(new String[]{"Kamera", "Galeri"}, (dialog, which) -> {
                    if (which == 0) {
                        dispatchTakePictureIntent();
                    } else {
                        openGallery();
                    }
                })
                .show();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = createImageFile();
                imageUri = FileProvider.getUriForFile(this, "com.example.finallesson.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE);
            } catch (IOException ex) {
                Log.e("TambahBarang", "Error creating image file", ex);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void openGallery() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, REQUEST_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE) {
            if (data != null && data.getData() != null) {
                imageUri = data.getData();
            }
            itemImageView.setImageURI(imageUri);
        }
    }

    private void addItemToFirestore(String id, String name, String category, String condition,
                                    String quantity, String dateAdded, String userId) {
        if (imageUri != null) {
            // Kompres gambar
            byte[] compressedImageData = compressImage(imageUri);

            if (compressedImageData != null) {
                // Unggah gambar ke Firebase Storage
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault());
                Date now = new Date();
                String fileName = formatter.format(now) + ".jpg";
                StorageReference imageRef = storageRef.child("images/" + fileName);
                UploadTask uploadTask = imageRef.putBytes(compressedImageData);

                uploadTask.addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Simpan data barang dan URL gambar di Firestore
                        Map<String, Object> itemData = new HashMap<>();
                        itemData.put("id", id);
                        itemData.put("name", name);
                        itemData.put("category", category);
                        itemData.put("condition", condition);
                        itemData.put("quantity", quantity);
                        itemData.put("dateAdded", dateAdded);
                        itemData.put("imageUrl", imageUrl);

                        db.collection("Users").document(userId).collection("Items")
                                .add(itemData)
                                .addOnSuccessListener(documentReference -> {
                                    Toast.makeText(TambahBarang.this, "Data Barang Berhasil ditambah", Toast.LENGTH_SHORT).show();
                                    imageUri = null;
                                    itemImageView.setImageResource(R.drawable.uploadingimg);
                                    etId.setText("");
                                    etName.setText("");
                                    etCategory.setText("");
                                    etCondition.setText("");
                                    etQuantity.setText("");
                                    etDateAdded.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(TambahBarang.this, "Error adding item", Toast.LENGTH_SHORT).show();
                                });
                    });
                }).addOnFailureListener(e -> {
                    Toast.makeText(TambahBarang.this, "Gagal Upload Gambar", Toast.LENGTH_SHORT).show();
                });
            } else {
                Toast.makeText(TambahBarang.this, "Gagal Kompres Gambar", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Handle jika tidak ada gambar yang dipilih
            Toast.makeText(TambahBarang.this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] compressImage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos); // Adjust compression quality (0-100)
            return baos.toByteArray();
        } catch (IOException e) {
            Log.e("TambahBarang", "Error compressing image", e);
            return null;
        }
    }
}