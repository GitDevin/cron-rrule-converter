package com.jsonar.rrule;

import java.time.LocalDateTime;

import com.cronutils.model.Cron;

/**
 * Converter that converts a Cron object to rrule string. Special character 'W' in 'day of month' cron field is not supported
 */
public interface RRuleConverter {

    /**
     * convert a Cron object to rrule string
     * @param cron a valid Cron object
     * @param dtStart a LocalDateTime object specify 'DTSTART' option in rrule string. for null value, 'DTSTART' is omitted.
     * @return a rrule string. 'null' if Cron object contains unsupported special character 'W'
     */
    String convert(Cron cron, LocalDateTime dtStart);
}
