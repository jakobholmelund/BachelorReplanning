package jTrolog.terms;

import jTrolog.terms.Number;
import jTrolog.terms.Long;

/**
 * Int class represents the integer prolog data type
 */
public class Int extends jTrolog.terms.Number {

    private int value;

    public Int() {

    }

    public Int(String v) throws NumberFormatException {
        value = java.lang.Integer.parseInt(v);
    }

    public Int(int v) {
        value = v;
    }


    /**
     *  Returns the value of the Integer as int
     */
    public int intValue() {
        return value;
    }

    /**
     *  Returns the value of the Integer as float
     */
    public float floatValue() {
        return (float) value;
    }

    /**
     *  Returns the value of the Integer as double
     */
    public double doubleValue() {
        return (double) value;
    }

    /**
     *  Returns the value of the Integer as long
     */
    public long longValue() {
        return value;
    }

    public String toString() {
        return java.lang.Integer.toString(value);
    }

    public static Number create(String s) throws NumberFormatException {
        try {
            return new Int(s);
        } catch (NumberFormatException e) {
            return new Long(s);
        }
    }

    public boolean equals(Object n) {
        if (n instanceof Int)
            return longValue() == ((Int) n).longValue();
        return false;
    }
}