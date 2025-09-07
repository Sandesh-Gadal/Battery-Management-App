package com.example.batterymanagementapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private List<String> imagePaths;
    private Context context;

    public ImageAdapter(Context context, List<String> imagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = view.findViewById(R.id.imageView);
        }
    }

    @NonNull
    @Override
    public ImageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageAdapter.ViewHolder holder, int position) {
        String path = imagePaths.get(position);
        File file = new File(path);
        if (file.exists()) {
            holder.imageView.setImageURI(Uri.fromFile(file));
        }

        holder.imageView.setOnClickListener(v -> {
            if (context instanceof CustomerDetailsActivity) {
                ((CustomerDetailsActivity) context).showImagePreview(path);
            }
        });

    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void updateList(List<String> newPaths) {
        imagePaths = newPaths;
        notifyDataSetChanged();
    }
}
