//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import nl.colorize.util.Tuple;

/**
 * A sequence of data points captured over a period of time. If needed, data
 * points can be consolidated to smooth out the effects of peaks in the data. 
 */
public class TimeSeries {

	private List<DataPoint> consolidatedDataPoints;
	private List<DataPoint> nonConsolidatedDataPoints;
	private long smoothingPeriod;
	private int maxDataPoints;
	
	public TimeSeries() {
		consolidatedDataPoints = new ArrayList<DataPoint>();
		nonConsolidatedDataPoints = new ArrayList<DataPoint>();
		smoothingPeriod = 0L;
		maxDataPoints = Integer.MAX_VALUE;
	}
	
	/**
	 * Adds a data point for a value captured at a certain point in time.
	 * @param time Point in time for the data point, in milliseconds.
	 * @throws IllegalStateException if a data point already exists after or at
	 *         the same time.
	 * @throws IllegalArgumentException if {@code time} is negative.
	 */
	public void addDataPoint(long time, float value) {
		if (time < 0L) {
			throw new IllegalArgumentException("Invalid data point time: " + time);
		}
		
		DataPoint lastDataPoint = getLastDataPoint();
		if (lastDataPoint != null) {
			if (time == lastDataPoint.time) {
				throw new IllegalStateException("Data point already exists at " + time);
			} else if (time < lastDataPoint.time) {
				throw new IllegalStateException("Data point is in the past: " + time);
			}
		}
		
		DataPoint dataPoint = new DataPoint(time, value);
		addDataPoint(dataPoint);
		
		if (getNumDataPoints() > maxDataPoints) {
			consolidatedDataPoints.remove(consolidatedDataPoints.get(0));
		}
	}
	
	private void addDataPoint(DataPoint dataPoint) {
		if (isSmoothingEnabled()) {
			nonConsolidatedDataPoints.add(dataPoint);
			if (shouldConsolidateDataPoints(nonConsolidatedDataPoints)) {
				DataPoint last = nonConsolidatedDataPoints.get(nonConsolidatedDataPoints.size() - 1);
				nonConsolidatedDataPoints.remove(last);
				consolidatedDataPoints.add(consolidateToDataPoint(nonConsolidatedDataPoints));
				nonConsolidatedDataPoints.clear();
				nonConsolidatedDataPoints.add(dataPoint);
			}
		} else {
			consolidatedDataPoints.add(dataPoint);
		}
	}

	private boolean shouldConsolidateDataPoints(List<DataPoint> dataPoints) {
		if (dataPoints.size() < 2) {
			return false;
		}
		return dataPoints.get(dataPoints.size() - 1).time - dataPoints.get(0).time >= smoothingPeriod;
	}

	private DataPoint consolidateToDataPoint(List<DataPoint> dataPoints) {
		if (dataPoints.size() < 1) {
			throw new IllegalArgumentException("Not enough data points to consolidate");
		}
		return new DataPoint(getAverageTime(dataPoints), getAverage(dataPoints));
	}
	
	private long getAverageTime(List<DataPoint> dataPoints) {
		long sum = 0L;
		for (DataPoint dataPoint : dataPoints) {
			sum += dataPoint.time;
		}
		return sum / dataPoints.size();
	}
	
	/**
	 * Returns a list with the "actual" data points, which are all consolidated
	 * data points plus one that combines all non-consolidated data points. The
	 * list is sorted based on the data points' time. 
	 */
	private List<DataPoint> getDataPoints() {
		List<DataPoint> dataPoints = new ArrayList<DataPoint>();
		dataPoints.addAll(consolidatedDataPoints);
		if (!nonConsolidatedDataPoints.isEmpty()) {
			dataPoints.add(consolidateToDataPoint(nonConsolidatedDataPoints));
		}
		return dataPoints;
	}
	
	/**
	 * Returns the data points in this time series as a list of tuples. Data
	 * points are sorted by time, with the first data point coming first.
	 */
	public List<Tuple<Long, Float>> getDataPointTuples() {
		List<Tuple<Long, Float>> tuples = new ArrayList<Tuple<Long, Float>>();
		for (DataPoint dataPoint : getDataPoints()) {
			tuples.add(dataPoint.toTuple());
		}
		return tuples;
	}
	
	/**
	 * Returns a list containing the (sorted) times for which data points exist
	 * in this time series.
	 */
	public List<Long> getDataPointTimes() {
		List<Long> times = new ArrayList<Long>();
		for (DataPoint dataPoint : getDataPoints()) {
			times.add(dataPoint.time);
		}
		return times;
	}
	
	/**
	 * Returns the data point for the specified time, or {@code null} when no
	 * such data point exists.
	 */
	public Tuple<Long, Float> getDataPointAt(long time) {
		for (DataPoint dataPoint : getDataPoints()) {
			if (dataPoint.time == time) {
				return dataPoint.toTuple();
			}
		}
		return null;
	}
	
