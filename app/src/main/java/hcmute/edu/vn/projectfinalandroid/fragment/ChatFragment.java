package hcmute.edu.vn.projectfinalandroid.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.projectfinalandroid.R;
import hcmute.edu.vn.projectfinalandroid.adapter.ChatAdapter;
import hcmute.edu.vn.projectfinalandroid.controller.ChatController;
import hcmute.edu.vn.projectfinalandroid.model.ChatMessage;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;
    private EditText editMessage;
    private ImageButton btnSend;
    private List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerMessages);
        editMessage = view.findViewById(R.id.editMessage);
        btnSend = view.findViewById(R.id.btnSend);

        adapter = new ChatAdapter(messages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        btnSend.setOnClickListener(v -> {
            String userText = editMessage.getText().toString().trim();
            if (userText.isEmpty()) return;

            // Thêm tin nhắn người dùng vào danh sách
            ChatMessage userMessage = new ChatMessage(1, "user", userText);
            messages.add(userMessage);
            adapter.notifyItemInserted(messages.size() - 1);
            recyclerView.scrollToPosition(messages.size() - 1);
            editMessage.setText("");

            // Gửi tin nhắn tới ChatController
            ChatController.sendMessage(userText, new ChatController.ChatBotCallback() {
                @Override
                public void onResponse(String message) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        // Thêm tin nhắn phản hồi từ bot
                        ChatMessage botMessage = new ChatMessage(1, "assistant", message);
                        messages.add(botMessage);
                        adapter.notifyItemInserted(messages.size() - 1);
                        recyclerView.scrollToPosition(messages.size() - 1);
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Lỗi: " + errorMessage, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        return view;
    }
}
