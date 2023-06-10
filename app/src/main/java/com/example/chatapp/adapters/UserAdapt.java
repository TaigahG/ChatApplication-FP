package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.UserContainerBinding;
import com.example.chatapp.listeners.UserListener;
import com.example.chatapp.models.Users;

import java.util.List;

public class UserAdapt extends RecyclerView.Adapter<UserAdapt.UserViewHolder>{

    private final List<Users> users;
    private final UserListener userListener;

    public UserAdapt(List<Users> users, UserListener userListener) {
        this.users = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserContainerBinding userContainerBinding = UserContainerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserViewHolder(userContainerBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder{
        UserContainerBinding binding;
        UserViewHolder(UserContainerBinding userContainerBinding){
            super(userContainerBinding.getRoot());
            binding = userContainerBinding;
        }

        void setData(Users users){
            binding.userName.setText(users.name);
            binding.imgProfile.setImageBitmap(userImg(users.img));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(users));
        }
    }
    private Bitmap userImg(String encodedImg){
        byte[] bytes = Base64.decode(encodedImg, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

    }
}
