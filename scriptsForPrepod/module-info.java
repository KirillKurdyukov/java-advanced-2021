module info.kgeorgiy.ja.kurdyukov.implementor {
    requires java.compiler;
    requires info.kgeorgiy.java.advanced.implementor;

    opens info.kgeorgiy.java.advanced.implementor;
    exports info.kgeorgiy.java.advanced.implementor;
}