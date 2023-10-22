package com.example.quhizz;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LeaderboardsFragment extends Fragment implements CategoriesAdapter.OnCategoryClickListener {

    private RecyclerView leaderboardCategoryView;
    private CategoriesAdapter categoriesAdapter;

    private ArrayList<Categories> mCategoriesData;

    public LeaderboardsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboards, container, false);
        leaderboardCategoryView = view.findViewById(R.id.leaderboardCategoryView);
        leaderboardCategoryView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mCategoriesData = new ArrayList<>();

        categoriesAdapter = new CategoriesAdapter(requireContext(), mCategoriesData, this);
        leaderboardCategoryView.setAdapter(categoriesAdapter);
        initializeData();
        return view;
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

        categoriesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCategoryClick(Categories category) {
        Intent intent = new Intent(requireContext(), Leaderboard.class);
        intent.putExtra("subject", category.getTitle());
        startActivity(intent);
    }
}
