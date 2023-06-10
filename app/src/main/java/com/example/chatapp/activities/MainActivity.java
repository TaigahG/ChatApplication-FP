package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.example.chatapp.R;
import com.example.chatapp.adapters.RecentConvoAdapt;
import com.example.chatapp.databinding.ActivityMainBinding;
import com.example.chatapp.databinding.ActivitySignUpBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.Users;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements UserListener {
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private List<Message> recentConversations;
    private RecentConvoAdapt recentConvoAdapt;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        userDetails();
        initialize();
        getToken();
        setListeners();
        listenerConvo();
    }

    private void setListeners(){
        binding.newChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), UserActivity.class)));
        binding.SignoutBtn.setOnClickListener(v -> SignOut());
    }

    private void initialize(){
        recentConversations = new ArrayList<>();
        recentConvoAdapt = new RecentConvoAdapt(recentConversations, this);
        binding.convoRecycleReview.setAdapter(recentConvoAdapt);
        db = FirebaseFirestore.getInstance();
    }

    private void listenerConvo(){
        db.collection(Constants.KEY_CONVERSATIONS_COLL)
                .whereEqualTo(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        db.collection(Constants.KEY_CONVERSATIONS_COLL)
                .whereEqualTo(Constants.KEY_RECEIVER, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if(error != null){
            return;
        }
        if(value != null){
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED){
                    String IDSender = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    String IDReceiver = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    Message message = new Message();
                    message.IdSend = IDSender;
                    message.IdRecive = IDReceiver;

                    if(preferenceManager.getString(Constants.KEY_USER_ID).equals(IDSender)){
                        message.UserConvoName = documentChange.getDocument().getString(Constants.KEY_NAME_RECEIVER);
                        message.UserConvoImg = documentChange.getDocument().getString(Constants.KEY_IMG_RECEIVER);
                        message.UserConvoId = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    }
                    else {
                        message.UserConvoName = documentChange.getDocument().getString(Constants.KEY_NAME_SENDER);
                        message.UserConvoImg = documentChange.getDocument().getString(Constants.KEY_IMG_SENDER);
                        message.UserConvoId = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    }
                    message.msg = documentChange.getDocument().getString(Constants.KEY_RECENT_CONVO);
                    message.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    recentConversations.add(message);
                } else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i = 0; i<recentConversations.size(); i++){
                        String IDSender = documentChange.getDocument().getString(Constants.KEY_SENDER);
                        String IDReceiver = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                        if(recentConversations.get(i).IdSend.equals(IDSender) && recentConversations.get(i).IdRecive.equals(IDReceiver)){
                            recentConversations.get(i).msg = documentChange.getDocument().getString(Constants.KEY_RECENT_CONVO);
                            recentConversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(recentConversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            recentConvoAdapt.notifyDataSetChanged();
            binding.convoRecycleReview.smoothScrollToPosition(0);
            binding.convoRecycleReview.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);

        }
    };

    public void userDetails(){
        binding.userName.setText(preferenceManager.getString(Constants.KEY_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        binding.imgProfile.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void FCMToken(String token){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));

    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::FCMToken);
    }

    private void SignOut(){
        showToast("Signing Out...");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                db.collection(Constants.KEY_COLLECTION_USERS).document(preferenceManager.getString(Constants.KEY_USER_ID));

        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                    finish();
                });
    }


    @Override
    public void onUserClicked(Users users) {
        Intent intent = new Intent(getApplicationContext(), ChattingActivity.class);
        intent.putExtra(Constants.KEY_USER, users);
        startActivity(intent);
    }
}