
package com.apkbuilder.calculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView display;
    private String current = "0";
    private double first = 0;
    private String operator = "";
    private boolean newNumber = true;

    private final int blue = Color.rgb(33, 150, 243);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(blue);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 20, 16, 16);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("MY CALCULATOR");
        title.setTextColor(blue);
        title.setTextSize(22);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(
            -1, 65
        ));

        display = new TextView(this);
        display.setText("0");
        display.setTextSize(38);
        display.setTextColor(Color.rgb(30, 40, 55));
        display.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        display.setPadding(12, 12, 12, 12);
        display.setSingleLine(true);
        root.addView(display, new LinearLayout.LayoutParams(
            -1, 110
        ));

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(4);

        String[] keys = {
            "7", "8", "9", "÷",
            "4", "5", "6", "×",
            "1", "2", "3", "−",
            "C", "0", "=", "+"
        };

        for (String key : keys) {
            Button button = new Button(this);
            button.setText(key);
            button.setTextSize(23);
            button.setAllCaps(false);

            boolean isOperator =
                key.equals("+") || key.equals("−") ||
                key.equals("×") || key.equals("÷") ||
                key.equals("=");

            button.setTextColor(isOperator ? blue :
                Color.rgb(35, 45, 60));

            GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();

            params.width = 0;
            params.height = 0;
            params.columnSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f
            );
            params.rowSpec = GridLayout.spec(
                GridLayout.UNDEFINED, 1f
            );
            params.setMargins(4, 4, 4, 4);

            grid.addView(button, params);
            button.setOnClickListener(v -> press(key));
        }

        root.addView(grid, new LinearLayout.LayoutParams(
            -1, 0, 1f
        ));

        setContentView(root);
    }

    private void press(String key) {
        if (key.matches("[0-9]")) {
            if (newNumber || current.equals("0")) {
                current = key;
            } else {
                current += key;
            }
            newNumber = false;
            show();
            return;
        }

        if (key.equals("C")) {
            current = "0";
            first = 0;
            operator = "";
            newNumber = true;
            show();
            return;
        }

        if (key.equals("=")) {
            if (operator.isEmpty() || newNumber) return;

            double second = Double.parseDouble(current);
            double result;

            switch (operator) {
                case "+": result = first + second; break;
                case "−": result = first - second; break;
                case "×": result = first * second; break;
                case "÷":
                    if (second == 0) {
                        display.setText("Cannot divide by zero");
                        current = "0";
                        operator = "";
                        newNumber = true;
                        return;
                    }
                    result = first / second;
                    break;
                default: return;
            }

            current = format(result);
            operator = "";
            newNumber = true;
            show();
            return;
        }

        first = Double.parseDouble(current);
        operator = key;
        newNumber = true;
    }

    private String format(double value) {
        if (value == Math.rint(value)
                && Math.abs(value) < 1e15) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    private void show() {
        display.setText(current);
    }
}
