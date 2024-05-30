package com.example.mushroomrecognitionapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MushroomAdapter extends RecyclerView.Adapter<MushroomAdapter.MushroomViewHolder> {

    private final String[] classes;
    private Context context;

    public MushroomAdapter(String[] classes) {
        this.classes = classes;
    }

    @NonNull
    @Override
    public MushroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_mushroom, parent, false);
        return new MushroomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MushroomViewHolder holder, int position) {
        String className = classes[position];
        String classNameDetails = className;
        String classNameImage = normalizeClassName(className);

        holder.mushroomName.setText(className);
        holder.image1.setImageResource(getDrawableResourceId(classNameImage + "1"));
        holder.image2.setImageResource(getDrawableResourceId(classNameImage + "2"));

        holder.detailsButton.setOnClickListener(v -> moveToDetailsActivity(classNameDetails));
    }

    @Override
    public int getItemCount() {
        return classes.length;
    }

    static class MushroomViewHolder extends RecyclerView.ViewHolder {
        TextView mushroomName;
        ImageView image1, image2;
        ImageButton detailsButton;

        MushroomViewHolder(@NonNull View itemView) {
            super(itemView);
            mushroomName = itemView.findViewById(R.id.mushroomName);
            image1 = itemView.findViewById(R.id.image1);
            image2 = itemView.findViewById(R.id.image2);
            detailsButton = itemView.findViewById(R.id.detailsButton);
        }
    }

    private void moveToDetailsActivity(String mushroomClass) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra("mushroomClass", mushroomClass);
        context.startActivity(intent);
    }

    private String normalizeClassName(String className) {
        return className.toLowerCase()
                .replaceAll("ž", "z")
                .replaceAll("ė", "e")
                .replaceAll("š", "s")
                .replaceAll("ū", "u")
                .replaceAll("Š", "S")
                .replaceAll("Ž", "Z")
                .replaceAll("č", "c")
                .split("\n")[0]
                .trim()
                .replace(" ", "_");
    }

    private int getDrawableResourceId(String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
    }
}
