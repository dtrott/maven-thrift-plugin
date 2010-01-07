package org.apache.thrift.maven;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class TestThrift {

    private File testRootDir;
    private File testResourceDir;

    @Before
    public void setup() throws Exception {
        final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        testRootDir = new File(tmpDir, "thrift-test");

        if (testRootDir.exists()) {
            FileUtils.cleanDirectory(testRootDir);
        } else {
            assertTrue("Failed to create output directory for test: "+ testRootDir.getPath(), testRootDir.mkdir());
        }

        testResourceDir = new File("src/test/resources");
        assertTrue("Unable to find test resources", testRootDir.exists());
    }

    @Test
    public void testThriftCompile() throws Exception {
        final File idlDir = new File(testResourceDir, "idl");
        final File genJavaDir = new File(testRootDir, Thrift.GENERATED_JAVA);
        final File thriftFile = new File(idlDir, "shared.thrift");

        Thrift.Builder builder = new Thrift.Builder("thrift", testRootDir);
        builder.addThriftPathElement(idlDir);
        builder.addThriftFile(thriftFile);

        final Thrift thrift = builder.build();

        assertTrue("File not found: shared.thrift", thriftFile.exists());
        assertFalse("gen-java directory should not exist", genJavaDir.exists());

        // execute the compile
        final int result = thrift.compile();
        assertEquals(0, result);

        assertFalse("gen-java directory was not removed", genJavaDir.exists());
        assertTrue("generated java code doesn't exist",
            new File(testRootDir, "shared/SharedService.java").exists());
    }

    @After
    public void cleanup() throws Exception {
        if (testRootDir.exists()) {
            FileUtils.cleanDirectory(testRootDir);
            assertTrue("Failed to delete output directory for test: "+ testRootDir.getPath(), testRootDir.delete());
        }
    }
}
