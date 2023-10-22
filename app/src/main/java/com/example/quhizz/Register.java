package com.example.quhizz;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Base64;

import com.example.quhizz.Login;
import com.example.quhizz.R;
import com.example.quhizz.Users;
import com.example.quhizz.databinding.ActivityRegisterBinding;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
public class Register extends AppCompatActivity {

    ActivityRegisterBinding binding;
    Button button_Register;
    FirebaseAuth mAuth;
    FirebaseDatabase db;
    DatabaseReference reference;
    TextView loginNow;

    ImageView profileView;

    String base64Image;

    FloatingActionButton set_image;
    boolean isImageSelected = false;

    String email, password, firstName, lastName, userName, set_Birth, profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        button_Register = binding.buttonRegister;
        set_image = binding.setImage;
        profileView = binding.profileView;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app/");
        reference = db.getReference("Users");

        set_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImagePicker.with(Register.this).crop().compress(1024).maxResultSize(1080,1080).start();
            }
        });

        button_Register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = binding.email.getText().toString();
                password = binding.password.getText().toString();
                firstName = binding.firstName.getText().toString();
                lastName = binding.lastName.getText().toString();
                userName = binding.username.getText().toString();
                set_Birth = binding.setBirth.getText().toString();

                if (!isImageSelected){
                    Toast.makeText(Register.this, "Please choose an image", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(set_Birth)) {
                    Toast.makeText(Register.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users");

                usersRef.orderByChild("userName").equalTo(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!dataSnapshot.exists()) {
                            mAuth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                String emailKey = encodeEmailAsKey(email);
                                                Users userData = new Users(email, firstName, lastName, set_Birth, userName);
                                                reference.child(emailKey).setValue(userData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    reference.child(emailKey).child("profilePicture").setValue(base64Image);
                                                                    Toast.makeText(getApplicationContext(), "Your account has been created", Toast.LENGTH_SHORT).show();
                                                                    Intent intent = new Intent(getApplicationContext(), Login.class);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(Register.this, "Failed to save data. Please try again.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                            } else {
                                                Toast.makeText(Register.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(Register.this, "This username is already taken. Please choose another one.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(Register.this, "Database error. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    private String encodeEmailAsKey(String email) {
        return email.replace('.', '_');
    }

    public void toLoginPage(View view) {
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
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Uri uri = data.getData();
        profileView.setImageURI(uri);

        if (uri != null) {
            profileView.setImageURI(uri);
            isImageSelected = true;
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                base64Image = bitmapToBase64(bitmap);
                profile = base64Image;
                Log.d("Image Conversion", profile);
            } catch (IOException e) {
                Log.d("Image Conversion", "Failed");
            }
        }
    }
    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
