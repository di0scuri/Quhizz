package com.example.quhizz;


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


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QuizActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String LOG_TAG = QuizActivity.class.getSimpleName();

    private TextView questionTextView;
    private RadioButton choiceA, choiceB, choiceC, choiceD;
    private Button button_submit;

    private RadioGroup choices_layout;

    private List<Integer> allQuestionIndices = new ArrayList<>();

    private List<String> playerAnswer = new ArrayList<>();

    private List<String> playerAccuracy = new ArrayList<>();

    int score = 0;
    private int totalQuestion = QuestionsAndAnswers.question.length;
    private int currentQuestionIndex = 0;
    private String selectedAnswer = "";
    private List<Integer> usedQuestionIndices = new ArrayList<>();

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save important data to the bundle
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

    // Modify this method to load the saved question
    public void loadSavedQuestion() {
        if (usedQuestionIndices.size() == totalQuestion) {
            //endQuiz();
            return;
        }

        questionTextView.setText(QuestionsAndAnswers.question[currentQuestionIndex]);
        choiceA.setText(QuestionsAndAnswers.choices[currentQuestionIndex][0]);
        choiceB.setText(QuestionsAndAnswers.choices[currentQuestionIndex][1]);
        choiceC.setText(QuestionsAndAnswers.choices[currentQuestionIndex][2]);
        choiceD.setText(QuestionsAndAnswers.choices[currentQuestionIndex][3]);

        // Highlight the previously selected answer
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

        // Enable or disable the submit button based on whether an answer is selected
        button_submit.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionTextView = findViewById(R.id.question);
        choiceA = findViewById(R.id.choice_A);
        choiceB = findViewById(R.id.choice_B);
        choiceC = findViewById(R.id.choice_C);
        choiceD = findViewById(R.id.choice_D);
        button_submit = findViewById(R.id.submit_button);
        choices_layout = findViewById(R.id.choices_layout);

        choiceA.setOnClickListener(this);
        choiceB.setOnClickListener(this);
        choiceC.setOnClickListener(this);
        choiceD.setOnClickListener(this);
        button_submit.setOnClickListener(this);

        button_submit.setVisibility(View.INVISIBLE);

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
            if (selectedAnswer.equals(QuestionsAndAnswers.correctAnswers[currentQuestionIndex])) {
                score++;
            } else {
                // Handle incorrect answers if needed
            }
            playerAnswer.add(selectedAnswer);

            // Highlight the correct answer
            highlightCorrectAnswer();

            // Make the "Submit" button invisible
            button_submit.setVisibility(View.INVISIBLE);

            // Delay for 1 second before loading the next question
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
            // Make the "Submit" button visible when an answer is selected
            button_submit.setVisibility(View.VISIBLE);
        }
    }


    /*public AlertDialog endQuiz() {
        AlertDialog dialog = savePrompt();
        dialog.show();
        return dialog;
    }*/

    public void loadNewRandomQuestion() {
        if (usedQuestionIndices.size() == 10) {
            //endQuiz();
            return;
        }

        int randomIndex;
        do {
            randomIndex = new Random().nextInt(totalQuestion);
        } while (usedQuestionIndices.contains(randomIndex));

        usedQuestionIndices.add(randomIndex);
        currentQuestionIndex = randomIndex;

        allQuestionIndices.add(currentQuestionIndex);

        questionTextView.setText(QuestionsAndAnswers.question[currentQuestionIndex]);
        choiceA.setText(QuestionsAndAnswers.choices[currentQuestionIndex][0]);
        choiceB.setText(QuestionsAndAnswers.choices[currentQuestionIndex][1]);
        choiceC.setText(QuestionsAndAnswers.choices[currentQuestionIndex][2]);
        choiceD.setText(QuestionsAndAnswers.choices[currentQuestionIndex][3]);
        button_submit.setVisibility(View.INVISIBLE);
    }

    //AlertDialog savePrompt() {
    //    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    //    builder.setTitle("Would you to see all the correct answers?");
    //    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    //        @Override
    /**        public void onClick(DialogInterface dialogInterface, int which) {
     Intent intent = new Intent(QuizActivity.this, showAnswers.class);
     intent.putIntegerArrayListExtra("questionSequence", (ArrayList<Integer>) usedQuestionIndices);
     intent.putStringArrayListExtra("playerAnswer", (ArrayList<String>) playerAnswer);
     intent.putExtra("score", score);
     startActivity(intent);

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
     }*/
    private void highlightCorrectAnswer() {
        String correctAnswer = QuestionsAndAnswers.correctAnswers[currentQuestionIndex];
        Log.d(LOG_TAG, correctAnswer);
        int backgroundColor = Color.DKGRAY;

        String chosenAnswer = "";

// Assuming you have some code to determine the value of chosenAnswer

        if(choiceA.getText().toString().equals(correctAnswer)) {
            choiceA.setBackgroundColor(Color.GREEN);
        }else if(choiceB.getText().toString().equals(correctAnswer)) {
            choiceA.setBackgroundColor(Color.GREEN);
        }else if(choiceC.getText().toString().equals(correctAnswer)) {
            choiceC.setBackgroundColor(Color.GREEN);
        }else{
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
        }, 1000); // Delay for 1 second
    }

}
