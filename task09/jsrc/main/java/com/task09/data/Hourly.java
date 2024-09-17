package com.task09.data;

public record Hourly(String[] time,
                     double[] temperature2m,
                     int[] relativeHumidity2m,
                     double windSpeed10m) {
}
