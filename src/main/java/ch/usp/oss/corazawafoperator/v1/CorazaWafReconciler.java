/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.v1;

import ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafDeploymentResource;
import ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafEnvoyConfigMapResource;
import ch.usp.oss.corazawafoperator.v1.dependent.CorazaWafServiceResource;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import lombok.extern.jbosslog.JBossLog;

@JBossLog
@Workflow(
        dependents = {
                @Dependent( type = CorazaWafEnvoyConfigMapResource.class),
                @Dependent( type = CorazaWafDeploymentResource.class),
                @Dependent( type = CorazaWafServiceResource.class)
        }
)
public class CorazaWafReconciler implements Reconciler<CorazaWaf> {

    public static final String SELECTOR_KEY = "app.kubernetes.io/managed-by";
    public static final String SELECTOR_VALUE = CorazaWaf.OPERATOR_NAME;
    public static final String SELECTOR = SELECTOR_KEY + "=" + SELECTOR_VALUE;

    @Override
    public UpdateControl<CorazaWaf> reconcile(CorazaWaf corazaWaf, Context<CorazaWaf> context) throws Exception {
        log.info("CorazaWafReconciler: Starting reconcile");
        CorazaWafStatus status = corazaWaf.getStatusOrInit();

        String cmName = context.getSecondaryResource(ConfigMap.class).orElseThrow().getMetadata().getName();
        String svcName = context.getSecondaryResource(Service.class).orElseThrow().getMetadata().getName();
        String deployName = context.getSecondaryResource(Deployment.class).orElseThrow().getMetadata().getName();
        status.setConfigMapName(cmName);
        status.setServiceName(svcName);
        status.setDeploymentName(deployName);
        status.setErrorMessage(null);

        return UpdateControl.patchStatus(corazaWaf);
    }

    @Override
    public ErrorStatusUpdateControl<CorazaWaf> updateErrorStatus(CorazaWaf corazaWaf, Context<CorazaWaf> context, Exception e) {
        corazaWaf.getStatusOrInit().setErrorMessage( "Error: " + e.getMessage());
        return ErrorStatusUpdateControl.patchStatus(corazaWaf);
    }
}
