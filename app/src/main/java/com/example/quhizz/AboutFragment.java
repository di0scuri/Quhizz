package com.example.quhizz;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Base64;

public class AboutFragment extends Fragment {

    private FirebaseAuth auth;

    private String base64Image;

    public AboutFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        View view = inflater.inflate(R.layout.fragment_about, container, false);

        ImageView LogOutView = view.findViewById(R.id.image_logout);
        ImageView profileImageView = view.findViewById(R.id.profileImageView);
        ImageView repositoryButton = view.findViewById(R.id.source_code);

        LogOutView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut(v);
            }
        });
        repositoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRepository();
            }
        });

        if (user != null) {
            String email = user.getEmail();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
            email.replace('.', '_');

            databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String fname = userSnapshot.child("firstName").getValue(String.class);
                            String lname = userSnapshot.child("lastName").getValue(String.class);
                            String username = userSnapshot.child("userName").getValue(String.class);

                            TextView usernameAbout = view.findViewById(R.id.usernameAbout);
                            TextView nameAbout = view.findViewById(R.id.nameAbout);

                            usernameAbout.setText(username);
                            nameAbout.setText(fname + " " + lname);

                            base64Image = userSnapshot.child("profilePicture").getValue(String.class);
                            if (base64Image != null){
                                Bitmap bitmap = decodeBase64toBitmap(base64Image);
                                profileImageView.setImageBitmap(bitmap);
                            }
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
    private  Bitmap decodeBase64toBitmap(String base64image){
        byte[] decodedBytes = Base64.decode(base64image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), Login.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
