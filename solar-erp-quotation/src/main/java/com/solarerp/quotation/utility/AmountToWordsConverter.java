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

    public String convert(BigDecimal amount) {
        if (amount == null) throw new IllegalArgumentException("Amount cannot be null");

        amount = amount.setScale(2, RoundingMode.HALF_UP);

        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            return "Minus " + convert(amount.abs());
        }

        long totalCents = amount.multiply(new BigDecimal("100"))
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();

        long wholePart = totalCents / 100;
        int  cents     = (int) (totalCents % 100);

        String result = wholePart == 0 ? "Zero" : convertIndian(wholePart);

        if (cents > 0) {
            result += " and " + String.format("%02d", cents) + "/100";
        }

        return result.trim();
    }

    private String convertIndian(long number) {
        if (number == 0) return "";

        // Indian grouping: ones(3) | thousands(2) | lakhs(2) | crores(2) | ...
        long crore     = number / 10_000_000L;
        long lakh      = (number % 10_000_000L) / 100_000L;
        long thousand  = (number % 100_000L)     / 1_000L;
        long hundred   = (number % 1_000L)       / 100L;
        long remainder = number % 100L;

        StringBuilder result = new StringBuilder();

        if (crore > 0) {
            result.append(convertIndian(crore)).append(" Crore");
        }
        if (lakh > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(convertTwoDigits((int) lakh)).append(" Lakh");
        }
        if (thousand > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(convertTwoDigits((int) thousand)).append(" Thousand");
        }
        if (hundred > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(ONES[(int) hundred]).append(" Hundred");
        }
        if (remainder > 0) {
            if (result.length() > 0) result.append(" ");
            result.append(convertTwoDigits((int) remainder));
        }

        return result.toString();
    }

    private String convertTwoDigits(int number) {
        if (number < 20) {
            return ONES[number];
        }
        String result = TENS[number / 10];
        if (number % 10 > 0) {
            result += " " + ONES[number % 10];
        }
        return result;
    }
}