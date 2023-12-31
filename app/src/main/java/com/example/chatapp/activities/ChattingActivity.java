package com.example.chatapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.chatapp.R;
import com.example.chatapp.adapters.ChtAdapt;
import com.example.chatapp.databinding.ActivityChattingBinding;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.Users;
import com.example.chatapp.utilities.Constants;
import com.example.chatapp.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChattingActivity extends AppCompatActivity {

    private ActivityChattingBinding binding;
    private Users receiverUsers;
    private ChtAdapt chtAdapt;
    private List<Message> messageList;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;
    public String IdConvo = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChattingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiver();
        initialize();
        listenMsg();
    }

    private void setListeners(){
        binding.backImg.setOnClickListener(v -> onBackPressed());
        binding.sendCht.setOnClickListener(v -> SendingMsg());
    }

    private void initialize(){
        preferenceManager = new PreferenceManager(getApplicationContext());
        messageList = new ArrayList<>();
        chtAdapt = new ChtAdapt(messageList, preferenceManager.getString(Constants.KEY_USER_ID));
        binding.chatRecyclerView.setAdapter(chtAdapt);
        db = FirebaseFirestore.getInstance();
    }

    //when hitting the send button
    private void SendingMsg(){
        //hash map for storing the data
        HashMap<String, Object> msg = new HashMap<>();
        msg.put(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID));
        msg.put(Constants.KEY_RECEIVER, receiverUsers.id);
        msg.put(Constants.KEY_MSG, binding.TypeMsg.getText().toString());
        msg.put(Constants.KEY_TIMESTAMP, new Date());
        db.collection(Constants.KEY_CHAT_COLL).add(msg);

        //if there's already a convo between two users
        if(IdConvo != null){
            updateHistoryConvo(binding.TypeMsg.getText().toString());
        }
        else {
            // Create a new conversation document in Firestore
            HashMap<String, Object> convo = new HashMap<>();
            convo.put(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID));
            convo.put(Constants.KEY_NAME_SENDER, preferenceManager.getString(Constants.KEY_NAME));
            convo.put(Constants.KEY_IMG_SENDER, preferenceManager.getString(Constants.KEY_IMAGE));
            convo.put(Constants.KEY_RECEIVER, receiverUsers.id);
            convo.put(Constants.KEY_NAME_RECEIVER, receiverUsers.name);
            convo.put(Constants.KEY_IMG_RECEIVER, receiverUsers.img);
            convo.put(Constants.KEY_RECENT_CONVO, binding.TypeMsg.getText().toString());
            convo.put(Constants.KEY_TIMESTAMP, new Date());
            addHistoryConvo(convo);

        }
        // Clear the message input field
        binding.TypeMsg.setText(null);
    }

    private void listenMsg(){
        // Listen for new messages sent by the current user
        db.collection(Constants.KEY_CHAT_COLL)
                .whereEqualTo(Constants.KEY_SENDER, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER, receiverUsers.id)
                .addSnapshotListener(eventListener);
        // Listen for new messages received by the current user
        db.collection(Constants.KEY_CHAT_COLL)
                .whereEqualTo(Constants.KEY_SENDER,receiverUsers.id)
                .whereEqualTo(Constants.KEY_RECEIVER, preferenceManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) ->{
        if(error != null){
            return;
        }
        if(value != null){
            int cnt = messageList.size();
            for(DocumentChange documentChange : value.getDocumentChanges()){
                if(documentChange.getType() == DocumentChange.Type.ADDED) {
                    // Retrieve the message details from Firestore
                    Message message = new Message();
                    message.IdSend = documentChange.getDocument().getString(Constants.KEY_SENDER);
                    message.IdRecive = documentChange.getDocument().getString(Constants.KEY_RECEIVER);
                    message.dateTime = TimeFormat(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    message.msg = documentChange.getDocument().getString(Constants.KEY_MSG);
                    message.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    messageList.add(message);


                }
            }
            // Sort the message list based on the timestamp
            Collections.sort(messageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if(cnt == 0){
                // If this is the first set of messages, notify the adapter
                chtAdapt.notifyDataSetChanged();
            }else {
                // If there were already messages, notify the adapter about the new items
                chtAdapt.notifyItemRangeInserted(messageList.size(), messageList.size());
                // Scroll the RecyclerView to the last position
                binding.chatRecyclerView.smoothScrollToPosition(messageList.size()-1);
            }
            // Make the chat RecyclerView visible
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if(IdConvo == null){
            // Check if a conversation document exists between the two users
            ConvoChecking();
        }
    };

    //create the past convo
    private void addHistoryConvo(HashMap<String, Object> convo){
        db.collection(Constants.KEY_CONVERSATIONS_COLL)
                .add(convo)
                .addOnSuccessListener(documentReference -> IdConvo = documentReference.getId());
    }

    private void updateHistoryConvo(String msg){
        DocumentReference documentReference = db.collection(Constants.KEY_CONVERSATIONS_COLL).document(IdConvo);
        documentReference.update(Constants.KEY_RECENT_CONVO, msg, Constants.KEY_TIMESTAMP, new Date());
    }

    //checking the convo from both perspectives.
    private void ConvoChecking(){
        if(messageList.size()!=0){
            ConvoCheckingRemotely(preferenceManager.getString(Constants.KEY_USER_ID), receiverUsers.id);
            ConvoCheckingRemotely(receiverUsers.id, preferenceManager.getString(Constants.KEY_USER_ID));

        }

    }

    //checking if the database has the convo collection or if convo exists between two users
    private void ConvoCheckingRemotely(String IdSender, String IdReceiver){
        db.collection(Constants.KEY_CONVERSATIONS_COLL)
                .whereEqualTo(Constants.KEY_SENDER, IdSender)
                .whereEqualTo(Constants.KEY_RECEIVER, IdReceiver)
                .get()
                .addOnCompleteListener(convoCompleteListener);
    }

    // Listener for checking the conversation document completion
    private final OnCompleteListener<QuerySnapshot> convoCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            IdConvo = documentSnapshot.getId();
        }
    };

    //to set the time and date
    public String TimeFormat(Date date){
        return new SimpleDateFormat("MMMM, dd, yy - hh:mm", Locale.getDefault()).format(date);
    }
    private void loadReceiver(){
        receiverUsers = (Users) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.userName.setText(receiverUsers.name);
    }




}