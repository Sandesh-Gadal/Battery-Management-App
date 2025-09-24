package com.example.batterymanagementapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Updated import
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText customerName, companyName, vehicleNo, batteryModel, batteryQuantity, comingDate;
    private DatabaseHelper dbHelper;
    private List<Customer> allCustomers; // Master list from DB
    private com.example.batterymanagementapp.CustomerAdapter adapter;
    private SearchView searchView;
    private RecyclerView recyclerView;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private Uri imageUri;
    private int currentCustomerId; // store after insert
    private RecyclerView imageRecycler;
    private ImageAdapter imageAdapter;
    private List<String> imagePaths = new ArrayList<>();

    private File currentPhotoFile;

    private TextView headerText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        customerName = findViewById(R.id.customerName);
        companyName = findViewById(R.id.companyName);
        vehicleNo = findViewById(R.id.vehicleNo);
        batteryModel = findViewById(R.id.batteryModel);
        batteryQuantity = findViewById(R.id.batteryQuantity);
        comingDate = findViewById(R.id.comingDate);
        Button submitButton = findViewById(R.id.submitButton);
//        Button scanButton = findViewById(R.id.action_scan);

        imageRecycler = findViewById(R.id.imageContainer);
        imageAdapter = new ImageAdapter(this, imagePaths);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecycler.setLayoutManager(layoutManager);
        imageRecycler.setAdapter(imageAdapter);


        Button addImageBtn = findViewById(R.id.addImageButton);
        addImageBtn.setOnClickListener(v -> openCamera());

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm a", Locale.getDefault());
        comingDate.setText(sdf.format(new Date()));

        headerText = findViewById(R.id.headerText);
        boolean isEdit = getIntent().getBooleanExtra("isEdit", false);
        int customerId = getIntent().getIntExtra("customerId", -1);
        String ucode = getIntent().getStringExtra("unique_code");

        if (isEdit && customerId != -1) {
            headerText.setText("Edit Details");
            submitButton.setText("Update");

            // Fetch customer from DB
            Customer existingCustomer = dbHelper.getCustomerById(ucode);
            if (existingCustomer != null) {
                customerName.setText(existingCustomer.getCustomerName());
                companyName.setText(existingCustomer.getCompanyName());
                vehicleNo.setText(existingCustomer.getVehicleNo());
                batteryModel.setText(existingCustomer.getBatteryModel());
                batteryQuantity.setText(String.valueOf(existingCustomer.getBatteryQuantity()));
                comingDate.setText(existingCustomer.getComingDate());

                // Load saved images
                List<String> savedImages = dbHelper.getCustomerImages(customerId);
                imagePaths.clear();
                imagePaths.addAll(savedImages);
                imageAdapter.notifyDataSetChanged();

                // store for later update
                currentCustomerId = customerId;
            }
        }

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Customer customer = new Customer(
                        customerName.getText().toString(),
                        companyName.getText().toString(),
                        vehicleNo.getText().toString(),
                        batteryModel.getText().toString(),
                        Integer.parseInt(batteryQuantity.getText().toString()),
                        comingDate.getText().toString(),
                        null,
                        null // uniqueCode will be generated in DB
                );

                Customer insertedCustomer = dbHelper.addCustomer(customer);

                if (insertedCustomer.getId() != -1) {
                    currentCustomerId = insertedCustomer.getId(); // ✅ now correct

                    // Save images
                    for (String path : imagePaths) {
                        dbHelper.addCustomerImage(currentCustomerId, path);
                    }

                    logCustomerWithImages(insertedCustomer);

                    // ✅ Pass both ID & unique code to QRActivity
                    Intent intent = new Intent(MainActivity.this, QRActivity.class);
                    intent.putExtra("customer_id", insertedCustomer.getId());
                    intent.putExtra("unique_code", insertedCustomer.getUniqueCode());
                    startActivity(intent);

                    clearInputs();
                    imagePaths.clear();
                    imageAdapter.notifyDataSetChanged();

                    Toast.makeText(this, "Customer added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add customer", Toast.LENGTH_SHORT).show();
                }
            }
        });


