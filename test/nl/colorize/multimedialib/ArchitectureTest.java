//-----------------------------------------------------------------------------
// Colorize MultimediaLib
// Copyright 2009-2026 Colorize
// Apache license (http://www.apache.org/licenses/LICENSE-2.0)
//-----------------------------------------------------------------------------

package nl.colorize.multimedialib;

import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.tngtech.archunit.core.importer.ImportOption.Predefined.DO_NOT_INCLUDE_TESTS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArchitectureTest {

    private JavaClasses classes;

    @BeforeEach
    public void before() {
        ClassFileImporter classFileImporter = new ClassFileImporter(List.of(DO_NOT_INCLUDE_TESTS));
        classes = classFileImporter.importPackages("nl.colorize.multimedialib");
    }

    @Test
    void cannotAccessFileSystemDirectly() {
        String violations = classes.stream()
            .flatMap(c -> c.getAccessesFromSelf().stream())
            .filter(access -> isPackageCall(access, "java.nio") || isClassCall(access, "java.io.File"))
            .map(access -> access.getOriginOwner().getName())
            .filter(c -> !c.startsWith("nl.colorize.multimedialib.tool."))
            .filter(c -> !c.startsWith("nl.colorize.multimedialib.renderer.java2d."))
            .filter(c -> !c.startsWith("nl.colorize.multimedialib.renderer.libgdx."))
            .filter(c -> !c.endsWith(".RenderConfig"))
            .distinct()
            .sorted()
            .collect(Collectors.joining("\n"));

        assertEquals("", violations);
    }

    @Test
    public void cannotRelyOnSystemProperties() {
        String violations = classes.stream()
            .flatMap(c -> c.getAccessesFromSelf().stream())
            .filter(access -> isMethodCall(access, "java.lang.System", "getProperty"))
            .map(access -> access.getOriginOwner().getName())
            .filter(c -> !c.endsWith(".GDXRenderer") && !c.endsWith(".RenderConfig"))
            .distinct()
            .sorted()
            .collect(Collectors.joining("\n"));

        assertEquals("", violations);
    }

    @Test
    void cannotReturnSubject() {
        String violations = classes.stream()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.getReturnType().getName().startsWith("nl.colorize.util.Subject"))
            .map(m -> m.getOwner().getName())
            .filter(name -> !name.endsWith(".SceneContext"))
            .filter(name -> !name.contains(".teavm."))
            .distinct()
            .sorted()
            .collect(Collectors.joining("\n"));

        assertEquals("", violations);
    }

    private boolean isPackageCall(JavaAccess<?> access, String toPackage) {
        return access.getTargetOwner().getName().startsWith(toPackage);
    }

    private boolean isClassCall(JavaAccess<?> access, String toClass) {
        return access.getTargetOwner().getName().equals(toClass);
    }

    private boolean isMethodCall(JavaAccess<?> access, String toClass, String toMethod) {
        return isClassCall(access, toClass) && access.getTarget().getName().equals(toMethod);
    }
}
