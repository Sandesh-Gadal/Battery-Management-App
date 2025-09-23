package com.example.batterymanagementapp;

import java.util.ArrayList;
import java.util.List;

public class Customer {
    private int id;
    private String customerName;
    private String companyName;
    private String vehicleNo;
    private String batteryModel;
    private int batteryQuantity;
    private String comingDate;
    private String outgoingDate;
    private String uniqueCode; // 6-char alphanumeric
    private List<String> imagePaths = new ArrayList<>();

    // runtime only
    private boolean active;

    // ✅ Constructor with uniqueCode
    public Customer(String customerName, String companyName, String vehicleNo,
                    String batteryModel, int batteryQuantity, String comingDate,
                    String outgoingDate, String uniqueCode) {
        this.customerName = customerName;
        this.companyName = companyName;
        this.vehicleNo = vehicleNo;
        this.batteryModel = batteryModel;
        this.batteryQuantity = batteryQuantity;
        this.comingDate = comingDate;
        this.outgoingDate = outgoingDate;
        this.uniqueCode = uniqueCode;
        // mark as active if no outgoing date yet
        this.active = (outgoingDate == null || outgoingDate.isEmpty());
    }

    // ✅ Active getter
    public boolean isActive() {
        return active;
    }

    // ✅ Setter in case you want to toggle it manually
    public void setActive(boolean active) {
        this.active = active;
    }

    // Getters & Setters
    public String getUniqueCode() {
        return uniqueCode;
    }

    public void setUniqueCode(String uniqueCode) {
        this.uniqueCode = uniqueCode;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getVehicleNo() {
        return vehicleNo;
    }

    public String getBatteryModel() {
        return batteryModel;
    }

    public int getBatteryQuantity() {
        return batteryQuantity;
    }

    public String getComingDate() {
        return comingDate;
    }

    public String getOutgoingDate() {
        return outgoingDate;
    }
}
