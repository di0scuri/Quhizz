package com.example.quhizz;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = QuizActivity.class.getSimpleName();

    private TextView questionTextView;
    private RadioButton choiceA, choiceB, choiceC, choiceD;
    private Button button_submit;

    private String subject;

    private RadioGroup choices_layout;

    private List<Integer> allQuestionIndices = new ArrayList<>();

    private List<String> playerAnswer = new ArrayList<>();

    private List<String> playerAccuracy = new ArrayList<>();

    private int score = 0;
    private int totalQuestion;
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private List<Integer> usedQuestionIndices = new ArrayList<>();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private String userEmail;

    private String[] questions;
    private String[][] choices;
    private String[] correctAnswers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        Intent intent = getIntent();
        subject = intent.getStringExtra("key");

        questionTextView = findViewById(R.id.question);
        choiceA = findViewById(R.id.choice_A);
        choiceB = findViewById(R.id.choice_B);
        choiceC = findViewById(R.id.choice_C);
        choiceD = findViewById(R.id.choice_D);
        button_submit = findViewById(R.id.submit_button);
        choices_layout = findViewById(R.id.choices_layout);

        // ... other initialization code ...

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        userEmail = currentUser.getEmail();

        choiceA.setOnClickListener(this);
        choiceB.setOnClickListener(this);
        choiceC.setOnClickListener(this);
        choiceD.setOnClickListener(this);
        button_submit.setOnClickListener(this);

        button_submit.setVisibility(View.INVISIBLE);

        initializeQuiz();
    }

    private void initializeQuiz(){
        totalQuestion = 20;

        switch (subject){
            case "Math":
                questions = QuestionsAndAnswers.Math.questions;
                choices = QuestionsAndAnswers.Math.choices;
                correctAnswers = QuestionsAndAnswers.Math.correctAnswers;
                break;
            case "Science":
                questions = QuestionsAndAnswers.Science.questions;
                choices = QuestionsAndAnswers.Science.choices;
                correctAnswers = QuestionsAndAnswers.Science.correctAnswers;
                break;
            case "History":
                questions = QuestionsAndAnswers.History.questions;
                choices = QuestionsAndAnswers.History.choices;
                correctAnswers = QuestionsAndAnswers.History.correctAnswers;
                break;
            case "Trivia":
                questions = QuestionsAndAnswers.Trivia.questions;
                choices = QuestionsAndAnswers.Trivia.choices;
                correctAnswers = QuestionsAndAnswers.Trivia.correctAnswers;
                break;
            case "Geography":
                questions = QuestionsAndAnswers.Geography.questions;
                choices = QuestionsAndAnswers.Geography.choices;
                correctAnswers = QuestionsAndAnswers.Geography.correctAnswers;
                break;
            case "Technology":
                questions = QuestionsAndAnswers.Technology.questions;
                choices = QuestionsAndAnswers.Technology.choices;
                correctAnswers = QuestionsAndAnswers.Technology.correctAnswers;
                break;
        }
        usedQuestionIndices = new ArrayList<>();
        currentQuestionIndex = 0;
        score = 0;
        selectedAnswer = "";
        playerAnswer = new ArrayList<>();
        playerAccuracy = new ArrayList<>();

        loadNewRandomQuestion();
    }

    @Override
    public void onClick(View view) {
        choiceA.setBackgroundColor(Color.DKGRAY);
        choiceB.setBackgroundColor(Color.DKGRAY);
        choiceC.setBackgroundColor(Color.DKGRAY);
        choiceD.setBackgroundColor(Color.DKGRAY);

        Button clickedButton = (Button) view;
        if (clickedButton.getId() == R.id.submit_button) {
            if (selectedAnswer.equals(correctAnswers[currentQuestionIndex])) {
                score++;
            }
            playerAnswer.add(selectedAnswer);

            highlightCorrectAnswer();

            button_submit.setVisibility(View.INVISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    currentQuestionIndex++;
                    loadNewRandomQuestion();

                    Log.d(LOG_TAG, String.valueOf(playerAccuracy));
                    Log.d(LOG_TAG, String.valueOf(playerAnswer));
                    choices_layout.clearCheck();
                }
            }, 1000); // Delay for 1 second
        } else {
            selectedAnswer = clickedButton.getText().toString();
            clickedButton.setBackgroundColor(Color.MAGENTA);
            button_submit.setVisibility(View.VISIBLE);
        }
    }
    private void saveScoreToDatabase(String userEmail, String subject, int score) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
        usersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String username = userSnapshot.child("userName").getValue(String.class);

                        if (username != null) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app")
                                    .getReference("Leaderboards")
                                    .child(subject)
                                    .child(username);

                            databaseReference.child("score").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Integer recordedScore = snapshot.getValue(Integer.class);
                                        if (recordedScore != null) {
                                            if (recordedScore < score) {
                                                databaseReference.child("score").setValue(score);
                                            } else if (recordedScore > score) {
                                                // Handle the case when the user wants to save a lower score
                                                AlertDialog.Builder builder = new AlertDialog.Builder(QuizActivity.this);
                                                builder.setTitle("Are you sure you want to save the data?");
                                                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        databaseReference.child("score").setValue(score);
                                                    }
                                                });
                                                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        // Handle the case when the user clicks "No"
                                                    }
                                                });
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            } else {
                                                // Handle the case when the scores are the same
                                                AlertDialog.Builder builder = new AlertDialog.Builder(QuizActivity.this);
                                                builder.setTitle("You have the same score as your recorded score");
                                                builder.setCancelable(true);
                                                AlertDialog alertDialog = builder.create();
                                                alertDialog.show();
                                            }
                                        }
                                    } else {
                                        databaseReference.child("score").setValue(score);
                                        Toast.makeText(getApplicationContext(), "Your score is saved in the database", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(QuizActivity.this, "Cannot connect to the database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String username = userSnapshot.child("userName").getValue(String.class);
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference("Leaderboards")
                                .child(subject)
                                .child(username);
                        databaseReference.child("score").setValue(score);
                        Toast.makeText(getApplicationContext(), "Saved to database", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(QuizActivity.this, "Cannot connect to Database", Toast.LENGTH_SHORT).show();
            }
        });
    }
            @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt("score", score);
        savedInstanceState.putInt("currentQuestionIndex", currentQuestionIndex);
        savedInstanceState.putString("selectedAnswer", selectedAnswer);
        savedInstanceState.putIntegerArrayList("usedQuestionIndices", new ArrayList<>(usedQuestionIndices));
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the saved data from the bundle
        score = savedInstanceState.getInt("score");
        currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex");
        selectedAnswer = savedInstanceState.getString("selectedAnswer");
        usedQuestionIndices = savedInstanceState.getIntegerArrayList("usedQuestionIndices");

        loadSavedQuestion();
    }

    public void loadSavedQuestion() {

        if (usedQuestionIndices.size() == 19) {
            endQuiz();
            return;
        }

        questionTextView.setText(questions[currentQuestionIndex]);
        choiceA.setText(choices[currentQuestionIndex][0]);
        choiceB.setText(choices[currentQuestionIndex][1]);
        choiceC.setText(choices[currentQuestionIndex][2]);
        choiceD.setText(choices[currentQuestionIndex][3]);

        if (!selectedAnswer.isEmpty()) {
            switch (selectedAnswer) {
                case "A":
                    choiceA.setBackgroundColor(Color.MAGENTA);
                    break;
                case "B":
                    choiceB.setBackgroundColor(Color.MAGENTA);
                    break;
                case "C":
                    choiceC.setBackgroundColor(Color.MAGENTA);
                    break;
                case "D":
                    choiceD.setBackgroundColor(Color.MAGENTA);
                    break;
            }
        }

        button_submit.setVisibility(View.INVISIBLE);
    }

    public void loadNewRandomQuestion() {
        if (usedQuestionIndices.size() == 1) {
            endQuiz();
            return;
        }

        int randomIndex;
        Random random = new Random();
        do {
            randomIndex = random.nextInt(totalQuestion);
        } while (usedQuestionIndices.contains(randomIndex));

        usedQuestionIndices.add(randomIndex);
        currentQuestionIndex = randomIndex;

        allQuestionIndices.add(currentQuestionIndex);

        questionTextView.setText(questions[currentQuestionIndex]);
        choiceA.setText(choices[currentQuestionIndex][0]);
        choiceB.setText(choices[currentQuestionIndex][1]);
        choiceC.setText(choices[currentQuestionIndex][2]);
        choiceD.setText(choices[currentQuestionIndex][3]);
        button_submit.setVisibility(View.INVISIBLE);
    }

    private void highlightCorrectAnswer() {
        String correctAnswer = correctAnswers[currentQuestionIndex];
        Log.d(LOG_TAG, correctAnswer);
        int backgroundColor = Color.DKGRAY;

        if (choiceA.getText().toString().equals(correctAnswer)) {
            choiceA.setBackgroundColor(Color.GREEN);
        } else if (choiceB.getText().toString().equals(correctAnswer)) {
            choiceB.setBackgroundColor(Color.GREEN);
        } else if (choiceC.getText().toString().equals(correctAnswer)) {
            choiceC.setBackgroundColor(Color.GREEN);
        } else if (choiceD.getText().toString().equals(correctAnswer)) {
            choiceD.setBackgroundColor(Color.GREEN);
        }

        final int finalBackgroundColor = backgroundColor;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        choiceA.setBackgroundColor(finalBackgroundColor);
                        choiceB.setBackgroundColor(finalBackgroundColor);
                        choiceC.setBackgroundColor(finalBackgroundColor);
                        choiceD.setBackgroundColor(finalBackgroundColor);
                    }
                });
            }
        }, 1000);
    }
    public AlertDialog endQuiz() {
        AlertDialog dialog = savePrompt();
        dialog.show();
        return dialog;
    }
    AlertDialog savePrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Would you like to save your score?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                saveScoreToDatabase(userEmail,subject,score);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(getApplicationContext(), Leaderboard.class);
                        intent.putExtra("subject", subject);
                        Log.d(LOG_TAG, "Intent");
                        startActivity(intent);
                    }
                }, 3000);

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                Intent intent = new Intent(QuizActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        return builder.create();
    }
}