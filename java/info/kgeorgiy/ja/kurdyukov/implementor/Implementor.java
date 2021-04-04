package info.kgeorgiy.ja.kurdyukov.implementor;

import info.kgeorgiy.java.advanced.implementor.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Implementor implements Impler {
    private static final String TAB = "    ";
    private static final String WHITESPACE = " ";
    private static final String OPEN_BRACKET = "{";
    private static final String CLOSE_BRACKET = "}";
    private static final String IMPL = "Impl";
    private static final String PUBLIC = "public";
    private static final String SUPER = "super";
    private static final String SEMICOLON = ";";
    private static final String THROWS = "throws";

    private String generateParams(Class<?>[] params, boolean flag) {
        return IntStream.range(0, params.length)
                .mapToObj(i -> flag ? params[i].getCanonicalName().concat(" a" + i)
                        : "a" + i)
                .collect(Collectors.joining(", ", "(", ")"));
    }

    private String implMethod(Method method) {
        if (void.class.equals(method.getReturnType())) {
            return "";
        }
        if (method.getReturnType().isPrimitive()) {
            return method.getReturnType().equals(boolean.class) ?
                    " return false; " : " return 0; ";
        }
        return " return null; ";
    }

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
                implMethod(method) +
                CLOSE_BRACKET;
    }

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

    private String generateConstructor(Constructor<?> constructor, String name) {
        return TAB +
                PUBLIC +
                WHITESPACE +
                name +
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

    public class MethodSignature {
        private final Method method;

        public MethodSignature(Method method) {
            this.method = method;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.method.getName() + generateParams(this.method.getParameterTypes(), true));
        }

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

    private String implConstructors(Class<?> aClass) throws ImplerException {
        if (aClass.isInterface())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        for (Constructor<?> constructor : aClass.getDeclaredConstructors()) {
            if (!Modifier.isPrivate(constructor.getModifiers()))
                stringBuilder.append(generateConstructor(constructor, aClass.getSimpleName()))
                        .append(System.lineSeparator());
        }
        if (stringBuilder.length() == 0)
            throw new ImplerException("No extends this class.");
        return stringBuilder.toString();
    }

    private String getPackage(Class<?> aClass) {
        if (aClass.getPackageName().equals(""))
            return "";
        else return "package " + aClass.getPackageName() + SEMICOLON +
                System.lineSeparator();
    }

    private String generateHeader(Class<?> aClass) {
        int mods = aClass.getModifiers();
        return  getPackage(aClass) +
                "public class " + aClass.getSimpleName() + IMPL +
                (Modifier.isInterface(mods) ? " implements " : " extends ") +
                aClass.getCanonicalName() +
                OPEN_BRACKET +
                System.lineSeparator();
    }

    private Path createPath(Path path, Class<?> aClass) throws ImplerException {
        try {
            path = path.resolve(aClass.getPackageName().replace('.', File.separatorChar))
                    .resolve(aClass.getSimpleName() + IMPL + ".java");
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            return path;
        } catch (IOException e) {
            throw new ImplerException("Can't directory create for implementor class.");
        }
    }

    @Override
    public void implement(Class<?> aClass, Path path) throws ImplerException {
        if (aClass.isPrimitive()
                || aClass.isArray()
                || aClass == Enum.class
                || Modifier.isFinal(aClass.getModifiers())
                || Modifier.isPrivate(aClass.getModifiers())
        )
            throw new ImplerException("Not implementor this type.");
        path = createPath(path, aClass);
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            bufferedWriter.write(generateHeader(aClass)
                    + implConstructors(aClass)
                    + implMethods(aClass)
                    + CLOSE_BRACKET);

        } catch (IOException e) {
            throw new ImplerException("Error to create file.java");
        }
    }

    public static void main(String[] args) {
        try {
            if (args == null || args.length != 2
                    || args[0] == null || args[1] == null) {
                throw new ImplerException("Incorrect run program data");
            }
            Implementor implementor = new Implementor();
            implementor.implement(Class.forName(args[0]), Path.of(args[1]));
        } catch (InvalidPathException e) {
            System.err.println("Invalid path implementor class: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Class or Implementor not found exception: " + e.getMessage());
        } catch (ImplerException e) {
            System.err.println("Implementor exception: " + e.getMessage());
        }
    }
}