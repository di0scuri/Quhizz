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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
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
        subject = intent.getStringExtra("subject"); // Assign the value to subject

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

        // Create and set up the RecyclerView
        usersView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter(leaderboardItemList);
        usersView.setAdapter(leaderboardAdapter);

        Log.d(LOG_TAG, "Leaderboard onCreate");
        fetchLeaderboardData(currentSortingOrder,subject);
    }

    private void fetchLeaderboardData(String currentSortingOrder, String subject) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Leaderboards").child(subject);

        Query query;

        if (currentSortingOrder.equals("ascending")) {
            query = databaseReference.orderByChild("score");
        } else if (currentSortingOrder.equals("descending")) {
            query = databaseReference.orderByChild("score").limitToLast(10);
        } else {
            query = databaseReference.orderByKey(); // Sort alphabetically by default
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

                if (currentSortingOrder.equals("ascending")) {
                    Collections.sort(leaderboardItemList, (item1, item2) -> item1.getScore() - item2.getScore());
                } else if (currentSortingOrder.equals("descending")) {
                    Collections.sort(leaderboardItemList, (item1, item2) -> item2.getScore() - item1.getScore());
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

        // Fetch data based on the selected sorting order
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