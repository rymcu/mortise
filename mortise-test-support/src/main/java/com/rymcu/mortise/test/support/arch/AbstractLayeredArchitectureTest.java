package com.rymcu.mortise.test.support.arch;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

/**
 * Minimal reusable entry point for layered-module ArchUnit tests.
 */
public abstract class AbstractLayeredArchitectureTest {

    protected abstract String basePackage();

    protected JavaClasses importedClasses() {
        return new ClassFileImporter().importPackages(basePackage());
    }

    protected void assertCurrentLayeredArchitecture() {
        LayeredArchitectureAssertions.assertSystemArchitecture(importedClasses(), basePackage());
    }
}
