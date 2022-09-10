package com.example.hourlyworker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	private static boolean isRunning = false;
	private static long startTime;
	private Button mainButton;
	private TextView moneyView, timeView;
	private final double moneyPerHour = 60;
	private static final double millisecondsInHour = 3600000;
	final double moneyPerMillisecond = moneyPerHour / millisecondsInHour;
	private static final DecimalFormat df = new DecimalFormat("0.00");

	@SuppressLint("DefaultLocale")
	private String getTimerText() {
		long delta = System.currentTimeMillis() - startTime;
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(delta), TimeUnit.MILLISECONDS.toMinutes(delta) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(delta)), TimeUnit.MILLISECONDS.toSeconds(delta) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delta)));
	}

	private String getMoneyEarned() {
		long delta = System.currentTimeMillis() - startTime;
		return Double.toString(Double.parseDouble(df.format(delta * moneyPerMillisecond)));
	}

	private void setupStartStopButton() {
		mainButton.setOnClickListener(view -> {
			startTime = System.currentTimeMillis();
			System.out.println("Starting now: " + startTime);
			isRunning = !isRunning;
		});
	}

	private void setup() {
		mainButton = findViewById(R.id.start_stop);
		moneyView = findViewById(R.id.money);
		timeView = findViewById(R.id.timer);
		setupStartStopButton();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
		Handler handler = new Handler();
		int delay = 1; //milliseconds
		handler.postDelayed(new Runnable() {
			@SuppressLint("SetTextI18n")
			public void run() {
				if (isRunning) {
					moneyView.setText(getMoneyEarned());
					timeView.setText(getTimerText());
				}
				handler.postDelayed(this, delay);
			}
		}, delay);
	}
}