package hcmute.edu.vn.projectfinalandroid.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.model.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_ASSISTANT = 2;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        if ("user".equalsIgnoreCase(message.getRole())) {
            return TYPE_USER;
        } else {
            return TYPE_ASSISTANT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_bot, parent, false);
            return new AssistantViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);

        // Lấy chiều rộng màn hình (pixel)
        int screenWidth = holder.itemView.getContext().getResources().getDisplayMetrics().widthPixels;
        // Giới hạn tối đa 70% chiều rộng màn hình
        int maxWidthPx = (int) (screenWidth * 0.7);

        if (holder instanceof UserViewHolder) {
            UserViewHolder userHolder = (UserViewHolder) holder;
            userHolder.txtMessage.setText(message.getContent());
            userHolder.txtMessage.setMaxWidth(maxWidthPx); // set maxWidth
        } else if (holder instanceof AssistantViewHolder) {
            AssistantViewHolder assistantHolder = (AssistantViewHolder) holder;
            assistantHolder.txtMessage.setText(message.getContent());
            assistantHolder.txtMessage.setMaxWidth(maxWidthPx); // set maxWidth
        }
    }


    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.textMessageUser);
        }
    }

    static class AssistantViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        public AssistantViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.textMessageAssistant);

        }
    }
}