	private DataPoint getLastDataPoint() {
		List<DataPoint> dataPoints = getDataPoints();
		if (dataPoints.isEmpty()) {
			return null;
		}
		return dataPoints.get(dataPoints.size() - 1);
	}
	
	public int getNumDataPoints() {
		if (nonConsolidatedDataPoints.isEmpty()) {
			return consolidatedDataPoints.size();
		} else {
			return consolidatedDataPoints.size() + 1;
		}
	}
	
	public void clearDataPoints() {
		consolidatedDataPoints.clear();
		nonConsolidatedDataPoints.clear();
	}
	
	public void setSmoothingPeriod(long smoothingPeriod) {
		this.smoothingPeriod = smoothingPeriod;
	}
	
	public long getSmoothingPeriod() {
		return smoothingPeriod;
	}
	
	private boolean isSmoothingEnabled() {
		return smoothingPeriod > 0;
	}
	
	public void setMaxDataPoints(int maxDataPoints) {
		if (maxDataPoints < 1) {
			throw new IllegalArgumentException("Must have at least one data point");
		}
		this.maxDataPoints = maxDataPoints;
	}
	
	public int getMaxDataPoints() {
		return maxDataPoints;
	}

	public float getMin() {
		float min = Float.MAX_VALUE;
		for (DataPoint dataPoint : getDataPoints()) {
			min = Math.min(dataPoint.value, min);
		}
		return min;
	}
	
	public float getMax() {
		float max = Float.MIN_VALUE;
		for (DataPoint dataPoint : getDataPoints()) {
			max = Math.max(dataPoint.value, max);
		}
		return max;
	}
	
	public float getAverage() {
		return getAverage(getDataPoints());
	}
	
	private float getAverage(Collection<DataPoint> dataPoints) {
		float sum = 0f;
		for (DataPoint dataPoint : dataPoints) {
			sum += dataPoint.value;
		}
		return sum / dataPoints.size();
	}

	public float getMedian() {
		List<Float> sortedValues = new ArrayList<Float>();
		for (DataPoint dataPoint : getDataPoints()) {
			sortedValues.add(dataPoint.value);
		}
		Collections.sort(sortedValues);
		return sortedValues.get(sortedValues.size() / 2);
	}
	
	/**
	 * Calculates the trend line based on the data points in this time series,
	 * and returns a new {@code TimeSeries} that contains its values. The number
	 * of data points that is created for the trend line is configurable, with the
	 * {@code timeInterval} representing the time between the data points.
	 * @throws IllegalStateException if there is not enough data to calculate
	 *         the trend line.
	 * @throws IllegalArgumentException if the timeInterval is negative or zero.
	 */
	public TimeSeries createTrendLine(long timeInterval) {
		if (timeInterval < 1L) {
			throw new IllegalArgumentException("Invalid time interval: " + timeInterval);
		}
		
		List<DataPoint> dataPoints = getDataPoints();
		if (dataPoints.size() < 2) {
			throw new IllegalStateException("Insufficient data to create trend line");
		}
		
		return calculateTrendLine(dataPoints, timeInterval);
	}
	
	private TimeSeries calculateTrendLine(List<DataPoint> dataPoints, long timeInterval) {
		long sumX = 0L;
		float sumY = 0f;
		float sumXY = 0f;
		float sumXSquared = 0f;
		
		for (DataPoint dataPoint : dataPoints) {
			sumX += dataPoint.time;
			sumY += dataPoint.value;
			sumXY += dataPoint.time * dataPoint.value;
			sumXSquared += dataPoint.time * dataPoint.time;
		}
		
		int n = dataPoints.size();
		float slope = (n * sumXY - sumX * sumY) / (n * sumXSquared - sumX * sumX);
		float intercept = (sumY - (slope * sumX)) / n;
		
		return calculateTrendLine(dataPoints.get(0).time, dataPoints.get(dataPoints.size() - 1).time,
				timeInterval, slope, intercept);
	}

	private TimeSeries calculateTrendLine(long startTime, long endTime, long timeInterval,
			float slope, float intercept) {
		TimeSeries trendLine = new TimeSeries();
		for (long t = startTime; t <= endTime; t += timeInterval) {
			trendLine.addDataPoint(t, t * slope + intercept);
		}
		return trendLine;
	}

	/**
	 * A measurement taken at a certain point in time.
	 */
	private static class DataPoint implements Comparable<DataPoint> {
		
		private long time;
		private float value;
		
		public DataPoint(long time, float value) {
			this.time = time;
			this.value = value;
		}

		public int compareTo(DataPoint other) {
			return (int) (time - other.time);
		}
		
		public Tuple<Long, Float> toTuple() {
			return Tuple.of(time, value);
		}
	}
}
