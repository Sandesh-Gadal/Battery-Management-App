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
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView; // Updated import
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        Button scanButton = findViewById(R.id.scanButton);

        imageRecycler = findViewById(R.id.imageContainer);
        imageAdapter = new ImageAdapter(this, imagePaths);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        imageRecycler.setLayoutManager(layoutManager);
        imageRecycler.setAdapter(imageAdapter);


        Button addImageBtn = findViewById(R.id.addImageButton);
        addImageBtn.setOnClickListener(v -> openCamera());

        // Set current date
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        comingDate.setText(sdf.format(new Date()));

        submitButton.setOnClickListener(v -> {
            if (validateInputs()) {
                Customer customer = new Customer(
                        customerName.getText().toString(),
                        companyName.getText().toString(),
                        vehicleNo.getText().toString(),
                        batteryModel.getText().toString(),
                        Integer.parseInt(batteryQuantity.getText().toString()),
                        comingDate.getText().toString(),
                        null
                );

                long insertedId = dbHelper.addCustomer(customer);

                if (insertedId != -1) {
                    currentCustomerId = (int) insertedId; // ✅ Set the currentCustomerId here

                    // Save images to DB now
                    for (String path : imagePaths) {
                        dbHelper.addCustomerImage(currentCustomerId, path);
                    }
                    logCustomerWithImages(customer);
                    Intent intent = new Intent(MainActivity.this, QRActivity.class);
                    intent.putExtra("customer_id", insertedId);
                    intent.putExtra("unique_code", customer.getUniqueCode());

                    startActivity(intent);
                    clearInputs();
                    imagePaths.clear(); // Clear temp images for next customer
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

        scanButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ScanActivity.class));
        });
        Button viewCustomersButton = findViewById(R.id.viewCustomersButton);
        viewCustomersButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, CustomerListActivity.class));
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
        if (customerName.getText().toString().isEmpty() ||
                companyName.getText().toString().isEmpty() ||
                vehicleNo.getText().toString().isEmpty() ||
                batteryModel.getText().toString().isEmpty() ||
                batteryQuantity.getText().toString().isEmpty() ||
                comingDate.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
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