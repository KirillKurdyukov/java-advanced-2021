package info.kgeorgiy.ja.kurdyukov.i18n;

import org.junit.Assert;

import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;

public class Test {

    private static final String text1 = "June 1, 2020\n" +
            "06/18/2020\n" +
            "June 16, 2020\n" +
            "Tuesday, June 2, 2020\n" +
            "6/06/2020\n" +
            "June 4, 2020\n" +
            "June 16, 2020\n" +
            "Friday, June 19, 2020\n" +
            "June 10, 2020\n" +
            "June 5, 2020\n" +
            "06/18/2020\n" +
            "06/15/2020\n" +
            "06/06/2020\n" +
            "Friday, June 12, 2020\n" +
            "Sunday, June 7, 2020\n" +
            "June 16, 2020\n" +
            "June 10, 2020\n" +
            "06/08/2020\n" +
            "06/18/2020\n" +
            "06/18/2020\n" +
            "June 9, 2020\n" +
            "June 10, 2020\n" +
            "June 16, 2020\n" +
            "06/06/2020\n" +
            "June 10, 2020\n" +
            "06/11/2020\n" +
            "Friday, June 12, 2020\n" +
            "Friday, June 12, 2020\n" +
            "06/06/2020\n" +
            "June 16, 2020\n" +
            "Saturday, June 13, 2020\n" +
            "Sunday, June 14, 2020\n" +
            "June 10, 2020\n" +
            "June 10, 2020\n" +
            "June 23, 2020\n" +
            "June 16, 2020\n" +
            "06/15/2020\n" +
            "06/18/2020\n" +
            "June 16, 2020\n" +
            "06/17/2020\n" +
            "06/18/2020\n" +
            "June 10, 2020\n" +
            "06/06/2020\n" +
            "Friday, June 19, 2020\n" +
            "Saturday, June 20, 2020\n" +
            "06/06/2020\n" +
            "June 21, 2020\n" +
            "Friday, June 12, 2020\n" +
            "06/22/2020\n" +
            "June 23, 2020\n" +
            "06/06/2020";

    private ResultStatistics<String> sentences;
    private ResultStatistics<String> worlds;
    private ResultStatistics<Number> numbers;
    private ResultStatistics<Number> currency;
    private ResultStatistics<Date> dates;

    private void setDates(TextStatistics statistics, String text1) throws TextStatisticsException {
        sentences = statistics.getTextStatistics(TextStatistics.Type.SENTENCE, text1);
        worlds = statistics.getTextStatistics(TextStatistics.Type.WORLD, text1);
        numbers = statistics.getNumbersStatistics(TextStatistics.Type.NUMBER, text1);
        currency = statistics.getNumbersStatistics(TextStatistics.Type.MONEY, text1);
        dates = statistics.getDatesStatistics(text1);
    }

    @org.junit.Test
    public void test1() throws TextStatisticsException {
        TextStatistics statistics = new TextStatistics(Locale.US, new Locale("ru"));
        setDates(statistics, text1);
        Assert.assertEquals(1, sentences.getSizeData());
        Assert.assertEquals(43, worlds.getSizeData());
        Assert.assertEquals("Friday", worlds.getMinElement());
        Assert.assertEquals("Tuesday", worlds.getMaxElement());
        Assert.assertEquals("Mon Jun 01 00:00:00 MSK 2020", dates.getMinElement().toString());
        Assert.assertEquals("Tue Jun 23 00:00:00 MSK 2020", dates.getMaxElement().toString());
    }


    private static final String text2 = "該文本是中文的特殊文本，出於說明目的，是辛勤工作的一部分。 這是第二句話。 這是第三句話。";

    @org.junit.Test
    public void test2() throws TextStatisticsException {
        TextStatistics statistics = new TextStatistics(Locale.TRADITIONAL_CHINESE, new Locale("en"));
        setDates(statistics, text2);
        Assert.assertEquals(3, sentences.getSizeData());
        Assert.assertEquals(5, worlds.getSizeData());
    }


    private static final String text3 = "Henry Ford (July 30, 1863 – April 7, 1947) was an\n" +
            "American industrialist and business magnate, founder of\n" +
            "the Ford Motor Company and chief developer of the\n" +
            "assembly line technique of mass production. By creating\n" +
            "the first automobile that middle-class Americans could\n" +
            "afford, he converted the automobile from an expensive\n" +
            "curiosity into an accessible conveyance that would\n" +
            "profoundly impact the landscape of the 20th century.\n" +
            "His introduction of the Model T automobile\n" +
            "revolutionized transportation and American industry. As\n" +
            "the owner of the Ford Motor Company, he became one of\n" +
            "the richest and best-known people in the world. He is\n" +
            "credited with \"Fordism\": mass production of inexpensive\n" +
            "goods coupled with high wages for workers. Ford had a\n" +
            "global vision, with consumerism as the key to peace. His\n" +
            "intense commitment to systematically lowering costs\n" +
            "resulted in many technical and business innovations,\n" +
            "including a franchise system that put dealerships\n" +
            "throughout most of North America and in major cities on\n" +
            "six continents. Ford left most of his vast wealth to the\n" +
            "Ford Foundation and arranged for his family to control\n" +
            "the company permanently.\n" +
            "Ford was also widely known for his pacifism during the\n" +
            "first years of World War I, and for promoting antisemitic\n" +
            "content, including The Protocols of the Elders of Zion,\n" +
            "through his newspaper The Dearborn Independent and\n" +
            "the book The International Jew, having an alleged\n" +
            "influence on the development of Nazism.";

