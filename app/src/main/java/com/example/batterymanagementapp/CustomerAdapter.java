package com.example.batterymanagementapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.batterymanagementapp.Customer;
import com.example.batterymanagementapp.CustomerDetailsActivity;
import com.example.batterymanagementapp.CustomerListActivity;
import com.example.batterymanagementapp.R;

import java.util.ArrayList;
import java.util.List;

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {
    private List<Customer> customerList;
    private Context context;
    private Activity activity;



    public CustomerAdapter(Activity activity, List<Customer> customerList , Context context) {
        this.activity = activity;
        this.customerList = customerList;
        this.context = context;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, vehicleText, statusText;
        ImageButton showDetails , showQR;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            vehicleText = view.findViewById(R.id.vehicleText);
            statusText = view.findViewById(R.id.statusText);
            showDetails = view.findViewById(R.id.viewButton);
            showQR = view.findViewById(R.id.qrButton);
        }
    }

    @NonNull
    @Override
    public CustomerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Customer c = customerList.get(position);
        holder.nameText.setText(c.getCustomerName() + " (" + c.getCompanyName() + ")");
        holder.vehicleText.setText("Ampere: " + c.getVehicleNo());
        holder.statusText.setText(c.getOutgoingDate() == null ? "Status: Active" : "Taken on " + c.getOutgoingDate());
        // Open CustomerDetailsActivity
        holder.showDetails.setOnClickListener(v -> {
//            Log.d("id111:","454545:    "+c.getUniqueCode());
            Intent intent = new Intent(activity, CustomerDetailsActivity.class);


            intent.putExtra("customerId", c.getUniqueCode()); // pass customer ID
            activity.startActivity(intent);
        });

       holder.showQR.setOnClickListener(v->{
           Intent intent = new Intent(activity , QRActivity.class);
//           Log.d("ucode",c.getUniqueCode());
           intent.putExtra("unique_code",c.getUniqueCode());
           activity.startActivity(intent);
       });
    }

    @Override
    public int getItemCount() {
        return customerList.size();
    }



    public void updateList(List<Customer> newList) {
        this.customerList = newList;
        notifyDataSetChanged();
    }


}
