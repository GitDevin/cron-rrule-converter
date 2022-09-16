package com.jsonar.rrule;

enum RRuleName {
    RRULE("RRULE"), FREQ("FREQ"), SECOND("BYSECOND"), MINUTE("BYMINUTE"), HOUR("BYHOUR"), MONTHDAY("BYMONTHDAY"), MONTH("BYMONTH"),
    BYSETPOS("BYSETPOS"), WEEKDAY("BYDAY"), YEARDAY("BYYEARDAY"), UNSUPPORTED("UNSUPPORTED"), DTSTART("DTSTART"), INTERVAL("INTERVAL");
    private final String name;

    RRuleName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
