package com.signomix.util;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTool {
    public static Timestamp parseTimestamp(String input, String secondaryInput, boolean useSystemTimeOnError) {
        Timestamp ts = null;
        try {
            ts = new Timestamp(Long.parseLong(input));
            return ts;
        } catch (Exception e3) {
        }
        try {
            return getTimestamp(input, "yyyy-MM-dd'T'HH:mm:ssX");
        } catch (Exception e1) {
        }
        try {
            return getTimestamp(input, "yyyy-MM-dd'T'HH:mm:ss.SSSX");
        } catch (Exception e2) {
        }
        try {
            ts = Timestamp.from(Instant.parse(secondaryInput));
            return ts;
        } catch (Exception e4) {
        }
        return new Timestamp(System.currentTimeMillis());
    }

    private static Timestamp getTimestamp(String input, String pattern)
            throws IllegalArgumentException, DateTimeParseException, DateTimeException, NullPointerException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(input, formatter);
        ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset.withZoneSameInstant(ZoneOffset.UTC);
        return Timestamp.from(zdtInstanceAtUTC.toInstant());
    }
}
