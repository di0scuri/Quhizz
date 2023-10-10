package com.example.quhizz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the button in your XML layout by its ID
        View myButton = view.findViewById(R.id.playButton);

        // Set an OnClickListener for the button
        myButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the button click event here
                // For example, you can start a new activity
                Intent intent = new Intent(getActivity(), CategoryRecyclerView.class);
                startActivity(intent);
            }
        });
    }
}
