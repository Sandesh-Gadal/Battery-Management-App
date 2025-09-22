package com.example.batterymanagementapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BatteryDB.db";
    private static final int DATABASE_VERSION = 4;
    private static final String TABLE_NAME = "customers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_CUSTOMER_NAME = "customer_name";
    private static final String COLUMN_COMPANY_NAME = "company_name";
    private static final String COLUMN_VEHICLE_NO = "vehicle_no";
    private static final String COLUMN_BATTERY_MODEL = "battery_model";
    private static final String COLUMN_BATTERY_QUANTITY = "battery_quantity";
    private static final String COLUMN_COMING_DATE = "coming_date";
    private static final String COLUMN_OUTGOING_DATE = "outgoing_date";
    private static final String COLUMN_UNIQUE_CODE = "unique_code"; // ✅ new


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CUSTOMER_NAME + " TEXT, " +
                COLUMN_COMPANY_NAME + " TEXT, " +
                COLUMN_VEHICLE_NO + " TEXT, " +
                COLUMN_BATTERY_MODEL + " TEXT, " +
                COLUMN_BATTERY_QUANTITY + " INTEGER, " +
                COLUMN_COMING_DATE + " TEXT, " +
                COLUMN_OUTGOING_DATE + " TEXT, " +
                COLUMN_UNIQUE_CODE + " TEXT)";
        db.execSQL(createTable);

        // ✅ Create image table
        db.execSQL("CREATE TABLE customer_images (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customer_id INTEGER, " +
                "image_path TEXT, " +
                "FOREIGN KEY(customer_id) REFERENCES " + TABLE_NAME + "(id))");
    }


    // ✅ Generate random alphanumeric code
    private String generateUniqueCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
    // Insert image path
    public void addCustomerImage(int customerId, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("customer_id", customerId);
        values.put("image_path", imagePath);
        db.insert("customer_images", null, values);
        db.close();
    }

    // Fetch all images of a customer
    public List<String> getCustomerImages(int customerId) {
        List<String> images = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT image_path FROM customer_images WHERE customer_id=?",
                new String[]{String.valueOf(customerId)});
        if (cursor.moveToFirst()) {
            do {
                images.add(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return images;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS customer_images");
        db.execSQL("DROP TABLE IF EXISTS customers");
        onCreate(db);
    }

    public long addCustomer(Customer customer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CUSTOMER_NAME, customer.getCustomerName());
        values.put(COLUMN_COMPANY_NAME, customer.getCompanyName());
        values.put(COLUMN_VEHICLE_NO, customer.getVehicleNo());
        values.put(COLUMN_BATTERY_MODEL, customer.getBatteryModel());
        values.put(COLUMN_BATTERY_QUANTITY, customer.getBatteryQuantity());
        values.put(COLUMN_COMING_DATE, customer.getComingDate());

        // generate and save unique code
        String uniqueCode = generateUniqueCode(6);
        values.put(COLUMN_UNIQUE_CODE, uniqueCode);
        long insertedId = db.insert(TABLE_NAME, null, values);
        customer.setId((int) insertedId);  // ✅ Set the ID back
        customer.setUniqueCode(uniqueCode); // keep in object
        db.close();
        return insertedId; // Return the generated row ID (used in QR)
    }


    public Customer getCustomerById(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d("id","dbh "+id);

        Cursor cursor = db.query(TABLE_NAME,
                null,
                COLUMN_ID + "=?",
                new String[]{id},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Customer customer = new Customer(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_MODEL)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_QUANTITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMING_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTGOING_DATE))

            );
            customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            cursor.close();
            return customer;
        }

        if (cursor != null) cursor.close();
        return null;
    }


    public boolean updateOutgoingDate(String id, String outgoingDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_OUTGOING_DATE, outgoingDate);

        int result = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{id});
        db.close();
        return result > 0;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_MODEL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMING_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTGOING_DATE))
                );
                customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                list.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME +
                        " WHERE " + COLUMN_CUSTOMER_NAME + " LIKE ? OR " + COLUMN_ID + " LIKE ?",
                new String[]{"%" + keyword + "%", "%" + keyword + "%"});

        if (cursor.moveToFirst()) {
            do {
                Customer customer = new Customer(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOMER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMPANY_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VEHICLE_NO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_MODEL)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BATTERY_QUANTITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMING_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_OUTGOING_DATE))
                );
                customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                customers.add(customer);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return customers;
    }



}
