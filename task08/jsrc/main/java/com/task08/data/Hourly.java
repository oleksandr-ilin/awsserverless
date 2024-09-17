package com.task08.data;

import java.util.Map;

public record Hourly(String[] time,
                     double[] temperature2m,
                     int[] relativeHumidity2m,
                     double windSpeed10m) {
}
