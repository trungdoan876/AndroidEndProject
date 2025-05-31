package hcmute.edu.vn.projectfinalandroid.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatController {
    private static final String API_URL = "https://api.deepseek.com/v1/chat/completions";
    private static final String API_KEY = "sk-64ec86526ab74f68948e90be4c612351";

    public interface ChatBotCallback {
        void onResponse(String message);
        void onError(String errorMessage);
    }

    public static void sendMessage(String userInput, ChatBotCallback callback) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        try {
            JSONArray messages = new JSONArray();

            // 1. System prompt: thiết lập vai trò AI
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content",
                    "Bạn tên là Lada, là một trợ lý dịch thuật. Khi người dùng gửi một đoạn văn, bạn phải tự động phát hiện ngôn ngữ gốc và làm theo yêu cầu người dùng. Trả lời lại bằng ngôn ngữ mà người dùng gửi đến nếu không được yêu cầu trả lời bằng ngôn ngữ khác.");
            messages.put(systemMessage);

            // 2. User prompt: nội dung người dùng thực sự nhập
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", userInput);
            messages.put(userMessage);

            // Gửi lên API
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", "deepseek-chat");
            jsonBody.put("messages", messages);

            RequestBody body = RequestBody.create(
                    MediaType.get("application/json; charset=utf-8"),
                    jsonBody.toString()
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            Handler mainHandler = new Handler(Looper.getMainLooper());

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e("ChatController", "Lỗi mạng: ", e);
                    mainHandler.post(() -> callback.onError("Lỗi mạng: " + e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) {
                    try {
                        Log.d("ChatController", "Response code: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            InputStream inputStream = response.body().byteStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }

                            String resBody = sb.toString();
                            JSONObject resJson = new JSONObject(resBody);
                            JSONArray choices = resJson.getJSONArray("choices");
                            JSONObject firstChoice = choices.getJSONObject(0);
                            JSONObject message = firstChoice.getJSONObject("message");
                            String result = message.getString("content");

                            mainHandler.post(() -> callback.onResponse(result));
                        } else {
                            mainHandler.post(() -> callback.onError("Lỗi từ server: " + response.message()));
                        }
                    } catch (Exception e) {
                        Log.e("ChatController", "Lỗi xử lý response: ", e);
                        mainHandler.post(() -> callback.onError("Lỗi xử lý response: " + e.getMessage()));
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("ChatController", "Lỗi tạo JSON: ", e);
            callback.onError("Lỗi tạo JSON: " + e.getMessage());
        }
    }
}
