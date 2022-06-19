package org.pipservices3.prometheus.count;

import com.google.api.client.util.DateTime;
import org.junit.Test;
import org.pipservices3.commons.random.RandomDateTime;
import org.pipservices3.components.count.Counter;
import org.pipservices3.components.count.CounterType;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class PrometheusCounterConverterTest {

    @Test
    public void testKnownCounter_Exec_ServiceMetrics_Good() {
        List<Map<String, String>> knownCounterExecServiceMetricsGoodTestCases = List.of(
                Map.of(
                        "counterName", "MyService1.MyCommand1.exec_count", "expectedName", "exec_count"
                ),
                Map.of(
                        "counterName", "MyService1.MyCommand1.exec_time", "expectedName", "exec_time"
                ),
                Map.of(
                        "counterName", "MyService1.MyCommand1.exec_errors", "expectedName", "exec_errors"
                )
        );

        for (var testData : knownCounterExecServiceMetricsGoodTestCases) {
            var counterName = testData.get("counterName");
            var expectedName = testData.get("expectedName");

            var counters = new ArrayList<Counter>();

            var counter1 = new Counter(counterName, CounterType.Increment);
            counter1.setCount(1);
            counter1.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter1);

            var counter2 = new Counter(counterName, CounterType.Interval);
            counter2.setCount(11);
            counter2.setMax(13F);
            counter2.setMin(3F);
            counter2.setAverage(3.5F);
            counter2.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter2);

            var counter3 = new Counter(counterName, CounterType.LastValue);
            counter3.setLast(2F);
            counter3.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter3);

            var counter4 = new Counter(counterName, CounterType.Statistics);
            counter4.setCount(111);
            counter4.setMax(113F);
            counter4.setMin(13F);
            counter4.setAverage(13.5F);
            counter4.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter4);

            var body = PrometheusCounterConverter.toString(counters, "MyApp", "MyInstance");

            var expected = String.format("# TYPE %s gauge\n%s{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 1\n"
                            + "# TYPE %s_max gauge\n%s_max{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 13.0\n"
                            + "# TYPE %s_min gauge\n%s_min{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 3.0\n"
                            + "# TYPE %s_average gauge\n%s_average{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 3.5\n"
                            + "# TYPE %s_count gauge\n%s_count{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 11\n"
                            + "# TYPE %s gauge\n%s{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 2.0\n"
                            + "# TYPE %s_max gauge\n%s_max{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 113.0\n"
                            + "# TYPE %s_min gauge\n%s_min{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 13.0\n"
                            + "# TYPE %s_average gauge\n%s_average{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 13.5\n"
                            + "# TYPE %s_count gauge\n%s_count{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\"} 111\n",
                    expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName,
                    expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName
            );

            assertEquals(expected, body);
        }
    }

    @Test
    public void testKnownCounter_Exec_ClientMetrics_Good() {
        List<Map<String, String>> knownCounterExecClientMetricsGoodTestCases = List.of(
                Map.of(
                        "counterName", "MyTarget1.MyService1.MyCommand1.call_count", "expectedName", "call_count"
                ),
                Map.of(
                        "counterName", "MyTarget1.MyService1.MyCommand1.call_time", "expectedName", "call_time"
                ),
                Map.of(
                        "counterName", "MyTarget1.MyService1.MyCommand1.call_errors", "expectedName", "call_errors"
                )
        );

        for (var testData : knownCounterExecClientMetricsGoodTestCases) {
            var counterName = testData.get("counterName");
            var expectedName = testData.get("expectedName");

            var counters = new ArrayList<Counter>();

            var counter1 = new Counter(counterName, CounterType.Increment);
            counter1.setCount(1);
            counter1.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter1);

            var counter2 = new Counter(counterName, CounterType.Interval);
            counter2.setCount(11);
            counter2.setMax(13F);
            counter2.setMin(3F);
            counter2.setAverage(3.5F);
            counter2.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter2);

            var counter3 = new Counter(counterName, CounterType.LastValue);
            counter3.setLast(2F);
            counter3.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter3);

            var counter4 = new Counter(counterName, CounterType.Statistics);
            counter4.setCount(111);
            counter4.setMax(113F);
            counter4.setMin(13F);
            counter4.setAverage(13.5F);
            counter4.setTime(RandomDateTime.nextDateTime(ZonedDateTime.now().getYear(), ZonedDateTime.now().getYear() + 1));
            counters.add(counter4);

            var body = PrometheusCounterConverter.toString(counters, "MyApp", "MyInstance");

            var expected = String.format("# TYPE %s gauge\n%s{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 1\n"
                            + "# TYPE %s_max gauge\n%s_max{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 13.0\n"
                            + "# TYPE %s_min gauge\n%s_min{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 3.0\n"
                            + "# TYPE %s_average gauge\n%s_average{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 3.5\n"
                            + "# TYPE %s_count gauge\n%s_count{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 11\n"
                            + "# TYPE %s gauge\n%s{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 2.0\n"
                            + "# TYPE %s_max gauge\n%s_max{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 113.0\n"
                            + "# TYPE %s_min gauge\n%s_min{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 13.0\n"
                            + "# TYPE %s_average gauge\n%s_average{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 13.5\n"
                            + "# TYPE %s_count gauge\n%s_count{source=\"MyApp\",instance=\"MyInstance\",service=\"MyService1\",command=\"MyCommand1\",target=\"MyTarget1\"} 111\n",
                    expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName,
                    expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName, expectedName
            );

            assertEquals(expected, body);
        }
    }
}
