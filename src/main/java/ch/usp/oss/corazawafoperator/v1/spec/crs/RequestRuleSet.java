/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec.crs;

import java.util.Arrays;

import io.quarkus.qute.TemplateEnum;
import lombok.Getter;

/**
 * Enum of all request rule sets, including ones that are not allowed to be configured and ones always implicitly added.
 */
public enum RequestRuleSet {
	REQUEST_911_METHOD_ENFORCEMENT(911),
	REQUEST_913_SCANNER_DETECTION(913),

	REQUEST_920_PROTOCOL_ENFORCEMENT(920),
	REQUEST_921_PROTOCOL_ATTACK(921),
	REQUEST_922_MULTIPART_ATTACK(922),

	REQUEST_930_APPLICATION_ATTACK_LFI(930),
	REQUEST_931_APPLICATION_ATTACK_RFI(931),
	REQUEST_932_APPLICATION_ATTACK_RCE(932),
	REQUEST_933_APPLICATION_ATTACK_PHP(933),
	REQUEST_934_APPLICATION_ATTACK_GENERIC(934),

	REQUEST_941_APPLICATION_ATTACK_XSS(941),
	REQUEST_942_APPLICATION_ATTACK_SQLI(942),
	REQUEST_943_APPLICATION_ATTACK_SESSION_FIXATION(943),
	REQUEST_944_APPLICATION_ATTACK_JAVA(944);

    private final int number;
	RequestRuleSet(final int number) {
		this.number = number;
	}

	public String getRuleSetName() {
		return this.name().replace('_', '-');
	}
    public int getNumber() {return this.number;}

	public static RequestRuleSet get(final int number) {
		return Arrays.stream(RequestRuleSet.values())
				.filter(rs -> rs.getNumber() == number)
				.findAny()
				.orElse(null);
	}
}
