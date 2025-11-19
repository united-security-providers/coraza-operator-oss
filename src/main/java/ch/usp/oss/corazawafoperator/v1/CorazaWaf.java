/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1;

import ch.usp.oss.corazawafoperator.v1.spec.CorazaWafSpec;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

import static ch.usp.oss.corazawafoperator.v1.CorazaWaf.VERSION;

@Version(VERSION)
@Group("oss.u-s-p.ch")
@SuppressWarnings("serial")
public class CorazaWaf extends CustomResource<CorazaWafSpec, CorazaWafStatus> implements Namespaced {

    public static final String VERSION = "v1alpha1";
    public static final String NAME_PREFIX = "corazawaf-";
    public static final String OPERATOR_NAME = NAME_PREFIX + "operator";
    public static final IntOrString TRAFFIC_CONTAINER_PORT = new IntOrString(8080);
    public static final IntOrString ADMIN_CONTAINER_PORT = new IntOrString(9901);

    @JsonIgnore
    public CorazaWafStatus getStatusOrInit() {
        CorazaWafStatus status = getStatus();
        if (status == null) {
            status = new CorazaWafStatus();
            this.setStatus(status);
        }
        return status;
    }

    @Override
    public String toString() {
        return "Coraza WAF { " +
                "metadata=" + this.getMetadata().toString() +
                "; spec=" + spec +
                "; status=" + status +
                "}";
    }

    @Override
    public void setSpec(CorazaWafSpec spec) {
        this.spec = spec;
    }
}
