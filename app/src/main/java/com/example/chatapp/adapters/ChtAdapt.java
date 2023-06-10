package com.example.chatapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapp.databinding.RecievedMessageContainerBinding;
import com.example.chatapp.databinding.SentMessageContainerBinding;
import com.example.chatapp.models.Message;

import java.util.List;

public class ChtAdapt extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<Message> messages;
    private final String IdSend;

    public ChtAdapt(List<Message> messages, String idSend) {
        this.messages = messages;
        IdSend = idSend;
    }

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(SentMessageContainerBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            )
            );
        }else {
            return new RecieveMessageViewHolder(
                    RecievedMessageContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder) holder).setData(messages.get(position));
        }else{
            ((RecieveMessageViewHolder) holder).setData(messages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(messages.get(position).IdSend.equals(IdSend)){
            return VIEW_TYPE_SENT;
        }
        else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final SentMessageContainerBinding binding;

        SentMessageViewHolder(SentMessageContainerBinding sentMessageContainerBinding){
            super(sentMessageContainerBinding.getRoot());
            binding = sentMessageContainerBinding;
        }

        void setData(Message message){
            binding.txtMessages.setText(message.msg);
            binding.dateTime.setText(message.dateTime);
        }
    }

    static class RecieveMessageViewHolder extends RecyclerView.ViewHolder{
        private final RecievedMessageContainerBinding binding;

        RecieveMessageViewHolder(RecievedMessageContainerBinding recievedMessageContainerBinding){
            super(recievedMessageContainerBinding.getRoot());
            binding = recievedMessageContainerBinding;
        }

        void setData(Message message){
            binding.txtMessages.setText(message.msg);
            binding.dateTime.setText(message.dateTime);
        }
    }
}
