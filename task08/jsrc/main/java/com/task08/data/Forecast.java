package com.task08.data;

public record Forecast(double latitude,
                       double longitude,
                       double generationtimeMs,
                       long utcOffsetSeconds,
                       String timezone,
                       String timezoneAbbreviation,
                       double elevation,
                       Units currentUnits,
                       Hourly hourly,
                       Units current,
                       Units hourlyUnits
                       ) {
}
