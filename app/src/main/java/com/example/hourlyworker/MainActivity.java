package com.example.hourlyworker;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	private static boolean isRunning = false;
	private static double moneyEarned = 0;
	private static double moneySaved = 0;
	private static double timeSaved = 0;
	private static long startTime;
	private MaterialButton startStopButton, resetButton;
	private Spinner spinner;
	private TextView moneyView, timeView;
	SharedPreferences sharedPreferences;
	private EditText rateView;
	private static final DecimalFormat df = new DecimalFormat("0.00");

	@SuppressLint("DefaultLocale")
	private String getTimerPassed() {
		long delta = System.currentTimeMillis() - startTime;
		delta += timeSaved;
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(delta), TimeUnit.MILLISECONDS.toMinutes(delta) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(delta)), TimeUnit.MILLISECONDS.toSeconds(delta) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delta)));
	}

	private double getMoneyPerMillisecond() {
		String rateString = String.valueOf(rateView.getText());
		double moneyPerHour = !rateString.equals("") ? Integer.parseInt(rateString) : 0;
		return moneyPerHour / 3600000;
	}

	private double getMoneyEarned() {
		long delta = System.currentTimeMillis() - startTime;
		return delta * getMoneyPerMillisecond();
	}

	private String getAllMoney() {
		double allMoney = Double.parseDouble(df.format(moneySaved + moneyEarned));
		return Double.toString(allMoney);
	}

	@SuppressLint("SetTextI18n")
	private void setupStartStopButton() {
		startStopButton.setOnClickListener(view -> {
			if (isRunning) {
				moneyEarned = 0;
				moneySaved += getMoneyEarned();
				timeSaved += System.currentTimeMillis() - startTime;
			}
			startTime = System.currentTimeMillis();
			isRunning = !isRunning;
			startStopButton.setText(isRunning ? "STOP" : "RESUME");
			startStopButton.setIcon(isRunning ? ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_circle_filled_24) : ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_circle_filled_24));
			resetButton.setEnabled(!isRunning);
		});
	}

	@SuppressLint("SetTextI18n")
	private void setupResetButton() {
		resetButton.setOnClickListener(view -> {
			isRunning = false;
			moneyView.setText("0.00");
			moneySaved = 0;
			moneyEarned = 0;
			timeSaved = 0;
			timeView.setText("00:00:00");
			startStopButton.setText("START");
			resetButton.setEnabled(false);
		});
	}

	private void setupButtons() {
		setupStartStopButton();
		setupResetButton();
	}

	private void findViews() {
		startStopButton = findViewById(R.id.start_stop);
		resetButton = findViewById(R.id.reset);
		moneyView = findViewById(R.id.money);
		timeView = findViewById(R.id.timer);
		rateView = findViewById(R.id.rate);
		spinner = findViewById(R.id.spinner);

		sharedPreferences = getPreferences(MODE_PRIVATE);
		rateView.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				sharedPreferences.edit().putString("hourlyRate", editable.toString()).apply();
			}

		});
		rateView.setText(sharedPreferences.getString("hourlyRate", "50"));
	}

	private void setupDropdown() {
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.currencies, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
		spinner.setAdapter(adapter);
		sharedPreferences = getPreferences(MODE_PRIVATE);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@SuppressLint("SetTextI18n")
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				sharedPreferences.edit().putInt("currency", i).apply();
				moneyView.setText(moneyView.getText().toString().substring(0, moneyView.getText().toString().length() - 1) + spinner.getSelectedItem().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		sharedPreferences = getPreferences(MODE_PRIVATE);
		spinner.setSelection(sharedPreferences.getInt("currency", 5));
	}

	private void setupHandler() {
		Handler handler = new Handler();
		int delay = 1;
		handler.postDelayed(new Runnable() {
			@SuppressLint("SetTextI18n")
			public void run() {
				if (isRunning) {
					moneyEarned = getMoneyEarned();
					moneyView.setText(getAllMoney() + " " + spinner.getSelectedItem().toString());
					timeView.setText(getTimerPassed());
				}
				handler.postDelayed(this, delay);
			}
		}, delay);
	}

	private void setup() {
		findViews();
		setupDropdown();
		setupButtons();
		setupHandler();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();

	}
}