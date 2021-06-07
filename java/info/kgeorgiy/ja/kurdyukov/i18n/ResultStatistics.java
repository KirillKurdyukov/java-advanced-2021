package info.kgeorgiy.ja.kurdyukov.i18n;

public class ResultStatistics<T> {
    public int getSizeData() {
        return sizeData;
    }

    public void setSizeData(int sizeData) {
        this.sizeData = sizeData;
    }

    public int getSizeDifferentEl() {
        return sizeDifferentEl;
    }

    public void setSizeDifferentEl(int sizeDifferentEl) {
        this.sizeDifferentEl = sizeDifferentEl;
    }

    public T getMaxLengthEl() {
        return maxLengthEl;
    }

    public void setMaxLengthEl(T maxLengthEl) {
        this.maxLengthEl = maxLengthEl;
    }

    public T getMinLengthEl() {
        return minLengthEl;
    }

    public void setMinLengthEl(T minLengthEl) {
        this.minLengthEl = minLengthEl;
    }

    private int sizeData;
    private int sizeDifferentEl;

    public double getMiddleSize() {
        return middleSize;
    }

    public void setMiddleSize(double middleSize) {
        this.middleSize = middleSize;
    }

    private double middleSize;
    private T minElement;
    private T maxElement;
    private T averageData;
    private T maxLengthEl;
    private T minLengthEl;

    public T getMinElement() {
        return minElement;
    }

    public void setMinElement(T minElement) {
        this.minElement = minElement;
    }

    public void setMaxElement(T maxElement) {
        this.maxElement = maxElement;
    }

    public void setAverageData(T averageData) {
        this.averageData = averageData;
    }

    public T getMaxElement() {
        return maxElement;
    }

    public T getAverageData() {
        return averageData;
    }

}