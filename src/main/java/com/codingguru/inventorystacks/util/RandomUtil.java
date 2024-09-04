package com.codingguru.inventorystacks.util;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {

	private final static Random RANDOM = new Random();
	
	public static boolean getRandomBoolean() {
		return RANDOM.nextBoolean();
	}

	public static int getRandomInt(int maxBound) {
		return RANDOM.nextInt(maxBound) + 1;
	}
	
	public static double getRandomDouble(double min, double max) {
		return min + ThreadLocalRandom.current().nextDouble(Math.abs(max - min + 1));
	}
	
	public static boolean isSuccessful(double percentage) {
		return RANDOM.nextDouble() < percentage;
	}
	
	public static <T> T getRandomItemAndRemove(List<T> types) {
		T randomItem = types.get(RANDOM.nextInt(types.size()));
		types.remove(randomItem);
		return randomItem;
	}

	public static <T> T getRandomItem(List<T> types) {
		return types.get(RANDOM.nextInt(types.size()));
	}

	public static <T> T getRandomItem(T[] types) {
		return types[RANDOM.nextInt(types.length)];
	}
	
}