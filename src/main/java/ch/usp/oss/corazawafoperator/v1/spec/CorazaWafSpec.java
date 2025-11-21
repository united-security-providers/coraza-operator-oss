/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec;


import ch.usp.oss.corazawafoperator.v1.Utils;
import ch.usp.oss.corazawafoperator.v1.spec.crs.CorazaWafCrs;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.*;
import io.quarkus.qute.TemplateData;
import jakarta.json.bind.annotation.JsonbNillable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;


@Jacksonized @Builder @Data @NoArgsConstructor @AllArgsConstructor @JsonbNillable @TemplateData
public class CorazaWafSpec {

    // Coraza limits to 1GB...
    private static final long REQ_BODY_LIMIT_MAX = Utils.toBytesAsLong("1Gb");
    private static final long RESP_BODY_LIMIT_MAX = Utils.toBytesAsLong("1Gb");

    /** Mode used both for CRS and GraphQL */
    public enum Mode { DETECT, BLOCK }

    /** Body limit action for requests and responses */
    public enum BodyLimitAction { ProcessPartial, Reject }

    @JsonPropertyDescription("Backend" +
            " || required")
    @Required
    private CorazaWafBackend backend;

    @JsonPropertyDescription("Whether to scan request bodies or not, " +
            "must be true if GraphQL is enabled on any route " +
            "(if this setting is disabled, POST parameters and other content submitted in the request body will not be inspected)" +
            " || default true")
    @Builder.Default
    @Pattern("true|false")
    @Default("true")
    private boolean requestBodyAccess = true;

    @JsonPropertyDescription("Request body limit in KB, body bytes beyond the limit are not parsed " +
            "(also make sure that operation.bufferLimitBytes is set accordingly)" +
            " || default 128, min 0, max 1048576")
    @Builder.Default
    @Min(0)
    @Max(1048576)
    @Default("128")
    private long requestBodyLimitKb = 128L;

    @JsonIgnore
    @SuppressWarnings("unused")
    public long getRequestBodyLimitBytes() {
        return Math.min(requestBodyLimitKb * 1024L, REQ_BODY_LIMIT_MAX);
    }
    @JsonPropertyDescription("How to handle requests with a larger body than specified in coraza.requestBodyLimitKb" +
            " (ProcessPartial = validate request body up to limit, let additional bytes through unchecked;" +
            " Reject = reject request if body is larger than limit)" +
            " || default ProcessPartial")
    @Builder.Default
    @Pattern("(ProcessPartial|Reject)")
    @Default("Reject")
    private BodyLimitAction requestBodyLimitAction =  BodyLimitAction.Reject;

    @JsonPropertyDescription("Whether to scan response bodies or not; " +
            "only allowed to set to false if coraza.crs.enabledResponseRules is empty " +
            "(GraphQL does so far not parse response bodies, the backend is trusted)" +
            " || default false")
    @Builder.Default
    @Pattern("true|false")
    @Default("false")
    private boolean responseBodyAccess = false;

    @JsonPropertyDescription("Response body limit in KB, body bytes beyond the limit are not parsed" +
            "(also make sure that operation.bufferLimitBytes is set accordingly)" +
            " || default 256, min 0, max 1048576")
    @Builder.Default
    @Min(0)
    @Max(1048576)
    @Default("256")
    private long responseBodyLimitKb = 256L;

    @JsonIgnore
    @SuppressWarnings("unused")
    public long getResponseBodyLimitBytes() {
        return Math.min(responseBodyLimitKb * 1024L, RESP_BODY_LIMIT_MAX);
    }

    @JsonPropertyDescription("How to handle responses with a larger body than specified in coraza.responseBodyLimitKb" +
            " (ProcessPartial = validate response body up to limit, let additional bytes through unchecked;" +
            " Reject = reject response if body is larger than limit)" +
            " || default ProcessPartial")
    @Builder.Default
    @Pattern("(ProcessPartial|Reject)")
    @Default("Reject")
    private BodyLimitAction responseBodyLimitAction =  BodyLimitAction.Reject;

    @JsonPropertyDescription("Whether to apply CRS protection rules for JSON payloads or not; " +
            "must be true if GraphQL is enabled on any route" +
            " || default true")
    @Builder.Default
    @Pattern("true|false")
    @Default("true")
    private boolean parseJson = true;

    @JsonPropertyDescription("Special rule which checks the syntax of JSON requests " +
            "(if the syntax is invalid and the current mode is BLOCK, such requests are blocked); " +
            "if set to true, across all routes with effectively enabled CRS and/or GraphQL " +
            "only either mode BLOCK or DETECT must be used" +
            " || default true")
    @Builder.Default
    @Pattern("true|false")
    @Default("true")
    private boolean validateJson = true;

    @JsonPropertyDescription("OWASP Core Rule Set (CRS) settings (version " + CorazaWafCrs.CRS_VERSION + ")")
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private CorazaWafCrs crs = new CorazaWafCrs();

}
