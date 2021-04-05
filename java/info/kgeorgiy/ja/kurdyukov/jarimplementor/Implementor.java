package info.kgeorgiy.ja.kurdyukov.jarimplementor;

import info.kgeorgiy.java.advanced.implementor.*;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Kirill Kurdyukov
 * @version 1.0
 *
 * <p>
 * Class implements {@link JarImpler} interface.
 * </p>
 */
public class Implementor implements JarImpler {
    /**
     * Tab for generated code.
     */
    private static final String TAB = "    ";
    /**
     * Whitespace for generated code.
     */
    private static final String WHITESPACE = " ";
    /**
     * Special open bracket for generated code.
     */
    private static final String OPEN_BRACKET = "{";
    /**
     * Special close bracket for generated code.
     */
    private static final String CLOSE_BRACKET = "}";
    /**
     * Suffix of generated name class.
     */
    private static final String IMPL = "Impl";
    /**
     * Access identifier for generated class methods.
     */
    private static final String PUBLIC = "public";
    /**
     * Intended for constructors of generated class.
     */
    private static final String SUPER = "super";
    /**
     * Semicolon for generated class.
     */
    private static final String SEMICOLON = ";";
    /**
     * Intended for throws exception generated class.
     */
    private static final String THROWS = "throws";
    /**
     * Intended for generated java file.
     */
    private static final String JAVA = ".java";
    /**
     * Intended for generated class file.
     */
    private static final String CLASS = ".class";

    /**
     * Checks that the specified arguments is not <i>null</i>.
     *
     * @param args are specified arguments to check for <i>null</i>.
     * @throws ImplerException when any argument is <i>null</i>.
     */
    private void checkArguments(Object... args) throws ImplerException {
        for (var arg : args) {
            if (arg == null)
                throw new ImplerException("Argument can't be null.");
        }
    }

