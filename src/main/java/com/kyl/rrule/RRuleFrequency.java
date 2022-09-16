package com.jsonar.rrule;

enum RRuleFrequency {
    // YEARLY(6, "YEARLY") is not supported by rrule js
    // SECOND(0), MINUTE(1), HOUR(2), DAY_OF_MONTH(3), MONTH(4), DAY_OF_WEEK(5), YEAR(6), DAY_OF_YEAR(7)
    SECONDLY(0, "SECONDLY"),  MINUTELY(1, "MINUTELY"), HOURLY(2, "HOURLY"), DAILY(3, "DAILY"), WEEKLY(4, "WEEKLY"), MONTHLY(5, "MONTHLY");
    private final int order;
    private final String description;

    RRuleFrequency(final int order, final String description) {
        this.order = order;
        this.description = description;
    }

    public int getOrder() {
        return order;
    }

    public String getDescription() {
        return description;
    }
}
