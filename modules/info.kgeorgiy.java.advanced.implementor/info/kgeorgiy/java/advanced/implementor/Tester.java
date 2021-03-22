package info.kgeorgiy.java.advanced.implementor;

import info.kgeorgiy.java.advanced.base.BaseTester;
import info.kgeorgiy.java.advanced.implementor.full.interfaces.CovariantReturns;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public class Tester extends BaseTester {
    public static void main(final String... args) {
        new Tester()
                .add("interface", InterfaceImplementorTest.class)
                .add("class", ClassImplementorTest.class)
                .add("advanced", AdvancedImplementorTest.class)
                .add("covariant", CovariantImplementorTest.class)
                .run(args);
    }
}
