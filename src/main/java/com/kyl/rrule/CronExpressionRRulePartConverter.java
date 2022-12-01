package com.kyl.rrule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.Always;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.expression.QuestionMark;
import com.cronutils.model.field.value.IntegerFieldValue;

public class CronExpressionRRulePartConverter implements RRulePartConverter {

    private static final String RRULE_SUNDAY = "SU";
    private static final String RRULE_SATURDAY = "SA";
    private static final String RRULE_MONDAY = "MO";
    private static final String RRULE_TUESDAY = "TU";
    private static final String RRULE_WEDNESDAY = "WE";
    private static final String RRULE_THURSDAY = "TH";
    private static final String RRULE_FRIDAY = "FR";
    private static final Map<Integer, String> RRULE_DAY_OF_WEEK_MAPPING = MapUtils.unmodifiableMap(new HashMap<>(){{
        put(1, RRULE_SUNDAY);
        put(2, RRULE_MONDAY);
        put(3, RRULE_TUESDAY);
        put(4, RRULE_WEDNESDAY);
        put(5, RRULE_THURSDAY);
        put(6, RRULE_FRIDAY);
        put(7, RRULE_SATURDAY);
    }});

    private static final List<String> RRULE_WEEKDAYS = ListUtils.unmodifiableList(new LinkedList<>() {{
        add(RRULE_MONDAY);
        add(RRULE_TUESDAY);
        add(RRULE_WEDNESDAY);
        add(RRULE_THURSDAY);
        add(RRULE_FRIDAY);
    }});
    private static final Map<CronFieldName, RRuleName> CRON_RRULE_MAPPING = MapUtils.unmodifiableMap(new HashMap<>(){{
        put(CronFieldName.SECOND, RRuleName.SECOND);
        put(CronFieldName.MINUTE, RRuleName.MINUTE);
        put(CronFieldName.HOUR, RRuleName.HOUR);
        put(CronFieldName.DAY_OF_MONTH, RRuleName.MONTHDAY);
        put(CronFieldName.MONTH, RRuleName.MONTH);
        put(CronFieldName.DAY_OF_WEEK, RRuleName.WEEKDAY);
        put(CronFieldName.DAY_OF_YEAR, RRuleName.YEARDAY);
        //no YEAR support in QUARTZ cron and rrule
    }});
    private final CronFieldName cronFieldName;
    private final RRuleName rruleName;
    private final FieldConstraints fieldConstraints;

    private final Map<RRuleName, List<String>> rruleParts;

    public CronExpressionRRulePartConverter(CronFieldName cronFieldName, FieldConstraints fieldConstraints) {
        this.cronFieldName = cronFieldName;
        this.rruleName = CRON_RRULE_MAPPING.get(this.cronFieldName);
        this.fieldConstraints = fieldConstraints;
        this.rruleParts = new HashMap<>();
    }

    protected void put(RRuleName rruleName, String value) {
        this.rruleParts.compute(rruleName, (k, v) -> {
            if (CollectionUtils.isEmpty(v) && value == null) {
                return new ArrayList<>();
            } else if (CollectionUtils.isEmpty(v) && value != null) {
                return new ArrayList<>() {{
                    add(value);
                }};
            } else {
                List<String> l = new ArrayList<>(v);
                l.add(value);
                return l;
            }
        });
    }

    /**
     * Day of week in cron and rrule have the same value, but string values have different length.
     * @param rRuleName rrule name
     * @return if rrule name is 'BYDAY' RRUlE_DAY_OF_WEEK_MAPPING is used. Otherwise, String::valueOf is used.
     */
    protected Function<Integer, String> getMappingFunction(RRuleName rRuleName) {
        if (rRuleName == RRuleName.WEEKDAY) {
            return RRULE_DAY_OF_WEEK_MAPPING::get;
        } else {
            return String::valueOf;
        }
    }

    /**
     * always is converted to nothing in rrule
     * @param always - Always instance, never null
     * @return the same Always instance
     */
    @Override
    public FieldExpression visit(Always always) {
        // intentionally left empty
        return always;
    }

    /**
     * visit all FieldExpression within the specified And instance
     * @param and - And instance, never null
     * @return the same And instance
     */
    @Override
    public FieldExpression visit(And and) {
        and.getExpressions().forEach(e -> e.accept(this));
        return and;
    }

