/*
 * Copyright (c) 2025 United Security Providers AG, Switzerland
 */
package ch.usp.oss.corazawafoperator.tools;

import static java.util.Comparator.comparing;
import static org.apache.commons.collections4.CollectionUtils.containsAny;
import static org.apache.commons.collections4.ListUtils.emptyIfNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.jbosslog.JBossLog;

@Builder
@Data
@JBossLog
public class QuarkusReflectionConfigGenerator {

    @Builder
    @Data
    public static class ScanTarget {
        private final String packageRoot;
        @SuppressWarnings("java:S1170")
        private final List<String> eligibleIfHasAnySuper;
        private final List<String> eligibleIfHasAnyIface;
        private final String eligibleIfHasFieldAnnotation;

        @Override
        public String toString() {
            return "ScanTarget ["
                    + "packageRoot=" + packageRoot
                    + ", eligibleIfHasAnySuper=" + eligibleIfHasAnySuper
                    + ", eligibleIfHasAnyIface=" + eligibleIfHasAnyIface
                    + ", eligibleIfHasFieldAnnotation=" + eligibleIfHasFieldAnnotation
                    + "]";
        }
    }

    @SuppressWarnings("unused")
    private static class ReflectionClassDescription {
        @Getter
        private final String name;
        private final boolean allDeclaredConstructors = true;
        private final boolean allPublicConstructors = true;
        private final boolean allDeclaredFields = true;
        private final boolean allPublicFields = true;
        private final boolean allDeclaredMethods = true;
        private final boolean allPublicMethods = true;

        public ReflectionClassDescription(String name) {
            this.name = name;
        }
    }

    private final List<QuarkusReflectionConfigGenerator.ScanTarget> scanTargets;
    private final String reflectionConfigFilepath;

    public void run() throws IOException {
        String content = getReflectConfigContent();
        writeReflectConfigFile(content);
    }

    private String getReflectConfigContent() {
        List<QuarkusReflectionConfigGenerator.ReflectionClassDescription> allClassDescriptions = getAllClasses();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(allClassDescriptions);
    }

    private void writeReflectConfigFile(String serialized) throws IOException {
        Path reflectConfigPath = Paths.get(reflectionConfigFilepath);
        ensureNewFile(reflectConfigPath);
        try (BufferedWriter writer = Files.newBufferedWriter(reflectConfigPath)) {
            writer.write(serialized);
        }
    }

    @SuppressWarnings("java:S899")
    private void ensureNewFile(Path path) throws IOException {
        File file = path.toFile();
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

    private List<QuarkusReflectionConfigGenerator.ReflectionClassDescription> getAllClasses() {
        List<QuarkusReflectionConfigGenerator.ReflectionClassDescription> results = new ArrayList<>();
        for (QuarkusReflectionConfigGenerator.ScanTarget scanTarget : scanTargets) {
            log.infov("Processing {0}", scanTarget);
            String currentPackageRoot = scanTarget.getPackageRoot();
            if (currentPackageRoot == null){
                break;
            }
            int sizeBefore = results.size();
            try (ScanResult scanResult = new ClassGraph()
                    .enableAllInfo()
                    .acceptPackages(currentPackageRoot)
                    .scan()) {

                scanResult.getAllClasses().stream()
                        .filter(clazz -> this.isEligible(clazz, scanTarget))
                        .map(this::toReflectionClassDescription)
                        .sorted(comparing(ReflectionClassDescription::getName))
                        .forEach(results::add);

                emptyIfNull(scanTarget.getEligibleIfHasAnySuper()).stream()
                        .map(QuarkusReflectionConfigGenerator.ReflectionClassDescription::new)
                        .forEach(results::add);

                log.infov("Got {0} new entries", results.size() - sizeBefore);
            }
        }
        return results;
    }

    private QuarkusReflectionConfigGenerator.ReflectionClassDescription toReflectionClassDescription(ClassInfo source) {
        return new ReflectionClassDescription(source.getName());
    }

    private boolean isEligible(ClassInfo classInfoToInspect, QuarkusReflectionConfigGenerator.ScanTarget scanTarget) {
        if (hasNoRestrictions(scanTarget)) {
            return true;
        }
        if (hasEligibleSuperClasses(classInfoToInspect, scanTarget)) {
            return true;
        }
        if (hasEligibleInterfaces(classInfoToInspect, scanTarget)) {
            return true;
        }
        return classInfoToInspect.hasDeclaredFieldAnnotation(scanTarget.getEligibleIfHasFieldAnnotation());
    }

    private boolean hasNoRestrictions(QuarkusReflectionConfigGenerator.ScanTarget scanTarget) {
        return scanTarget.getEligibleIfHasAnySuper() == null
                && scanTarget.getEligibleIfHasAnyIface() == null
                && scanTarget.getEligibleIfHasFieldAnnotation() == null;
    }

    private boolean hasEligibleSuperClasses(ClassInfo classInfoToInspect, QuarkusReflectionConfigGenerator.ScanTarget scanTarget) {
        if (scanTarget.getEligibleIfHasAnySuper() == null) {
            return false;
        }
        return containsAny(classInfoToInspect.getSuperclasses().getNames(), scanTarget.getEligibleIfHasAnySuper());
    }

    private boolean hasEligibleInterfaces(ClassInfo classInfoToInspect, QuarkusReflectionConfigGenerator.ScanTarget scanTarget) {
        if (scanTarget.getEligibleIfHasAnyIface() == null) {
            return false;
        }
        return containsAny(classInfoToInspect.getInterfaces().getNames(), scanTarget.getEligibleIfHasAnyIface());
    }

}
