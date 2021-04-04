package info.kgeorgiy.ja.kurdyukov.student;

import info.kgeorgiy.java.advanced.student.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements GroupQuery {
    private static final Student STUDENT_EMPTY = new Student(-1, "", "", GroupName.M3237);
    private static final Comparator<Student> COMPARATOR = Comparator
            .comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparingInt(Student::getId);


    private <A, C extends Collection<Student>>
    Stream<Map.Entry<GroupName, C>> getMapToGroup(Collection <Student> students,
                                                  Collector<Student, A, C> collector) {
        return students.stream()
                .collect(Collectors.groupingBy(Student::getGroup, collector))
                .entrySet().stream();
    }

    private List<Group> getGroupsBySomething(Collection <Student> students, UnaryOperator<List<Student>> sorter) {
        return getMapToGroup(students, Collectors.collectingAndThen(Collectors.toList(), sorter))
                .sorted(Entry.comparingByKey())
                .map(element -> new Group(element.getKey(), element.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> collection) {
        return getGroupsBySomething(collection, this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> collection) {
        return getGroupsBySomething(collection, this::sortStudentsById);
    }

    private <T> GroupName getLargestGroupBySomething(Collection <Student> collection,
                                                     Function<List<Student>, Integer> function,
                                                     Comparator<Map.Entry<GroupName, List<Student>>> comparator) {
        return getMapToGroup(collection, Collectors.toList())
                .max(Comparator.comparingInt((Entry<GroupName, List<Student>> group) -> function.apply(group.getValue()))
                .thenComparing(comparator))
                .map(Entry::getKey)
                .orElse(null);
    }

    @Override
    public GroupName getLargestGroup(Collection<Student> collection) {
        return getLargestGroupBySomething(collection, Collection::size,
                Entry.comparingByKey());
    }

    @Override
    public GroupName getLargestGroupFirstName(Collection<Student> collection) {
        return getLargestGroupBySomething(collection, a -> getDistinctFirstNames(a).size(),
                Entry.<GroupName, List<Student>>comparingByKey().reversed());
    }

    private <T, C extends Collection<T>> C getCollectionSomething(final List<Student> students,
                                                                    Function<Student, T> function,
                                                                    Supplier<C> cSupplier) {
        return students.stream()
                .map(function)
                .collect(Collectors.toCollection(cSupplier));
    }

    private <T> List<T> getListSomething(List<Student> list, Function<Student, T> function) {
        return getCollectionSomething(list, function, ArrayList::new);
    }

    @Override
    public List<String> getFirstNames(List<Student> list) {
        return getListSomething(list, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> list) {
        return getListSomething(list, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> list) {
        return getListSomething(list, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> list) {
        return getListSomething(list,
                (s) -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> list) {
        return getCollectionSomething(list, Student::getFirstName, TreeSet::new);
    }

    @Override
    public String getMaxStudentFirstName(List<Student> list) {
        return list.stream()
                .max(Comparator.comparingInt(Student::getId))
                .orElse(STUDENT_EMPTY)
                .getFirstName();
    }

    private List<Student> sortStudentsBySomething(Collection<Student> students,
                                                  Comparator<Student> comparator) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> collection) {
        return sortStudentsBySomething(collection, Comparator.comparingInt(Student::getId));
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> collection) {
        return sortStudentsBySomething(collection, COMPARATOR);
    }

    private <A, R> R findSomething(Collection<Student> students,
                                   Predicate<Student> predicate,
                                   Collector<Student, A, R> collector) {
        return students.stream()
                .filter(predicate)
                .sorted(COMPARATOR)
                .collect(collector);
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> collection,
                                                 String name) {
        return findSomething(collection, (s) -> s.getFirstName().equals(name), Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> collection,
                                                String name) {
        return findSomething(collection, (s) -> s.getLastName().equals(name), Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> collection,
                                             GroupName groupName) {
        return findSomething(collection, (s) -> s.getGroup() == groupName, Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> collection,
                                                       GroupName groupName) {
        return findSomething(collection, (s) -> s.getGroup() == groupName,
                Collectors.toMap(Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }
}
