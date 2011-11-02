package jTrolog.terms;

/**
 * Double class represents the double prolog data type
 */
public class Double extends jTrolog.terms.Float {

    private double value;

    public Double(double v) {
        value = v;
    }

    public Double(String v) throws NumberFormatException {
        value = java.lang.Double.parseDouble(v);
    }

    /**
     *  Returns the value of the Double as int
     */
    final public int intValue() {
        return (int) value;
    }

    /**
     *  Returns the value of the Double as float
     */
    final public float floatValue() {
        return (float) value;
    }

    /**
     *  Returns the value of the Double as double
     */
    final public double doubleValue() {
        return value;
    }

    /**
     *  Returns the value of the Double as long
     */
    final public long longValue() {
        return (long) value;
    }

    public String toString() {
        return java.lang.Double.toString(value);
    }
}