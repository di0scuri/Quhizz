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

public class CategoryRecyclerView extends AppCompatActivity implements CategoriesAdapter.OnCategoryClickListener {
    private RecyclerView mRecyclerView;
    private ArrayList<Categories> mCategoriesData;
    private CategoriesAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_recycler_view);

        mRecyclerView = findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCategoriesData = new ArrayList<>();
        mAdapter = new CategoriesAdapter(this, mCategoriesData, this);
        mRecyclerView.setAdapter(mAdapter);
        initializeData();

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onCategoryClick(Categories category) {
        // Handle the click event specific to this activity
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("key", category.getTitle());
        startActivity(intent);
    }

    private void initializeData() {
        String[] quizList = getResources().getStringArray(R.array.quiz_categories);
        String[] quizInfo = getResources().getStringArray(R.array.quiz_subtitle);
        TypedArray quizImageResources = getResources().obtainTypedArray(R.array.category_images);

        mCategoriesData.clear();

        for (int i = 0; i < quizList.length; i++) {
            mCategoriesData.add(new Categories(quizList[i], quizInfo[i], quizImageResources.getResourceId(i, 0)));
        }

        quizImageResources.recycle();

        mAdapter.notifyDataSetChanged();
    }

    public void returnMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