    /**
     * Generated correct {@link String} java code enumeration of parameters with type or without type argument.
     *
     * <p>
     * Method takes specified array of {@link Class}. It's type <i>tokens</i> of parameters.
     * And generated correct java code: enumeration of parameters with type or without type argument.
     * How in signature of method or constructor.
     * </p>
     *
     * <p>
     * Example: {@code ([Object] a0, [Object] a1, ...)}.
     * </p>
     *
     * @param params are array of {@link Class}.
     * @param flag   is <i>true</i> then generated with type argument else without.
     * @return correct for compilation java code {@link String} parameters.
     */
    private String generateParams(Class<?>[] params, boolean flag) {
        return IntStream.range(0, params.length)
                .mapToObj(i -> flag ? params[i].getCanonicalName().concat(" a" + i)
                        : "a" + i)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    /**
     * Generated correct java code {@link String} implements body of the specified {@link Method}.
     *
     * <p>
     * If specified {@link Method} return <i>void.class token</i> then java code implemented
     * this method is {@code ""}. Else if <i>return type token</i> is primitive, then if this primitive
     * <i>type token == boolean.class</i> then code implemented this method is {@code return false;}
     * for other primitives {@code return 0;}. If specified {@link Method} return not primitive <i>token</i>.
     * Java code implemented this method is {@code return null;}.
     *
     * </p>
     *
     * @param method is {@link Method} to implement his body.
     * @return {@link String} implemented body of specified {@link Method}.
     */
    private String implMethod(Method method) {
        if (void.class.equals(method.getReturnType())) {
            return "";
        }
        if (method.getReturnType().isPrimitive()) {
            return method.getReturnType().equals(boolean.class) ?
                    "return false;" : "return 0;";
        }
        return "return null;";
    }

    /**
     * Generated correct java code {@link String} that fully implements the specified {@link Method}.
     *
     * <p>
     * Method implemented will have:
     * <ul>
     *     <li>access identifier is public</li>
     *     <li>Return type == {@link Method#getReturnType()}</li>
     *     <li>Name of method == {@link Method#getName()}</li>
     *     <li>Enumeration of parameters == {@link Implementor#generateParams}</li>
     *     <li>Enumeration of exceptions == {@link Implementor#generateThrowException}</li>
     *     <li>Body of this method == {@link Implementor#implMethod}</li>
     * </ul>
     *
     *
     *
     * Example: {@code public Object action(java.lang.Object a0, java.lang.String a1) { return null; }}
     *
     *
     * @param method is {@link Method} to implement.
     * @return {@link String} fully implements specified {@link Method}.
     */
    private String generateMethod(Method method) {
        return TAB +
                PUBLIC +
                WHITESPACE +
                method.getReturnType().getCanonicalName() +
                WHITESPACE +
                method.getName() +
                generateParams(method.getParameterTypes(), true) +
                WHITESPACE +
                generateThrowException(method) +
                OPEN_BRACKET +
                WHITESPACE +
                implMethod(method) +
                WHITESPACE +
                CLOSE_BRACKET;
    }

    /**
     * Generated correct java code {@link String} of exceptions the specified {@link Executable}.
     *
     * <p>
     * If specified {@link Executable} don't have exceptions then java code implemented this exceptions
     * is <code>""</code>. Else exceptions separated by a comma, after world <i>throws</i>.
     * </p>
     *
     * <p>
     * Example: throws {@link IOException}, {@link ImplerException}
     * </p>
     *
     * @param executable gives his exceptions.
     * @return {@link String} of exceptions specified {@link Executable}.
     */
    private String generateThrowException(Executable executable) {
        if (executable.getExceptionTypes().length == 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(THROWS);
        Arrays.stream(executable.getExceptionTypes())
                .forEach(token -> stringBuilder.append(WHITESPACE)
                        .append(token.getCanonicalName()));
        stringBuilder.append(WHITESPACE);
        return stringBuilder.toString();
    }

    /**
     * Generated correct java code {@link String} that fully implements the specified {@link Constructor}.
     *
     * <p>
     * Constructor implemented will have:
     *     <ul>
     *         <li>access identifier is public</li>
     *         <li>Name of constructor == {@link Constructor#getDeclaringClass()} + Impl</li>
     *         <li>Enumeration of parameters == {@link Implementor#generateParams}</li>
     *         <li>Enumeration of exceptions == {@link Implementor#generateThrowException}</li>
     *         <li>Body of this constructor == super({@link Implementor#generateParams} with flag == false)</li>
     *     </ul>
     *
     *
     *     Example: <code>public ExampleImpl(Object a0) throws {@link IOException} { super(a0);}</code>
     *
     *
     * @param constructor is {@link Constructor} to implement.
     * @return {@link String} fully implements specified {@link Constructor}
     */
    private String generateConstructor(Constructor<?> constructor) {
        return TAB +
                PUBLIC +
                WHITESPACE +
                constructor.getDeclaringClass().getSimpleName() +
                IMPL +
                generateParams(constructor.getParameterTypes(), true) +
                WHITESPACE +
                generateThrowException(constructor) +
                OPEN_BRACKET +
                WHITESPACE +
                SUPER +
                generateParams(constructor.getParameterTypes(), false) +
                SEMICOLON +
                CLOSE_BRACKET;
    }

    /**
     * Static class used for correct representing {@link Method}.
     */
    public class MethodSignature {

        /**
         * Wrapped instance of {@link Method}.
         */
        private final Method method;

        /**
         * Constructor of the wrapper class over the specified method.
         *
         * @param method for wrapper.
         */
        public MethodSignature(Method method) {
            this.method = method;
        }

        /**
         * Calculates the correct hash of the wrapped {@link Method}.
         *
         * <p>
         * The hash is calculated {@link Objects#hash} on {@link String}
         * <code>"this.method.getName() + generateParams(this.method.getParameterTypes(), true)"</code>
         * of this {@link Method}.
         * </p>
         *
         * @return correct hash of {@link Method}.
         */
        @Override
        public int hashCode() {
            return Objects.hash(this.method.getName() + generateParams(this.method.getParameterTypes(), true));
        }

        /**
         * Correct equals of wrapped methods.
         *
         * <p>
         * If specified <code>object != wrapper</code> return false.
         * Comparison first by {@link Class} of parameters <i>tokens</i> and
         * compare {@link Method#getName()}.
         * </p>
         *
         * @param obj is {@link Object} to compare.
         * @return comparison result this with specified {@link Object}.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof MethodSignature) {
                Class<?>[] params1 = ((MethodSignature) obj).method.getParameterTypes();
                Class<?>[] params2 = method.getParameterTypes();
                return method.getName().equals(((MethodSignature) obj).method.getName())
                        && Arrays.equals(params1, params2);
            }
            return false;
        }
    }

    /**
     * Generated correct java code {@link String} that implements all {@link Method}
     * the specified <i>aClass</i> and all of his ancestors.
     *
     * <p>
     * All methods <i>class token</i> and his ancestors put in HashSet wrappers
     * over {@link Method} for correct compare of method. Not public methods get
     * climbing the parse tree and calling {@link Class#getDeclaredMethods()}.
     * </p>
     *
     * @param aClass {@link Class} is interface or class to implement.
     * @return {@link String} that implements all {@link Method}.
     */
    private String implMethods(Class<?> aClass) {
        HashSet<MethodSignature> methods = Arrays.stream(aClass.getMethods())
                .map(MethodSignature::new)
                .collect(Collectors.toCollection(HashSet::new));
        StringBuilder stringBuilder = new StringBuilder();
        if (!aClass.isInterface()) {
            while (aClass != null) {
                methods.addAll(Arrays.stream(aClass.getDeclaredMethods()).map(MethodSignature::new).collect(Collectors.toList()));
                aClass = aClass.getSuperclass();
            }
        }
        for (var method : methods.stream()
                .map(m -> m.method)
                .filter(m -> Modifier.isAbstract(m.getModifiers()))
                .collect(Collectors.toList()))
            stringBuilder.append(generateMethod(method))
                    .append(System.lineSeparator());
        return stringBuilder.toString();
    }

    /**
     * Generated correct java code {@link String} that implements all {@link Constructor}
     * the specified <i>aClass</i>.
     *
     * <p>
     * If {@link Class} <i>aClass == Interface</i> then java code implemented constructors
     * is <code>""</code>. Else all not private constructors implemented {@link Implementor#generateConstructor}
     * and append in {@link StringBuilder}.
     * </p>
     *
     * @param aClass is {@link Class} that given constructors  {@link Constructor} to implement.
     * @return {@link String} that implements all {@link Constructor}.
     * @throws ImplerException when don't find {@link Constructor} at the specified {@link Class}.
     */
    private String implConstructors(Class<?> aClass) throws ImplerException {
        if (aClass.isInterface())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers()))
                stringBuilder.append(generateConstructor(constructor))
                        .append(System.lineSeparator());
        }
        if (stringBuilder.length() == 0)
            throw new ImplerException("No extends this class.");
        return stringBuilder.toString();
    }

