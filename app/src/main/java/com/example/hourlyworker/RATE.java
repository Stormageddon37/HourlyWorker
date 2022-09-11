package com.example.hourlyworker;

public enum RATE {
	NORMAL(1),
	OVERTIME(1.25),
	DOUBLE_OVERTIME(1.5);

	private final double value;

	RATE(final double newValue) {
		value = newValue;
	}

	public double getValue() {
		return value;
	}
}
