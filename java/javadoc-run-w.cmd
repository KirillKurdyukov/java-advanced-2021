SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET p=info\kgeorgiy\ja\kurdyukov\jarimplementor\Implementor.java
SET modules=..\modules\info.kgeorgiy.java.advanced.implementor\info\kgeorgiy\java\advanced\implementor
javadoc -private -author -version -link %link% -d ..\javadoc %p% %modules%\Impler.java %modules%\JarImpler.java %modules%\ImplerException.java
