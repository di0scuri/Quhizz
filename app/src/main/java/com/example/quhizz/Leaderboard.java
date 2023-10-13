package com.example.quhizz;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Leaderboard extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private TextView totalUsers;
    private RecyclerView usersView;

    private String currentSortingOrder = "ascending";
    private String subject; // Initialize subject

    private List<LeaderboardItem> leaderboardItemList = new ArrayList<>();
    private LeaderboardAdapter leaderboardAdapter;
    private DatabaseReference databaseReference;

    private static final String LOG_TAG = Leaderboard.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        Intent intent = getIntent();
        subject = intent.getStringExtra("subject");

        Spinner spinner = findViewById(R.id.sorting_spinner);
        if (spinner != null) {
            spinner.setOnItemSelectedListener(this);
        }
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        totalUsers = findViewById(R.id.total_users);
        usersView = findViewById(R.id.users_view);

        databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("leaderboard");

        usersView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter(leaderboardItemList);
        usersView.setAdapter(leaderboardAdapter);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            // Step 1: Query the "Users" node to find the username
            databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                            String username = userSnapshot.child("userName").getValue(String.class);
                            // Now you have the username of the current user
                            Log.d("CurrentUser", "Username: " + username);

                            // Step 2: Query the "Leaderboards" node to find the score
                            DatabaseReference leaderboardsReference = FirebaseDatabase.getInstance().getReference("Leaderboards/Math");
                            leaderboardsReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        Long score = dataSnapshot.child("score").getValue(Long.class);
                                        TextView usernameTextView = findViewById(R.id.usernameTextView);
                                        TextView totalScore = findViewById(R.id.total_score);
                                        usernameTextView.setText(username);
                                        totalScore.setText(String.valueOf(score));

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Log.e("Firebase", "Error: " + databaseError.getMessage());
                                }
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("Firebase", "Error: " + databaseError.getMessage());
                }
            });
        }


        fetchLeaderboardData(currentSortingOrder,subject);
    }

    private void fetchLeaderboardData(String currentSortingOrder, String subject) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Leaderboards").child(subject);
        Query query;
        if (currentSortingOrder.equalsIgnoreCase("ascending")) {
            query = databaseReference.orderByChild("score");
        } else if (currentSortingOrder.equalsIgnoreCase("descending")) {
            query = databaseReference.orderByChild("score").limitToLast(1000);
        } else {
            query = databaseReference.orderByKey();
        }


        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                leaderboardItemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String username = snapshot.getKey();
                    int score = snapshot.child("score").getValue(Integer.class);

                    LeaderboardItem item = new LeaderboardItem(username, score);
                    leaderboardItemList.add(item);
                }

                // Sort the data based on the selected sorting
                if (currentSortingOrder.equals("Ascending")) {
                    Collections.sort(leaderboardItemList, (item1, item2) -> item2.getScore() - item1.getScore());
                } else if (currentSortingOrder.equals("Descending")) {
                    Collections.sort(leaderboardItemList, Comparator.comparingInt(LeaderboardItem::getScore));
                } else {
                    Collections.sort(leaderboardItemList, Comparator.comparing(LeaderboardItem::getUsername));
                }

                leaderboardAdapter.notifyDataSetChanged();
                totalUsers.setText("Total Players: " + leaderboardItemList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if any.
            }
        });
}

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long id) {
        String spinnerLabel = adapterView.getItemAtPosition(i).toString();
        currentSortingOrder = spinnerLabel;

        fetchLeaderboardData(currentSortingOrder, subject);
    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
class LeaderboardItem {
    private String username;
    private int score;
    public LeaderboardItem(String username ,int score) {
        this.username = username;
        this.score = score;
    }

    public String getUsername() {
        return username;
    }

    public int getScore() {
        return score;
    }
}



class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
    private List<LeaderboardItem> leaderboardItemList;

    public LeaderboardAdapter(List<LeaderboardItem> leaderboardItemList) {
        this.leaderboardItemList = leaderboardItemList;
    }

    public class LeaderboardViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView scoreTextView;

        public LeaderboardViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreTextView = itemView.findViewById(R.id.total_score);
        }
    }

    @Override
    public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the correct layout that contains the TextView elements.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_layout, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = leaderboardItemList.get(position);

        if (item != null) {
            // Bind data to views within the ViewHolder.
            holder.usernameTextView.setText(item.getUsername());
            holder.scoreTextView.setText("Score: " + item.getScore());
        }
    }


    @Override
    public int getItemCount() {
        return leaderboardItemList.size();
    }

}