package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.UserContainerBinding;
import com.example.chatapp.databinding.UserContainerRecentChatBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.Message;
import com.example.chatapp.models.Users;
import com.google.firebase.firestore.auth.User;

import java.util.List;

public class RecentConvoAdapt extends RecyclerView.Adapter<RecentConvoAdapt.ConvoViewHolder> {

    private final List<Message> messageList;
    private final UserListener userListener;


    public RecentConvoAdapt(List<Message> messageList, UserListener userListener) {
        this.messageList = messageList;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public ConvoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConvoViewHolder(
                UserContainerRecentChatBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConvoViewHolder holder, int position) {
        holder.setData(messageList.get(position));

    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class ConvoViewHolder extends RecyclerView.ViewHolder{
        UserContainerRecentChatBinding binding;
        ConvoViewHolder(UserContainerRecentChatBinding userContainerRecentChatBinding){
            super(userContainerRecentChatBinding.getRoot());
            binding = userContainerRecentChatBinding;
        }

        void setData(Message message){
            binding.imgProfile.setImageBitmap(userImg(message.UserConvoImg));
            binding.userName.setText(message.UserConvoName);
            binding.recentConvo.setText(message.msg);
            binding.getRoot().setOnClickListener(v -> {
                Users users = new Users();
                users.id = message.UserConvoId;
                users.img = message.UserConvoImg;
                users.name = message.UserConvoName;
                userListener.onUserClicked(users);

            });
        }
    }


    private Bitmap userImg(String encodedImg){
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
}
