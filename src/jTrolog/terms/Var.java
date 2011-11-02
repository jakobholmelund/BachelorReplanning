package jTrolog.terms;

import jTrolog.errors.InvalidTermException;

/**
 * This class represents a variable term. Variables are identified by a name
 * (which must starts with an upper case letter) or the anonymous ('_') name.
 *
 * @see Term
 */
public class Var extends Term {

    public final static String ANY = "_".intern();

    private String name;
    public final int nrInStruct;

    /**
     * needed to implement Wrapper Objects. Do not use to stringToStructList ANY Vars
     */
    Var() {
        type = Term.VAR;
        nrInStruct = 0;
    }

    /**
     * Creates a variable identified by a name.
     *
     * @param n varaible name
     *          if n is "_" the variable is anonymous.
     * @param nr its variable number of its root parent Struct.
     */
    public Var(String n, int nr) throws InvalidTermException {
        type = Term.VAR;
        name = n.intern();
        nrInStruct = nr;
    }

    public boolean equals(Object t) {
        return t instanceof Var && nrInStruct == ((Var) t).nrInStruct;
    }

    public int hashCode() {
        return nrInStruct;
    }

    /**
     * Tests if this variable is ANY
     */
    public boolean isAnonymous() {
        return name == ANY;
    }

    /**
     * Gets the string representation of this variable.
     */
    public String toString() {
        return name;
    }

    public String toStringSmall() {
        return name;
    }

}