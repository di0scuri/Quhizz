package com.example.quhizz;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;


public class Leaderboard extends AppCompatActivity {

    private TextView totalUsers;
    private RecyclerView usersView;

    private List<LeaderboardItem> leaderboardItemList = new ArrayList<>();
    private LeaderboardAdapter leaderboardAdapter;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        totalUsers = findViewById(R.id.total_users);
        usersView = findViewById(R.id.users_view);

        databaseReference = FirebaseDatabase.getInstance().getReference("leaderboard");

        // Create and set up the RecyclerView
        usersView.setLayoutManager(new LinearLayoutManager(this));
        leaderboardAdapter = new LeaderboardAdapter(leaderboardItemList);
        usersView.setAdapter(leaderboardAdapter);

        // Fetch data from Firebase and update the RecyclerView
        fetchLeaderboardData();
    }

    private void fetchLeaderboardData() {
        databaseReference.orderByChild("score").limitToLast(10).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                leaderboardItemList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    LeaderboardItem item = snapshot.getValue(LeaderboardItem.class);
                    leaderboardItemList.add(item);
                }

                // Notify the adapter that the data has changed.
                leaderboardAdapter.notifyDataSetChanged();
                totalUsers.setText("Total Players: " + leaderboardItemList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle errors if any.
            }
        });
    }
}
 class LeaderboardItem {
    private String username;
    private int score;
    private int rank;

    public LeaderboardItem() {
        // Default constructor required for Firebase
    }

     public LeaderboardItem(int score) {
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
        public TextView rankTextView;

        public LeaderboardViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            scoreTextView = itemView.findViewById(R.id.total_score);
            rankTextView = itemView.findViewById(R.id.rank);
        }
    }

    @Override
    public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_leaderboard, parent, false);
        return new LeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
        LeaderboardItem item = leaderboardItemList.get(position);

        // Bind data to views within the ViewHolder.
        holder.scoreTextView.setText("Score: " + item.getScore());
    }

    @Override
    public int getItemCount() {
        return leaderboardItemList.size();
    }
}
