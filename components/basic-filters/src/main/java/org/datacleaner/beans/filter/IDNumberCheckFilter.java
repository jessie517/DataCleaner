package org.datacleaner.beans.filter;

import org.apache.metamodel.query.FilterItem;
import org.apache.metamodel.query.OperatorType;
import org.apache.metamodel.query.Query;
import org.apache.metamodel.query.SelectItem;
import org.apache.metamodel.schema.Column;
import org.datacleaner.api.*;
import org.datacleaner.components.categories.FilterCategory;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jessie on 2018/3/8.
 */
@Named("\u8eab\u4efd\u8bc1\u53f7\u6821\u9a8c")
@Description("过滤身份证号不合法的记录.")
@Categorized(FilterCategory.class)
@Distributed(true)
public class IDNumberCheckFilter implements Filter<ValidationCategory>, HasLabelAdvice {
    @Configured
    InputColumn<String> inputColumn;

    private int power[] = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};


    public IDNumberCheckFilter() {

    }

    public IDNumberCheckFilter(final InputColumn<String> column) {
        this();
        this.inputColumn = column;
    }

    @Override
    public String getSuggestedLabel() {
        if (inputColumn == null) {
            return null;
        }

        return inputColumn.getName() + " 身份证号校验";
    }

    @Override
    public ValidationCategory categorize(InputRow inputRow) {
        final Object inputValue = inputRow.getValue(inputColumn);
        if (inputValue == null) {
            return ValidationCategory.INVALID;
        }

        String idcard = String.valueOf(inputValue);
        // 非18位
        if (idcard.length() != 18) {
            return ValidationCategory.INVALID;
        }

        // 获取前17位
        String idcard17 = idcard.substring(0, 17);

        // 获取第18位
        String idcard18Code = idcard.substring(17, 18);

        char c[];
        String checkCode = "";

        // 是否都为数字
        if (isDigital(idcard17)) {
            c = idcard17.toCharArray();
        } else {
            return ValidationCategory.INVALID;
        }

        int bit[] = new int[idcard17.length()];

        bit = converCharToInt(c);

        int sum17 = 0;

        sum17 = getPowerSum(bit);

        // 将和值与11取模得到余数进行校验码判断
        checkCode = getCheckCodeBySum(sum17);

        // 将身份证的第18位与算出来的校码进行匹配，不相等就为假
        if (null == checkCode || !idcard18Code.equalsIgnoreCase(checkCode)) {
            return ValidationCategory.INVALID;
        }

        return ValidationCategory.VALID;
    }

    /**
     * 数字验证
     *
     * @param str
     * @return
     */
    private boolean isDigital(String str) {
        return str == null || "".equals(str) ? false : str.matches("^[0-9]*$");
    }

    private int[] converCharToInt(char[] c) throws NumberFormatException {
        int[] a = new int[c.length];
        int k = 0;
        for (char temp : c) {
            a[k++] = Integer.parseInt(String.valueOf(temp));
        }
        return a;
    }

    /**
     * 将身份证的每位和对应位的加权因子相乘之后，再得到和值
     *
     * @param bit
     * @return
     */
    private int getPowerSum(int[] bit) {
        int sum = 0;
        if (power.length != bit.length) {
            return sum;
        }

        for (int i = 0; i < bit.length; i++) {
            for (int j = 0; j < power.length; j++) {
                if (i == j) {
                    sum = sum + bit[i] * power[j];
                }
            }
        }
        return sum;
    }

    /**
     * 将和值与11取模得到余数进行校验码判断
     *
     * @param sum17
     * @return 校验位
     */
    private String getCheckCodeBySum(int sum17) {
        String checkCode = null;
        switch (sum17 % 11) {
            case 10:
                checkCode = "2";
                break;
            case 9:
                checkCode = "3";
                break;
            case 8:
                checkCode = "4";
                break;
            case 7:
                checkCode = "5";
                break;
            case 6:
                checkCode = "6";
                break;
            case 5:
                checkCode = "7";
                break;
            case 4:
                checkCode = "8";
                break;
            case 3:
                checkCode = "9";
                break;
            case 2:
                checkCode = "x";
                break;
            case 1:
                checkCode = "0";
                break;
            case 0:
                checkCode = "1";
                break;
        }
        return checkCode;
    }

}
