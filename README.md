# Тесты к курсу «Технологии Java»

[Условия домашних заданий](https://www.kgeorgiy.info/courses/java-advanced/homeworks.html)


## Домашнее задание 4. Implementor

Класс должен реализовывать интерфейс
[Impler](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/Impler.java).

Исходный код

 * простой вариант (`interface`): 
    [тесты](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/InterfaceImplementorTest.java)
 * сложный вариант (`class`):
    [тесты](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/ClassImplementorTest.java)
 * продвинутый вариант (`advanced`):
    [тесты](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/AdvancedImplementorTest.java)
 * преварительные тесты бонусного варианта (`covariant`):
    [тесты](modules/info.kgeorgiy.java.advanced.implementor/info/kgeorgiy/java/advanced/implementor/AdvancedImplementorTest.java)

Тестовый модуль: [info.kgeorgiy.java.advanced.implementor](artifacts/info.kgeorgiy.java.advanced.implementor.jar)


## Домашнее задание 3. Студенты

Исходный код

 * простой вариант (`StudentQuery`):
    [интерфейс](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentQuery.java),
    [тесты](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/StudentQueryTest.java)
 * сложный вариант (`GroupQuery`):
    [интерфейс](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/GroupQuery.java),
    [тесты](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/GroupQueryTest.java)
 * продвинутый вариант (`AdvancedQuery`):
    [интерфейс](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/AdvancedQuery.java),
    [тесты](modules/info.kgeorgiy.java.advanced.student/info/kgeorgiy/java/advanced/student/AdvancedQueryTest.java)

Тестовый модуль: [info.kgeorgiy.java.advanced.student](artifacts/info.kgeorgiy.java.advanced.student.jar)


## Домашнее задание 2. ArraySortedSet

Исходный код

 * простой вариант (`SortedSet`): 
    [тесты](modules/info.kgeorgiy.java.advanced.arrayset/info/kgeorgiy/java/advanced/arrayset/SortedSetTest.java)
 * сложный вариант (`NavigableSet`): 
    [тесты](modules/info.kgeorgiy.java.advanced.arrayset/info/kgeorgiy/java/advanced/arrayset/NavigableSetTest.java)
 * продвинутый вариант (`AdvancedSet`): 
    [тесты](modules/info.kgeorgiy.java.advanced.arrayset/info/kgeorgiy/java/advanced/arrayset/AdvancedSetTest.java)

Тестовый модуль: [info.kgeorgiy.java.advanced.arrayset](artifacts/info.kgeorgiy.java.advanced.arrayset.jar)


## Домашнее задание 1. Обход файлов

Исходный код

 * простой вариант (`Walk`):
    [тесты](modules/info.kgeorgiy.java.advanced.walk/info/kgeorgiy/java/advanced/walk/WalkTest.java)
 * сложный вариант (`RecursiveWalk`):
    [тесты](modules/info.kgeorgiy.java.advanced.walk/info/kgeorgiy/java/advanced/walk/RecursiveWalkTest.java)
 * продвинутый вариант (`AdvancedWalk`):
    должный проходить тесты от простого и с ложного вариантов

Тестовый модуль: [info.kgeorgiy.java.advanced.walk](artifacts/info.kgeorgiy.java.advanced.walk.jar)

Для того, чтобы протестировать программу:

 * Скачайте
    * тесты
        * [базовый модуль](artifacts/info.kgeorgiy.java.advanced.base.jar)
        * [тестовый модуль](artifacts/info.kgeorgiy.java.advanced.walk.jar) (свой для каждого ДЗ)
    * [библиотеки](lib)
 * Откомпилируйте решение домашнего задания
 * Протестируйте домашнее задание
    * Текущая директория должна:
       * содержать все скачанные `.jar` файлы;
       * содержать скомпилированное решение;
       * __не__ содержать скомпилированные самостоятельно тесты.
    * Запустите тесты:
        `java -cp . -p . -m <тестовый модуль> <вариант> <полное имя класса>`
    * Пример для простого варианта ДЗ-1:
        `java -cp . -p . -m info.kgeorgiy.java.advanced.walk Walk <полное имя класса>`
