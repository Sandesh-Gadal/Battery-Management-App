package com.example.batterymanagementapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.io.InputStream;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    private DatabaseHelper dbHelper;
    private TextView customerInfo;
    private Button takeAwayButton,galleryButton;
    private DecoratedBarcodeView barcodeScanner;

    private RelativeLayout headerText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        dbHelper = new DatabaseHelper(this);
        customerInfo = findViewById(R.id.customerInfo);
        takeAwayButton = findViewById(R.id.takeAwayButton);
        galleryButton = findViewById(R.id.galleryButton);
        barcodeScanner = findViewById(R.id.barcode_scanner);
        headerText = findViewById(R.id.headerText);

        // Start continuous scanning
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null) {
                    handleScannedResult(result.getText().trim());
                }
            }
            @Override
            public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {}
        });

        ImageButton closeBtn = findViewById(R.id.btnBack);

        closeBtn.setOnClickListener(v->{
            Intent intent = new Intent(this , MainActivity.class);
            startActivity(intent);
            finish();
        });

        galleryButton.setOnClickListener(v -> openGallery());

        // Camera permission check
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            barcodeScanner.resume();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


//    private void startQRScanner() {
//        IntentIntegrator integrator = new IntentIntegrator(this);
//        integrator.setCaptureActivity(PortraitCaptureActivity.class); // use custom portrait activity
//        integrator.setOrientationLocked(true); // lock to portrait
//        integrator.setPrompt("Scan customer QR code");
//        integrator.setBeepEnabled(true);
//
//        // Optional: small scanning rectangle
//        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
//        integrator.setCameraId(0); // rear camera
//        integrator.setBeepEnabled(true);
//        integrator.setBarcodeImageEnabled(true);
//
//        integrator.initiateScan();
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == CAMERA_PERMISSION_REQUEST) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startQRScanner();
//            } else {
//                customerInfo.setText("Camera permission denied");
//                takeAwayButton.setEnabled(false);
//                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//
//        if (result != null && result.getContents() != null) {
//            handleScannedResult(result.getContents().trim());
//        } else {
//            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
//            finish();
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                String scannedCode = scanQRCodeFromBitmap(bitmap);
                if (scannedCode != null) {
                    handleScannedResult(scannedCode);
                } else {
                    Toast.makeText(this, "No QR code found in image", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String scanQRCodeFromBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            Result result = new MultiFormatReader().decode(binaryBitmap);
            return result.getText();
        } catch (NotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                barcodeScanner.resume();
            } else {
                customerInfo.setText("Camera permission denied");
                takeAwayButton.setEnabled(false);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    private void handleScannedResult(String scannedCode) {
        if (!scannedCode.matches("[A-Z0-9]+")) {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            return;
        }

        Customer customer = dbHelper.getCustomerById(scannedCode);
        if (customer != null) {
            // Hide scanner + gallery when customer found
            barcodeScanner.setVisibility(View.GONE);
            galleryButton.setVisibility(View.GONE);

            // Show info + Take Away
            headerText.setVisibility(View.VISIBLE);
            customerInfo.setVisibility(View.VISIBLE);
            takeAwayButton.setVisibility(View.VISIBLE);

            String info = "Name: " + customer.getCustomerName() + "\n" +
                    "Company: " + customer.getCompanyName() + "\n" +
                    "Ampere: " + customer.getVehicleNo() + "\n" +
                    "Contact No: " + customer.getBatteryModel() + "\n" +
                    "Quantity: " + customer.getBatteryQuantity() + "\n" +
                    "Token: " + customer.getUniqueCode() + "\n" +
                    "Coming Date: " + customer.getComingDate();

            customerInfo.setText(info);

            if (customer.getOutgoingDate() != null) {
                customerInfo.append("\nOutgoing Date: " + customer.getOutgoingDate());
                takeAwayButton.setEnabled(false);
            } else {
                takeAwayButton.setEnabled(true);
                takeAwayButton.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Confirm Action")
                            .setMessage("Are you sure you want to record the outgoing date?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                String outgoingDate = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm", Locale.getDefault()).format(new Date());
                                if (dbHelper.updateOutgoingDate(scannedCode, outgoingDate)) {
                                    customerInfo.append("\nOutgoing Date: " + outgoingDate);
                                    takeAwayButton.setEnabled(false);
                                    Toast.makeText(this, "Outgoing date recorded", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(this , MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                            .show();
                });
            }
        } else {
            Toast.makeText(this, "Customer not found (unique ID: " + scannedCode + ")", Toast.LENGTH_SHORT).show();
            customerInfo.setVisibility(View.GONE);
            takeAwayButton.setVisibility(View.GONE);
        }
    }

}
