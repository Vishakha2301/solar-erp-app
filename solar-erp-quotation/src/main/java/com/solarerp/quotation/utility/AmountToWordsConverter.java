package com.solarerp.quotation.utility;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AmountToWordsConverter {

    private static final String[] ONES = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
            "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty",
            "Sixty", "Seventy", "Eighty", "Ninety"
    };

    private static final String[] THOUSANDS = {
            "", "Thousand", "Million", "Billion"
    };

    public String convert(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");

        // Scale to 2 decimal places
        amount = amount.setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "Minus " + convert(amount.abs());
        }

        // Split whole and fractional parts
        BigDecimal[] parts = amount.divideAndRemainder(BigDecimal.ONE);
        long wholePart = parts[0].longValueExact();
        int cents = parts[1].multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();

        String result = wholePart == 0 ? "Zero" : convertWholeNumber(wholePart);

        if (cents > 0) {
            result += " and " + String.format("%02d", cents) + "/100";
        }

        return result.trim();
    }

    private String convertWholeNumber(long number) {
        if (number == 0) return "Zero";

        String result = "";
        int groupIndex = 0;

        while (number > 0) {
            int group = (int) (number % 1000);
            if (group != 0) {
                String groupWords = convertHundreds(group);
                String suffix = THOUSANDS[groupIndex];
                result = groupWords + (suffix.isEmpty() ? "" : " " + suffix)
                        + (result.isEmpty() ? "" : " " + result);
            }
            number /= 1000;
            groupIndex++;
        }

        return result.trim();
    }

    private String convertHundreds(int number) {
        String result = "";

        if (number >= 100) {
            result += ONES[number / 100] + " Hundred";
            number %= 100;
            if (number > 0) result += " ";
        }

        if (number >= 20) {
            result += TENS[number / 10];
            if (number % 10 > 0) result += " " + ONES[number % 10];
        } else if (number > 0) {
            result += ONES[number];
        }

        return result;
    }
}
