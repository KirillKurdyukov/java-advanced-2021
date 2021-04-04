SET class1=info\kgeorgiy\ja\kurdyukov\jarimplementor\Implementor$MethodSignature.class
SET class2=info\kgeorgiy\ja\kurdyukov\jarimplementor\Implementor$1.class
SET class3=info\kgeorgiy\ja\kurdyukov\jarimplementor\Implementor.class

javac -cp ..\artifacts\info.kgeorgiy.java.advanced.implementor.jar info\kgeorgiy\ja\kurdyukov\jarimplementor\Implementor.java
jar cmvf MANIFEST.MF Implementor.jar %class1% %class2% %class3%
rm %class1%
rm %class2%
rm %class3%
java -jar Implementor.jar %*