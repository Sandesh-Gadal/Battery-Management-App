package com.example.batterymanagementapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;


import com.google.android.material.chip.ChipGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private DatabaseHelper dbHelper;

    private ChipGroup filterChipGroup;
    private List<Customer> allCustomers;   // master list
    private List<Customer> filteredList;   // list for RecyclerView
    private  ImageButton btnCalendar;



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

        btnCalendar = findViewById(R.id.btnCalendar);


        ImageButton closeBtn =findViewById(R.id.btnBack);

        SearchView searchView = findViewById(R.id.searchView);

        // Expand so tapping anywhere focuses
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search by name, company, or code");

        // Get the internal text field safely
        int searchTextId = androidx.appcompat.R.id.search_src_text;
        TextView searchText = searchView.findViewById(searchTextId);

        if (searchText != null) {
            searchText.setHintTextColor(Color.BLACK);  // Hint in black
            searchText.setTextColor(Color.BLACK);      // Input text in black
        } else {
            Log.e("SearchView", "search_src_text not found!");
        }

        // Make sure tapping anywhere opens input
        searchView.setOnClickListener(v -> searchView.setIconified(false));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadSearchResults(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadSearchResults(newText);
                return true;
            }
        });

        closeBtn.setOnClickListener(v->{
            Intent intent = new Intent(this , MainActivity.class);
            startActivity(intent);
            finish();
        });

        btnCalendar.setOnClickListener(v -> {
            // Get current date
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format selected date (yyyy-MM-dd)
                        String selectedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", selectedYear, (selectedMonth + 1), selectedDay);

                        Toast.makeText(this, "Selected: " + selectedDate, Toast.LENGTH_SHORT).show();
                        Log.d("date","this is selected date: "+selectedDate);

                        // 👉 Call your filter method here
                        filterListByDate(selectedDate);
                    },
                    year, month, day);

            datePicker.show();
        });
        // Setup filter chips
        filterChipGroup = findViewById(R.id.filterChipGroup);
        setupChipFilters();
    }

//    private void filterListByDate(String selectedDate) {
//        Log.d("date","this is seelcted date inside the filet fn: "+selectedDate);
//
//        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm a", Locale.getDefault());
//        List<Customer> filteredList = new ArrayList<>();
//
//
//        for (Customer c : allCustomers) {
//
//            try {
//                Date customerDate = sdf.parse(c.getComingDate()); // parse the stored date
//                Date targetDate = sdf.parse(selectedDate);
//
//                Log.d("date","this is coming date: "+customerDate);
//                Log.d("date","this is the selected Date: "+targetDate);
//
//
//                // compare only day, month, year
//                if (customerDate != null && targetDate != null) {
//                    if (customerDate.getYear() == targetDate.getYear() &&
//                            customerDate.getMonth() == targetDate.getMonth() &&
//                            customerDate.getDate() == targetDate.getDate()) {
//                        filteredList.add(c);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//        adapter.updateList(filteredList);
//    }

    private void filterListByDate(String selectedDateStr) {
        Log.d("date", "Selected date: " + selectedDateStr);

        Date selectedDate = parseCustomerDate(selectedDateStr);
        if (selectedDate == null) {
            Log.e("date", "Cannot parse selected date!");
            return;
        }

        List<Customer> filteredList = new ArrayList<>();
        for (Customer c : allCustomers) {
            Date customerDate = parseCustomerDate(c.getComingDate());
            if (customerDate == null) continue;

            if (customerDate.getYear() == selectedDate.getYear() &&
                    customerDate.getMonth() == selectedDate.getMonth() &&
                    customerDate.getDate() == selectedDate.getDate()) {
                filteredList.add(c);
            }
        }

        Log.d("date", "Filtered list size: " + filteredList.size());
        adapter.updateList(filteredList);
    }


    private Date parseCustomerDate(String dateStr) {
        String[] formats = {
                "EEE, dd MMM yyyy HH:mm a",
                "yyyy-MM-dd",
                "EEE, dd MMM yyyy HH:mm"  // fallback if no AM/PM
        };

        for (String fmt : formats) {
            try {
                return new SimpleDateFormat(fmt, Locale.getDefault()).parse(dateStr);
            } catch (ParseException ignored) {
            }
        }
        Log.e("date", "Failed to parse date: " + dateStr);
        return null;
    }




    private void loadSearchResults(String keyword) {
        filteredList.clear();

        if (keyword == null || keyword.trim().isEmpty()) {
            // Show all if search is empty
            filteredList.addAll(allCustomers);
        } else {
            for (Customer c : allCustomers) {
                if (c.getCustomerName().toLowerCase().contains(keyword.toLowerCase()) ||
                        c.getCompanyName().toLowerCase().contains(keyword.toLowerCase()) ||
                        String.valueOf(c.getUniqueCode()).contains(keyword)) {
                    filteredList.add(c);
                }
            }
        }

        adapter.notifyDataSetChanged();
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload list from DB
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            List<Customer> updatedList = dbHelper.getAllCustomers();
            Log.d("aa","this is the result list");
            adapter.updateList(updatedList);
        }
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
