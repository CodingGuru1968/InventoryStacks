package com.codingguru.trailpaths.utils;

import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

public class WeightedRandomUtil<T> {

	private class Entry {
		double accumulatedWeight;
		T item;
	}

	private final List<Entry> entries = Lists.newArrayList();
	private final Random rand = new Random();
	private double accumulatedWeight;

	public void addEntry(T item, double weight) {
		if (weight == 0)
			weight = 100;
		accumulatedWeight += weight;
		Entry e = new Entry();
		e.item = item;
		e.accumulatedWeight = accumulatedWeight;
		entries.add(e);
	}

	public List<Entry> getEntries() {
		return entries;
	}

	public T getRandom() {
		double r = rand.nextDouble() * accumulatedWeight;

		for (Entry entry : entries) {
			if (entry.accumulatedWeight >= r) {
				return entry.item;
			}
		}

		return null;
	}

	public int size() {
		return entries.size();
	}
}