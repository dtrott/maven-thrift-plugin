package org.apache.thrift.maven;

import com.google.common.collect.ImmutableList;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.List;

/**
 * This mojo executes the {@code thrift} compiler for generating java sources
 * from thrift definitions. It also searches dependency artifacts for
 * thrift files and includes them in the thriftPath so that they can be
 * referenced. Finally, it adds the thrift files to the project as resources so
 * that they are included in the final artifact.
 *
 * @phase generate-sources
 * @goal compile
 * @requiresDependencyResolution compile
 */

public final class ThriftCompileMojo extends AbstractThriftMojo {

    /**
     * The source directories containing the sources to be compiled.
     *
     * @parameter default-value="${basedir}/src/main/thrift"
     * @required
     */
    private File thriftSourceRoot;

    /**
     * This is the directory into which the {@code .java} will be created.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/thrift"
     * @required
     */
    private File outputDirectory;

    @Override
    protected List<Artifact> getDependencyArtifacts() {
        // TODO(gak): maven-project needs generics
        @SuppressWarnings("unchecked")
        List<Artifact> compileArtifacts = project.getCompileArtifacts();
        return compileArtifacts;
    }

    @Override
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    @Override
    protected File getThriftSourceRoot() {
        return thriftSourceRoot;
    }

    @Override
    protected void attachFiles() {
        project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        projectHelper.addResource(project, thriftSourceRoot.getAbsolutePath(),
                ImmutableList.of("**/*.thrift"), ImmutableList.of());
    }
}
