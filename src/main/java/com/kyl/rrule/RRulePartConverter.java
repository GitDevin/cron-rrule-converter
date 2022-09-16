package com.jsonar.rrule;

import java.util.List;
import java.util.Map;

import com.cronutils.model.field.expression.visitor.FieldExpressionVisitor;

/**
 * converter that converts cron FieldExpression to rrule part
 */
public interface RRulePartConverter extends FieldExpressionVisitor {

    /**
     * Get converted rrule parts. One FieldExpression may convert to multiple rrule parts
     * @return map contains rrule parts. key is the rrule name. value is rrule values in a list
     */
    Map<RRuleName, List<String>> getRRuleParts();
}
