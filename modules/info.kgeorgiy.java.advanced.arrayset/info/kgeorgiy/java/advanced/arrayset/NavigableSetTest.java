package info.kgeorgiy.java.advanced.arrayset;

import net.java.quickcheck.collection.Pair;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.java.quickcheck.generator.CombinedGeneratorsIterables.somePairs;
import static net.java.quickcheck.generator.PrimitiveGenerators.fixedValues;
import static org.junit.Assert.assertEquals;

/**
 * Tests for hard version
 * of <a href="https://www.kgeorgiy.info/courses/java-advanced/homeworks.html#homework-arrayset">ArraySet</a> homework
 * for <a href="https://www.kgeorgiy.info/courses/java-advanced/">Java Advanced</a> course.
 *
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NavigableSetTest extends BasicSortedSetTest {
    @Test
    public void test32_ceiling() {
        testElement("ceiling(%s)", NavigableSet::ceiling);
    }

    @Test
    public void test34_floor() {
        testElement("floor(%s)", NavigableSet::floor);
    }


    @Test
    public void test35_navigableTailSet() {
        testElement("tailSet(%s, true)", (s, e) -> s.tailSet(e, true));
        testElement("tailSet(%s, false)", (s, e) -> s.tailSet(e, false));
    }

    @Test
    public void test24_navigableSubSet() {
        testN((elements, comparator, treeSet, set, context) -> {
            final Collection<Integer> all = values(elements);
            for (final Pair<Integer, Integer> p : somePairs(fixedValues(all), fixedValues(all))) {
                final Integer from = p.getFirst();
                final Integer to = p.getSecond();
                if (comparator.compare(from, to) <= 0) {
                    for (int i = 0; i < 4; i++) {
                        assertEq(
                                set.subSet(from, i % 2 == 1, to, i / 2 == 1),
                                treeSet.subSet(from, i % 2 == 1, to, i / 2 == 1),
                                String.format("in subSet(%d, %b, %d, %b) (comparator = %s, elements = %s",
                                        from, i % 2 == 1,
                                        to, i / 2 == 1,
                                        comparator, elements
                                )
                        );
                    }
                }
            }
        });
    }

    @Test
    public void test26_descendingSet() {
        final List<Integer> data = List.of(10, 20, 30);
        final NavigableSet<Integer> s = set(data, Integer::compareTo);
        final NavigableSet<Integer> set = s.descendingSet();
        assertEquals("toArray()", List.of(30, 20, 10), toArray(set));
        assertEquals("size()", 3, set.size());
        assertEquals("first()", 30, set.first().intValue());
        assertEquals("last()", 10, set.last().intValue());
        assertEquals("descendingIterator().next()", 10, set.descendingIterator().next().intValue());

        testGet("floor(%s)", set::floor, descendingPairs(10, 10, 20, 20, 30, 30, null));
        testGet("lower(%s)", set::lower, descendingPairs(10, 20, 20, 30, 30, null, null));
        testGet("ceiling(%s)", set::ceiling, descendingPairs(null, 10, 10, 20, 20, 30, 30));
        testGet("higher(%s)", set::higher, descendingPairs(null, null, 10, 10, 20, 20, 30));

        testGet("headSet(%s).size()", i -> set.headSet(i).size(), descendingPairs(3, 2, 2, 1, 1, 0, 0));
        testGet("tailSet(%s).size()", i -> set.tailSet(i).size(), descendingPairs(0, 1, 1, 2, 2, 3, 3));

        assertEquals("descendingSet().toArray()", data, toArray(set.descendingSet()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Pair<Integer, Integer>[] descendingPairs(final Integer v5, final Integer v10, final Integer v15, final Integer v20, final Integer v25, final Integer v30, final Integer v35) {
        return new Pair[]{
                pair(5, v5),
                pair(10, v10),
                pair(15, v15),
                pair(20, v20),
                pair(25, v25),
                pair(30, v30),
                pair(35, v35),
        } ;
    }

    @SafeVarargs
    private static <T> void testGet(final String format, final Function<T, T> method, final Pair<T, T>... pairs) {
        for (final Pair<T, T> pair : pairs) {
            assertEquals(String.format(format, pair.getFirst()), pair.getSecond(), method.apply(pair.getFirst()));
        }
    }

    private static <T> Pair<T, T> pair(final T arg, final T result) {
        return new Pair<>(arg, result);
    }


    private static void testN(final TestCase<Integer, NavigableSet<Integer>> testCase) {
        test(testCase);
    }

    private static <R> void testElement(final String name, final BiFunction<NavigableSet<Integer>, Integer, R> f) {
        testN((elements, comparator, treeSet, set, context) -> {
            for (final Integer element : inAndOut(elements)) {
                assertEquals(
                        String.format("in %s %s", String.format(name, element), context),
                        f.apply(treeSet, element),
                        f.apply(set, element)
                );
            }
        });
    }
}
