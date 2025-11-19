/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1;

import ch.usp.oss.corazawafoperator.v1.spec.CorazaWafBackend;
import io.quarkus.qute.*;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.usp.oss.corazawafoperator.v1.CorazaWaf.ADMIN_CONTAINER_PORT;
import static ch.usp.oss.corazawafoperator.v1.CorazaWaf.TRAFFIC_CONTAINER_PORT;
import static ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafEnvoyConfigMapResource.FILENAME_CDS;
import static ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafEnvoyConfigMapResource.FILENAME_LDS;
import static ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafDeploymentResource.ENVOY_CONFIG_PATH;

@ApplicationScoped
public class CorazaWafEnvoyConfigTemplate {

    record envoy(EnvoyTemplateData envoyTemplateData) implements TemplateInstance {}
    record lds( CorazaWaf corazaWaf, int trafficPort, String goFilterPath)implements TemplateInstance {}
    record cds( CorazaWafBackend backend) implements TemplateInstance {}

    @ConfigProperty(name = "corazawaf.gofilter.path")
    String goFilterPath;

    public String getEnvoyYaml() {
        EnvoyTemplateData data = new EnvoyTemplateData(
                ADMIN_CONTAINER_PORT.getIntVal(),
                ENVOY_CONFIG_PATH,
                FILENAME_LDS,
                FILENAME_CDS
        );

        return new envoy(data).render();
    }

    public String getCdsYaml(CorazaWafBackend backend) {
        return new cds(backend).render();
    }

    public String getLDsYaml(CorazaWaf corazaWaf) {
        return  new lds( corazaWaf, TRAFFIC_CONTAINER_PORT.getIntVal(), this.goFilterPath).render();
    }

    @TemplateData
    public record EnvoyTemplateData(int adminPort, String xdsPath, String ldsFilename, String cdsFilename) {
    }
}
