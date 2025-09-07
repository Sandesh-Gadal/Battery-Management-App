package com.example.batterymanagementapp;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ScanActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST = 100;

    private DatabaseHelper dbHelper;
    private TextView customerInfo;
    private Button takeAwayButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        dbHelper = new DatabaseHelper(this);
        customerInfo = findViewById(R.id.customerInfo);
        takeAwayButton = findViewById(R.id.takeAwayButton);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startQRScanner();
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setCaptureActivity(PortraitCaptureActivity.class); // use custom portrait activity
        integrator.setOrientationLocked(true); // lock to portrait
        integrator.setPrompt("Scan customer QR code");
        integrator.setBeepEnabled(true);

        // Optional: small scanning rectangle
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setCameraId(0); // rear camera
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);

        integrator.initiateScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                customerInfo.setText("Camera permission denied");
                takeAwayButton.setEnabled(false);
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null && result.getContents() != null) {
            handleScannedResult(result.getContents().trim());
        } else {
            Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void handleScannedResult(String scannedCode) {
        if (!scannedCode.matches("[A-Z0-9]+")) {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        Customer customer = dbHelper.getCustomerById(scannedCode);
        if (customer != null) {
            String info = "Name: " + customer.getCustomerName() + "\n" +
                    "Company: " + customer.getCompanyName() + "\n" +
                    "Vehicle No: " + customer.getVehicleNo() + "\n" +
                    "Battery Model: " + customer.getBatteryModel() + "\n" +
                    "Quantity: " + customer.getBatteryQuantity() + "\n" +
                    "Coming Date: " + customer.getComingDate();

            customerInfo.setText(info);

            if (customer.getOutgoingDate() != null) {
                customerInfo.append("\nOutgoing Date: " + customer.getOutgoingDate());
                takeAwayButton.setEnabled(false);
            } else {
                takeAwayButton.setEnabled(true);
                takeAwayButton.setOnClickListener(v -> {
                    String outgoingDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    if (dbHelper.updateOutgoingDate(scannedCode, outgoingDate)) {
                        customerInfo.append("\nOutgoing Date: " + outgoingDate);
                        takeAwayButton.setEnabled(false);
                        Toast.makeText(this, "Outgoing date recorded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            customerInfo.setText("Customer not found ( unique ID: " + scannedCode + ")");
            takeAwayButton.setEnabled(false);
        }
    }
}