    /**
     * Generated correct java code {@link String} that implements {@link Package}
     * the specified <i>aClass</i> {@link Class}.
     *
     * <p>
     * If {@link Class#getPackageName()} == "" then java code implements this package
     * is "". Else return {@link String} "package " + <code>aClass.getPackageName()</code>.
     * </p>
     *
     * @param aClass is {@link Class} that given {@link Package} to implement.
     * @return {@link String} that implements {@link Package}.
     */
    private String getPackage(Class<?> aClass) {
        if (aClass.getPackageName().equals(""))
            return "";
        else return "package " + aClass.getPackageName() + SEMICOLON +
                System.lineSeparator();
    }

    /**
     * Generated correct java code {@link String} that implements header the
     * specified <i>aClass</i> {@link Class}.
     *
     * <p>
     * Header class implemented will have:
     *      <ul>
     *          <li>access identifier is public</li>
     *          <li>Name of header == {@link Class#getSimpleName() + Impl.java}</li>
     *          <li>if <i>aClass</i> is interface then generated class implements {@link Class}
     *          else extends</li>
     *          <li>Name of the implemented class == {@link Class#getCanonicalName()}</li>
     *      </ul>
     *
     *
     * @param aClass is {@link Class} to implement.
     * @return {@link String} is package.
     */
    private String generateHeader(Class<?> aClass) {
        int mods = aClass.getModifiers();
        return getPackage(aClass) +
                "public class " + aClass.getSimpleName() + IMPL +
                (Modifier.isInterface(mods) ? " implements " : " extends ") +
                aClass.getCanonicalName() +
                OPEN_BRACKET +
                System.lineSeparator();
    }

    /**
     * Create path for implemented java file or class file.
     *
     * @param path is root path {@link Path}.
     * @param aClass is {@link Class} to implement.
     * @param suffix given information about it's <i>file.java</i> or <i>class.file</i>.
     * @return {@link Path} where write new file.
     */
    private Path createOutPath(Path path, Class<?> aClass, String suffix) {
        return path.resolve(aClass.getPackageName().replace('.', File.separatorChar))
                .resolve(aClass.getSimpleName() + IMPL + suffix);
    }

    /**
     * Create parent path for specified path.
     *
     * @param path given his parent.
     * @throws ImplerException when can't directory create.
     */
    private void createParentPath(Path path) throws ImplerException {
        try {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            throw new ImplerException("Can't directory create for implementor class.");
        }
    }