//        searchView = findViewById(R.id.searchView);

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                filterCustomers(newText); // Call external method
//                return true;
//            }
//        });

//        scanButton.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, ScanActivity.class));
//        });
//        Button viewCustomersButton = findViewById(R.id.action_view_customers);
//        viewCustomersButton.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, CustomerListActivity.class));
//        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_scan) {
                startActivity(new Intent(MainActivity.this, ScanActivity.class));
                return true;
            } else if (id == R.id.action_view_customers) {
                startActivity(new Intent(MainActivity.this, CustomerListActivity.class));
                return true;
            }
            return false;
        });


    }

    private void filterCustomers(String text) {
        List<Customer> filteredList = new ArrayList<>();
        for (Customer customer : allCustomers) {
            if (customer.getCustomerName().toLowerCase().contains(text.toLowerCase()) ||
                    customer.getVehicleNo().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(customer);
            }
        }
        adapter.updateList(filteredList);
    }

    private List<Customer> fetchCustomersFromDB() {
        // TODO: Replace with actual DB call
        return new ArrayList<>();
    }

    private void logCustomerWithImages(Customer customer) {
        List<String> images = dbHelper.getCustomerImages(customer.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Customer ID: ").append(customer.getId()).append("\n");
        sb.append("Name: ").append(customer.getCustomerName()).append("\n");
        sb.append("Company: ").append(customer.getCompanyName()).append("\n");
        sb.append("Vehicle No: ").append(customer.getVehicleNo()).append("\n");
        sb.append("Battery Model: ").append(customer.getBatteryModel()).append("\n");
        sb.append("Battery Quantity: ").append(customer.getBatteryQuantity()).append("\n");
        sb.append("Coming Date: ").append(customer.getComingDate()).append("\n");
        sb.append("Outgoing Date: ").append(customer.getOutgoingDate()).append("\n");

        sb.append("Images:\n");
        for (String path : images) {
            sb.append("file://").append(path).append("\n");
        }

        android.util.Log.d("CustomerData", sb.toString());
    }


    private boolean validateInputs() {
        String name = customerName.getText().toString().trim();
        String phone = batteryModel.getText().toString().trim();

        boolean valid = true;

        if (name.isEmpty()) {
            customerName.setError("Please enter customer name");
            valid = false;
        } else {
            customerName.setError(null);
        }

        if (phone.isEmpty()) {
            batteryModel.setError("Please enter phone number");
            valid = false;
        } else if (phone.length() != 10) {
            batteryModel.setError("Phone number must be exactly 10 digits");
            valid = false;
        } else {
            batteryModel.setError(null);
        }

        if (comingDate.getText().toString().trim().isEmpty()) {
            comingDate.setError("Date cannot be empty");
            valid = false;
        } else {
            comingDate.setError(null);
        }

        return valid;
    }



    private int getLastInsertedId() {
        return (int) dbHelper.getWritableDatabase().compileStatement(
                "SELECT last_insert_rowid()").simpleQueryForLong();
    }

    private void clearInputs() {
        customerName.setText("");
        companyName.setText("");
        vehicleNo.setText("");
        batteryModel.setText("");
        batteryQuantity.setText("");
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                currentPhotoFile = createImageFile(); // keep reference here
                if (currentPhotoFile != null) {
                    // Authority must match Manifest <provider>
                    String authority = getPackageName() + ".fileprovider";
                    Uri photoURI = FileProvider.getUriForFile(this, authority, currentPhotoFile);

                    imageUri = photoURI; // Save for later use (preview/upload)

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Creates a real temp file
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (currentPhotoFile != null && currentPhotoFile.exists()) {
                String path = currentPhotoFile.getAbsolutePath();
                imagePaths.add(path);                // ✅ add to preview list
                imageAdapter.notifyDataSetChanged(); // ✅ update RecyclerView

                // DO NOT save to DB here if customerId not yet created
            }
        }
    }


}