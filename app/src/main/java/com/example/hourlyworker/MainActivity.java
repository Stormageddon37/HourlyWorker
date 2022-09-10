package com.example.hourlyworker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
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
	private TextView moneyView, timeView;
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

	private void setup() {
		startStopButton = findViewById(R.id.start_stop);
		resetButton = findViewById(R.id.reset);
		moneyView = findViewById(R.id.money);
		timeView = findViewById(R.id.timer);
		rateView = findViewById(R.id.rate);
		setupButtons();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
		Handler handler = new Handler();
		int delay = 1;
		handler.postDelayed(new Runnable() {
			@SuppressLint("SetTextI18n")
			public void run() {
				if (isRunning) {
					moneyEarned = getMoneyEarned();
					moneyView.setText(getAllMoney() + " $");
					timeView.setText(getTimerPassed());
				}
				handler.postDelayed(this, delay);
			}
		}, delay);
	}
}