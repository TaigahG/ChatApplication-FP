package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.adapters.UserAdapt;
import com.example.chatapp.databinding.ActivityUserBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.Users;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.List;

public class UserActivity extends AppCompatActivity implements UserListener {

    private ActivityUserBinding binding;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.backImg.setOnClickListener(v -> onBackPressed());
    }

    //get the user data
    private void getUsers(){
        Loading(true);
        // Access Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    Loading(false);
                    String currUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult() != null){
                        List<Users> users = new ArrayList<>();
                        // Iterate over the retrieved documents
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if(currUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            // Extract user information from the document
                            Users user = new Users();
                            user.id = queryDocumentSnapshot.getId();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.img = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            users.add(user);
                        }
                        if(users.size() > 0){
                            UserAdapt userAdapt = new UserAdapt(users, this);
                            binding.usersRecycleReview.setAdapter(userAdapt);
                            binding.usersRecycleReview.setVisibility(View.VISIBLE);
                        }
                        else{
                            errors();
                        }
                    }
                    else {
                        errors();
                    }
                });
    }

    private void errors(){
        binding.txtError.setText(String.format("%s", "User is not found"));
        binding.txtError.setVisibility(View.VISIBLE);
    }

    private void Loading(Boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
        intent.putExtra(Constants.KEY_USER, users);
        startActivity(intent);
        finish();
    }
}