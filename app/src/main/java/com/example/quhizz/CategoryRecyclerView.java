package com.example.quhizz;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class CategoryRecyclerView extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private ArrayList<Categories> mCategoriesData;
    private CategoriesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_recycler_view); // Replace with your activity layout

        mRecyclerView = findViewById(R.id.recyclerView);

        // Set the Layout Manager.
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the ArrayList that will contain the data.
        mCategoriesData = new ArrayList<>();

        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new CategoriesAdapter(this, mCategoriesData);
        mRecyclerView.setAdapter(mAdapter);

        // Get the data.
        initializeData();

        // Example navigation code (if needed)
        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initializeData() {
        // Get the resources from the XML file.
        String[] quizList = getResources().getStringArray(R.array.quiz_categories);
        String[] quizInfo = getResources().getStringArray(R.array.quiz_subtitle);
        TypedArray quizImageResources = getResources().obtainTypedArray(R.array.category_images);

        // Clear the existing data (to avoid duplication).
        mCategoriesData.clear();

        // Create the ArrayList of Sports objects with titles and
        // information about each sport.
        for (int i = 0; i < quizList.length; i++) {
            mCategoriesData.add(new Categories(quizList[i], quizInfo[i], quizImageResources.getResourceId(i, 0)));
        }

        quizImageResources.recycle();

        // Notify the adapter of the change.
        mAdapter.notifyDataSetChanged();
    }

    public void returnMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
