package com.kyl.rrule;

import java.util.List;
import java.util.Map;

/**
 * builder for recurrence rule string
 */
public interface RRuleStringBuilder {

    /**
     * specify 'DTSTART' in rrule
     * @param dtStart must be in yyyyMMdd'T'HHmmss'Z' format
     */
    void withDTStart(String dtStart);

    /**
     * specify rule parts in rrule
     * @param rRuleParts Map containing rrule parts. key is rule name value is a list of rule parts
     */
    void withRRuleParts(Map<RRuleName, List<String>> rRuleParts);

    /**
     * build rrule string
     * @return rrule string
     */
    String build();
}
