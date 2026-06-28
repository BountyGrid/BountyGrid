package com.bountygrid.mobile;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final String PREFS = "bountygrid";
    private static final int PRIMARY = Color.rgb(15, 118, 110);
    private static final int BG = Color.rgb(247, 248, 251);
    private static final int INK = Color.rgb(23, 32, 42);
    private static final int MUTED = Color.rgb(102, 112, 133);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private SharedPreferences prefs;
    private LinearLayout content;
    private String apiBase;
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        apiBase = prefs.getString("apiBase", "http://10.0.2.2:8080/api");
        token = prefs.getString("token", "");
        showShell();
        if (token.isEmpty()) {
            showAuth();
        } else {
            showHome();
        }
    }

    private void showShell() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER_VERTICAL);
        nav.setPadding(dp(10), dp(8), dp(10), dp(8));
        nav.setBackgroundColor(Color.WHITE);

        nav.addView(navButton("Home", v -> showHome()));
        nav.addView(navButton("Post", v -> showPostAlert()));
        nav.addView(navButton("Wallet", v -> showWallet()));
        nav.addView(navButton("More", v -> showMore()));
        root.addView(nav, new LinearLayout.LayoutParams(-1, dp(58)));

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(18), dp(18), dp(18), dp(32));
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
    }

    private Button navButton(String label, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextColor(INK);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setOnClickListener(listener);
        button.setAllCaps(false);
        button.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1));
        return button;
    }

    private void showAuth() {
        clear("BountyGrid");
        paragraph("Geo-targeted lost and found alerts with rewards, tips, wallet escrow, SOS, and recovery stories.");

        EditText base = input("API base URL", apiBase, InputType.TYPE_CLASS_TEXT);
        EditText email = input("Email", "", InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText password = input("Password", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        EditText name = input("Name for registration", "", InputType.TYPE_CLASS_TEXT);
        EditText city = input("City", "", InputType.TYPE_CLASS_TEXT);

        content.addView(primaryButton("Log in", v -> {
            saveBase(base.getText().toString());
            JSONObject body = new JSONObject();
            put(body, "email", email.getText().toString());
            put(body, "password", password.getText().toString());
            request("POST", "/auth/login", body, json -> saveTokenAndOpen(json.getString("token")));
        }));

        content.addView(button("Register", v -> {
            saveBase(base.getText().toString());
            JSONObject body = new JSONObject();
            put(body, "name", name.getText().toString());
            put(body, "email", email.getText().toString());
            put(body, "password", password.getText().toString());
            put(body, "city", city.getText().toString());
            request("POST", "/auth/register", body, json -> saveTokenAndOpen(json.getString("token")));
        }));
    }

    private void showHome() {
        clear("Nearby alerts");
        paragraph("Default emulator backend: " + apiBase);
        EditText lat = input("Latitude", "19.07", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText lng = input("Longitude", "72.88", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        content.addView(primaryButton("Load nearby alerts", v -> {
            String path = "/alerts/nearby?lat=" + lat.getText() + "&lng=" + lng.getText() + "&radius=25";
            requestArray("GET", path, null, this::renderAlerts);
        }));
        content.addView(button("Log out", v -> {
            prefs.edit().remove("token").apply();
            token = "";
            showAuth();
        }));
    }

    private void renderAlerts(JSONArray alerts) {
        clear("Nearby alerts");
        content.addView(primaryButton("Refresh", v -> showHome()));
        if (alerts.length() == 0) {
            paragraph("No active alerts found near this location.");
            return;
        }
        for (int i = 0; i < alerts.length(); i++) {
            JSONObject alert = alerts.optJSONObject(i);
            if (alert == null) continue;
            card(alert.optString("title", "Alert"),
                    alert.optString("description", "")
                            + "\nCategory: " + alert.optString("category", "")
                            + "\nReward: " + alert.optDouble("rewardAmount", 0)
                            + "\nDistance: " + String.format("%.2f km", alert.optDouble("distanceKm", 0)));
        }
    }

    private void showPostAlert() {
        clear("Post alert");
        EditText title = input("Title", "", InputType.TYPE_CLASS_TEXT);
        EditText description = input("Description", "", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        EditText category = input("Category: PET, ELECTRONICS, BAGS, KEYS, WALLETS, VEHICLES, ACCESSORIES, OTHER", "OTHER", InputType.TYPE_CLASS_TEXT);
        EditText type = input("Type: LOST or FOUND", "LOST", InputType.TYPE_CLASS_TEXT);
        EditText lat = input("Latitude", "19.07", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText lng = input("Longitude", "72.88", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        EditText city = input("City", "Mumbai", InputType.TYPE_CLASS_TEXT);
        EditText reward = input("Reward amount", "0", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        content.addView(primaryButton("Create alert", v -> {
            JSONObject body = new JSONObject();
            put(body, "title", title.getText().toString());
            put(body, "description", description.getText().toString());
            put(body, "alertType", type.getText().toString().trim().toUpperCase());
            put(body, "category", category.getText().toString().trim().toUpperCase());
            put(body, "latitude", number(lat));
            put(body, "longitude", number(lng));
            put(body, "city", city.getText().toString());
            put(body, "radiusKm", 5.0);
            put(body, "rewardAmount", number(reward));
            request("POST", "/alerts", body, json -> {
                toast("Alert created");
                showHome();
            });
        }));
    }

    private void showWallet() {
        clear("Wallet");
        request("GET", "/wallet", null, json -> {
            clear("Wallet");
            TextView balance = title("Balance: " + json.optDouble("balance", 0));
            balance.setTextSize(26);
            content.addView(balance);
            EditText amount = input("Amount", "100", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            content.addView(primaryButton("Deposit", v -> request("POST", "/wallet/deposit?amount=" + amount.getText(), null, r -> showWallet())));
            content.addView(button("Withdraw", v -> request("POST", "/wallet/withdraw?amount=" + amount.getText(), null, r -> showWallet())));
            content.addView(button("Transactions", v -> requestArray("GET", "/wallet/transactions", null, this::renderTransactions)));
        });
    }

    private void renderTransactions(JSONArray transactions) {
        clear("Transactions");
        content.addView(primaryButton("Back to wallet", v -> showWallet()));
        for (int i = 0; i < transactions.length(); i++) {
            JSONObject tx = transactions.optJSONObject(i);
            if (tx != null) {
                card(tx.optString("type"), tx.optString("description") + "\nAmount: " + tx.optDouble("amount"));
            }
        }
    }

    private void showMore() {
        clear("More");
        content.addView(primaryButton("Leaderboard", v -> requestArray("GET", "/leaderboard", null, this::renderLeaderboard)));
        content.addView(button("SOS broadcasts", v -> requestArray("GET", "/sos/active", null, this::renderSos)));
        content.addView(button("Recovery stories", v -> requestArray("GET", "/stories", null, this::renderStories)));
        content.addView(button("Profile", v -> request("GET", "/auth/me", null, this::renderProfile)));
    }

    private void renderLeaderboard(JSONArray users) {
        clear("Leaderboard");
        content.addView(primaryButton("Back", v -> showMore()));
        for (int i = 0; i < users.length(); i++) {
            JSONObject user = users.optJSONObject(i);
            if (user != null) {
                card((i + 1) + ". " + user.optString("name"), "Points: " + user.optInt("points") + "\nBadge: " + user.optString("currentBadge"));
            }
        }
    }

    private void renderSos(JSONArray broadcasts) {
        clear("SOS broadcasts");
        content.addView(primaryButton("Back", v -> showMore()));
        for (int i = 0; i < broadcasts.length(); i++) {
            JSONObject item = broadcasts.optJSONObject(i);
            JSONObject alert = item == null ? null : item.optJSONObject("alert");
            if (alert != null) {
                card(alert.optString("title", "SOS alert"), "Radius: " + item.optDouble("radiusKm") + " km");
            }
        }
    }

    private void renderStories(JSONArray stories) {
        clear("Recovery stories");
        content.addView(primaryButton("Back", v -> showMore()));
        for (int i = 0; i < stories.length(); i++) {
            JSONObject story = stories.optJSONObject(i);
            if (story != null) {
                card(story.optString("title"), story.optString("story") + "\nHearts: " + story.optInt("hearts") + " Claps: " + story.optInt("claps"));
            }
        }
    }

    private void renderProfile(JSONObject user) {
        clear("Profile");
        card(user.optString("name"), user.optString("email") + "\nPoints: " + user.optInt("points") + "\nBadge: " + user.optString("currentBadge"));
        content.addView(primaryButton("Back", v -> showMore()));
    }

    private void request(String method, String path, JSONObject body, JsonHandler handler) {
        executor.execute(() -> {
            try {
                String response = http(method, path, body);
                JSONObject json = response.isEmpty() ? new JSONObject() : new JSONObject(response);
                main.post(() -> handleJson(handler, json));
            } catch (Exception ex) {
                main.post(() -> toast(ex.getMessage()));
            }
        });
    }

    private void requestArray(String method, String path, JSONObject body, ArrayHandler handler) {
        executor.execute(() -> {
            try {
                JSONArray array = new JSONArray(http(method, path, body));
                main.post(() -> handler.handle(array));
            } catch (Exception ex) {
                main.post(() -> toast(ex.getMessage()));
            }
        });
    }

    private String http(String method, String path, JSONObject body) throws Exception {
        URL url = new URL(apiBase + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setRequestProperty("Accept", "application/json");
        if (!token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        if (body != null) {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }
        }
        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        String response = read(stream);
        if (code >= 400) {
            throw new IllegalStateException(response.isEmpty() ? "HTTP " + code : response);
        }
        return response;
    }

    private String read(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private void saveBase(String value) {
        apiBase = value.trim();
        prefs.edit().putString("apiBase", apiBase).apply();
    }

    private void saveTokenAndOpen(String nextToken) {
        token = nextToken;
        prefs.edit().putString("token", token).apply();
        showHome();
    }

    private void handleJson(JsonHandler handler, JSONObject json) {
        try {
            handler.handle(json);
        } catch (Exception ex) {
            toast(ex.getMessage());
        }
    }

    private void clear(String heading) {
        content.removeAllViews();
        content.addView(title(heading));
    }

    private TextView title(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(INK);
        view.setTextSize(24);
        view.setGravity(Gravity.START);
        view.setPadding(0, dp(8), 0, dp(12));
        view.setTypeface(null, 1);
        return view;
    }

    private void paragraph(String text) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(MUTED);
        view.setTextSize(15);
        view.setLineSpacing(0, 1.15f);
        view.setPadding(0, 0, 0, dp(14));
        content.addView(view);
    }

    private EditText input(String hint, String value, int type) {
        EditText editText = new EditText(this);
        editText.setHint(hint);
        editText.setText(value);
        editText.setInputType(type);
        editText.setSingleLine((type & InputType.TYPE_TEXT_FLAG_MULTI_LINE) == 0);
        editText.setTextColor(INK);
        editText.setHintTextColor(MUTED);
        editText.setPadding(dp(12), dp(8), dp(12), dp(8));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), 0, dp(8));
        editText.setLayoutParams(params);
        content.addView(editText);
        return editText;
    }

    private Button primaryButton(String text, View.OnClickListener listener) {
        Button button = button(text, listener);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(PRIMARY);
        return button;
    }

    private Button button(String text, View.OnClickListener listener) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setOnClickListener(listener);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(48));
        params.setMargins(0, dp(6), 0, dp(8));
        button.setLayoutParams(params);
        return button;
    }

    private void card(String heading, String body) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackgroundColor(Color.WHITE);
        TextView h = new TextView(this);
        h.setText(heading);
        h.setTextColor(INK);
        h.setTextSize(18);
        h.setTypeface(null, 1);
        TextView b = new TextView(this);
        b.setText(body);
        b.setTextColor(MUTED);
        b.setTextSize(14);
        b.setPadding(0, dp(6), 0, 0);
        card.addView(h);
        card.addView(b);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, dp(8));
        content.addView(card, params);
    }

    private double number(EditText input) {
        String text = input.getText().toString().trim();
        return text.isEmpty() ? 0 : Double.parseDouble(text);
    }

    private void put(JSONObject object, String key, Object value) {
        try {
            object.put(key, value);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void toast(String message) {
        Toast.makeText(this, message == null ? "Something went wrong" : message, Toast.LENGTH_LONG).show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private interface JsonHandler {
        void handle(JSONObject json) throws Exception;
    }

    private interface ArrayHandler {
        void handle(JSONArray array);
    }
}
