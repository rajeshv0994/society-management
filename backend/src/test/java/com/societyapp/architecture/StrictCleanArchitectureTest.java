package com.societyapp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.societyapp.domain.type.DomainCode;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "com.societyapp", importOptions = ImportOption.DoNotIncludeTests.class)
class StrictCleanArchitectureTest {
    private static final String[] FRAMEWORKS = {
        "org.springframework..",
        "jakarta..",
        "org.hibernate..",
        "org.apache.kafka..",
        "com.fasterxml..",
        "tools.jackson..",
        "com.lowagie..",
        "io.jsonwebtoken..",
        "lombok.."
    };

    @ArchTest
    static final ArchRule frameworkScannedApplicationServicesRemainProxyable =
            classes()
                    .that()
                    .resideInAnyPackage(
                            "com.societyapp.application.service..",
                            "com.societyapp.application.usecase..")
                    .and()
                    .areTopLevelClasses()
                    .should()
                    .notHaveModifier(JavaModifier.FINAL);

    private static final ArchCondition<JavaClass> NOT_DEPEND_ON_OTHER_INTERACTORS =
            new ArchCondition<>("depend only on ports when collaborating with other interactors") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    item.getDirectDependenciesFromSelf().stream()
                            .filter(
                                    dependency ->
                                            dependency
                                                    .getTargetClass()
                                                    .getPackageName()
                                                    .startsWith(
                                                            "com.societyapp.application.usecase"))
                            .filter(
                                    dependency ->
                                            !topLevelName(dependency.getTargetClass().getName())
                                                    .equals(topLevelName(item.getName())))
                            .forEach(
                                    dependency ->
                                            events.add(
                                                    SimpleConditionEvent.violated(
                                                            item, dependency.getDescription())));
                }
            };

    private static final ArchCondition<JavaClass> IMPLEMENT_AN_INPUT_PORT =
            new ArchCondition<>("implement an application input port") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    if (item.getName().contains("$")) return;
                    boolean implementsInputPort =
                            item.getAllRawInterfaces().stream()
                                    .anyMatch(
                                            contract ->
                                                    contract.getPackageName()
                                                            .startsWith(
                                                                    "com.societyapp.application.port.in"));
                    if (!implementsInputPort) {
                        events.add(
                                SimpleConditionEvent.violated(
                                        item,
                                        item.getName()
                                                + " does not implement an application input port"));
                    }
                }
            };

    @ArchTest
    static final ArchRule domainModelsArePlainJava =
            noClasses()
                    .that()
                    .resideInAnyPackage("com.societyapp.domain.model..", "com.societyapp.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(FRAMEWORKS);

    @ArchTest
    static final ArchRule domainEnumsUseTheSharedBoundaryContract =
            classes()
                    .that()
                    .areEnums()
                    .and()
                    .resideInAPackage("com.societyapp.domain.type..")
                    .should()
                    .implement(DomainCode.class);

    @ArchTest
    static final ArchRule applicationServicesAreFrameworkFree =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.societyapp.application.usecase..",
                            "com.societyapp.application.service..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(FRAMEWORKS);

    @ArchTest
    static final ArchRule everyInteractorImplementsAUseCase =
            classes()
                    .that()
                    .resideInAPackage("com.societyapp.application.usecase..")
                    .should(IMPLEMENT_AN_INPUT_PORT);

    @ArchTest
    static final ArchRule applicationPortsAreFrameworkFree =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.societyapp.application.port.out.persistence..",
                            "com.societyapp.application.port..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(FRAMEWORKS);

    @ArchTest
    static final ArchRule applicationDoesNotDependOnOuterAdapters =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.societyapp.application.usecase..",
                            "com.societyapp.domain.model..",
                            "com.societyapp.domain..",
                            "com.societyapp.application.port.out.persistence..",
                            "com.societyapp.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "com.societyapp.controller..",
                            "com.societyapp.infrastructure..",
                            "com.societyapp.documents..",
                            "com.societyapp.security..",
                            "com.societyapp.config..");

    @ArchTest
    static final ArchRule springDataLivesOnlyInPersistenceAdapters =
            noClasses()
                    .that()
                    .resideOutsideOfPackage("..infrastructure..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("org.springframework.data..");

    @ArchTest
    static final ArchRule adaptersUseInputPortsInsteadOfInteractorImplementations =
            noClasses()
                    .that()
                    .resideOutsideOfPackages(
                            "com.societyapp.application.usecase..", "com.societyapp.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.societyapp.application.usecase..");

    @ArchTest
    static final ArchRule inboundAdaptersDoNotAccessPersistencePorts =
            noClasses()
                    .that()
                    .resideInAnyPackage("com.societyapp.controller..", "com.societyapp.security..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.societyapp.application.port.out.persistence..");

    @ArchTest
    static final ArchRule inputPortsDoNotDependOnInteractorImplementations =
            noClasses()
                    .that()
                    .resideInAPackage("com.societyapp.application.port.in..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.societyapp.application.usecase..");

    @ArchTest
    static final ArchRule inputPortsExposeApplicationContractsInsteadOfDomainEntities =
            noClasses()
                    .that()
                    .resideInAPackage("com.societyapp.application.port.in..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.societyapp.domain.model..");

    @ArchTest
    static final ArchRule interactorsDependOnPortsInsteadOfOtherInteractors =
            classes()
                    .that()
                    .resideInAPackage("com.societyapp.application.usecase..")
                    .should(NOT_DEPEND_ON_OTHER_INTERACTORS);

    @ArchTest
    static final ArchRule coreDoesNotDependOnHttpCommonPackage =
            noClasses()
                    .that()
                    .resideInAnyPackage(
                            "com.societyapp.domain.model..",
                            "com.societyapp.domain..",
                            "com.societyapp.application.usecase..",
                            "com.societyapp.application.port.out.persistence..",
                            "com.societyapp.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("com.societyapp.common..");

    @ArchTest
    static final ArchRule applicationBoundariesDoNotExposeFilesystemPaths =
            noClasses()
                    .that()
                    .resideInAPackage("com.societyapp.application.port..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("java.nio.file..");

    private static String topLevelName(String className) {
        int nestedClass = className.indexOf('$');
        return nestedClass < 0 ? className : className.substring(0, nestedClass);
    }
}
