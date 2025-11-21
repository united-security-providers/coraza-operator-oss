package ch.usp.oss.corazawafoperator.v1.spec.crs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.generator.annotation.Required;
import io.quarkus.qute.TemplateData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Coraza WAF CRS Path specific settings for method and content-types.
 */
@Jacksonized
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@TemplateData
public class CorazaWafPathSettings {



    /** HTTP Methods
     * Source <a href="https://www.iana.org/assignments/http-methods/http-methods.xhtml">IANA Hypertext Transfer Protocol (HTTP) Method Registry</a>
     **/
    public enum HttpMethod { ACL, BASELINE_CONTROL, BIND, CHECKIN, CHECKOUT, CONNECT, COPY, DELETE, GET, HEAD, LABEL,
        LINK, LOCK, MERGE, MKACTIVITY, MKCALENDAR, MKCOL, MKREDIRECTREF, MKWORKSPACE, MOVE, OPTIONS, ORDERPATCH,
        PATCH, POST, PRI, PROPFIND, PROPPATCH, PUT, REBIND, REPORT, SEARCH, TRACE, UNBIND, UNCHECKOUT, UNLINK, UNLOCK,
        UPDATE, UPDATEREDIRECTREF, VERSION_CONTROL;

        @Override
        public String toString() {
            return name().replace('_', '-');
        }
    }

    public enum ContentTypeParser {
        URLENCODED, MULTIPART, XML, JSON
    }

    @JsonPropertyDescription("Path to which these settings applies. || required")
    @Required
    private String path;

    @JsonPropertyDescription("Additionally allowed Content-Types for this Path (Exception for Rule 920420)")
    private String allowedContentType;


    @JsonPropertyDescription("Request Body parser to use for this content-type")
    private ContentTypeParser contentTypeParser;


    @JsonPropertyDescription("A list of allowed HTTP Methods for this Path (Rule 911100)")
    @Builder.Default
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonDeserialize(as=TreeSet.class)
    private Set<HttpMethod> allowedMethods = new TreeSet<>();

    @JsonIgnore
    @SuppressWarnings("unused")
    public String getAllowedMethodsString() {
        return allowedMethods.stream().map(HttpMethod::toString).collect(Collectors.joining(" "));
    }
}
