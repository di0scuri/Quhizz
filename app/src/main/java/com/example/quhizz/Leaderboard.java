        package com.example.quhizz;

        import android.content.Intent;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.os.Bundle;
        import android.util.Base64;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.ArrayAdapter;
        import android.widget.ImageView;
        import android.widget.Spinner;
        import android.widget.TextView;

        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import androidx.recyclerview.widget.LinearLayoutManager;
        import androidx.recyclerview.widget.RecyclerView;

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
        import java.util.HashSet;
        import java.util.List;
        import java.util.Set;
        import java.util.concurrent.atomic.AtomicInteger;

        public class Leaderboard extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

            private TextView totalUsers;
            private RecyclerView usersView;

            private String currentSortingOrder = "Descending";
            private String subject;

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

                databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Leaderboard");

                usersView.setLayoutManager(new LinearLayoutManager(this));
                leaderboardAdapter = new LeaderboardAdapter(leaderboardItemList);
                usersView.setAdapter(leaderboardAdapter);

                TextView leaderboard_header = findViewById(R.id.leaderboard_header);
                leaderboard_header.setText(subject+ " Leaderboard");

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    String email = currentUser.getEmail();
                    DatabaseReference usersReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");

                    usersReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String username = userSnapshot.child("userName").getValue(String.class);
                                    Log.d("CurrentUser", "Username: " + username);
                                    DatabaseReference leaderboardsReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Leaderboards").child(subject);
                                    leaderboardsReference.child(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.exists()) {
                                                int score = dataSnapshot.child("score").getValue(Integer.class);
                                                TextView usernameTextView = findViewById(R.id.currentUsernameTextView);
                                                TextView totalScore = findViewById(R.id.currentTotal_score);
                                                ImageView currentProfileImageView = findViewById(R.id.profile_current);
                                                usernameTextView.setText(username);
                                                totalScore.setText("Score: " + score);
                                                DatabaseReference currentUser = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("Users");
                                                currentUser.orderByChild("userName").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if (snapshot.exists()) {
                                                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                                                String base64 = userSnapshot.child("profilePicture").getValue(String.class);
                                                                if (base64 != null) {
                                                                    Bitmap bitmap = decodeBase64toBitmap(base64);
                                                                    currentProfileImageView.setImageBitmap(bitmap);
                                                                }
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Log.e("Firebase", "Error: " + error.getMessage());
                                                    }
                                                });
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

                fetchLeaderboardData(currentSortingOrder, subject);
            }

            private void fetchLeaderboardData(String currentSortingOrder, String subject) {
                if (!subject.isEmpty()) {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("Leaderboards").child(subject);
                    Query query;
                    if (currentSortingOrder.equals("Ascending")) {
                        query = databaseReference.orderByChild("score");
                    } else if (currentSortingOrder.equals("Descending")) {
                        query = databaseReference.orderByChild("score").limitToLast(1000);
                    } else {
                        query = databaseReference.orderByKey();
                    }

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            List<LeaderboardItem> newItems = new ArrayList<>();
                            final int totalItems = (int) dataSnapshot.getChildrenCount();
                            final AtomicInteger itemsProcessed = new AtomicInteger(0);

                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String username = snapshot.getKey();
                                int score = snapshot.child("score").getValue(Integer.class);

                                FirebaseDatabase database = FirebaseDatabase.getInstance("https://quhizz-default-rtdb.asia-southeast1.firebasedatabase.app");
                                DatabaseReference usersRef = database.getReference("Users");
                                Query userQuery = usersRef.orderByChild("userName").equalTo(username);

                                userQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot user : snapshot.getChildren()) {
                                            String profilePicture = user.child("profilePicture").getValue(String.class);
                                            newItems.add(new LeaderboardItem(username, score, decodeBase64toBitmap(profilePicture)));
                                        }
                                        // Increment itemsProcessed using AtomicInteger
                                        int processedCount = itemsProcessed.incrementAndGet();

                                        if (processedCount == totalItems) {
                                            if (currentSortingOrder.equals("Ascending")) {
                                                Collections.sort(newItems, Comparator.comparingInt(LeaderboardItem::getScore));
                                            } else if (currentSortingOrder.equals("Descending")) {
                                                Collections.sort(newItems, (item1, item2) -> item2.getScore() - item1.getScore());
                                            } else {
                                                Collections.sort(newItems, Comparator.comparing(LeaderboardItem::getUsername));
                                            }
                                            leaderboardItemList.clear();
                                            leaderboardItemList.addAll(newItems);
                                            leaderboardAdapter.notifyDataSetChanged();
                                            totalUsers.setText("Total Players: "+ leaderboardItemList.size());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            private Bitmap decodeBase64toBitmap(String base64image) {
                byte[] decodedBytes = Base64.decode(base64image, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
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

            @Override
            public void onBackPressed(){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        }

        class LeaderboardItem {
            private final Bitmap image;
            private String username;
            private int score;

            public LeaderboardItem(String username, int score, Bitmap image) {
                this.username = username;
                this.score = score;
                this.image = image;
            }

            public String getUsername() {
                return username;
            }

            public int getScore() {
                return score;
            }

            public Bitmap getImage() {
                return image;
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
                public ImageView profileImageView;

                public LeaderboardViewHolder(View itemView) {
                    super(itemView);
                    usernameTextView = itemView.findViewById(R.id.usernameTextView);
                    scoreTextView = itemView.findViewById(R.id.total_score);
                    profileImageView = itemView.findViewById(R.id.profile_leaderboards);
                }
            }

            @Override
            public LeaderboardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.leaderboard_layout, parent, false);
                return new LeaderboardViewHolder(view);
            }

            @Override
            public void onBindViewHolder(LeaderboardViewHolder holder, int position) {
                LeaderboardItem item = leaderboardItemList.get(position);

                if (item != null) {
                    holder.usernameTextView.setText(item.getUsername());
                    holder.scoreTextView.setText("Score: " + item.getScore());
                    holder.profileImageView.setImageBitmap(item.getImage());
                }
            }

            @Override
            public int getItemCount() {
                return leaderboardItemList.size();
            }
        }
