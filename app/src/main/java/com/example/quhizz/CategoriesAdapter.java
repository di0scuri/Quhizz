package com.example.quhizz;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {

    private OnCategoryClickListener categoryClickListener;
    private ArrayList<Categories> mCategoriesData;
    private Context mContext;

    CategoriesAdapter(Context context, ArrayList<Categories> categoriesData, OnCategoryClickListener listener) {
        this.mCategoriesData = categoriesData;
        this.mContext = context;
        this.categoryClickListener = listener;
    }

    @NonNull
    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Categories currentCategory = mCategoriesData.get(position);
        holder.bindTo(currentCategory);
    }

    @Override
    public int getItemCount() {
        return mCategoriesData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleText;
        private TextView mInfoText;
        private ImageView mCategoryImage;

        ViewHolder(View itemView) {
            super(itemView);

            mTitleText = itemView.findViewById(R.id.title);
            mInfoText = itemView.findViewById(R.id.subTitle);
            mCategoryImage = itemView.findViewById(R.id.sportsImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            Categories currentCategory = mCategoriesData.get(getAdapterPosition());
            if (categoryClickListener != null) {
                categoryClickListener.onCategoryClick(currentCategory);
            }
        }

        public void bindTo(Categories currentCategory) {
            Glide.with(mContext).load(currentCategory.getImageResource()).into(mCategoryImage);
            mTitleText.setText(currentCategory.getTitle());
            mInfoText.setText(currentCategory.getInfo());
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Categories category);
    }
}
