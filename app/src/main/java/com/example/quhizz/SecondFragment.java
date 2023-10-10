package com.example.quhizz;

import android.content.res.TypedArray;
    import android.os.Bundle;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;

    import androidx.annotation.NonNull;

    import androidx.fragment.app.Fragment;
    import androidx.navigation.fragment.NavHostFragment;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;


    import com.example.quhizz.databinding.FragmentSecondBinding;

    import java.util.ArrayList;
    
    public class SecondFragment extends Fragment { 
        private FragmentSecondBinding binding;

        private RecyclerView mRecyclerView;
        private ArrayList<Categories> mCategoriesData;
        private CategoriesAdapter mAdapter;

        @Override
        public View onCreateView(
                LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState
        ) {

            
            
            binding = FragmentSecondBinding.inflate(inflater, container, false);
            View rootView = binding.getRoot();

            mRecyclerView = rootView.findViewById(R.id.recyclerView);

            // Set the Layout Manager.
            mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

            // Initialize the ArrayList that will contain the data.
            mCategoriesData = new ArrayList<>();

            // Initialize the adapter and set it to the RecyclerView.
            mAdapter = new CategoriesAdapter(requireContext(), mCategoriesData);
            mRecyclerView.setAdapter(mAdapter);

            // Get the data.
            initializeData();
            

            return rootView;
        }
        


        public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            binding.fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(SecondFragment.this)
                            .navigate(R.id.action_SecondFragment_to_FirstFragment);
                }
            });
        }

        private void initializeData() {
            // Get the resources from the XML file.
            String[] quizList = getResources()
                    .getStringArray(R.array.quiz_categories);
            String[] quizInfo = getResources()
                    .getStringArray(R.array.quiz_subtitle);
            TypedArray quizImageResources = getResources().obtainTypedArray(R.array.category_images);



            // Clear the existing data (to avoid duplication).
            mCategoriesData.clear();

            // Create the ArrayList of Sports objects with titles and
            // information about each sport.
            for (int  i=0; i<quizList.length; i++){
                mCategoriesData.add(new Categories(quizList[i], quizInfo[i], quizImageResources.getResourceId(i,0)));
            }

            quizImageResources.recycle();

            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
            
            
        }
        
        
        

    @Override
        public void onDestroyView() {
            super.onDestroyView();
            binding = null;
        }

    }