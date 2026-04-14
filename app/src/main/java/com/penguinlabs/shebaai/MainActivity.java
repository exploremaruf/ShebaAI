package com.penguinlabs.shebaai;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShebaAI_Log";
    private static final int MAX_PROMPT_CHARS = 1000;

    private LlmInference llmInference;
    private TextView statusText;
    private EditText userInput;
    private FloatingActionButton sendButton;
    private LinearLayout chatContainer;
    private NestedScrollView chatScrollView;

    private MaterialCardView loadingAnimationContainer;
    private ImageView loadingHeartView;
    private Animation pulseAnimation;

    private String healthKnowledgeBase;

    private boolean isGenerating = false;
    private Handler typewriterHandler = new Handler(Looper.getMainLooper());
    private Runnable typewriterRunnable;
    private Thread aiThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        userInput = findViewById(R.id.userInput);
        sendButton = findViewById(R.id.sendButton);
        chatContainer = findViewById(R.id.chatContainer);
        chatScrollView = findViewById(R.id.main_scrollview);
        loadingAnimationContainer = findViewById(R.id.loadingAnimationContainer);
        loadingHeartView = findViewById(R.id.loadingHeartView);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            }
        });

        pulseAnimation = AnimationUtils.loadAnimation(this, R.anim.pulse);
        healthKnowledgeBase = loadJSONFromAsset("symptoms.json");

        initAI();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGenerating) {
                    stopGeneration();
                } else {
                    String input = userInput.getText().toString().trim();
                    if (!input.isEmpty()) {
                        handleInput(input);
                    }
                }
            }
        });
    }

    private void handleInput(String input) {
        addMessageBubble("You", input);
        userInput.setText("");
        hideKeyboard();

        final String lowerInput = input.toLowerCase().trim();

        if (isGreeting(lowerInput)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    addMessageBubble("ShebaAI", getGreetingResponse(lowerInput));
                }
            }, 400);
            return;
        }

        showLoading();
        generateAIResponse(input);
    }

    private void generateAIResponse(final String question) {
        aiThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String context = getRelevantKnowledge(question);
                    String finalPrompt = "System: You are ShebaAI, a first-aid assistant...\n" +
                            "User: " + question + "\nAnswer:";

                    if (llmInference != null && isGenerating) {
                        final String response = llmInference.generateResponse(finalPrompt);

                        if (!isGenerating) return;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopLoading();
                                addMessageBubble("ShebaAI", response.trim());
                            }
                        });
                    }
                } catch (Exception e) {
                    if (isGenerating) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                stopLoading();
                                addMessageBubble("ShebaAI", "Error processing request.");
                            }
                        });
                    }
                }
            }
        });
        aiThread.start();
    }

    private void addMessageBubble(final String sender, final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MaterialCardView card = new MaterialCardView(MainActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 32);

                if (sender.equals("You")) {
                    params.gravity = android.view.Gravity.END;
                    card.setCardBackgroundColor(0xFF2C3E50);
                    card.setRadius(45f);
                } else {
                    params.gravity = android.view.Gravity.START;
                    card.setCardBackgroundColor(0xFFF1F3F4);
                    card.setRadius(45f);
                }

                card.setLayoutParams(params);
                card.setCardElevation(2f);

                final TextView tv = new TextView(MainActivity.this);
                tv.setPadding(35, 25, 35, 25);
                tv.setTextSize(15);
                tv.setTextColor(sender.equals("You") ? 0xFFFFFFFF : 0xFF202124);

                card.addView(tv);
                chatContainer.addView(card);

                if (sender.equals("ShebaAI")) {
                    isGenerating = true;
                    sendButton.setImageResource(android.R.drawable.ic_media_pause);

                    final int[] i = {0};
                    typewriterRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (isGenerating && i[0] < message.length()) {
                                tv.append(String.valueOf(message.charAt(i[0])));
                                i[0]++;
                                typewriterHandler.postDelayed(this, 12);
                                chatScrollView.fullScroll(View.FOCUS_DOWN);
                            } else {
                                resetGenerationUI();
                            }
                        }
                    };
                    typewriterHandler.post(typewriterRunnable);
                } else {
                    tv.setText(message);
                    chatScrollView.fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }

    private void stopGeneration() {
        isGenerating = false;

        if (aiThread != null && aiThread.isAlive()) {
            aiThread.interrupt();
        }

        if (typewriterHandler != null && typewriterRunnable != null) {
            typewriterHandler.removeCallbacks(typewriterRunnable);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopLoading();
                resetGenerationUI();
                addMessageBubble("ShebaAI", "... [Generation Stopped]");
            }
        });
    }

    private void resetGenerationUI() {
        isGenerating = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendButton.setImageResource(android.R.drawable.ic_menu_send);
            }
        });
    }

    private boolean isGreeting(String input) {
        String[] greets = {"hi", "hello", "hey", "morning", "afternoon", "evening", "how are you", "who are you", "thanks", "thank you"};
        for (String s : greets) if (input.contains(s)) return true;
        return false;
    }

    private String getGreetingResponse(String input) {
        if (input.contains("how are you"))
            return "I am ready to help! Please describe your injury.";
        if (input.contains("who are you")) return "I am ShebaAI, your offline first-aid assistant.";
        if (input.contains("thank")) return "You are welcome! Stay safe.";
        return "Hello! I am ShebaAI. Tell me about your medical emergency.";
    }

    private void initAI() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String modelPath = getModelPath("model.bin");
                    LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                            .setModelPath(modelPath)
                            .setMaxTokens(512)
                            .setTemperature(0.1f)
                            .build();
                    llmInference = LlmInference.createFromOptions(MainActivity.this, options);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("● ShebaAI Online");
                            statusText.setTextColor(0xFF4CAF50);
                            sendButton.setEnabled(true);
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusText.setText("● Offline Engine Error");
                        }
                    });
                }
            }
        }).start();
    }

    private String getRelevantKnowledge(String question) {
        if (healthKnowledgeBase == null) return "";
        String lowerQuestion = question.toLowerCase();
        StringBuilder context = new StringBuilder();
        try {
            JSONArray jsonArray = new JSONArray(healthKnowledgeBase);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject entry = jsonArray.getJSONObject(i);
                if (lowerQuestion.contains(entry.optString("name").toLowerCase())) {
                    context.append(entry.optString("name")).append(": ").append(entry.optJSONArray("first_aid_steps").toString());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        return context.toString();
    }

    private void showLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isGenerating = true;
                loadingAnimationContainer.setVisibility(View.VISIBLE);
                loadingHeartView.startAnimation(pulseAnimation);
                sendButton.setImageResource(android.R.drawable.ic_media_pause);
                sendButton.setEnabled(true);
            }
        });
    }

    private void stopLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loadingHeartView.clearAnimation();
                loadingAnimationContainer.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }
        });
    }

    private String getModelPath(String modelName) {
        File file = new File(getFilesDir(), modelName);
        if (!file.exists()) {
            try (InputStream is = getAssets().open(modelName);
                 OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) > 0) os.write(buffer, 0, len);
            } catch (IOException e) {
                Log.e(TAG, "Model Copy Error");
            }
        }
        return file.getAbsolutePath();
    }

    private String loadJSONFromAsset(String filename) {
        try {
            InputStream is = getAssets().open(filename);
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (llmInference != null) llmInference.close();
    }
}