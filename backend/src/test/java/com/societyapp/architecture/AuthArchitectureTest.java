package com.societyapp.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(
        packages = "com.societyapp.auth",
        importOptions = ImportOption.DoNotIncludeTests.class)
class AuthArchitectureTest {

    @ArchTest
    static final ArchRule domainHasNoFrameworkDependencies =
            noClasses()
                    .that()
                    .resideInAPackage("..auth.domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework..",
                            "jakarta..",
                            "com.societyapp.auth.application..");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnAdapters =
            noClasses()
                    .that()
                    .resideInAPackage("..auth.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("..auth.infrastructure..", "..auth.web..");

    @ArchTest
    static final ArchRule applicationHasNoFrameworkDependencies =
            noClasses()
                    .that()
                    .resideInAPackage("..auth.application..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage("org.springframework..", "jakarta..");
}
