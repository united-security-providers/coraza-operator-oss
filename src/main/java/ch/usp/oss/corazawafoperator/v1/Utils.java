/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

public class Utils {
    private Utils() {
    }

    public static String createValidLabelString(String input) {
        String output = input.toLowerCase();
        output = output.replace(' ', '-');
        output = output.replaceAll("[^a-z0-9_-]", "_");
        return output;
    }

    private static final String UNIT_PREFIXES = "kMGTPE";
    private static final BigDecimal UNIT_BASE = BigDecimal.valueOf(1024);
    private static final Map<String, Integer> powerMap = initializePowerMap();

    private static Map<String, Integer> initializePowerMap() {
        Map<String, Integer> powerMap = new HashMap<>();
        {
            powerMap.put("", 0);
            char[] chars = UNIT_PREFIXES.toUpperCase().toCharArray();
            for (int i = 0; i < chars.length; i++) {
                powerMap.put(String.valueOf(chars[i]), i + 1);
            }
        }
        return powerMap;
    }

    public static long toBytesAsLong(String sizeString) throws NumberFormatException {
        return toBytes(sizeString).longValue();
    }
    public static BigDecimal toBytes(String sizeString) throws NumberFormatException {
        Pattern patt = Pattern.compile("^([\\d.,]+)\\s*([" + UNIT_PREFIXES.toUpperCase() + "]*)B*$", CASE_INSENSITIVE);
        Matcher matcher = patt.matcher(trimToEmpty(sizeString));
        if (matcher.find() && matcher.groupCount() == 2) {
            String strNumber = matcher.group(1);
            Integer pow = powerMap.get(matcher.group(2).toUpperCase());
            if (pow != null) {
                BigDecimal number = new BigDecimal(strNumber.replace(',', '.'));
                return number.multiply(UNIT_BASE.pow(pow));
            }
        }
        throw new NumberFormatException("Wrong format: " + sizeString);
    }
}
