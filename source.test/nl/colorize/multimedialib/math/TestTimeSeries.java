//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2011-2016 Colorize
// Apache license (http://www.colorize.nl/code_license.txt)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib.math;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import nl.colorize.util.Tuple;
import static org.junit.Assert.*;

/**
 * Unit test for the {@code TimeSeries} class.
 */
public class TestTimeSeries {
	
	private static final float EPSILON = 0.001f;
	
	@Test
	public void testGetDataPointAt() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(123L, 100f);
		
		assertEquals(Tuple.of(123L, 100f), data.getDataPointAt(123L));
		assertNull(data.getDataPointAt(456L));
	}
	
	@Test
	public void testGetDataPointTimes() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(123L, 100f);
		data.addDataPoint(456L, 1000f);
		
		assertEquals(ImmutableList.of(123L, 456L), data.getDataPointTimes());
	}
	
	@Test
	public void testSmoothingPeriod() {
		TimeSeries data = new TimeSeries();
		data.setSmoothingPeriod(1000L);
		data.addDataPoint(0L, 10f);
		data.addDataPoint(300L, 280f);
		data.addDataPoint(700L, 140f);
		data.addDataPoint(1100L, 90f);
		data.addDataPoint(1400L, 120f);
		
		assertEquals(2, data.getNumDataPoints());
		assertEquals("[<333, 143.33333>, <1250, 105.0>]", data.getDataPointTuples().toString());
		assertEquals(124.167, data.getAverage(), EPSILON);
	}
	
	@Test
	public void testStatistics() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(1L, 10f);
		data.addDataPoint(2L, 40f);
		data.addDataPoint(3L, 20f);
		
		assertEquals(10f, data.getMin(), EPSILON);
		assertEquals(40f, data.getMax(), EPSILON);
		assertEquals(23.333f, data.getAverage(), EPSILON);
		assertEquals(20f, data.getMedian(), EPSILON);
	}
	
	@Test
	public void testMaxDataPoints() {
		TimeSeries data = new TimeSeries();
		data.setMaxDataPoints(2);
		data.addDataPoint(1L, 10f);
		data.addDataPoint(2L, 20f);
		data.addDataPoint(3L, 30f);
		
		assertEquals(2, data.getNumDataPoints());
		assertEquals("[<2, 20.0>, <3, 30.0>]", data.getDataPointTuples().toString());
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCannotAddSameDataPointTwice() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(10L, 10f);
		data.addDataPoint(10L, 5f);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testDataPointCannotBeInPast() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(10L, 10f);
		data.addDataPoint(9L, 10f);
	}
	
	@Test
	public void testCreateTrendLine() {
		TimeSeries data = new TimeSeries();
		data.addDataPoint(10L, 10f);
		data.addDataPoint(20L, 20f);
		data.addDataPoint(30L, 30f);
		
		TimeSeries trendLine = data.createTrendLine(5L);
		
		assertEquals(5, trendLine.getNumDataPoints());
		assertEquals(10f, trendLine.getDataPointAt(10L).getRight(), EPSILON);
		assertEquals(15f, trendLine.getDataPointAt(15L).getRight(), EPSILON);
		assertEquals(20f, trendLine.getDataPointAt(20L).getRight(), EPSILON);
		assertEquals(25f, trendLine.getDataPointAt(25L).getRight(), EPSILON);
		assertEquals(30f, trendLine.getDataPointAt(30L).getRight(), EPSILON);
	}
}
