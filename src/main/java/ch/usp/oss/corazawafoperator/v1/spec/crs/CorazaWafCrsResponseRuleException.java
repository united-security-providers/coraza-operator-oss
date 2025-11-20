/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec.crs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.quarkus.qute.TemplateData;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Coraza WAF CRS response rule exception.
 */
@Jacksonized @Builder @Data @NoArgsConstructor @AllArgsConstructor @TemplateData
@SuppressWarnings("java:S2166")
public class CorazaWafCrsResponseRuleException {

	@JsonPropertyDescription("A list of Rule IDs" +
			" || required")
	@Builder.Default
	private List<Integer> ruleIds = new LinkedList<>();

    @JsonIgnore
    @SuppressWarnings("unused")
    public String getExceptionRuleAction() {
        return ruleIds.stream()
                .map(ruleId -> "ctl:ruleRemoveById=" + ruleId)
                .collect(Collectors.joining(","));
    }

	@JsonPropertyDescription("Path")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String path;
}
