package com.example.batterymanagementapp;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class CustomerListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private com.example.batterymanagementapp.CustomerAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Customer> customers = dbHelper.getAllCustomers();
        adapter = new com.example.batterymanagementapp.CustomerAdapter(this ,customers , getApplicationContext());
        recyclerView.setAdapter(adapter);
    }

    public void showCustomerImages(int customerId){
        List<String> images = dbHelper.getCustomerImages(customerId);
        if(images.isEmpty()){
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show images in a dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_image_preview);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        ImageAdapter imageAdapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(imageAdapter);

        dialog.show();
    }


}