    /**
     * Between is converted to a list of values in rrule. In Cron, day of week and month can be string. In rrule, only day of week is
     * string. Cron month and day of week string to int mapping is stored in the FieldConstraint. Month in Cron and rrule have the same int
     * value. Day of week in Cron is 3 letters but in rrule is 2 letters.
     * @param between - Between instance, never null
     * @return the same Between instance
     */
    @Override
    public
    FieldExpression visit(Between between) {
        Object fromValue = between.getFrom().getValue();
        Object toValue = between.getTo().getValue();
        Integer from, to;

        if (fromValue instanceof Integer && toValue instanceof Integer) {
            from = (Integer) fromValue;
            to = (Integer) toValue;
        } else if (fromValue instanceof String && toValue instanceof String) {
            from = this.fieldConstraints.getStringMappingValue(fromValue.toString());
            to = this.fieldConstraints.getStringMappingValue(toValue.toString());
        } else {
            throw new RuntimeException(fromValue.getClass().getCanonicalName() + " is not supported");
        }

        String rulePartStr = IntStream.rangeClosed(from, to).boxed().map(getMappingFunction(this.rruleName)).collect(Collectors.joining(","));
        this.put(this.rruleName, rulePartStr);
        return between;
    }

    /**
     * Every instance for day of month is converted to 'INTERVAL' in rrule. For other fields, period value is expanded into array of values.
     * @param every - Every instance, never null
     * @return the same Every instance
     */
    @Override
    public FieldExpression visit(Every every) {
        IntegerFieldValue period = every.getPeriod();
        if (period.getValue() != 1 && CronFieldName.DAY_OF_MONTH == this.cronFieldName) {
            this.put(RRuleName.INTERVAL, String.valueOf(period.getValue()));
        } else if (period.getValue() != 1) {
            String value = IntStream.rangeClosed(this.fieldConstraints.getStartRange(), this.fieldConstraints.getEndRange())
                    .filter(i -> i % period.getValue() == 0)
                    .boxed()
                    .map(getMappingFunction(this.rruleName))
                    .collect(Collectors.joining(","));
            this.put(this.rruleName, value);
        } else {
            // period == 1 is equivalent to always
        }
        return every;
    }

    /**
     * On instance can be converted to a single value. Special characters are handled here.
     * <a href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html#special-characters">cron special characters</a>
     * @param on - On instance, never null
     * @return the same On instance
     */
    @Override
    public FieldExpression visit(On on) {
        Integer time = on.getTime().getValue();
        Integer nth = on.getNth().getValue();
        switch (on.getSpecialChar().getValue()) {
            case L:
                if (CronFieldName.DAY_OF_MONTH == this.cronFieldName) {
                    //cron month day
                    this.put(this.rruleName, String.valueOf(-nth));
                } else if (CronFieldName.DAY_OF_WEEK == this.cronFieldName) {
                    //last xxx day of the week
                    String dayOfWeek = this.getMappingFunction(this.rruleName).apply(time);
                    this.put(this.rruleName, dayOfWeek);
                    this.put(RRuleName.BYSETPOS, String.valueOf(-1));
                }
                break;
            case W: // cron day of month
                this.put(RRuleName.UNSUPPORTED, null);
                break;
            case HASH: // cron day of week
                String dayOfWeek = this.getMappingFunction(this.rruleName).apply(time);
                this.put(this.rruleName, nth + dayOfWeek);
                break;
            case LW: // cron day of month
                String weekday = String.join(",", RRULE_WEEKDAYS);
                this.put(RRuleName.WEEKDAY, weekday);
                this.put(RRuleName.BYSETPOS, String.valueOf(-1));
                break;
            default:
                String val = this.getMappingFunction(this.rruleName).apply(time);
                this.put(this.rruleName, val);
                break;
        }
        return on;
    }

    /**
     * QuestionMark converts to nothing in rrule
     * @param questionMark - QuestionMark instance, never null
     * @return the same QuestionMark instance
     */
    @Override
    public FieldExpression visit(QuestionMark questionMark) {
        // intentionally left empty
        return questionMark;
    }

    @Override
    public Map<RRuleName, List<String>> getRRuleParts() {
        return this.rruleParts;
    }
}
