SET class1=info\kgeorgiy\ja\kurdyukov\implementor\Implementor$MethodSignature.class
SET class2=info\kgeorgiy\ja\kurdyukov\implementor\Implementor$1.class
SET class3=info\kgeorgiy\ja\kurdyukov\implementor\Implementor.class

javac -d jar --module-path ..\..\java-advanced-2021\lib;..\..\java-advanced-2021\artifacts info\kgeorgiy\ja\kurdyukov\implementor\Implementor.java module-info.java
cd jar
jar cmvf ..\MANIFEST.MF ..\Implementor.jar %class1% %class2% %class3% module-info.class
cd ..
java -jar Implementor.jar %*