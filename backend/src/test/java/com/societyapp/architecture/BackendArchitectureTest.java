package com.societyapp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.*;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(packages = "com.societyapp", importOptions = ImportOption.DoNotIncludeTests.class)
class BackendArchitectureTest {

    @ArchTest
    static final ArchRule controllersLiveAtTheHttpBoundary =
            classes()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule controllersDoNotAccessPersistence =
            noClasses()
                    .that()
                    .resideInAPackage("..controller..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "..application.port.out.persistence..",
                            "..infrastructure.persistence..",
                            "..domain.model..",
                            "org.springframework.jdbc..",
                            "jakarta.persistence..");

    @ArchTest
    static final ArchRule persistenceAdaptersDoNotDependOnWebOrUseCases =
            noClasses()
                    .that()
                    .resideInAPackage("..infrastructure.persistence..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..controller..", "..application.usecase..");

    @ArchTest
    static final ArchRule useCasesDoNotDependOnControllers =
            noClasses()
                    .that()
                    .resideInAPackage("..application.usecase..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAPackage("..controller..");

    @ArchTest
    static final ArchRule springDataRepositoriesUseTheRepositoryLayer =
            classes()
                    .that()
                    .areInterfaces()
                    .and()
                    .haveSimpleNameEndingWith("Repository")
                    .and()
                    .areNotAnnotatedWith(Repository.class)
                    .should()
                    .resideInAnyPackage(
                            "..infrastructure.persistence.repository..",
                            "..application.port.out..");
}
