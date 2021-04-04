//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package info.kgeorgiy.ja.kurdyukov.jarimplementor;

public final class Interfaces {
    public static final Class<?>[] OK = new Class[]{Interfaces.PublicInterface.class, Interfaces.PackagePrivateInterface.class, Interfaces.InheritedInterface.class, Interfaces.ProtectedInterface.class};
    public static final Class<?>[] FAILED = new Class[]{Interfaces.PrivateInterface.class};

    public Interfaces() {
    }

    public interface PublicInterface {
        String hello();
    }

    interface PackagePrivateInterface {
        String hello();
    }

    interface InheritedInterface extends Interfaces.PrivateInterface {
    }

    protected interface ProtectedInterface {
        String hello();
    }

    private interface PrivateInterface {
        String hello();
    }
}
