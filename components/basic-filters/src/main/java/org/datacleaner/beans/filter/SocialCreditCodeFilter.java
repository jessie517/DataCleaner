package org.datacleaner.beans.filter;

import org.datacleaner.api.*;
import org.datacleaner.components.categories.FilterCategory;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Jessie on 2018/3/8.
 */
@Named("\u7edf\u4e00\u793e\u4f1a\u4fe1\u7528\u4ee3\u7801\u6821\u9a8c")
@Description("过滤统一社会信用代码不合法的记录.")
@Categorized(FilterCategory.class)
@Distributed(true)
public class SocialCreditCodeFilter implements Filter<ValidationCategory>, HasLabelAdvice {
    @Configured
    InputColumn<String> inputColumn;

    private int power[] = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};


    public SocialCreditCodeFilter() {

    }

    public SocialCreditCodeFilter(final InputColumn<String> column) {
        this();
        this.inputColumn = column;
    }

    @Override
    public String getSuggestedLabel() {
        if (inputColumn == null) {
            return null;
        }

        return inputColumn.getName() + " 统一社会信用代码校验";
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        final Object inputValue = inputRow.getValue(inputColumn);
        if (inputValue == null) {
            return ValidationCategory.INVALID;
        }

        String socialCreditCode = String.valueOf(inputValue);

        // 非18位
        if (socialCreditCode.length() != 18) {
            return ValidationCategory.INVALID;
        }

        if(!socialCreditCode.matches("^[1-9A-GY][1239][1-5][0-9]{5}[0-9A-Z]{10}$")){
            return ValidationCategory.INVALID;
        }

        // 统一社会信用代码中允许使用的字符
        String baseCode = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        char[] baseCodeArray = baseCode.toCharArray();
        Map<Character, Integer> codes = new HashMap<Character, Integer>();
        for (int i = 0; i < baseCode.length(); i++) {
            codes.put(baseCodeArray[i], i);
        }

        char[] socialCreditCodeArray = socialCreditCode.toCharArray();
        Character checkBit = socialCreditCodeArray[17];

        // 校验位不是合法字符，直接返回假
        if (!codes.containsKey(checkBit)) {
            return ValidationCategory.INVALID;
        }

        int[] power = {1, 3, 9, 27, 19, 26, 16, 17, 20, 29, 25, 13, 8, 24, 10, 30, 28};
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            Character key = socialCreditCodeArray[i];
            if (baseCode.indexOf(key) == -1) {
                return ValidationCategory.INVALID;
            }
            sum += (codes.get(key) * power[i]);
        }

        int value = 31 - sum % 31;
        if (value != codes.get(checkBit)) {
            return ValidationCategory.INVALID;
        }

        return ValidationCategory.VALID;
    }

}
