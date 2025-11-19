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

    @JsonIgnore
    public String getExceptionRuleAction() {
        return ruleIds.stream()
                .map(ruleId -> {
                    StringBuilder part = new StringBuilder();
                    part.append("ctl:ruleRemoveTargetById=").append(ruleId);
                    if (requestPartType != null) {
                        part.append(";").append(requestPartType);
                        if (requestPartName != null) {
                            part.append(":").append(requestPartName);
                        }
                    }
                    return part.toString();
                })
                .collect(Collectors.joining(","));
    }

	@JsonPropertyDescription("Request part type (only has an effect if request rule exception)")
	private String requestPartType;

	@JsonPropertyDescription("Request part name (e.g. 'User-Agent'; only has an effect if request rule exception)")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String requestPartName;

	@JsonPropertyDescription("Path to which this exception is applied.")
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private String path;
}
