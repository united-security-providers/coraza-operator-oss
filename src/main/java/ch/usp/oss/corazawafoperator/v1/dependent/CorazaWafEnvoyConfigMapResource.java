/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1.dependent;


import ch.usp.oss.corazawafoperator.v1.CorazaWaf;
import ch.usp.oss.corazawafoperator.v1.CorazaWafEnvoyConfigTemplate;
import ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.config.informer.Informer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;
import io.javaoperatorsdk.operator.processing.event.ResourceID;
import jakarta.inject.Inject;
import lombok.extern.jbosslog.JBossLog;

import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_KEY;
import static ch.usp.oss.corazawafoperator.v1.CorazaWafReconciler.SELECTOR_VALUE;

@JBossLog
@KubernetesDependent(informer = @Informer(labelSelector = CorazaWafReconciler.SELECTOR))
public class CorazaWafEnvoyConfigMapResource extends CRUDKubernetesDependentResource<ConfigMap, CorazaWaf> {

    public static final String CM_NAME_PREFIX = CorazaWaf.NAME_PREFIX + "envoyconfig-";
    public static final String FILENAME_ENVOY = "envoy.yaml";
    public static final String DIRNAME_XDS = "xds/";
    public static final String FILENAME_CDS = "cds.yaml";
    public static final String FILENAME_LDS = "lds.yaml";

    @Inject
    CorazaWafEnvoyConfigTemplate templates;

    @Override
    protected ConfigMap desired(CorazaWaf corazaWaf, Context<CorazaWaf> context) {


        final ConfigMapBuilder configMapBuilder = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(CM_NAME_PREFIX + corazaWaf.getMetadata().getName())
                .withNamespace(corazaWaf.getMetadata().getNamespace())
                .addToLabels("app.kubernetes.io/name", CM_NAME_PREFIX + corazaWaf.getMetadata().getName())
                .addToLabels("app.kubernetes.io/instance", corazaWaf.getMetadata().getName())
                .addToLabels("app.kubernetes.io/part-of", CorazaWaf.NAME_PREFIX + corazaWaf.getMetadata().getName())
                .addToLabels(SELECTOR_KEY, SELECTOR_VALUE)
                .endMetadata();
        configMapBuilder.addToData(FILENAME_ENVOY, templates.getEnvoyYaml());
        configMapBuilder.addToData(FILENAME_CDS, templates.getCdsYaml(corazaWaf.getSpec().getBackend()));
        configMapBuilder.addToData(FILENAME_LDS, templates.getLDsYaml(corazaWaf));
        ConfigMap configMap = configMapBuilder.build();
        log.infof("Generated desired Coraza WAF Config Map: %s", ResourceID.fromResource(configMap));
        log.debug("ConfigMap: " + configMap);
        return configMap;
    }
}
