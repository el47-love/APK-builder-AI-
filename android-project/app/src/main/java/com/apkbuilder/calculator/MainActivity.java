
package com.apkbuilder.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int blue = Color.rgb(33, 150, 243);
    private final int dark = Color.rgb(30, 40, 55);
    private final int pale = Color.rgb(240, 247, 255);

    private LinearLayout root, content;
    private SharedPreferences prefs;
    private final ArrayList<JSONObject> sales = new ArrayList<>();
    private final ArrayList<JSONObject> visits = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(blue);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        prefs = getSharedPreferences("sales_rep_ai", MODE_PRIVATE);
        loadData();
        buildShell();
        showDashboard();
    }

    private void buildShell() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setPadding(20, 16, 20, 16);
        header.setBackgroundColor(blue);

        TextView title = new TextView(this);
        title.setText("SALES REP AI");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        header.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Your sales. Your shops. Your progress.");
        subtitle.setTextColor(Color.WHITE);
        subtitle.setTextSize(13);
        header.addView(subtitle);
        root.addView(header);

        HorizontalScrollView navScroll = new HorizontalScrollView(this);
        navScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(4, 5, 4, 5);
        nav.setBackgroundColor(pale);

        String[] pages = {"Home", "Record Sale", "Plan Visit", "History"};
        Runnable[] actions = {
            () -> showDashboard(),
            () -> showSaleForm(),
            () -> showVisitForm(),
            () -> showHistory()
        };

        for (int i = 0; i < pages.length; i++) {
            final Runnable action = actions[i];
            Button b = new Button(this);
            b.setText(pages[i]);
            b.setTextSize(12);
            b.setAllCaps(false);
            b.setTextColor(blue);
            nav.addView(b);
            b.setOnClickListener(v -> action.run());
        }
        navScroll.addView(nav);
        root.addView(navScroll);

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(18, 16, 18, 24);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1f));

        setContentView(root);
    }

    private TextView text(String value, int size, int color,
                          boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD);
        t.setPadding(0, 7, 0, 7);
        return t;
    }

    private void heading(String value) {
        content.addView(text(value, 21, dark, true));
    }

    private EditText field(String hint, String type) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(15);
        e.setSingleLine(true);
        e.setPadding(12, 8, 12, 8);
        e.setBackgroundColor(pale);

        if (type.equals("number")) {
            e.setInputType(8194);
        } else if (type.equals("decimal")) {
            e.setInputType(8194 | 4096);
        } else {
            e.setInputType(1);
        }

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 5, 0, 7);
        content.addView(e, p);
        return e;
    }

    private void button(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(blue));
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);
        p.setMargins(0, 8, 0, 8);
        content.addView(b, p);
        b.setOnClickListener(v -> action.run());
    }

    private void gap() {
        View line = new View(this);
        line.setBackgroundColor(Color.LTGRAY);
        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, 1);
        p.setMargins(0, 10, 0, 10);
        content.addView(line, p);
    }

    private void showDashboard() {
        content.removeAllViews();
        heading("Welcome to Sales Rep AI");
        content.addView(text(
                "Manage your daily sales and shop visits in one place.",
                15, dark, false));

        double total = 0;
        for (JSONObject s : sales) {
            total += s.optDouble("amount", 0);
        }

        gap();
        content.addView(text("TOTAL SALES RECORDED",
                13, blue, true));
        content.addView(text("K" + money(total),
                30, dark, true));

        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.HORIZONTAL);

        stats.addView(statCard("Sales records",
                String.valueOf(sales.size())));
        stats.addView(statCard("Planned visits",
                String.valueOf(visits.size())));
        content.addView(stats);

        gap();
        heading("Quick Actions");
        button("＋ Record a Sale", () -> showSaleForm());
        button("＋ Plan a Shop Visit", () -> showVisitForm());
        button("View Sales & Visits", () -> showHistory());

        gap();
        heading("Upcoming Visit Plans");
        if (visits.isEmpty()) {
            content.addView(text(
                    "No visits planned yet. Add your first shop visit.",
                    14, dark, false));
        } else {
            for (JSONObject v : visits) {
                content.addView(text(
                        v.optString("shop") + " • " +
                        v.optString("date") + "\n" +
                        v.optString("notes"),
                        14, dark, false));
            }
        }
    }

    private View statCard(String label, String value) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(12, 12, 8, 12);
        card.setBackgroundColor(pale);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, -2, 1f);
        p.setMargins(0, 8, 8, 8);
        card.setLayoutParams(p);

        card.addView(text(value, 23, blue, true));
        card.addView(text(label, 12, dark, false));
        return card;
    }

    private void showSaleForm() {
        content.removeAllViews();
        heading("Record a Sale");
        content.addView(text(
                "Enter the details of a completed sale.",
                14, dark, false));

        EditText shop = field("Shop / customer name", "text");
        EditText product = field("Product sold", "text");
        EditText quantity = field("Quantity", "number");
        EditText amount = field("Total sale amount (K)", "decimal");

        button("SAVE SALE", () -> {
            String sh = shop.getText().toString().trim();
            String pr = product.getText().toString().trim();
            String q = quantity.getText().toString().trim();
            String a = amount.getText().toString().trim();

            if (sh.isEmpty() || pr.isEmpty() ||
                    q.isEmpty() || a.isEmpty()) {
                toast("Please complete all fields.");
                return;
            }

            try {
                int qty = Integer.parseInt(q);
                double value = Double.parseDouble(a);
                if (qty <= 0 || value < 0 ||
                        Double.isNaN(value) ||
                        Double.isInfinite(value)) {
                    toast("Enter a valid quantity and amount.");
                    return;
                }

                JSONObject record = new JSONObject();
                record.put("shop", sh);
                record.put("product", pr);
                record.put("quantity", qty);
                record.put("amount", value);
                record.put("date", java.text.DateFormat
                        .getDateTimeInstance().format(
                                new java.util.Date()));

                sales.add(0, record);
                saveData();
                toast("Sale saved successfully.");
                showDashboard();
            } catch (Exception ex) {
                toast("Check the quantity and sale amount.");
            }
        });

        button("CANCEL", () -> showDashboard());
    }

    private void showVisitForm() {
        content.removeAllViews();
        heading("Plan a Shop Visit");
        content.addView(text(
                "Create a visit plan for a shop.",
                14, dark, false));

        EditText shop = field("Shop name", "text");
        EditText date = field("Visit date (e.g. 25 Oct 2026)",
                "text");
        EditText notes = field("Notes / purpose of visit", "text");

        button("SAVE VISIT", () -> {
            String sh = shop.getText().toString().trim();
            String dt = date.getText().toString().trim();
            String nt = notes.getText().toString().trim();

            if (sh.isEmpty() || dt.isEmpty()) {
                toast("Enter the shop name and visit date.");
                return;
            }

            try {
                JSONObject record = new JSONObject();
                record.put("shop", sh);
                record.put("date", dt);
                record.put("notes", nt);
                visits.add(record);
                saveData();
                toast("Shop visit saved.");
                showDashboard();
            } catch (Exception ex) {
                toast("Could not save this visit.");
            }
        });

        button("CANCEL", () -> showDashboard());
    }

    private void showHistory() {
        content.removeAllViews();
        heading("Sales History");

        if (sales.isEmpty()) {
            content.addView(text("No sales recorded yet.",
                    14, dark, false));
        }

        for (int i = 0; i < sales.size(); i++) {
            final int index = i;
            JSONObject s = sales.get(i);
            content.addView(text(
                    s.optString("shop") + "\n" +
                    s.optString("product") + " × " +
                    s.optInt("quantity") + "\n" +
                    "Amount: K" + money(s.optDouble("amount")) +
                    "\n" + s.optString("date"),
                    14, dark, false));

            button("DELETE THIS SALE", () -> {
                sales.remove(index);
                saveData();
                showHistory();
            });
            gap();
        }

        heading("Planned Shop Visits");
        if (visits.isEmpty()) {
            content.addView(text("No visits planned yet.",
                    14, dark, false));
        }

        for (int i = 0; i < visits.size(); i++) {
            final int index = i;
            JSONObject v = visits.get(i);
            content.addView(text(
                    v.optString("shop") + "\n" +
                    "Date: " + v.optString("date") + "\n" +
                    "Notes: " + v.optString("notes"),
                    14, dark, false));

            button("DELETE THIS VISIT", () -> {
                visits.remove(index);
                saveData();
                showHistory();
            });
            gap();
        }

        button("BACK TO HOME", () -> showDashboard());
    }

    private String money(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void saveData() {
        JSONArray sa = new JSONArray();
        JSONArray vi = new JSONArray();

        for (JSONObject s : sales) sa.put(s);
        for (JSONObject v : visits) vi.put(v);

        prefs.edit()
                .putString("sales", sa.toString())
                .putString("visits", vi.toString())
                .apply();
    }

    private void loadData() {
        try {
            JSONArray sa = new JSONArray(
                    prefs.getString("sales", "[]"));
            JSONArray vi = new JSONArray(
                    prefs.getString("visits", "[]"));

            for (int i = 0; i < sa.length(); i++) {
                sales.add(sa.getJSONObject(i));
            }
            for (int i = 0; i < vi.length(); i++) {
                visits.add(vi.getJSONObject(i));
            }
        } catch (Exception ignored) {
            sales.clear();
            visits.clear();
        }
    }
}
