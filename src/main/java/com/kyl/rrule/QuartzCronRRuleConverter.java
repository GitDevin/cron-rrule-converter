package com.jsonar.rrule;

import com.cronutils.model.Cron;
import com.cronutils.model.field.CronField;
import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

public class QuartzCronRRuleConverter implements RRuleConverter {
    private static final Map<CronFieldName, Integer> CRON_FIELD_NAME_ORDER = new HashMap<>() {{
        put(CronFieldName.SECOND, CronFieldName.SECOND.getOrder());
        put(CronFieldName.MINUTE, CronFieldName.MONTH.getOrder());
        put(CronFieldName.HOUR, CronFieldName.HOUR.getOrder());
        put(CronFieldName.DAY_OF_MONTH, CronFieldName.DAY_OF_MONTH.getOrder());
        put(CronFieldName.MONTH, 5);
        put(CronFieldName.DAY_OF_WEEK, 4);
        put(CronFieldName.YEAR, CronFieldName.YEAR.getOrder());
        put(CronFieldName.DAY_OF_YEAR, CronFieldName.DAY_OF_MONTH.getOrder());
    }};
    private static final Map<CronFieldName, RRuleFrequency> FREQUENCY_MAPPING = new HashMap<>() {{
        put(CronFieldName.SECOND, RRuleFrequency.SECONDLY);
        put(CronFieldName.MINUTE, RRuleFrequency.MINUTELY);
        put(CronFieldName.HOUR, RRuleFrequency.HOURLY);
        put(CronFieldName.DAY_OF_MONTH, RRuleFrequency.DAILY);
        put(CronFieldName.MONTH, RRuleFrequency.MONTHLY);
        put(CronFieldName.DAY_OF_WEEK, RRuleFrequency.WEEKLY);
        put(CronFieldName.YEAR, RRuleFrequency.MONTHLY);
        put(CronFieldName.DAY_OF_YEAR, RRuleFrequency.MONTHLY);
    }};

    /**
     * check if FieldExpression should be mapped to rrule frequency
     * @param fieldExpression FieldExpression instance
     * @return true
     */
    protected boolean isFrequency(FieldExpression fieldExpression) {
        Predicate isEveryOne = (fe) -> {
            if (fe instanceof Every) {
                Every e = (Every) fe;
                return e.getPeriod().getValue() == 1;
            }
            return false;
        };

        return (!(fieldExpression instanceof On) && fieldExpression != FieldExpression.questionMark())
                || isEveryOne.evaluate(fieldExpression);
    }

    /**
     * Find equivalent rrule frequency in the cron field map. For example, Every second maps to SECONDLY.
     * @param cronFieldNameCronFieldMap cron field name to cron field map
     * @return equivalent rrule frequency
     */
    protected RRuleFrequency findFrequency(Map<CronFieldName, CronField> cronFieldNameCronFieldMap) {
        Optional<CronField> frequencyFieldOpt = cronFieldNameCronFieldMap.values()
                .stream()
                .sorted(Comparator.comparingInt(f -> CRON_FIELD_NAME_ORDER.get(f.getField())))
                .filter(f -> this.isFrequency(f.getExpression()))
                .findFirst();

        return frequencyFieldOpt.map(f -> FREQUENCY_MAPPING.get(f.getField())).orElse(RRuleFrequency.MONTHLY);
    }

    public String convert(Cron cron, LocalDateTime dtStart) {
        Map<CronFieldName, CronField> cronFieldNameCronFieldMap = cron.retrieveFieldsAsMap();

        RRuleFrequency frequency = this.findFrequency(cronFieldNameCronFieldMap);

        Stream<Map<RRuleName, List<String>>> rulePartStream = cronFieldNameCronFieldMap.entrySet().stream()
                .filter((entry) -> CronFieldName.YEAR != entry.getKey())
                .map((entry) -> {
                    CronField field = entry.getValue();
                    CronExpressionRRulePartConverter visitor = new CronExpressionRRulePartConverter(entry.getKey(), field.getConstraints());
                    field.getExpression().accept(visitor);
                    return visitor.getRRuleParts();
                });

        Map<RRuleName, List<String>> rruleParts = rulePartStream.reduce(new HashMap<>(), (acc, curr) -> {
            curr.forEach((rRuleName, strings) ->
                    acc.compute(rRuleName, (k, v) -> {
                        if (CollectionUtils.isEmpty(v)) {
                            return strings;
                        } else {
                            v.addAll(strings);
                            return v;
                        }
                    })
            );
            return acc;
        });

        if (rruleParts.containsKey(RRuleName.UNSUPPORTED)) {
            return null;
        }

        RRuleStringBuilder defaultRRuleStringBuilder = new RecurrenceRuleStringBuilder(frequency);
        if (dtStart != null) {
            defaultRRuleStringBuilder.withDTStart(this.convertToDTStartFormat(dtStart));
        }

        defaultRRuleStringBuilder.withRRuleParts(rruleParts);

        return defaultRRuleStringBuilder.build();
    }

    /**
     * format a LocalDateTime instance in rrule instant format
     * @param dtStart LocalDateTime instance
     * @return string in rrule instant format
     */
    protected String convertToDTStartFormat(LocalDateTime dtStart) {
        DateTimeFormatter dtStartFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

        return dtStartFormat.format(dtStart);
    }
}
