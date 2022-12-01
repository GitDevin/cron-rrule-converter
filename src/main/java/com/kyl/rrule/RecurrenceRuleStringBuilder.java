package com.kyl.rrule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cronutils.utils.StringUtils;

public class RecurrenceRuleStringBuilder implements RRuleStringBuilder {
    protected static final char RULE_PART_SEPARATOR = ';';
    protected static final char RULE_NAME_VALUE_SEPARATOR = '=';
    protected static final char RULE_NAME_PARTS_SEPARATOR = ':';

    protected static final char RULE_OPTION_SEPARATOR = '\n';
    protected StringBuilder stringBuilder;
    private final Map<RRuleName, List<String>> rruleParts;

    private String dtStartStr; // in UTC
    private final RRuleFrequency rruleFrequency;

    public RecurrenceRuleStringBuilder(RRuleFrequency rruleFrequency) {
        this.stringBuilder = new StringBuilder();
        this.rruleParts = new HashMap<>();
        this.dtStartStr = null;
        this.rruleFrequency = rruleFrequency;
    }

    protected void appendRRuleFrequency(RRuleFrequency rRuleFrequency) {
        this.stringBuilder.append(RRuleName.FREQ.getName());
        this.stringBuilder.append(RULE_NAME_VALUE_SEPARATOR);
        this.stringBuilder.append(rRuleFrequency.getDescription());
    }

    @Override
    public void withRRuleParts(Map<RRuleName, List<String>> rRuleParts) {
        this.rruleParts.putAll(rRuleParts);
    }

    @Override
    public String build() {
        if (!StringUtils.isEmpty(this.dtStartStr)) {
            this.appendDTStart(this.dtStartStr);
        }
        this.stringBuilder.append(RRuleName.RRULE.getName());
        this.stringBuilder.append(RULE_NAME_PARTS_SEPARATOR);
        this.appendRRuleFrequency(this.rruleFrequency);
        this.rruleParts.entrySet().stream()
                .filter((entry) -> entry.getKey() != RRuleName.DTSTART && entry.getKey() != RRuleName.RRULE && entry.getKey() != RRuleName.FREQ)
                .forEach((entry) -> this.appendRRuleParts(entry.getKey(), entry.getValue()));

        return this.stringBuilder.toString();
    }

    private void appendDTStart(String dtStartStr) {
        this.stringBuilder.append(RRuleName.DTSTART);
        this.stringBuilder.append(RULE_NAME_PARTS_SEPARATOR);
        this.stringBuilder.append(dtStartStr);
        this.stringBuilder.append(RULE_OPTION_SEPARATOR);
    }

    private void appendRRuleParts(RRuleName partName, List<String> rulePartValues) {
        this.stringBuilder.append(RULE_PART_SEPARATOR);
        this.stringBuilder.append(partName.getName());
        this.stringBuilder.append(RULE_NAME_VALUE_SEPARATOR);
        this.stringBuilder.append(String.join(",", rulePartValues));
    }

    @Override
    public void withDTStart(String dtStart) {
        this.dtStartStr = dtStart;
    }

}
