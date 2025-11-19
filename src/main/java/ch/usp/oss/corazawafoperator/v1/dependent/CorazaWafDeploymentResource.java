/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.dependent;

import ch.usp.oss.corazawafoperator.v1.CorazaWaf;
import ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler;
import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;

import lombok.extern.jbosslog.JBossLog;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_KEY;
import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_VALUE;

import java.util.*;

@JBossLog
@KubernetesDependent(informer = @Informer(labelSelector = CorazaWafReconciler.SELECTOR))
public class CorazaWafDeploymentResource extends CRUDKubernetesDependentResource<Deployment, CorazaWaf> {

    public static final String DEPLOY_NAME_PREFIX = CorazaWaf.NAME_PREFIX + "deployment";

    private static final String VOLUME_NAME_CONFIG = "envoy-config-volume";
    private static final String VOLUME_NAME_CORAZA_TMP = "coraza-tmp-volume";
    public static final String ENVOY_CONFIG_PATH = "/etc/envoy/";

    @ConfigProperty(name = "corazawaf.envoyimage.name")
    String envoyImageName;
    @ConfigProperty(name = "corazawaf.envoyimage.version")
    String envoyImageVersion;

    @Override
    protected Deployment desired(CorazaWaf corazaWaf, Context<CorazaWaf> context) {
        Map<String, String> selectorLabels = new HashMap<>(2);
        selectorLabels.put("app.kubernetes.io/name", DEPLOY_NAME_PREFIX);
        selectorLabels.put("app.kubernetes.io/instance", corazaWaf.getMetadata().getName());

        Map<String, String> metadataLabels = new HashMap<>(5);
        metadataLabels.put("app.kubernetes.io/name", DEPLOY_NAME_PREFIX);
        metadataLabels.put("app.kubernetes.io/instance", corazaWaf.getMetadata().getName());
        metadataLabels.put("app.kubernetes.io/part-of", CorazaWaf.NAME_PREFIX + corazaWaf.getMetadata().getName());
        metadataLabels.put(SELECTOR_KEY, SELECTOR_VALUE);

        Deployment deployment = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(DEPLOY_NAME_PREFIX + "-" + corazaWaf.getMetadata().getName())
                    .withNamespace(corazaWaf.getMetadata().getNamespace())
                    .withLabels(metadataLabels)
                .endMetadata()
                .withNewSpec()
                    .withReplicas(1)
                    .withProgressDeadlineSeconds(600)
                    .withRevisionHistoryLimit(3)
                    .withNewSelector()
                        .withMatchLabels(selectorLabels)
                    .endSelector()
                    .withNewStrategy()
                        .withType("RollingUpdate")
                        .withNewRollingUpdate()
                        .withMaxSurge(new IntOrString(25))
                        .withMaxUnavailable(new IntOrString(25))
                        .endRollingUpdate()
                    .endStrategy()
                    .withNewTemplate()
                        .withNewMetadata()
                            .withLabels(metadataLabels)
                        .endMetadata()
                        .withNewSpec()
                            .withContainers(
                                new ContainerBuilder()
                                    .withName(CorazaWaf.NAME_PREFIX + "envoy")
                                    .withImage(envoyImageName + ":" + envoyImageVersion)
                                    .withImagePullPolicy("Always")
                                    .withPorts(
                                            new ContainerPortBuilder()
                                                    .withName("traffic-port")
                                                    .withContainerPort(CorazaWaf.TRAFFIC_CONTAINER_PORT.getIntVal())
                                                    .withProtocol("TCP")
                                                    .build(),
                                            new ContainerPortBuilder()
                                                    .withName("admin-port")
                                                    .withContainerPort(CorazaWaf.ADMIN_CONTAINER_PORT.getIntVal())
                                                    .withProtocol("TCP")
                                                    .build()
                                    )
                                    .withVolumeMounts(
                                            new VolumeMountBuilder()
                                                    .withName(VOLUME_NAME_CONFIG)
                                                    .withMountPath(ENVOY_CONFIG_PATH)
                                                    .build(),
                                            /*new VolumeMountBuilder()
                                                    .withName(VOLUME_NAME_CONFIG)
                                                    .withMountPath(ENVOY_CONFIG_PATH + DIRNAME_XDS + FILENAME_CDS)
                                                    .withSubPath(FILENAME_CDS)
                                                    .build(),
                                            new VolumeMountBuilder()
                                                    .withName(VOLUME_NAME_CONFIG)
                                                    .withMountPath(ENVOY_CONFIG_PATH + DIRNAME_XDS + FILENAME_LDS)
                                                    .withSubPath(FILENAME_LDS)
                                                    .build(),*/
                                            new VolumeMountBuilder()
                                                    .withName(VOLUME_NAME_CORAZA_TMP)
                                                    .withMountPath("/tmp")
                                                    .build()
                                    )
                                    .withLivenessProbe(
                                            new ProbeBuilder()
                                                    .withFailureThreshold(3)
                                                    .withHttpGet(
                                                            new HTTPGetActionBuilder()
                                                                    .withScheme("HTTP")
                                                                    .withPort(CorazaWaf.ADMIN_CONTAINER_PORT)
                                                                    .withPath("/listeners")
                                                                    .build()
                                                    )
                                                    .withInitialDelaySeconds(5)
                                                    .withPeriodSeconds(30)
                                                    .withSuccessThreshold(1)
                                                    .withTimeoutSeconds(10)
                                                    .build())
                                    .withReadinessProbe(
                                            new ProbeBuilder()
                                                    .withFailureThreshold(3)
                                                    .withHttpGet(
                                                            new HTTPGetActionBuilder()
                                                                    .withScheme("HTTP")
                                                                    .withPort(CorazaWaf.ADMIN_CONTAINER_PORT)
                                                                    .withPath("/ready")
                                                                    .build()
                                                    )
                                                    .withPeriodSeconds(30)
                                                    .withSuccessThreshold(1)
                                                    .withTimeoutSeconds(10)
                                                    .build())
                                    .build()
                            )
                            .withVolumes(
                                    new VolumeBuilder()
                                            .withName(VOLUME_NAME_CONFIG)
                                            .withConfigMap(new ConfigMapVolumeSourceBuilder()
                                                    .withName(CorazaWafEnvoyConfigMapResource.CM_NAME_PREFIX + corazaWaf.getMetadata().getName())
                                                    .build()
                                            )
                                            .build(),
                                    new VolumeBuilder()
                                            .withName(VOLUME_NAME_CORAZA_TMP)
                                            .withEmptyDir(new EmptyDirVolumeSourceBuilder().build())
                                            .build()
                            )
                        .endSpec()
                    .endTemplate()
                .endSpec()
                .build();

        log.info("Generate desired Coraza WAF Deployment: " + ResourceID.fromResource(deployment));
        log.debug("Deployment: " + deployment.toString());
        return deployment;
    }
}
