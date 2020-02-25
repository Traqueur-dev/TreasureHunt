package fr.traqueur.treasurehunt.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class NumberUtils {

	// integer
	public static Integer integerOrNull(String raw) {
		try {
			return Integer.valueOf(raw.trim());
		} catch (Throwable ignored) {
			return null;
		}
	}

	public static List<Integer> getListOfIntegers(int start, int end) {
		List<Integer> result = new ArrayList<Integer>();
		for (int i = start; i <= end; ++i) {
			result.add(i);
		}
		return result;
	}

	// long
	public static Long longOrNull(String raw) {
		try {
			return Long.valueOf(raw.trim());
		} catch (Throwable ignored) {
			return null;
		}
	}

	// double
	public static Double doubleOrNull(String raw) {
		try {
			return Double.valueOf(raw.trim());
		} catch (Throwable ignored) {
			return null;
		}
	}

	public static double round(double value, int places) {
		if (places <= 0) {
			return value;
		}
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	// random
	public static int random(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static double random(double min, double max) {
		return ThreadLocalRandom.current().nextDouble(min, max + 1d);
	}

	public static long random(long min, long max) {
		return ThreadLocalRandom.current().nextLong(min, max + 1L);
	}

	public static boolean random() {
		return ThreadLocalRandom.current().nextBoolean();
	}

}
