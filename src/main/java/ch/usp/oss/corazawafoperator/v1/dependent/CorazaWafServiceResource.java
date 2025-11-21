/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.dependent;

import ch.usp.oss.corazawafoperator.v1.CorazaWaf;
import ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.api.model.ServicePortBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import lombok.extern.jbosslog.JBossLog;

import java.util.Map;

import static ch.usp.oss.corazawafoperator.v1.CorazaWaf.TRAFFIC_CONTAINER_PORT;
import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_KEY;
import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_VALUE;
import static ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafDeploymentResource.DEPLOY_NAME_PREFIX;

@JBossLog
@KubernetesDependent(informer = @Informer(labelSelector = CorazaWafReconciler.SELECTOR))
public class CorazaWafServiceResource extends CRUDKubernetesDependentResource<Service, CorazaWaf> {

    private static final String SVC_NAME_PREFIX = CorazaWaf.NAME_PREFIX + "service";

    @Override
    protected Service desired(CorazaWaf corazaWaf, Context<CorazaWaf> context) {
        Service service = new ServiceBuilder()
                .withNewMetadata()
                .withName(SVC_NAME_PREFIX + "-" + corazaWaf.getMetadata().getName())
                .withNamespace(corazaWaf.getMetadata().getNamespace())
                .addToLabels("app.kubernetes.io/name", SVC_NAME_PREFIX)
                .addToLabels("app.kubernetes.io/instance", corazaWaf.getMetadata().getName())
                .addToLabels("app.kubernetes.io/part-of", CorazaWaf.NAME_PREFIX + corazaWaf.getMetadata().getName())
                .addToLabels(SELECTOR_KEY, SELECTOR_VALUE)
                .endMetadata()
                .withNewSpec()
                .withPorts(new ServicePortBuilder()
                        .withName("http")
                        .withPort(TRAFFIC_CONTAINER_PORT.getIntVal())
                        .withTargetPort(TRAFFIC_CONTAINER_PORT)
                        .withProtocol("TCP")
                        .build())
                .withSelector(Map.of(
                        "app.kubernetes.io/name", DEPLOY_NAME_PREFIX,
                        "app.kubernetes.io/instance", corazaWaf.getMetadata().getName()
                ))
                .endSpec()
                .build();
        log.info("Generate desired Coraza WAF Service: " + ResourceID.fromResource(service));
        log.debug("Service: " + service);
        return service;
    }
}
