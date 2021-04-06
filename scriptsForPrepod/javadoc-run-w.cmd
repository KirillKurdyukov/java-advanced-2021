SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET p=info\kgeorgiy\ja\kurdyukov\implementor\Implementor.java
SET modules=..\..\java-advanced-2021\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
javadoc -private -author -version -link %link% -d ..\javadoc %p% %modules%\Impler.java %modules%\JarImpler.java %modules%\ImplerException.java
