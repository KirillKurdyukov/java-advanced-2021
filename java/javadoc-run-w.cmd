SET link=https://docs.oracle.com/en/java/javase/11/docs/api/
SET p1=../artifacts/info.kgeorgiy.java.advanced.implementor.jar
SET p2=info/kgeorgiy/ja/kurdyukov/jarimplementor/Implementor.java
SET package=info.kgeorgiy.ja.kurdyukov.jarimplementor

javadoc -private -author -version -link %link%  -cp %p1% -d ../javadoc %p2%
