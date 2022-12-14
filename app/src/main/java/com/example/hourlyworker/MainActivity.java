package com.example.hourlyworker;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

	private static boolean isRunning = false;
	private static double moneyEarned = 0;
	private static double moneySaved = 0;
	private static double timeSaved = 0;
	private static long startTime;

	private TextView moneyView, timeView, overtimeView, currencyView;
	private MaterialButton startStopButton, resetButton;
	private SharedPreferences sharedPreferences;
	private EditText rateView;
	private Spinner spinner;

	private static RATE rate = RATE.NORMAL;
	private static final DecimalFormat df = new DecimalFormat("0.00");
	private static final int OVERTIME_MIN_MILLIS = 28800000; //8 hours in milliseconds
	private static final int DOUBLE_OVERTIME_MIN_MILLIS = 36000000; //10 hours in milliseconds

	private String getRateString() {
		return String.valueOf(rate).replace('_', ' ') + " (x" + rate.getValue() + ")";
	}

	private String getCurrencyString() {
		return " " + spinner.getSelectedItem().toString();
	}

	private String getAllMoneyString() {
		double allMoney = Double.parseDouble(df.format(moneySaved + moneyEarned));
		return Double.toString(allMoney);
	}

	private double getMoneyPerMillisecond() {
		String rateString = String.valueOf(rateView.getText());
		double moneyPerHour = !rateString.equals("") ? Integer.parseInt(rateString) : 0;
		return moneyPerHour / 3600000;
	}

	private double getMoneyEarned() {
		//TODO: cleanup
		long timePassed = System.currentTimeMillis() - startTime;
		if (timePassed > DOUBLE_OVERTIME_MIN_MILLIS) {
			long normal_time = OVERTIME_MIN_MILLIS;
			long over_time = DOUBLE_OVERTIME_MIN_MILLIS - OVERTIME_MIN_MILLIS;
			long double_over_time = timePassed - normal_time - over_time;

			double normal_time_money = normal_time * getMoneyPerMillisecond() * RATE.NORMAL.getValue();
			double over_time_money = over_time * getMoneyPerMillisecond() * RATE.OVERTIME.getValue();
			double double_over_time_money = double_over_time * getMoneyPerMillisecond() * RATE.DOUBLE_OVERTIME.getValue();

			return normal_time_money + over_time_money + double_over_time_money;
		} else if (timePassed > OVERTIME_MIN_MILLIS) {
			long normal_time = OVERTIME_MIN_MILLIS;
			long over_time = timePassed - normal_time;

			double normal_time_money = normal_time * getMoneyPerMillisecond() * RATE.NORMAL.getValue();
			double over_time_money = over_time * getMoneyPerMillisecond() * RATE.OVERTIME.getValue();

			return normal_time_money + over_time_money;
		}
		return timePassed * getMoneyPerMillisecond() * RATE.NORMAL.getValue();
	}

	private long getAllTime() {
		if (!isRunning) return (long) timeSaved;
		long delta = System.currentTimeMillis() - startTime;
		return (long) (timeSaved + delta);
	}

	@SuppressLint("DefaultLocale")
	private String getTimerPassed() {
		long delta = System.currentTimeMillis() - startTime;
		delta += timeSaved;
		return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(delta), TimeUnit.MILLISECONDS.toMinutes(delta) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(delta)), TimeUnit.MILLISECONDS.toSeconds(delta) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(delta)));
	}

	@SuppressLint("SetTextI18n")
	private void setupStartStopButton() {
		startStopButton.setOnClickListener(view -> {
			sharedPreferences = getPreferences(MODE_PRIVATE);
			if (isRunning) {
				moneyEarned = 0;
				moneySaved += getMoneyEarned();
				timeSaved += System.currentTimeMillis() - startTime;
			}
			startTime = System.currentTimeMillis();
			isRunning = !isRunning;
			startStopButton.setText(isRunning ? "STOP" : "RESUME");
			currencyView.setText(getCurrencyString());
			startStopButton.setIcon(isRunning ? ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_circle_filled_24) : ContextCompat.getDrawable(this, R.drawable.ic_baseline_play_circle_filled_24));
			resetButton.setEnabled(!isRunning);
		});
	}

	private void setupResetButton() {
		resetButton.setOnClickListener(view -> resetSession());
	}

	@SuppressLint("SetTextI18n")
	private void resetSession() {
		isRunning = false;
		moneyView.setText("0.0");
		currencyView.setText(getCurrencyString());
		moneySaved = 0;
		moneyEarned = 0;
		timeSaved = 0;
		timeView.setText("00:00:00");
		startStopButton.setText("START");
		resetButton.setEnabled(false);
		rate = RATE.NORMAL;
		overtimeView.setText("NORMAL (x1.0)");
	}

	private void showTimeDialog() {
		final Calendar calendar = Calendar.getInstance();

		@SuppressLint("SetTextI18n") TimePickerDialog.OnTimeSetListener timeSetListener = (view, hourOfDay, minute) -> {
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);

			resetSession();
			long selectedTime = calendar.getTime().getTime();
			if (selectedTime > System.currentTimeMillis()) {
				Toast.makeText(this, "Cannot start a shift in the future", Toast.LENGTH_SHORT).show();
				return;
			}
			startTime = selectedTime;
			isRunning = true;
			startStopButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_circle_filled_24));
			currencyView.setText(getCurrencyString());
			startStopButton.setText("STOP");
			resetButton.setEnabled(false);
		};

		new TimePickerDialog(MainActivity.this, timeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show();
	}

	private void setupBackdateTimeButton() {
		timeView.setOnLongClickListener(view -> {
			if (isRunning) return true;
			showTimeDialog();
			return true;
		});
	}

	private void setupButtons() {
		setupStartStopButton();
		setupResetButton();
		setupBackdateTimeButton();
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
				currencyView.setText(getCurrencyString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {

			}
		});
		sharedPreferences = getPreferences(MODE_PRIVATE);
		spinner.setSelection(sharedPreferences.getInt("currency", 0));
	}

	private void setupHandler() {
		Handler handler = new Handler();
		int delay = 1;
		handler.postDelayed(new Runnable() {
			@SuppressLint("SetTextI18n")
			public void run() {
				currencyView.setText(getCurrencyString());
				if (getAllTime() >= DOUBLE_OVERTIME_MIN_MILLIS) {
					rate = RATE.DOUBLE_OVERTIME;
				} else if (getAllTime() >= OVERTIME_MIN_MILLIS) {
					rate = RATE.OVERTIME;
				} else {
					rate = RATE.NORMAL;
				}
				sharedPreferences = getPreferences(MODE_PRIVATE);
				sharedPreferences.edit().putBoolean("isRunning", isRunning).apply();
				sharedPreferences.edit().putLong("moneySaved", (long) moneySaved).apply();
				sharedPreferences.edit().putLong("timeSaved", (long) timeSaved).apply();
				sharedPreferences.edit().putLong("startTime", startTime).apply();
				if (isRunning) {
					moneyEarned = getMoneyEarned();
					timeView.setText(getTimerPassed());
					moneyView.setText(getAllMoneyString());
					overtimeView.setText(getRateString());
				}
				handler.postDelayed(this, delay);
			}
		}, delay);
	}

	private void findViews() {
		startStopButton = findViewById(R.id.start_stop);
		resetButton = findViewById(R.id.reset);
		moneyView = findViewById(R.id.money);
		timeView = findViewById(R.id.timer);
		rateView = findViewById(R.id.rate);
		currencyView = findViewById(R.id.currency);
		overtimeView = findViewById(R.id.overtime);
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

	private void setup() {
		findViews();
		setupDropdown();
		setupButtons();
		setupHandler();
		currencyView.setText(getCurrencyString());
	}

	@SuppressLint("SetTextI18n")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setup();
		sharedPreferences = getPreferences(MODE_PRIVATE);
		if (sharedPreferences.getBoolean("isRunning", false)) {
			isRunning = true;
			moneySaved = sharedPreferences.getLong("moneySaved", 0);
			timeSaved = sharedPreferences.getLong("timeSaved", 0);
			startTime = sharedPreferences.getLong("startTime", 0);
			resetButton.setEnabled(false);
			startStopButton.setText("STOP");
			startStopButton.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_pause_circle_filled_24));
		}
	}
}
