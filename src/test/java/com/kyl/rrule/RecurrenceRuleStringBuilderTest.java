package com.kyl.rrule;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RecurrenceRuleStringBuilderTest {
    private RecurrenceRuleStringBuilder builder;

    @BeforeEach
    public void setUp() {
        this.builder = new RecurrenceRuleStringBuilder(RRuleFrequency.SECONDLY);
        this.builder.withRRuleParts(new HashMap<>());
    }

    @Test
    public void testBuildMinimalRRuleString() {
        String result = this.builder.build();
        assertEquals("RRULE:FREQ=SECONDLY", result);
    }

    @Test
    public void testBuildRRuleStringWithDTStart() {
        this.builder.withDTStart("dtstart");

        String result = this.builder.build();

        assertEquals("DTSTART:dtstart\nRRULE:FREQ=SECONDLY", result);
    }

    @Test
    public void testBuildWithRRulePart() {
        this.builder.withRRuleParts(new HashMap<>(){{
            put(RRuleName.HOUR, List.of("3","4","5"));
        }});

        String result = this.builder.build();
        assertEquals("RRULE:FREQ=SECONDLY;BYHOUR=3,4,5", result);
    }

    @Test
    public void testBuildWithRRuleParts() {
        this.builder.withRRuleParts(new HashMap<>(){{
            put(RRuleName.HOUR, List.of("1,3,4", "6,7"));
        }});

        String result = this.builder.build();
        assertEquals("RRULE:FREQ=SECONDLY;BYHOUR=1,3,4,6,7", result);
    }
}
