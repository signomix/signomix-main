package com.signomix.common;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTool {
    public static Timestamp parseTimestamp(String input, String secondaryInput, boolean useSystemTimeOnError) {
        if (null == input || input.isEmpty()) {
            return null;
        }
        String timeString = input.replace('~', '+');
        Timestamp ts = null;
        if (input.startsWith("-")) {
            int multiplicand = 1;
            int zonePosition = input.indexOf("-", 1);
            char unitSymbol;
            long millis;
            String zoneId = "";
            if (zonePosition == -1) {
                millis = Long.parseLong(input.substring(1, input.length() - 1));
                unitSymbol = input.charAt(input.length() - 1);
            } else {
                millis = Long.parseLong(input.substring(1, 2));
                unitSymbol = input.charAt(2);
                zoneId = input.substring(zonePosition + 1).replaceFirst("\\.", "/");
            }
            switch (unitSymbol) {
                case 'd':
                    multiplicand = 86400 * 1000;
                    break;
                case 'h':
                    multiplicand = 3600 * 1000;
                    break;
                case 'm':
                    multiplicand = 60 * 1000;
                    break;
                default: // seconds
                    multiplicand = 1000;
            }
            if (millis == 0 && multiplicand == 86400 * 1000) {
                ts = new Timestamp(getStartOfDayAsUTC(zoneId));
                return ts;
            } else if (millis == 0 && multiplicand != 86400 * 1000) {
                // cannot be parsed (parsing error) - actual timestamp will be returned
            } else {
                ts = new Timestamp(System.currentTimeMillis() - millis * multiplicand);
                return ts;
            }
        } else {
            try {
                ts = new Timestamp(Long.parseLong(timeString));
                return ts;
            } catch (Exception e3) {
            }
            try {
                return getTimestamp(timeString, "yyyy-MM-dd'T'HH:mm:ssX");
            } catch (Exception e1) {
            }
            try {
                return getTimestamp(timeString, "yyyy-MM-dd'T'HH:mm:ss.SSSX");
            } catch (Exception e2) {
            }
            try {
                return getTimestamp(timeString, "yyyy-MM-dd'T'HHmmssX");
            } catch (Exception e1) {
            }
            try {
                return getTimestamp(timeString, "yyyy-MM-dd'T'HHmmss.SSSX");
            } catch (Exception e2) {
            }
            try {
                ts = Timestamp.from(Instant.parse(secondaryInput));
                return ts;
            } catch (Exception e4) {
            }
        }
        if (useSystemTimeOnError) {
            return new Timestamp(System.currentTimeMillis());
        } else {
            return new Timestamp(0);
        }
    }

    private static Timestamp getTimestamp(String input, String pattern)
            throws IllegalArgumentException, DateTimeParseException, DateTimeException, NullPointerException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        ZonedDateTime zdtInstanceAtOffset = ZonedDateTime.parse(input, formatter);
        ZonedDateTime zdtInstanceAtUTC = zdtInstanceAtOffset.withZoneSameInstant(ZoneOffset.UTC);
        return Timestamp.from(zdtInstanceAtUTC.toInstant());
    }

    /*
     * public static long getStartOfDayAsUTC(String zoneId) {
     * try{
     * long result2 =
     * LocalDate.now(ZoneId.of(zoneId)).atStartOfDay().toInstant(ZoneOffset.UTC).
     * toEpochMilli();
     * long result =
     * Timestamp.valueOf(LocalDate.now(ZoneId.of(zoneId)).atStartOfDay()).getTime();
     * long result3 =
     * LocalDate.now(ZoneId.of(zoneId)).atStartOfDay().toInstant(ZoneOffset.of(
     * zoneId)).toEpochMilli();
     * System.out.println("day start "+zoneId+": "+result);
     * System.out.println("day start2 "+zoneId+": "+result2);
     * System.out.println("day start3 "+zoneId+": "+result3);
     * return result;
     * }catch(Exception ex){
     * ex.printStackTrace();
     * return 0;
     * }
     * }
     */
    public static long getStartOfDayAsUTC(String zoneId) {
        long result;
        LocalDate localDate = LocalDate.now(ZoneId.of(zoneId));
        ZonedDateTime startOfDayInEurope2 = localDate.atTime(LocalTime.MIN)
                .atZone(ZoneId.of(zoneId));
        long offset = startOfDayInEurope2.getOffset().getTotalSeconds() * 1000;
        result=Timestamp.valueOf(startOfDayInEurope2.toLocalDateTime()).getTime() - offset;
        return result;
    }
}