    /**
     * Generated java file implements the specified <i>aClass</i> and
     * puts in the {@link Path} with name is {@link Class#getSimpleName()} + Impl.
     *
     *
     *
     * @param aClass is {@link Class} to implement.
     * @param path where to generate the implementation.
     * @throws ImplerException when:
     * <ul>
     *     <li><i>aClass == null or path == null</i></li>
     *     <li><i>aClass == Primitive</i></li>
     *     <li><i>aClass == Array</i></li>
     *     <li><i>aClass == Final class</i></li>
     *     <li><i>aClass == Private class or interface</i></li>
     *     <li><i>aClass == Enum.class</i></li>
     * </ul>
     */
    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        checkArguments(aClass, path);
        if (aClass.isPrimitive()
                || aClass.isArray()
                || aClass == Enum.class
                || Modifier.isFinal(aClass.getModifiers())
                || Modifier.isPrivate(aClass.getModifiers())
        )
            throw new ImplerException("Not implementor this type.");
        path = createOutPath(path, aClass, JAVA);
        createParentPath(path);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write ((generateHeader(aClass)
                    + implConstructors(aClass)
                    + implMethods(aClass)
                    + CLOSE_BRACKET).chars()
            .mapToObj(c -> String.format("\\u%04x", c))
                    .collect(Collectors.joining()));
        } catch (IOException e) {
            throw new ImplerException("Error to create file.java");
        }
    }

    /**
     * Generated jar file with file.class that implements the specified <i>aClass</i>
     * puts in the {@link Path}.
     *
     * @param aClass aClass is {@link Class} to implement.
     * @param path where to generate the jar.file.
     * @throws ImplerException when:
     * <ul>
     *     <li><i>aClass == null or path == null</i></li>
     *     <li>Can't create temporary directory.</li>
     *     <li>Can't find java compiler.</li>
     *     <li>Can't compile generated class.</li>
     *     <li>Generated class with compilation wrongs.</li>
     *     <li>Error writing to jar-file.</li>
     * </ul>
     */
    @Override
    public void implementJar(Class<?> aClass, Path path) throws ImplerException {
        checkArguments(aClass, path);
        createParentPath(path);
        Path tempDirectory;
        try {
            tempDirectory = Files.createTempDirectory("tempDirectory");
        } catch (IOException e) {
            throw new ImplerException("Can't create temporary directory.");
        }
        try {
            implement(aClass, tempDirectory);
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
                throw new ImplerException("Can't find java compiler.");
            String[] args;
            try {
                args = new String[]{"-cp",
                        Path.of(aClass.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(),
                        createOutPath(tempDirectory, aClass, JAVA).toString()
                };
            } catch (URISyntaxException e) {
                throw new ImplerException("Can't compile generated class.");
            }
            int exit_code = compiler.run(null, null, null, args);
            if (exit_code != 0)
                throw new ImplerException("Generated class with compilation wrongs.");
            try (ZipOutputStream jarOutputStream = new ZipOutputStream(Files.newOutputStream(path))) {
                jarOutputStream.putNextEntry(new ZipEntry(aClass.getPackageName().replace(".", "/")
                        .concat("/" + aClass.getSimpleName() + IMPL + CLASS)));
                Files.copy(createOutPath(tempDirectory, aClass, CLASS), jarOutputStream);
            } catch (IOException e) {
                throw new ImplerException("Error writing to jar-file.");
            }
        } finally {
            try {
                assert tempDirectory != null;
                Files.walkFileTree(tempDirectory, DELETE_VISITOR);
            } catch (IOException e) {
                System.out.println("Can't delete temp directory");
            }
        }
    }

    /**
     * Class for delete temporary directory.
     */
    private static final SimpleFileVisitor<Path> DELETE_VISITOR = new SimpleFileVisitor<>() {
        /**
         * Delete file that visit.
         *
         * @param file that delete.
         * @param attrs addition attributes.
         * @return FileVisitResult
         * @throws IOException when can't delete file or directory.
         */
        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * Delete directory that visit.
         *
         * @param dir that delete.
         * @param exc is {@link IOException} that appeared from recursive.
         * @return FileVisitResult
         * @throws IOException when can't delete file or directory.
         */
        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    };

    /**
     * Main method Implementor class gets 2 or 3 arguments.
     * If (size arguments == 2) Implementor generated java - file that
     * implements {@link Class#forName} <code>args[0]</code>.
     * Else Implementor generated jar - file with implemented {@link Class#forName} <code>args[1]</code>.
     * @param args is command line arguments.
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length < 2 || args.length > 3) {
                throw new ImplerException("Incorrect run program data.");
            }
            Implementor implementor = new Implementor();
            implementor.checkArguments(Arrays.stream(args).toArray());
            if (args.length == 2)
                implementor.implement(Class.forName(args[0]), Path.of(args[1]));
            else if (args[0].equals("-jar"))
                implementor.implementJar(Class.forName(args[1]), Path.of(args[2]));
            else throw new ImplerException("Incorrect run program data.");
        } catch (InvalidPathException e) {
            System.err.println("Invalid path implementor class: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class or Implementor not found exception: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Implementor exception: " + e.getMessage());
        }
    }
}