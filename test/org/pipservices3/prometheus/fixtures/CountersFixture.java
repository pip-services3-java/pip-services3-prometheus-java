package org.pipservices3.prometheus.fixtures;

import org.pipservices3.commons.errors.InvocationException;
import org.pipservices3.components.count.CachedCounters;
import org.pipservices3.components.count.CounterType;

import static org.junit.Assert.*;

public class CountersFixture {
    private final CachedCounters _counters;

    public CountersFixture(CachedCounters counters) {
        this._counters = counters;
    }

    public void testSimpleCounters() throws InterruptedException, InvocationException {
        this._counters.last("Test.LastValue", 123);
        this._counters.last("Test.LastValue", 123456);

        var counter = this._counters.get("Test.LastValue", CounterType.LastValue);
        assertNotNull(counter);
        assertNotNull(counter.getLast());
        assertEquals(counter.getLast(), 123456, 3);

        this._counters.incrementOne("Test.Increment");
        this._counters.increment("Test.Increment", 3);

        counter = this._counters.get("Test.Increment", CounterType.Increment);
        assertNotNull(counter);
        assertEquals(counter.getCount(), Integer.valueOf(4));

        this._counters.timestampNow("Test.Timestamp");
        this._counters.timestampNow("Test.Timestamp");

        counter = this._counters.get("Test.Timestamp", CounterType.Timestamp);
        assertNotNull(counter);
        assertNotNull(counter.getTime());

        this._counters.stats("Test.Statistics", 1);
        this._counters.stats("Test.Statistics", 2);
        this._counters.stats("Test.Statistics", 3);

        counter = this._counters.get("Test.Statistics", CounterType.Statistics);
        assertNotNull(counter);
        assertEquals(counter.getAverage(), 2, 3);

        this._counters.dump();

        Thread.sleep(1000);
    }

    public void testMeasureElapsedTime() throws InterruptedException, InvocationException {
        var timer = this._counters.beginTiming("Test.Elapsed");

        Thread.sleep(100);

        timer.endTiming();

        var counter = this._counters.get("Test.Elapsed", CounterType.Interval);
        assertTrue(counter.getLast() > 50);
        assertTrue(counter.getLast() < 5000);

        this._counters.dump();

        Thread.sleep(1000);
    }
}
