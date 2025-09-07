package com.example.batterymanagementapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        TextView tvcode = findViewById(R.id.tvcode);
        ImageView qrImage = findViewById(R.id.qrImage);
        Button backbtn = findViewById(R.id.backButton);
//        long customerId = getIntent().getLongExtra("customer_id", -1L);
        String uniqueCode = getIntent().getStringExtra("unique_code");
        tvcode.setText("Code: "+ uniqueCode);
backbtn.setOnClickListener(v->{
    Intent intent = new Intent (this , MainActivity.class);
    startActivity(intent);
});

        if (uniqueCode == null) {
            finish();
            return;
        }

        try {
            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(String.valueOf(uniqueCode), BarcodeFormat.QR_CODE, 400, 400);
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            qrImage.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
