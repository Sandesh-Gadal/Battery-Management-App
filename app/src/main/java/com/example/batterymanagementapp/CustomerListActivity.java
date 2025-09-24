package com.example.batterymanagementapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class CustomerListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private DatabaseHelper dbHelper;

    private ChipGroup filterChipGroup;
    private List<Customer> allCustomers;   // master list
    private List<Customer> filteredList;   // list for RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_list);

        dbHelper = new DatabaseHelper(this);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Load customers
        allCustomers = dbHelper.getAllCustomers();
        filteredList = new ArrayList<>(allCustomers);

        adapter = new CustomerAdapter(this, filteredList, getApplicationContext());
        recyclerView.setAdapter(adapter);

        ImageButton closeBtn =findViewById(R.id.btnBack);

        closeBtn.setOnClickListener(v->{
            Intent intent = new Intent(this , MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Setup filter chips
        filterChipGroup = findViewById(R.id.filterChipGroup);
        setupChipFilters();
    }

    /**
     * Setup ChipGroup filters
     */
    private void setupChipFilters() {
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            filteredList.clear();

            if (checkedId == R.id.chipAll) {
                filteredList.addAll(allCustomers);
            } else {
                for (Customer c : allCustomers) {
                    if (checkedId == R.id.chipIncoming) {
                        if (c.getOutgoingDate() == null) filteredList.add(c);
                    } else if (checkedId == R.id.chipOutgoing) {
                        if (c.getOutgoingDate() != null) filteredList.add(c);
                    } else if (checkedId == R.id.chipActive) {
                        if (c.isActive()) filteredList.add(c);
                    } else if (checkedId == R.id.chipToday) {
                        if (c.getComingDate().equals(Utils.getTodayDate())) filteredList.add(c);
                    }
                }
            }

            adapter.notifyDataSetChanged();
        });

    }

    /**
     * Show customer images inside a fullscreen dialog
     */
    public void showCustomerImages(int ucode) {
        List<String> images = dbHelper.getCustomerImages(ucode);

        if (images.isEmpty()) {
            Toast.makeText(this, "No images found", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_image_preview);

        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerViewImages);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        ImageAdapter imageAdapter = new ImageAdapter(this, images);
        recyclerView.setAdapter(imageAdapter);

        // Optional: dim background
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        }

        dialog.show();
    }
}
