/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.spec;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.fabric8.generator.annotation.Max;
import io.fabric8.generator.annotation.Min;
import io.fabric8.generator.annotation.Pattern;
import io.fabric8.generator.annotation.Required;
import io.quarkus.qute.TemplateData;
import jakarta.json.bind.annotation.JsonbNillable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;


@Jacksonized @Builder @Data @NoArgsConstructor @AllArgsConstructor @JsonbNillable @TemplateData
public class CorazaWafBackend {

    @JsonPropertyDescription("Backend Service hostname" +
            " || required")
    @Required
    @Pattern(".") // non-empty
    private String hostname;

    @JsonPropertyDescription("Backend port number" +
            " || required, min 1, max 65535")
    @Required
    @Min(1)
    @Max(65535)
    private int port;

}
