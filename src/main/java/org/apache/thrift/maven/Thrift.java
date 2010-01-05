package org.apache.thrift.maven;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.newHashSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * This class represents an invokable configuration of the {@code thrift}
 * compiler. The actual executable is invoked using the plexus
 * {@link Commandline}.
 * <p/>
 * This class currently only supports generating java source files.
 *
 * @author gak@google.com (Gregory Kick)
 */
final class Thrift {

    final static String GENERATED_JAVA = "gen-java";

    private final String executable;
    private final ImmutableSet<File> thriftPathElements;
    private final ImmutableSet<File> thriftFiles;
    private final File javaOutputDirectory;

    /**
     * Constructs a new instance. This should only be used by the {@link Builder}.
     *
     * @param executable          The path to the {@code thrift} executable.
     * @param thriftPath          The directories in which to search for imports.
     * @param thriftFiles         The thrift source files to compile.
     * @param javaOutputDirectory The directory into which the java source files
     *                            will be generated.
     */
    private Thrift(String executable, ImmutableSet<File> thriftPath,
                   ImmutableSet<File> thriftFiles, File javaOutputDirectory) {
        this.executable = checkNotNull(executable, "executable");
        this.thriftPathElements = checkNotNull(thriftPath, "thriftPath");
        this.thriftFiles = checkNotNull(thriftFiles, "thriftFiles");
        this.javaOutputDirectory =
            checkNotNull(javaOutputDirectory, "javaOutputDirectory");
    }

    /**
     * Invokes the {@code thrift} compiler using the configuration specified at
     * construction.
     *
     * @return The exit status of {@code thrift}.
     * @throws CommandLineException
     */
    public int compile() throws CommandLineException {

        for (File thriftFile : thriftFiles) {
            Commandline cl = new Commandline(executable);
            cl.addArguments(buildThriftCommand(thriftFile).toArray(new String[]{}));
            StreamConsumer output = new DefaultConsumer();
            StreamConsumer error = new DefaultConsumer();
            final int result = CommandLineUtils.executeCommandLine(cl, null, output, error);

            if (result != 0) {
                return result;
            }
        }

        // result will always be 0 here.
        moveGeneratedFiles();

        return 0;
    }

    /**
     * Creates the command line arguments.
     * <p/>
     * This method has been made visible for testing only.
     *
     * @param thriftFile
     * @return A list consisting of the executable followed by any arguments.
     */
    ImmutableList<String> buildThriftCommand(final File thriftFile) {
        final List<String> command = newLinkedList();
        // add the executable
        for (File thriftPathElement : thriftPathElements) {
            command.add("-I");
            command.add(thriftPathElement.toString());
        }
        command.add("-o");
        command.add(javaOutputDirectory.toString());
        command.add("--gen");
        command.add("java");
        command.add(thriftFile.toString());
        return ImmutableList.copyOf(command);
    }

    private void moveGeneratedFiles() {
        File genDir = new File(javaOutputDirectory, GENERATED_JAVA);
        final File[] generatedFiles = genDir.listFiles();
        for (File generatedFile : generatedFiles) {
            final String filename = generatedFile.getName();
            final File targetLocation = new File(javaOutputDirectory, filename);
            if (targetLocation.exists()) {
                if (!targetLocation.delete()) {
                    throw new RuntimeException("File Overwrite Failed: " + targetLocation.getPath());
                }
            }
            if (!generatedFile.renameTo(targetLocation)) {
                throw new RuntimeException("Rename Failed: " + targetLocation.getPath());
            }
        }

        if (!genDir.delete()) {
            throw new RuntimeException("Failed to delete directory: " + genDir.getPath());
        }
    }

    /**
     * This class builds {@link Thrift} instances.
     *
     * @author gak@google.com (Gregory Kick)
     */
    static final class Builder {
        private final String executable;
        private final File javaOutputDirectory;
        private Set<File> thriftPathElements;
        private Set<File> thriftFiles;

        /**
         * Constructs a new builder. The two parameters are present as they are
         * required for all {@link Thrift} instances.
         *
         * @param executable          The path to the {@code thrift} executable.
         * @param javaOutputDirectory The directory into which the java source files
         *                            will be generated.
         * @throws NullPointerException     If either of the arguments are {@code null}.
         * @throws IllegalArgumentException If the {@code javaOutputDirectory} is
         *                                  not a directory.
         */
        public Builder(String executable, File javaOutputDirectory) {
            this.executable = checkNotNull(executable, "executable");
            this.javaOutputDirectory = checkNotNull(javaOutputDirectory);
            checkArgument(javaOutputDirectory.isDirectory());
            this.thriftFiles = newHashSet();
            this.thriftPathElements = newHashSet();
        }

        /**
         * Adds a thrift file to be compiled. Thrift files must be on the thriftpath
         * and this method will fail if a thrift file is added without first adding a
         * parent directory to the thriftpath.
         *
         * @param thriftFile
         * @return The builder.
         * @throws IllegalStateException If a thrift file is added without first
         *                               adding a parent directory to the thriftpath.
         * @throws NullPointerException  If {@code thriftFile} is {@code null}.
         */
        public Builder addThriftFile(File thriftFile) {
            checkNotNull(thriftFile);
            checkArgument(thriftFile.isFile());
            checkArgument(thriftFile.getName().endsWith(".thrift"));
            checkThriftFileIsInThriftPath(thriftFile);
            thriftFiles.add(thriftFile);
            return this;
        }

        private void checkThriftFileIsInThriftPath(File thriftFile) {
            assert thriftFile.isFile();
            checkState(checkThriftFileIsInThriftPathHelper(thriftFile.getParentFile()));
        }

        private boolean checkThriftFileIsInThriftPathHelper(File directory) {
            assert directory.isDirectory();
            if (thriftPathElements.contains(directory)) {
                return true;
            } else {
                final File parentDirectory = directory.getParentFile();
                return (parentDirectory == null) ? false
                    : checkThriftFileIsInThriftPathHelper(parentDirectory);
            }
        }

        /**
         * @see #addThriftFile(File)
         */
        public Builder addThriftFiles(Iterable<File> thriftFiles) {
            for (File thriftFile : thriftFiles) {
                addThriftFile(thriftFile);
            }
            return this;
        }

        /**
         * Adds the {@code thriftPathElement} to the thriftPath.
         *
         * @param thriftPathElement A directory to be searched for imported thrift message
         *                          buffer definitions.
         * @return The builder.
         * @throws NullPointerException     If {@code thriftPathElement} is {@code null}.
         * @throws IllegalArgumentException If {@code thriftPathElement} is not a
         *                                  directory.
         */
        public Builder addThriftPathElement(File thriftPathElement) {
            checkNotNull(thriftPathElement);
            checkArgument(thriftPathElement.isDirectory());
            thriftPathElements.add(thriftPathElement);
            return this;
        }

        /**
         * @see #addThriftPathElement(File)
         */
        public Builder addThriftPathElements(Iterable<File> thriftPathElements) {
            for (File thriftPathElement : thriftPathElements) {
                addThriftPathElement(thriftPathElement);
            }
            return this;
        }

        /**
         * @return A configured {@link Thrift} instance.
         * @throws IllegalStateException If no thrift files have been added.
         */
        public Thrift build() {
            checkState(!thriftFiles.isEmpty());
            return new Thrift(executable, ImmutableSet.copyOf(thriftPathElements),
                ImmutableSet.copyOf(thriftFiles), javaOutputDirectory);
        }
    }
}
