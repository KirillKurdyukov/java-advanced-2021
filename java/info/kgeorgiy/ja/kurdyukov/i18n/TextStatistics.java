package info.kgeorgiy.ja.kurdyukov.i18n;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TextStatistics {

    public enum Type {
        SENTENCE, WORLD, MONEY, NUMBER
    }

    private final Locale input;
    private final Locale output;
    private final Parser parser;
    private final static String lineSeparator = System.lineSeparator();

    public TextStatistics(Locale input, Locale output) {
        this.input = input;
        this.output = output;
        parser = new Parser(input);
    }

    public static void main(String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Correct usage: <text locale> <output locale> <text file> <report file>");

        try {
            TextStatistics statistics = new TextStatistics(args[0], args[1]);
            statistics.process(args[2], args[3]);
        } catch (TextStatisticsException e) {
            System.err.println(e.getMessage());
        }
    }

    private Object[] getBlogForString(ResultStatistics<String> result) {
        return new Object[]{
                result.getSizeData(),
                result.getSizeDifferentEl(),
                result.getMinElement(),
                result.getMaxElement(),
                result.getMinLengthEl(),
                result.getMaxLengthEl(),
                result.getMiddleSize()
        };
    }

    private <T> Object[] getBlogForNumberElement(ResultStatistics<T> result) {
        return new Object[]{
                result.getSizeData(),
                result.getSizeDifferentEl(),
                result.getMinElement(),
                result.getMaxElement(),
                result.getAverageData()
        };
    }

    private String FORM_FOR_STRING = null;
    private String FORM_FOR_STAT = null;
    private String FORM_FOR_STAT_UNIQUE = null;
    private String FORM_FOR_STAT_EXAMPLE = null;
    private String FORM_FOR_MONEY = null;

    private ResourceBundle bundle;

    private void writeStringStatistics(BufferedWriter writer,
                                       Object[] args,
                                       String title,
                                       String codeWord) throws IOException {
        writerForAnyStatistics(writer, args, title, codeWord, false);
        writer.write(MessageFormat.format(FORM_FOR_STAT_EXAMPLE,
                bundle.getString("minLen" + codeWord),
                (args[4] == null) ? 0 : args[4].toString().length(), args[4]));
        writer.write(MessageFormat.format(FORM_FOR_STAT_EXAMPLE,
                bundle.getString("maxLen" + codeWord),
                (args[5] == null) ? 0 : args[5].toString().length(), args[5]));
        writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("averageLen" + codeWord), args[6]));
    }

    private void writeNumberStatistics(BufferedWriter writer,
                                       Object[] args,
                                       String title,
                                       String codeWord,
                                       boolean fl) throws IOException {
        writerForAnyStatistics(writer, args, title, codeWord, fl);
        writer.write(MessageFormat.format(fl ? FORM_FOR_MONEY : FORM_FOR_STAT, bundle.getString("average" + codeWord), args[4]));
    }

    private void writerForAnyStatistics(BufferedWriter writer,
                                        Object[] args,
                                        String title,
                                        String codeWord,
                                        boolean fl) throws IOException {
        writer.write(MessageFormat.format(FORM_FOR_STRING + lineSeparator,
                bundle.getString(title)));
        writer.write(MessageFormat.format(FORM_FOR_STAT_UNIQUE,
                bundle.getString("count" + codeWord),
                args[0],
                args[1],
                bundle.getString("different")));
        writer.write(MessageFormat.format(fl ? FORM_FOR_MONEY : FORM_FOR_STAT,
                bundle.getString("min" + codeWord),
                args[2]));
        writer.write(MessageFormat.format(fl ? FORM_FOR_MONEY : FORM_FOR_STAT,
                bundle.getString("max" + codeWord),
                args[3]));
    }

    private void process(String inputPath, String outputPath) throws TextStatisticsException {
        final String text;
        try {
            text = Files.readString(Path.of(inputPath));
        } catch (IOException e) {
            throw new TextStatisticsException("Input file error. " + e.getMessage());
        }
        ResultStatistics<String> sentences = getTextStatistics(Type.SENTENCE, text);
        ResultStatistics<String> worlds = getTextStatistics(Type.WORLD, text);
        ResultStatistics<Number> numbers = getNumbersStatistics(Type.NUMBER, text);
        ResultStatistics<Number> currency = getNumbersStatistics(Type.MONEY, text);
        ResultStatistics<Date> dates = getDatesStatistics(text);
        bundle = ResourceBundle.getBundle("info.kgeorgiy.ja.kurdyukov.i18n.UsageResourseBundle", output);
        FORM_FOR_STRING = bundle.getString("formString");
        FORM_FOR_STAT = bundle.getString("formStat") + lineSeparator;
        FORM_FOR_STAT_UNIQUE = bundle.getString("formStatUnique") + lineSeparator;
        FORM_FOR_STAT_EXAMPLE = bundle.getString("formStatEx") + lineSeparator;
        FORM_FOR_MONEY = bundle.getString("formMoney") + lineSeparator;
        Object[] blog1 = {sentences.getSizeData(),
                worlds.getSizeData(),
                numbers.getSizeData(),
                currency.getSizeData(),
                dates.getSizeData()};
        Object[] blog2 = getBlogForString(sentences);
        Object[] blog3 = getBlogForString(worlds);
        Object[] blog4 = getBlogForNumberElement(numbers);
        Object[] blog5 = getBlogForNumberElement(currency);
        Object[] blog6 = getBlogForNumberElement(dates);
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputPath))) {
            writer.write(MessageFormat.format(FORM_FOR_STRING + "{1}" + lineSeparator,
                    bundle.getString("analyzedFile"), inputPath));


            writer.write(MessageFormat.format(FORM_FOR_STRING + lineSeparator,
                    bundle.getString("summaryStatistics")));
            writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("countSentence"), blog1[0]));
            writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("countWord"), blog1[1]));
            writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("countNumber"), blog1[2]));
            writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("countMoney"), blog1[3]));
            writer.write(MessageFormat.format(FORM_FOR_STAT, bundle.getString("countDate"), blog1[4]));

            writeStringStatistics(writer, blog2, "sentenceStatistics", "Sentence");
            writeStringStatistics(writer, blog3, "wordStatistics", "Word");
            if (numbers.getSizeData() != 0)
                writeNumberStatistics(writer, blog4, "numberStatistics", "Number", false);
            if (currency.getSizeData() != 0)
                writeNumberStatistics(writer, blog5, "moneyStatistics", "Money", true);
            if (dates.getSizeData() != 0)
                writeNumberStatistics(writer, blog6, "dateStatistics", "Date", false);
        } catch (IOException e) {
            throw new TextStatisticsException("Output file error. " + e.getMessage());
        }
    }

    private List<String> splitSentences(final String text, final BreakIterator boundary) {
        boundary.setText(text);
        final List<String> parts = new ArrayList<>();
        for (
                int begin = boundary.first(), end = boundary.next();
                end != BreakIterator.DONE;
                begin = end, end = boundary.next()
        ) {
            parts.add(text.substring(begin, end).trim());
        }
        return parts;
    }

    private List<Number> splitNumbers(final String text) {
        final BreakIterator boundary = BreakIterator.getWordInstance(input);
        boundary.setText(text);
        final List<Number> parts = new ArrayList<>();
        for (
                int begin = boundary.first(), end = boundary.next(), parsePosition = 0;
                end != BreakIterator.DONE;
                begin = end, end = boundary.next()
        ) {
            if (begin < parsePosition)
                continue;
            ParsePosition position = new ParsePosition(begin);
            if (parser.parseDate(text, position) != null || parser.parseMoney(text, position) != null) {
                parsePosition = position.getIndex();
                continue;
            }

            Number currentNum = parser.parseNumber(text, new ParsePosition(begin));

            if (currentNum != null)
                parts.add(currentNum);
        }
        return parts;
    }

    private <T> List<T> splitMoneyOrDate(final String text, final BiFunction<String, ParsePosition, T> function) {
        final BreakIterator boundary = BreakIterator.getWordInstance(input);
        boundary.setText(text);
        final List<T> parts = new ArrayList<>();
        for (
                int begin = boundary.first(), end = boundary.next(), parsePosition = 0;
                end != BreakIterator.DONE;
                begin = end, end = boundary.next()
        ) {
            if (begin < parsePosition)
                continue;

            ParsePosition position = new ParsePosition(begin);
            T currentNum = function.apply(text, position);

            if (currentNum != null) {
                parts.add(currentNum);
                parsePosition = position.getIndex();
            }
        }
        return parts;
    }

    private <T> T getExtremalElement(List<T> parts, Comparator<T> comparator) {
        return parts.stream()
                .max(comparator)
                .orElse(null);
    }

    private <T> ResultStatistics<T> competeStatistics(List<T> parts,
                                                      Comparator<T> comparator) {
        final ResultStatistics<T> result = new ResultStatistics<>();
        result.setSizeData(parts.size());
        result.setSizeDifferentEl(parts.stream()
                .collect(Collectors.toCollection(() -> new TreeSet<>(comparator)))
                .size());
        result.setMaxElement(getExtremalElement(parts, comparator));
        result.setMinElement(getExtremalElement(parts, comparator.reversed()));
        return result;
    }


    public ResultStatistics<String> getTextStatistics(Type mode, String text) throws TextStatisticsException {
        List<String> parts = null;
        switch (mode) {
            case WORLD:
                parts = splitSentences(text, BreakIterator.getWordInstance(input))
                        .stream()
                        .filter(world -> world
                                .codePoints()
                                .anyMatch(Character::isLetter))
                        .collect(Collectors.toList());
                break;
            case SENTENCE:
                parts = splitSentences(text, BreakIterator.getSentenceInstance(input));
                break;
        }
        if (parts == null)
            throw new TextStatisticsException("Error retrieving text information");
        Collator collator = Collator.getInstance(input);
        collator.setStrength(Collator.IDENTICAL);
        ResultStatistics<String> resultStatistics = competeStatistics(parts, collator::compare);
        resultStatistics.setMaxLengthEl(getExtremalElement(parts, Comparator.comparingInt(String::length)));
        resultStatistics.setMinLengthEl(getExtremalElement(parts, Comparator.comparingInt(String::length).reversed()));
        resultStatistics.setMiddleSize(parts.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0));
        return resultStatistics;
    }

    public ResultStatistics<Number> getNumbersStatistics(Type type, String text) throws TextStatisticsException {
        List<Number> parts = null;
        switch (type) {
            case NUMBER:
                parts = splitNumbers(text);
                break;
            case MONEY:
                parts = splitMoneyOrDate(text, parser::parseMoney);
                break;
        }
        if (parts == null)
            throw new TextStatisticsException("Error retrieving text information");
        ResultStatistics<Number> result = competeStatistics(parts, Comparator.comparingDouble(Number::doubleValue));
        result.setAverageData(parts.stream()
                .mapToDouble(Number::doubleValue)
                .average()
                .orElse(0));
        return result;
    }

    public ResultStatistics<Date> getDatesStatistics(String text) {
        List<Date> parts = splitMoneyOrDate(text, parser::parseDate);
        ResultStatistics<Date> result = competeStatistics(parts, Date::compareTo);
        result.setAverageData(new Date((long) parts.stream()
                .mapToLong(Date::getTime)
                .average()
                .orElse(0)));
        return result;
    }

    public TextStatistics(String languageIn, String languageOut) throws TextStatisticsException {
        String[] l1 = languageIn.split("_");
        String[] l2 = languageOut.split("_");
        this.input = new Locale.Builder()
                .setLanguageTag(l1[0])
                .setRegion(l1[1])
                .build();
        this.output = new Locale.Builder()
                .setLanguageTag(l2[0])
                .setRegion(l2[1])
                .build();
        if (!(output.getLanguage().equals("ru") || output.getLanguage().equals("en")))
            throw new TextStatisticsException("Incorrect output locale. Only the use of Russian or English is supported.");
        this.parser = new Parser(input);
    }

}
