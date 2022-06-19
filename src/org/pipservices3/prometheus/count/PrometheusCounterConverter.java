package org.pipservices3.prometheus.count;

import org.pipservices3.commons.convert.StringConverter;
import org.pipservices3.components.count.Counter;
import org.pipservices3.components.count.CounterType;

import java.util.*;

/**
 * Helper class that converts performance counter values into
 * a response from Prometheus metrics service.
 */
public class PrometheusCounterConverter {
    /**
     * Converts the given counters to a string that is returned by Prometheus metrics service.
     *
     * @param counters a list of counters to convert.
     * @param source   a source (context) name.
     * @param instance a unique instance name (usually a host name).
     */
    public static String toString(List<Counter> counters, String source, String instance) {
        if (counters == null || counters.size() == 0) return "";

        StringBuilder builder = new StringBuilder();

        for (var counter : counters) {
            var counterName = parseCounterName(counter);
            var labels = generateCounterLabel(counter, source, instance);

            switch (counter.getType()) {
                case CounterType.Increment -> {
                    builder.append("# TYPE ").append(counterName).append(" gauge\n");
                    builder.append(counterName).append(labels).append(" ").append(StringConverter.toString(counter.getCount())).append("\n");
                }
                case CounterType.Interval, CounterType.Statistics -> {
                    builder.append("# TYPE ").append(counterName).append("_max gauge\n");
                    builder.append(counterName).append("_max").append(labels).append(" ").append(StringConverter.toString(counter.getMax())).append("\n");
                    builder.append("# TYPE ").append(counterName).append("_min gauge\n");
                    builder.append(counterName).append("_min").append(labels).append(" ").append(StringConverter.toString(counter.getMin())).append("\n");
                    builder.append("# TYPE ").append(counterName).append("_average gauge\n");
                    builder.append(counterName).append("_average").append(labels).append(" ").append(StringConverter.toString(counter.getAverage())).append("\n");
                    builder.append("# TYPE ").append(counterName).append("_count gauge\n");
                    builder.append(counterName).append("_count").append(labels).append(" ").append(StringConverter.toString(counter.getCount())).append("\n");
                }
                case CounterType.LastValue -> {
                    builder.append("# TYPE ").append(counterName).append(" gauge\n");
                    builder.append(counterName).append(labels).append(" ").append(StringConverter.toString(counter.getLast())).append("\n");
                }
                //case CounterType.Timestamp: // Prometheus doesn't support non-numeric metrics
                //builder += "# TYPE " + counterName + " untyped\n";
                //builder += counterName + labels + " " + StringConverter.toString(counter.time) + "\n";
                //break;
            }
        }

        return builder.toString();
    }

    private static String generateCounterLabel(Counter counter, String source, String instance) {
        var labels = new LinkedHashMap<String, String>();

        if (source != null && !source.equals("")) labels.put("source", source);
        if (instance != null && !instance.equals("")) labels.put("instance", instance);

        var nameParts = counter.getName().split("\\.");

        // If there are other predictable names from which we can parse labels, we can add them below
        if ((nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_count"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_time"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_errors"))
        ) {
            labels.put("service", nameParts[0]);
            labels.put("command", nameParts[1]);
        }

        if ((nameParts.length >= 4 && Objects.equals(nameParts[3], "call_count"))
                || (nameParts.length >= 4 && Objects.equals(nameParts[3], "call_time"))
                || (nameParts.length >= 4 && Objects.equals(nameParts[3], "call_errors"))
        ) {
            labels.put("service", nameParts[1]);
            labels.put("command", nameParts[2]);
            labels.put("target", nameParts[0]);
        }

        if ((nameParts.length >= 3 && Objects.equals(nameParts[2], "sent_messages"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "received_messages"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "dead_messages"))
        ) {
            labels.put("queue", nameParts[1]);
        }

        if (labels.isEmpty()) return "";

        StringBuilder builder = new StringBuilder("{");
        for (var key : labels.keySet()) {
            if (builder.length() > 1) builder.append(",");
            builder.append(key).append("=\"").append(labels.get(key)).append('"');
        }
        builder.append("}");

        return builder.toString();
    }

    private static String parseCounterName(Counter counter) {
        if (counter == null || counter.getName() == null || Objects.equals(counter.getName(), "")) return "";

        var nameParts = counter.getName().split("\\.");

        // If there are other predictable names from which we can parse labels, we can add them below
        // Rest Service Labels
        if (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_count")) {
            return nameParts[2];
        }
        if (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_time")) {
            return nameParts[2];
        }
        if (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_errors")) {
            return nameParts[2];
        }

        // Rest & Direct Client Labels
        if (nameParts.length >= 4 && Objects.equals(nameParts[3], "call_count")) {
            return nameParts[3];
        }
        if (nameParts.length >= 4 && Objects.equals(nameParts[3], "call_time")) {
            return nameParts[3];
        }
        if (nameParts.length >= 4 && Objects.equals(nameParts[3], "call_errors")) {
            return nameParts[3];
        }

        // Queue Labels
        if ((nameParts.length >= 3 && Objects.equals(nameParts[2], "sent_messages"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "received_messages"))
                || (nameParts.length >= 3 && Objects.equals(nameParts[2], "dead_messages"))
        ) {
            var name = nameParts[0] + "." + nameParts[2];
            return name.toLowerCase().replace(".", "_").replace("/", "_");
        }

        // TODO: are there other assumptions we can make?
        // Or just return as a single, valid name
        return counter.getName().toLowerCase()
                .replace(".", "_").replace("/", "_");
    }

    private static Map<String, String> parseCounterLabels(Counter counter, String source, String instance) {
        var labels = new LinkedHashMap<String, String>();

        if (source != null && !source.equals("")) labels.put("source", source);
        if (instance != null && !instance.equals("")) labels.put("instance", instance);

        var nameParts = counter.getName().split("\\.");

        // If there are other predictable names from which we can parse labels, we can add them below
        if (nameParts.length >= 3 && Objects.equals(nameParts[2], "exec_time")) {
            labels.put("service", nameParts[0]);
            labels.put("command", nameParts[1]);
        }

        return labels;
    }
}
