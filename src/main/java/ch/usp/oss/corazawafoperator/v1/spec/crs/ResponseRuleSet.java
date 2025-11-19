/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec.crs;

import java.util.Arrays;

import lombok.Getter;

/**
 * Enum of all response rule sets, including ones that are not allowed to be configured and ones always implicitly added.
 */
public enum ResponseRuleSet {
	RESPONSE_950_DATA_LEAKAGES(950),
	RESPONSE_951_DATA_LEAKAGES_SQL(951),
	RESPONSE_952_DATA_LEAKAGES_JAVA(952),
	RESPONSE_953_DATA_LEAKAGES_PHP(953),
	RESPONSE_954_DATA_LEAKAGES_IIS(954),
	RESPONSE_955_WEB_SHELLS(955),
	RESPONSE_956_DATA_LEAKAGES_RUBY(956);

	private final int number;
	ResponseRuleSet(final int number) {
		this.number = number;
	}

	public String getRuleSetName() {
		return this.name().replace('_', '-');
	}
    public int getNumber() {return this.number;}

	public static ResponseRuleSet get(final int number) {
		return Arrays.stream(ResponseRuleSet.values())
				.filter(rs -> rs.getNumber() == number)
				.findAny()
				.orElse(null);
	}

}
