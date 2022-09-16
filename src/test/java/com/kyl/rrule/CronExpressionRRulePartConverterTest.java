package com.jsonar.rrule;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cronutils.model.field.CronFieldName;
import com.cronutils.model.field.constraint.FieldConstraints;
import com.cronutils.model.field.expression.And;
import com.cronutils.model.field.expression.Between;
import com.cronutils.model.field.expression.Every;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.FieldValue;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.model.field.value.SpecialChar;
import com.cronutils.model.field.value.SpecialCharFieldValue;

import static org.junit.jupiter.api.Assertions.*;
import test.tags.UnitTest;

@UnitTest
public class CronExpressionRRulePartConverterTest {
    private static final int START_RANGE = 1;
    private static final int END_RANGE = 5;
    private RRulePartConverter converter;
    @BeforeEach
    public void setUp() {
        FieldConstraints constraints = new FieldConstraints(new HashMap<>(){{
            put("One", 1);
            put("Two", 2);
            put("Three", 3);
            put("Four", 4);
            put("Five", 5);
        }}, new HashMap<>(), new HashSet<>(), START_RANGE, END_RANGE, false);
        this.converter = new CronExpressionRRulePartConverter(CronFieldName.SECOND, constraints);
    }

    @Test
    public void testVisitAlways() {
        FieldExpression always = FieldExpression.always();
        always.accept(this.converter);
        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testVisitAnd() {
        Between between = new Between(new IntegerFieldValue(1), new IntegerFieldValue(3));
        On on = new On(new IntegerFieldValue(5));
        And and = new And().and(between).and(on);
        and.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.SECOND);
        assertNotNull(values);
        assertEquals(2, values.size());
        assertEquals(List.of("1,2,3", "5"), values);
    }

    @Test
    public void testVisitBetweenInteger() {
        Between between = new Between(new IntegerFieldValue(2), new IntegerFieldValue(4));
        between.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.SECOND);
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("2,3,4"), values);
    }

    @Test
    public void testVisitBetweenString() {
        Between between = new Between(new FieldValue<String>() {
            @Override
            public String getValue() {
                return "Two";
            }
        }, new FieldValue<String>() {
            @Override
            public String getValue() {
                return "Four";
            }
        });
        between.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.SECOND);
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("2,3,4"), values);
    }

    @Test
    public void testVisitBetweenInvalidType() {
        Between between = new Between(new FieldValue<Long>() {
            @Override
            public Long getValue() {
                return 2L;
            }
        }, new FieldValue<Long>() {
            @Override
            public Long getValue() {
                return 3L;
            }
        });

        RuntimeException result = assertThrows(RuntimeException.class, () -> between.accept(this.converter));

        assertNotNull(result.getMessage());
    }

    @Test
    public void testVisitEvery() {
        Every every = new Every(new IntegerFieldValue(2));

        every.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();
        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.SECOND);

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("2,4"), values);
    }

    @Test
    public void testVisitEveryDayOfMonth() {
        this.converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_MONTH,
                new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), START_RANGE, END_RANGE, false));

        Every every = new Every(new IntegerFieldValue(2));

        every.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();
        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.INTERVAL);

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("2"), values);
    }

    @Test
    public void testVisitEveryWithIntervalEqualsToOne() {
        Every every = new Every(new IntegerFieldValue(1));

        every.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testVisitOn() {
        FieldExpression on = new On(new IntegerFieldValue(3));
        on.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.SECOND);

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("3"), values);
    }

    @Test
    public void testVisitOnWithDayOfMonthL() {
        FieldExpression on = new On(new IntegerFieldValue(-1), new SpecialCharFieldValue(SpecialChar.L), new IntegerFieldValue(5));

        CronExpressionRRulePartConverter converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_MONTH, new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), 1, 7, false));

        on.accept(converter);

        Map<RRuleName, List<String>> result = converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values1 = result.get(RRuleName.MONTHDAY);

        assertNotNull(values1);
        assertEquals(1, values1.size());
        assertEquals(List.of("-5"), values1);
    }

    @Test
    public void testVisitOnWithDayW() {
        FieldExpression on = new On(new IntegerFieldValue(5), new SpecialCharFieldValue(SpecialChar.W));

        CronExpressionRRulePartConverter converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_MONTH, new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), 1, 7, false));

        on.accept(converter);

        Map<RRuleName, List<String>> result = converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.UNSUPPORTED);
        assertNotNull(values);
        assertTrue(values.isEmpty());
    }

    @Test
    public void testVisitOnWithDayLW() {
        FieldExpression on = new On(new IntegerFieldValue(-1), new SpecialCharFieldValue(SpecialChar.LW));

        CronExpressionRRulePartConverter converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_MONTH, new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), 1, 7, false));

        on.accept(converter);

        Map<RRuleName, List<String>> result = converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(2, result.size());

        List<String> values = result.get(RRuleName.WEEKDAY);
        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("MO,TU,WE,TH,FR"), values);

        List<String> values1 = result.get(RRuleName.BYSETPOS);
        assertNotNull(values1);
        assertEquals(1, values1.size());
        assertEquals(List.of("-1"), values1);
    }

    @Test
    public void testVisitOnWithDayOfWeekL() {
        FieldExpression on = new On(new IntegerFieldValue(5), new SpecialCharFieldValue(SpecialChar.L));

        CronExpressionRRulePartConverter converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_WEEK, new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), 1, 7, false));

        on.accept(converter);

        Map<RRuleName, List<String>> result = converter.getRRuleParts();

        assertNotNull(result);
        assertEquals(2, result.size());

        List<String> values = result.get(RRuleName.WEEKDAY);

        assertNotNull(values);
        assertEquals(1, values.size());
        assertEquals(List.of("TH"), values);

        List<String> values1 = result.get(RRuleName.BYSETPOS);

        assertNotNull(values1);
        assertEquals(1, values1.size());
        assertEquals(List.of("-1"), values1);
    }

    @Test
    public void testVisitOnWithHash() {
        FieldExpression on = new On(new IntegerFieldValue(5), new SpecialCharFieldValue(SpecialChar.HASH), new IntegerFieldValue(-3));

        CronExpressionRRulePartConverter converter = new CronExpressionRRulePartConverter(CronFieldName.DAY_OF_WEEK, new FieldConstraints(new HashMap<>(), new HashMap<>(), new HashSet<>(), 1, 7, false));

        on.accept(converter);

        Map<RRuleName, List<String>> result = converter.getRRuleParts();
        assertNotNull(result);
        assertEquals(1, result.size());

        List<String> values = result.get(RRuleName.WEEKDAY);
        assertEquals(1, result.size());
        assertEquals(List.of("-3TH"), values);
    }

    @Test
    public void testVisitQuestionMark() {
        FieldExpression questionMark = FieldExpression.questionMark();
        questionMark.accept(this.converter);

        Map<RRuleName, List<String>> result = this.converter.getRRuleParts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
