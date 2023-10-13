package com.example.quhizz;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AboutFragment extends Fragment {

    private FirebaseAuth auth;

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView imageView = view.findViewById(R.id.image_log);
        ImageView repositoryButton = view.findViewById(R.id.source_code);

        repositoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRepository();
            }
        });

        if (user != null) {
            String email = user.getEmail();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String fname = userSnapshot.child("firstName").getValue(String.class);
                            String lname = userSnapshot.child("lastName").getValue(String.class);

                            TextView usernameAbout = view.findViewById(R.id.usernameAbout);
                            TextView nameAbout = view.findViewById(R.id.nameAbout);

                            usernameAbout.setText(email);
                            nameAbout.setText(fname + " " + lname);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            requireActivity().finish();
        }
        return view;
    }

    public void goToRepository() {
        Uri webpage = Uri.parse("https://github.com/di0scuri/Quhizz.git");
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(requireContext(), "Cannot get you to the repository", Toast.LENGTH_SHORT).show();
        }
    }
}