    @org.junit.Test
    public void test3() throws TextStatisticsException {
        TextStatistics statistics = new TextStatistics(Locale.US, Locale.ENGLISH);
        setDates(statistics, text3);
        Assert.assertEquals(9, sentences.getSizeData());
        Assert.assertEquals(224, worlds.getSizeData());
        Assert.assertEquals(144, worlds.getSizeDifferentEl());
        Assert.assertEquals(63, sentences.getMinLengthEl().length());
        Assert.assertEquals(309, sentences.getMaxLengthEl().length());
        Assert.assertEquals("Thu Jul 30 00:00:00 MSK 1863", dates.getMinElement().toString());
        Assert.assertEquals("Mon Apr 07 00:00:00 MSK 1947", dates.getMaxElement().toString());
        Assert.assertEquals("Fri Jun 02 23:30:17 MSK 1905", dates.getAverageData().toString());
        Assert.assertEquals(1, numbers.getSizeData());
        Assert.assertEquals(20L, numbers.getMaxElement());
        Assert.assertEquals(0, currency.getSizeData());
        Assert.assertNull(currency.getMaxElement());
    }

    private static final String text4 = "  \n" +
            "التاريخ في النهاية: الثلاثاء، ٩ يونيو ٢٠٢٠. التاريخ في النهاية: ٩\u200F/٦\u200F/٢٠٢٠. التاريخ في النهاية: السبت ٩ يونيو ٢٠٢٠.\n" +
            "٩ يونيو ٢٠٢٠ - التاريخ في الأمام. ٩\u200F/٦\u200F/٢٠٢٠ - التاريخ في الأمام. الثلاثاء، ٩ يونيو ٢٠٢٠ - التاريخ في الأمام.\n" +
            "التاريخ (٩ يونيو ٢٠٢٠) في المنتصف. التاريخ (٩\u200F/٦\u200F/٢٠٢٠) في المنتصف. التاريخ (الثلاثاء، ٩ يونيو ٢٠٢٠) في المنتصف.\n" +
            "العملة في النهاية: 100 دولار. 100 دولار - عملة في الأمام. العملة (100 دولار) في المنتصف.";

    @org.junit.Test
    public void test4() throws TextStatisticsException {
        TextStatistics statistics = new TextStatistics(new Locale("ar", "AE"), Locale.ENGLISH);
        setDates(statistics, text4);
        Assert.assertEquals(9, sentences.getSizeData());
        Assert.assertEquals(30, sentences.getMinLengthEl().length());
        Assert.assertEquals(49, worlds.getSizeData());
        Assert.assertEquals(11, worlds.getSizeDifferentEl());
        Assert.assertEquals(3, numbers.getSizeData());
        Assert.assertEquals(100L, numbers.getMaxElement());
        Assert.assertEquals(9, dates.getSizeData());
    }

    private static final String text5 = "Дата в конце: 6 июня 1992 г. Дата в конце: 06.06.1992. Дата в конце: Суббота, 6 июня 1992 г.\n" +
            "6 июня 1992 г. - дата в начале. 06.06.1992 - дата в начале. Суббота, 6 июня 1992 г. - дата в начале.\n" +
            "Дата (6 июня 1992 г.) в середине. Дата (06.06.1992) в середине. Дата (Суббота, 6 июня 1992 г.) в середине.\n" +
            "Денежная сумма в конце: 100 ₽. 100 ₽ - денежная сумма в начале. Денежная сумма (100 ₽) в середине.\n" +
            "Испорченная дата: 06.06.06.06.06.1992. Испорченная дата: Четверг, Пятнциа, Суббота, 6 июня 1992 г.\n" +
            "Испорченная дата: Пятница, 6 июня 1992 г. Испорченная дата: 6 июня -1992.";

    @org.junit.Test
    public void test5() throws TextStatisticsException {
        TextStatistics statistics = new TextStatistics(new Locale("ru", "RU"), Locale.ENGLISH);
        setDates(statistics, text5);
        Assert.assertEquals(13, sentences.getSizeData());
        Assert.assertEquals("Дата (06.06.1992) в середине.", sentences.getMinElement());
        Assert.assertEquals(71, worlds.getSizeData());
        Assert.assertEquals(16, worlds.getSizeDifferentEl());
        Assert.assertEquals("Испорченная", worlds.getMaxLengthEl());
        Assert.assertEquals(51.0, numbers.getAverageData());
        Assert.assertEquals("Sat Jun 06 00:00:00 MSD 1992", dates.getMinElement().toString());
        Assert.assertEquals("Fri Aug 06 02:00:00 MSD 1993", dates.getAverageData().toString());
    }

}
