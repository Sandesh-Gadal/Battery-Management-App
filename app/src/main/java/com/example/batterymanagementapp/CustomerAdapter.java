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
import androidx.core.content.ContextCompat;
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
    private Activity activity ;



    public CustomerAdapter(Activity activity, List<Customer> customerList , Context context) {
        this.activity = activity;
        this.customerList = customerList;
        this.context = context;

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, vehicleText, statusText;
        ImageButton showDetails , showQR , deleteBtn , editBtn;

        public ViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameText);
            vehicleText = view.findViewById(R.id.vehicleText);
            statusText = view.findViewById(R.id.statusText);
            showDetails = view.findViewById(R.id.viewButton);
            showQR = view.findViewById(R.id.qrButton);
            deleteBtn=view.findViewById(R.id.deleteButton);
            editBtn=view.findViewById(R.id.editButton);
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
//        holder.statusText.setText(c.getOutgoingDate() == null ? "Status: Active" : "Taken on " + c.getOutgoingDate());
        if (c.getOutgoingDate() == null) {
            holder.statusText.setText("Status: Active");
            holder.statusText.setBackgroundResource(R.drawable.status_background);
            holder.statusText.getBackground().setTint(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.green)
            );
        } else {
            holder.statusText.setText("Status: Taken");
            holder.statusText.setBackgroundResource(R.drawable.status_background);
            holder.statusText.getBackground().setTint(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.red)
            );
        }


        // Open CustomerDetailsActivity
        holder.showDetails.setOnClickListener(v -> {
//            Log.d("id111:","454545:    "+c.getUniqueCode());
            Intent intent = new Intent(activity, CustomerDetailsActivity.class);

            intent.putExtra("customerId", c.getUniqueCode()); // pass customer ID
            activity.startActivity(intent);
        });

        holder.editBtn.setOnClickListener(v -> {
            Intent intent = new Intent(activity , MainActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("unique_code",c.getUniqueCode());
            intent.putExtra("customerId", c.getId()); // pass DB id
            activity.startActivityForResult(intent , 100);
        });


        holder.showQR.setOnClickListener(v->{
           Intent intent = new Intent(activity , QRActivity.class);
//           Log.d("ucode",c.getUniqueCode());
           intent.putExtra("unique_code",c.getUniqueCode());
           activity.startActivity(intent);
       });

        holder.deleteBtn.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(activity)
                    .setTitle("Delete Customer")
                    .setMessage("Are you sure you want to delete this customer?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete customer from DB
                        DatabaseHelper dbHelper = new DatabaseHelper(activity);
                        dbHelper.deleteCustomer(c.getId());

                        // Remove from list and update RecyclerView
                        customerList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, customerList.size());
                    })
                    .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                    .show();
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
