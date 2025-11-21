/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec.crs;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.usp.oss.corazawafoperator.v1.spec.CorazaWafSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.generator.annotation.Default;
import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Pattern;
import io.quarkus.qute.TemplateData;
import jakarta.json.bind.annotation.JsonbNillable;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

/**
 * OWASP Coraza CRS (Core Rule Set) settings
 */
@Jacksonized @Builder @Data @NoArgsConstructor @AllArgsConstructor @JsonbNillable @TemplateData
public class CorazaWafCrs {

	/**
	 * CRS version - This should be only place in the code where the CRS version has to be set explicitly.
	 * If not possible for technical reasons in the future, list here where else to keep in sync.
	 * <p>
	 * NOTE: The CRS version here should match the CRS version in:
	 * - envoy proxy image
	 */
	public static final String CRS_VERSION = "4.18.0";

	public static final Set<RequestRuleSet> DEFAULT_REQUEST_RULE_SETS =
			Arrays.stream(RequestRuleSet.values()).collect(Collectors.toCollection(TreeSet::new));

    @JsonIgnore
    public String getCrsVersion() {
        return CRS_VERSION;
    }

	@JsonPropertyDescription("Mode (DETECT = traffic identified as suspicious is logged but not blocked; " +
			"BLOCK = traffic identified as suspicious is blocked)" +
			" || default BLOCK")
	@Builder.Default
	@Pattern("(BLOCK|DETECT)")
	@Default("BLOCK")
	private CorazaWafSpec.Mode mode = CorazaWafSpec.Mode.BLOCK;

	@JsonPropertyDescription("Defines under which conditions suspicious requests are blocked; " +
			"only has an effect if the mode is set to BLOCK " +
			" || default 5, min 1")
	@Builder.Default
	@Min(1)
	@Default("5")
	private int requestAnomalyScore = 5;

    @JsonPropertyDescription("Defines under which conditions suspicious responses are blocked; " +
            "only has an effect if the mode is set to BLOCK " +
            " || default 4, min 1")
    @Builder.Default
    @Min(1)
    @Default("4")
    private int responseAnomalyScore = 4;

	@JsonPropertyDescription("Paranoia level (the higher the level the better the protection " +
			"but also more likely false positives, see OWASP CRS for details)")
    @EqualsAndHashCode.Exclude
    @Builder.Default
	private ParanoiaLevel paranoiaLevel = new ParanoiaLevel();

    @Jacksonized @Builder(toBuilder = true) @Data @NoArgsConstructor @AllArgsConstructor @JsonbNillable
    public static class ParanoiaLevel {
        @JsonPropertyDescription("Enforcing Paranoia level (the higher the level the better the protection " +
                "but also more likely false positives, see OWASP CRS for details)" +
                " || default 1, min 1, max 4")
        @Builder.Default
        @Min(1)
        @Max(4)
        @Default("1")
        private int enforcing = 1;

        @JsonPropertyDescription("Reporting Paranoia level " +
                "it's always at least as the enforcing paranoia Level." +
                " || default 1, min 1, max 4")
        @Builder.Default
        @Min(1)
        @Max(4)
        @Default("1")
        private int detecting = 1;

    }
	@JsonPropertyDescription("Set of request rule classes (default is to include all rules, " +
			"rules REQUEST_901_INITIALIZATION and REQUEST_949_BLOCKING_EVALUATION are always included, " +
			"see https://github.com/coreruleset/coreruleset/tree/v" + CRS_VERSION + "/rules for all configurable values, " +
			"just replace '-' by '_' and omit '.conf')")
	@Builder.Default
	// allow to include empty because not present means all while empty should mean empty
	@JsonInclude(JsonInclude.Include.NON_ABSENT)
	@JsonDeserialize(as=TreeSet.class)
	private Set<RequestRuleSet> enabledRequestRules = DEFAULT_REQUEST_RULE_SETS;

	@JsonPropertyDescription("Conditionally disable request rules to avoid false positive alerts/blocks")
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<CorazaWafCrsRequestRuleException> requestRuleExceptions = new LinkedList<>();

	@JsonPropertyDescription("Set of response rule classes (default is to include no rules, " +
			"rules RESPONSE_959_BLOCKING_EVALUATION and RESPONSE_980_CORRELATION are always included, " +
			"see https://github.com/coreruleset/coreruleset/tree/v" + CRS_VERSION + "/rules for all configurable values, " +
			"just replace '-' by '_' and omit '.conf')")
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonDeserialize(as=TreeSet.class)
	private Set<ResponseRuleSet> enabledResponseRules = new TreeSet<>();

	@JsonPropertyDescription("Conditionally disable response rules to avoid false positive alerts/blocks")
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private List<CorazaWafCrsResponseRuleException> responseRuleExceptions = new LinkedList<>();

    @JsonPropertyDescription("Path specific settings for allowed Method or Content-Types, etc.")
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CorazaWafPathSettings> pathSettings = new LinkedList<>();

}
