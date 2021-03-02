package info.kgeorgiy.java.advanced.student;

import info.kgeorgiy.java.advanced.base.BaseTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for easy version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-student">Student</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StudentQueryTest extends BaseTest {
    private static final Random RANDOM = new Random(5252958235234523412L);
    private static final List<String> FIRST_NAMES = List.of("Анастасия", "Артем");
    private static final List<String> LAST_NAMES = List.of("Тушканова", "Шашуловский");
    private static final List<GroupName> GROUPS = List.of(GroupName.M3238, GroupName.M3239);

    private static final List<Student> STUDENTS = IntStream.range(0, 4)
            .mapToObj(id -> new Student(id, random(FIRST_NAMES), random(LAST_NAMES), random(GROUPS)))
            .collect(Collectors.toUnmodifiableList());

    private static final List<List<Student>> INPUTS = IntStream.range(1, STUDENTS.size())
            .mapToObj(size -> {
                final List<Student> students = new ArrayList<>(STUDENTS);
                Collections.shuffle(students, RANDOM);
                return List.copyOf(students.subList(0, size));
            })
            .collect(Collectors.toUnmodifiableList());


    private static <T> T random(final List<T> values) {
        return values.get(RANDOM.nextInt(values.size()));
    }

    private final StudentQuery db = createCUT();

    @Test
    public void test01_testGetFirstNames() {
        testGet(db::getFirstNames, "Артем", "Артем,Анастасия", "Анастасия,Анастасия,Артем");
    }

    @Test
    public void test02_testGetLastNames() {
        testGet(db::getLastNames, "Шашуловский", "Шашуловский,Тушканова", "Тушканова,Тушканова,Шашуловский");
    }

    @Test
    public void test03_testGetGroups() {
        testGet(db::getGroups, "M3238", "M3238,M3238", "M3239,M3239,M3238");
    }

    @Test
    public void test04_testGetFullNames() {
        testGet(
                db::getFullNames,
                "Артем Шашуловский", "Артем Шашуловский,Анастасия Тушканова", "Анастасия Тушканова,Анастасия Тушканова,Артем Шашуловский"
        );
    }

    @Test
    public void test05_testGetDistinctFirstNames() {
        testGet(db::getDistinctFirstNames, "Артем", "Анастасия,Артем", "Анастасия,Артем");
    }

    @Test
    public void test06_testGetMaxStudentFirstName() {
        testString(db::getMaxStudentFirstName, "Артем", "Анастасия", "Анастасия");
    }

    @Test
    public void test07_testSortStudentsById() {
        testList(db::sortStudentsById, new int[][]{{0}, {0, 1}, {1, 2, 0}});
    }

    @Test
    public void test08_testSortStudentsByName() {
        testList(db::sortStudentsByName, new int[][]{{0}, {0, 1}, {2, 1, 0}});
    }

    @Test
    public void test09_testFindStudentsByFirstName() {
        testFind(db::findStudentsByFirstName, FIRST_NAMES, new int[][]{{0}, {1}, {2}});
    }

    @Test
    public void test10_testFindStudentsByLastName() {
        testFind(db::findStudentsByLastName, LAST_NAMES, new int[][]{{0}, {1}, {2}});
    }

    @Test
    public void test11_testFindStudentsByGroup() {
        testFind(db::findStudentsByGroup, GROUPS, new int[][]{{}, {0, 1}, {1, 0}});
    }

    @Test
    public void test12_findStudentNamesByGroup() {
        testString(
                students -> find(db::findStudentNamesByGroupList, students, GROUPS).toString(),
                "[]", "[Тушканова=Анастасия, Шашуловский=Артем]", "[Тушканова=Анастасия]"
        );
    }

    public static <T, R, A> void test(final List<T> inputs, final Function<T, R> query, final BiFunction<T, A, R> answer, final A[] answers) {
        for (int i = 0; i < inputs.size(); i++) {
            final T input = inputs.get(i);
            Assert.assertEquals("For " + input, answer.apply(input, answers[i]), query.apply(input));
        }
    }

    private static <T> void testGet(final Function<List<Student>, Collection<T>> query, final String... answers) {
        testString(query.andThen(vs -> vs.stream().map(Object::toString).collect(Collectors.joining(","))), answers);
    }

    protected static <T> void testString(final Function<List<Student>, T> query, final String... answers) {
        test(query.andThen(Objects::toString), (students, answer) -> answer, answers);
    }

    private static <T> void testFind(final BiFunction<List<Student>, T, List<Student>> query, final List<T> values, final int[][] answers) {
        testList(students -> find(query, students, values), answers);
    }

    private static <T, R> R find(final BiFunction<List<Student>, T, R> query, final List<Student> students, final List<T> values) {
        return query.apply(students, values.get(students.size() % values.size()));
    }

    private static void testList(final Function<List<Student>, List<Student>> query, final int[][] answers) {
        test(query, StudentQueryTest::getStudents, answers);
    }

    @SafeVarargs @SuppressWarnings("varargs")
    public static <T, A> void test(final Function<List<Student>, T> query, final BiFunction<List<Student>, A, T> answer, final A... answers) {
        test(INPUTS, query, answer, answers);
    }

    public static List<Student> getStudents(final List<Student> students, final int[] answer) {
        return IntStream.of(answer).mapToObj(students::get).collect(Collectors.toList());
    }
}
