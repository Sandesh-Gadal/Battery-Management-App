package com.example.batterymanagementapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.BinderThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;



import java.util.List;

public class CustomerDetailsActivity extends AppCompatActivity {

    private TextView customerInfo;
    private RecyclerView recyclerViewImages;
    private DatabaseHelper dbHelper;
    private String ucode;



    private ImageButton closeBtn;
    private TextView tvName, tvCompany, tvVehicle, tvBattery, tvQuantity, tvComingDate, tvOutgoingDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_details);

        ucode =getIntent().getStringExtra("customerId");
//        Log.d("id",""+ucode);
        tvName = findViewById(R.id.tvName);
        tvCompany = findViewById(R.id.tvCompany);
        tvVehicle = findViewById(R.id.tvVehicle);
        tvBattery = findViewById(R.id.tvBattery);
        tvQuantity = findViewById(R.id.tvQuantity);
        tvComingDate = findViewById(R.id.tvComingDate);
        tvOutgoingDate = findViewById(R.id.tvOutgoingDate);

        closeBtn =findViewById(R.id.btnBack);

        closeBtn.setOnClickListener(v->{
            Intent intent = new Intent(this , CustomerListActivity.class);
            startActivity(intent);
            finish();
        });

        MaterialButton btnCall = findViewById(R.id.btnCall);
        btnCall.setOnClickListener(v -> {
            String phone = tvBattery.getText().toString().trim(); // or use correct field
            Log.d("phonenumber:", phone);

            if (!phone.isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + phone));
                if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    // Request permission at runtime
                    requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1);
                }
            }
        });


        recyclerViewImages = findViewById(R.id.recyclerViewImages);
        recyclerViewImages.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        dbHelper = new DatabaseHelper(this);

        loadCustomerDetails(ucode);
    }

    private void loadCustomerDetails(String ucode) {
//        Log.d("id","lcd"+ucode);

        Customer customer = dbHelper.getCustomerById(ucode);
        if (customer != null) {
            tvName.setText(customer.getCustomerName());
            tvCompany.setText(customer.getCompanyName());
            tvVehicle.setText(customer.getVehicleNo());
            tvBattery.setText(customer.getBatteryModel());
            tvQuantity.setText(String.valueOf(customer.getBatteryQuantity()));
            tvComingDate.setText(customer.getComingDate());
            tvOutgoingDate.setText(customer.getOutgoingDate() != null ? customer.getOutgoingDate() : "-");

            List<String> images = dbHelper.getCustomerImages(customer.getId());
//            Log.d("image", String.valueOf(images));
//            Log.d("image","sss"+tvBattery);
            ImageAdapter imageAdapter = new ImageAdapter(this, images);
            recyclerViewImages.setAdapter(imageAdapter);
        }
//        Log.d("id", String.valueOf(customer));
    }


    public void showImagePreview(String path) {
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_preview);

        ImageView fullImage = dialog.findViewById(R.id.fullImage);
        ImageButton btnClose = dialog.findViewById(R.id.btnBack);

        // Load image
        // Load with rotation fix
        Bitmap bitmap = getCorrectlyOrientedBitmap(path);
        fullImage.setImageBitmap(bitmap);

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private Bitmap getCorrectlyOrientedBitmap(String path) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);

        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED
            );

            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    return bitmap;
            }
            return Bitmap.createBitmap(bitmap, 0, 0,
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }


}
