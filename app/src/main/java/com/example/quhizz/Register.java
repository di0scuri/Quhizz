package com.example.quhizz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.quhizz.Login;
import com.example.quhizz.R;
import com.example.quhizz.Users;
import com.example.quhizz.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Register extends AppCompatActivity {

    ActivityRegisterBinding binding;
    Button button_Register;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    ProgressBar progressBar;
    TextView loginNow;

    String email, password, firstName, lastName, userName, birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        button_Register = binding.buttonRegister;
        progressBar = binding.progressBar;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app/");
        reference = db.getReference("Users");

        button_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();
                firstName = binding.firstName.getText().toString();
                lastName = binding.lastName.getText().toString();
                userName = binding.username.getText().toString();
                progressBar.setVisibility(View.VISIBLE);

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(userName)) {
                    Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Reference to the "Users" node in Firebase Realtime Database
                DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

                // Check if the username already exists
                usersRef.orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            // Username is not taken, proceed with user registration
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String emailKey = encodeEmailAsKey(email);
                                                Users userData = new Users(email, firstName, lastName, birthday, userName);
                                                reference.child(emailKey).setValue(userData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getApplicationContext(), "Your account has been created", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    progressBar.setVisibility(View.GONE);
                                                                    Toast.makeText(Register.this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "This username is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Register.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private String encodeEmailAsKey(String email) {
        // Replace all characters that Firebase doesn't allow in keys with a safe character
        return email.replace('.', '_');
    }


    public void loginNow(View view) {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }

    public void showDatePicker(View view) {
        DialogFragment newFragment = new com.example.pickerfordate.DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), getString(R.string.datepicker));
    }

    public void processDatePickerResult(int year, int month, int day) {
        String month_string = Integer.toString(month + 1);
        String day_string = Integer.toString(day);
        String year_string = Integer.toString(year);
        String dateMessage = (month_string + "/" + day_string + "/" + year_string);
        Toast.makeText(this, getString(R.string.date) + dateMessage, Toast.LENGTH_SHORT).show();
        birthday = dateMessage;
    }
}
