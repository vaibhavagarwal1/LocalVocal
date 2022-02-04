package com.dev.localvocal.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dev.localvocal.R;
import com.dev.localvocal.models.ModelCategories;

import java.util.ArrayList;

public class AdapterCategories extends RecyclerView.Adapter<AdapterCategories.HolderCategories> {

    Context context;
    ArrayList<ModelCategories> modelCategories;

    public AdapterCategories(Context context, ArrayList<ModelCategories> modelCategories) {
        this.context = context;
        this.modelCategories = modelCategories;
    }

    @NonNull
    @Override
    public HolderCategories onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_categories, parent, false);
        return new HolderCategories(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCategories holder, int position) {
        //set image
        holder.categoryIcon.setImageResource(modelCategories.get(position).getCategoriesIcon());
        //set name
        holder.categoryName.setText(modelCategories.get(position).getCategoriesName());
    }

    @Override
    public int getItemCount() {
        return modelCategories.size();
    }

    public class HolderCategories extends RecyclerView.ViewHolder {

        //UI Views
        private ImageView categoryIcon;
        private TextView categoryName;

        public HolderCategories(@NonNull View itemView) {
            super(itemView);

            //init UI views
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }
}
