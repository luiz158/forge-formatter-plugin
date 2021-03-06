package org.jboss.forge.formatter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.project.Project;
import org.jboss.forge.test.AbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Before;
import org.junit.Test;

public class FormatterPluginTest extends AbstractShellTest {

    private File javaSource;

    @Deployment
    public static JavaArchive getDeployment() {
        return AbstractShellTest.getDeployment().addPackages(true, FormatterPlugin.class.getPackage());
    }

    @Test
    public void should_use_default_formatter() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter " + javaSource.getAbsolutePath());

        // then
        compare("src/test/resources/reference/TestClass_sun.java");
    }

    @Test
    public void should_use_predefined_formatter() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter " + javaSource.getAbsolutePath() + " --configName JBoss");

        // then
        compare("src/test/resources/reference/TestClass_jboss.java");
    }

    @Test
    public void should_skip_comment_formatting() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter " + javaSource.getAbsolutePath() + " --configName JBoss --skipComments");

        // then
        compare("src/test/resources/reference/TestClass_comments.java");
    }

    @Test
    public void should_install_custom_formatter() throws Exception {
        // given
        Project p = initializeJavaProject();
        File formatter = new File("src/test/resources/files/eclipse-formatter.xml");
        File target = new File(p.getProjectRoot().getFullyQualifiedName() + "/eclipse-formatter.xml");
        FileUtils.copyFile(formatter, target);

        // when
        getShell().execute("formatter setup eclipse-formatter.xml --enableAutoFormat");
        getShell().execute("formatter " + javaSource.getAbsolutePath());

        // then
        compare("src/test/resources/reference/TestClass_eclipse.java");
    }

    @Test
    public void should_install_predefined_formatter() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter setup --configName JBoss");
        getShell().execute("formatter " + javaSource.getAbsolutePath());

        // then
        compare("src/test/resources/reference/TestClass_jboss.java");
    }

    @Test
    public void should_install_skip_comments() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter setup --configName JBoss --skipComments");
        getShell().execute("formatter " + javaSource.getAbsolutePath());

        // then
        compare("src/test/resources/reference/TestClass_comments.java");
    }

    @Test
    public void should_format_folder() throws Exception {
        // given
        initializeJavaProject();
        File recursive = new File("./target/test-classes/TestClass.java");
        if (recursive.exists()) {
            FileUtils.deleteQuietly(recursive);
        }
        FileUtils.moveFile(javaSource, recursive);

        // when
        getShell().execute("formatter setup --configName JBoss --skipComments");
        getShell().execute("formatter " + javaSource.getParentFile().getAbsolutePath() + " --recursive");

        // then
        compare("src/test/resources/reference/TestClass_comments.java", recursive);
    }

    /**
     * Formats the whole project when failing :)
     */
    @Test
    public void should_not_format_recursively() throws Exception {
        // given
        initializeJavaProject();

        // when
        getShell().execute("formatter setup --configName JBoss --skipComments");
        getShell().execute("formatter " + javaSource.getParentFile().getParentFile().getAbsolutePath());

        // then
        compare("src/test/resources/classes/TestClass.java");
    }

    @Before
    public void setup() throws IOException {
        javaSource = new File("./target/TestClass.java");
        File testFile = new File("src/test/resources/classes/TestClass.java");
        if (javaSource.exists()) {
            FileUtils.deleteQuietly(javaSource);
        }
        FileUtils.copyFile(testFile, javaSource);
    }
    
    void compare(String referenceName) throws IOException {
        compare(referenceName, javaSource);
    }

    void compare(String referenceName, File file) throws IOException {
        String content = IOUtils.toString(new FileInputStream(file));
        String reference = IOUtils.toString(new FileInputStream(new File(referenceName)));
        System.out.println("Reference: " + referenceName);
        System.out.println(content);
        Assert.assertEquals(reference, content);
    }

}
