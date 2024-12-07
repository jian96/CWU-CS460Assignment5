package com.example.signin.adapters;

import android.graphics.Bitmap;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.signin.databinding.ItemContainerReceivedMessagesBinding;
import com.example.signin.databinding.ItemContainerSentMessagesBinding;
import com.example.signin.models.ChatMessage;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Bitmap receiverProfileImage;
    private final List<ChatMessage> chatMessages;
    private final String sendID;

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String sendID) {
        this.receiverProfileImage = receiverProfileImage;
        this.chatMessages = chatMessages;
        this.sendID = sendID;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(ItemContainerSentMessagesBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceiverMessageViewHolder(ItemContainerReceivedMessagesBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        } else {
            ((ReceiverMessageViewHolder)holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public int getViewType(int position){
        if (chatMessages.get(position).senderID.equals(sendID)){
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }


    static class SentMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerSentMessagesBinding binding;
        public SentMessageViewHolder(ItemContainerSentMessagesBinding itemContainerSentMessagesBinding) {
            super(itemContainerSentMessagesBinding.getRoot());
            binding = itemContainerSentMessagesBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTimestamp.setText(chatMessage.dateTime);
        }
    }

    static class ReceiverMessageViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceivedMessagesBinding binding;
        public ReceiverMessageViewHolder(ItemContainerReceivedMessagesBinding itemContainerReceivedMessagesBinding) {
            super(itemContainerReceivedMessagesBinding.getRoot());
            binding = itemContainerReceivedMessagesBinding;
        }
        void setData(ChatMessage chatMessage, Bitmap receiverProfileImage){
            binding.textMessage.setText(chatMessage.message);
            binding.textDateTimestamp.setText(chatMessage.dateTime);
            binding.imageProfile.setImageBitmap(receiverProfileImage);
        }
    }

}
