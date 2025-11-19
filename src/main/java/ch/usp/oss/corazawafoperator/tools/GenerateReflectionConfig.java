/* 
 * Copyright (c) 2023 United Security Providers AG, Switzerland, All rights reserved.
 */
package ch.usp.oss.corazawafoperator.tools;

import com.sorbay.tools.QuarkusReflectionConfigGenerator;
import com.sorbay.tools.QuarkusReflectionConfigGenerator.ScanTarget;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * Tool that is called in the pom to generate reflection config for Quarkus native image build.
 *
 * @author Marcel Schön
 */
public final class GenerateReflectionConfig {

	private GenerateReflectionConfig() {}

	public static void main(final String[] args) throws IOException {
		File currentDir = new File(".");
		File waapDir = new File(currentDir, "waap-operator");
		if (!waapDir.exists()) {
			waapDir = currentDir;
		}
		QuarkusReflectionConfigGenerator.builder()
		.reflectionConfigFilepath(new File(waapDir, "src/main/resources/reflection-config.json").getPath())
		.scanTargets(asList(

				// our own operator/spec code in this git repo (core-waap-operator)
				ScanTarget.builder().packageRoot("ch.usp.oss.corazawafoperator").build(),

				// fabric8 (for annotations in our spec/CR)
				ScanTarget.builder()
					.packageRoot("io.fabric8.kubernetes.api.model")
					.eligibleIfHasAnyIface(singletonList(io.fabric8.kubernetes.api.model.KubernetesResource.class.getName()))
					.eligibleIfHasFieldAnnotation(com.fasterxml.jackson.annotation.JsonProperty.class.getName())
				.build()

		))
		.build()
		.run();
	}

}
