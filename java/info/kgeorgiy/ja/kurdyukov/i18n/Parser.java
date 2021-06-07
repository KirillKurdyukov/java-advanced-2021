package info.kgeorgiy.ja.kurdyukov.i18n;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class Parser {
    private final NumberFormat parseNumber;
    private final NumberFormat parseMoney;
    private final List<DateFormat> formatsDate;

    private static final List<Integer> lists = List.of(DateFormat.FULL,
            DateFormat.LONG,
            DateFormat.MEDIUM,
            DateFormat.SHORT);

    public Parser(Locale locale) {
        this.formatsDate = lists.stream()
                .map(style -> DateFormat.getDateInstance(style, locale))
                .collect(Collectors.toList());
        this.parseNumber = NumberFormat.getNumberInstance(locale);
        this.parseMoney = NumberFormat.getCurrencyInstance(locale);
    }

    public Number parseNumber(String text, ParsePosition position) {
        return parseNumber.parse(text, position);
    }

    public Number parseMoney(String text, ParsePosition position) {
        return parseMoney.parse(text, position);
    }

    public Date parseDate(String text, ParsePosition position) {
        return formatsDate.stream().map(format -> format.parse(text, position)).filter(Objects::nonNull).findAny().orElse(null);
    }

}
