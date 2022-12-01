package com.kyl.rrule;

import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpression;
import com.cronutils.model.field.expression.On;
import com.cronutils.model.field.value.IntegerFieldValue;
import com.cronutils.parser.CronParser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.cronutils.model.CronType.QUARTZ;

public class QuartzCronRRuleConverterTest {
    private static CronParser CRON_PARSER;

    private QuartzCronRRuleConverter converter;

    @BeforeAll
    public static void setUpAll() {
        CRON_PARSER = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(QUARTZ));
    }

    @BeforeEach
    public void setUpEach() {
        this.converter = new QuartzCronRRuleConverter();
    }

    @Test
    public void testIsFrequency() {
        boolean result = this.converter.isFrequency(FieldExpression.always());

        Assertions.assertTrue(result);
    }

    @Test
    public void testIsNotFrequency() {
        boolean result = this.converter.isFrequency(new On(new IntegerFieldValue(2)));

        Assertions.assertFalse(result);
    }

    @Test
    public void testIsFrequencyWithQuestionMark() {
        boolean result = this.converter.isFrequency(FieldExpression.questionMark());

        Assertions.assertFalse(result);
    }

    @Test
    public void testFindFrequency() {
        Cron cron = CRON_PARSER.parse("0 0 0 ? * 2#1 *");
        RRuleFrequency result = this.converter.findFrequency(cron.retrieveFieldsAsMap());

        assertSame(RRuleFrequency.MONTHLY, result);
    }

    @Test
    public void testFindFrequencyEverySecond() {
        Cron cron = CRON_PARSER.parse("* * * * * ? *");
        RRuleFrequency result = this.converter.findFrequency(cron.retrieveFieldsAsMap());

        assertSame(RRuleFrequency.SECONDLY, result);
    }

    @Test
    public void testFindFrequencyDaily() {
        Cron cron = CRON_PARSER.parse("0 0 0 1/1 * ? *");
        RRuleFrequency result = this.converter.findFrequency(cron.retrieveFieldsAsMap());

        assertSame(RRuleFrequency.DAILY, result);
    }

    @Test
    public void testFindFrequencyUnsupportedYearlyDefaultToMonthly() {
        Cron cron = CRON_PARSER.parse("0 0 0 1 1 ? *");
        RRuleFrequency result = this.converter.findFrequency(cron.retrieveFieldsAsMap());

        assertSame(RRuleFrequency.MONTHLY, result);
    }

    @Test
    public void testConvertComplicatedCron() {
        Cron input = CRON_PARSER.parse("*/15 */10 4,8 ? 1,6,7 1-3,6,7");
        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(6, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=SECONDLY"));
        Assertions.assertTrue(rruleParts.contains("BYMONTH=1,6,7"));
        Assertions.assertTrue(rruleParts.contains("BYDAY=SU,MO,TU,FR,SA"));
        Assertions.assertTrue(rruleParts.contains("BYHOUR=4,8"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=0,10,20,30,40,50"));
        Assertions.assertTrue(rruleParts.contains("BYSECOND=0,15,30,45"));
    }

    @Test
    public void testConvertWithMonthDayOfWeekStrings() {
        Cron input = CRON_PARSER.parse("0/5 14,18,20-39,52 * ? JAN,MAR,SEP MON-FRI 2002-2010");

        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(5, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=SECONDLY"));
        Assertions.assertTrue(rruleParts.contains("BYMONTH=1,3,9"));
        Assertions.assertTrue(rruleParts.contains("BYDAY=MO,TU,WE,TH,FR"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=14,18,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,52"));
        Assertions.assertTrue(rruleParts.contains("BYSECOND=0,5,10,15,20,25,30,35,40,45,50,55"));
    }

    @Test
    public void testConvertWithTwoEvery() {
        Cron input = CRON_PARSER.parse("* */20 */10 ? 1,6,7 1-3,6,7");

        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(5, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=SECONDLY"));
        Assertions.assertTrue(rruleParts.contains("BYMONTH=1,6,7"));
        Assertions.assertTrue(rruleParts.contains("BYDAY=SU,MO,TU,FR,SA"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=0,20,40"));
        Assertions.assertTrue(rruleParts.contains("BYHOUR=0,10,20"));
    }

    @Test
    public void testSpecialL() {
        Cron input = CRON_PARSER.parse("0 30 10 ? * 5L");

        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(6, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=MONTHLY"));
        Assertions.assertTrue(rruleParts.contains("BYDAY=TH"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=30"));
        Assertions.assertTrue(rruleParts.contains("BYHOUR=10"));
        Assertions.assertTrue(rruleParts.contains("BYSECOND=0"));
        Assertions.assertTrue(rruleParts.contains("BYSETPOS=-1"));
    }

    @Test
    public void testCronWithSpecialW() {
        Cron input = CRON_PARSER.parse("0 30 10 2w1 * ?");

        String result = this.converter.convert(input, null);

        Assertions.assertNull(result);
    }

    @Test
    public void testSpecialMonthL() {
        Cron input = CRON_PARSER.parse("0 0 0 L-5 * ? *");

        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(5, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=MONTHLY"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=0"));
        Assertions.assertTrue(rruleParts.contains("BYHOUR=0"));
        Assertions.assertTrue(rruleParts.contains("BYSECOND=0"));
        Assertions.assertTrue(rruleParts.contains("BYMONTHDAY=-5"));
    }

    @Test
    public void testSpecialWeekL() {
        Cron input = CRON_PARSER.parse("0 0 0 ? * 4L *");

        String result = this.converter.convert(input, null);

        Assertions.assertNotNull(result);

        Set<String> rruleParts = new HashSet<>(Arrays.asList(result.split(";")));
        assertEquals(6, rruleParts.size());
        Assertions.assertTrue(rruleParts.contains("RRULE:FREQ=MONTHLY"));
        Assertions.assertTrue(rruleParts.contains("BYMINUTE=0"));
        Assertions.assertTrue(rruleParts.contains("BYHOUR=0"));
        Assertions.assertTrue(rruleParts.contains("BYSECOND=0"));
        Assertions.assertTrue(rruleParts.contains("BYSETPOS=-1"));
        Assertions.assertTrue(rruleParts.contains("BYDAY=WE"));
    }

    @Test
    public void testFindFrequencyWeekly() {
        Cron input = CRON_PARSER.parse("0 0 7 ? * 2-6 *");

        RRuleFrequency result = this.converter.findFrequency(input.retrieveFieldsAsMap());

        assertEquals(RRuleFrequency.WEEKLY, result);
    }

    @Test
    public void testconvertToDTStartFormat() {
        String result = this.converter.convertToDTStartFormat(LocalDateTime.of(2022, 10, 4, 15, 7, 40));

        Assertions.assertNotNull(result);
        Assertions.assertEquals("20221004T150740Z", result);
    }
}
