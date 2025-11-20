/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec.crs;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.quarkus.qute.TemplateData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

/**
 * Coraza WAF CRS request rule exception.
 */
@Jacksonized @Builder @Data @NoArgsConstructor @AllArgsConstructor @TemplateData
public class CorazaWafCrsRequestRuleException {

	@JsonPropertyDescription("A list of Rule IDs" +
			" || required")
	@Builder.Default
	private List<Integer> ruleIds = new LinkedList<>();

	@JsonPropertyDescription("Request part type (only has an effect if request rule exception)")
	private String requestPartType;

	@JsonPropertyDescription("Request part name (e.g. 'User-Agent'; only has an effect if request rule exception)")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String requestPartName;

	@JsonPropertyDescription("Path to which this exception is applied.")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String path;

    @JsonIgnore
    @SuppressWarnings("unused")
    public String getExceptionRuleAction() {
        return ruleIds.stream()
                .map(ruleId -> {
                    if (requestPartType == null) {
                        return "ctl:ruleRemoveById=" + ruleId;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append("ctl:ruleRemoveTargetById=").append(ruleId).append(";").append(requestPartType);
                        if (requestPartName != null) {
                            sb.append(":").append(requestPartName);
                        }
                        return sb.toString();
                    }
                })
                .collect(Collectors.joining(","));
    }
}
