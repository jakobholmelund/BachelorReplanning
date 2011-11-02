package jTrolog.terms;

import jTrolog.terms.Term;
import jTrolog.terms.Var;
import jTrolog.terms.Wrapper;

/**
 * @author ivar.orstavik@hist.no
 */
public class WrapVar extends Var implements Wrapper {
    Var basis;
    int context;
    String[] nameNumbers;

    public WrapVar(Var var, int renameVarID) {
        if (var instanceof WrapVar)
            throw new RuntimeException("building a WrapVar from another WrapVar");
        basis = var;
        context = renameVarID;
    }

    public boolean equals(Object t) {
        return t instanceof WrapVar && context == ((WrapVar) t).context && basis.equals(((WrapVar) t).basis);
    }

    public int hashCode() {
        return basis.hashCode() + context*100;
    }

    public boolean isAnonymous() {
        return basis.isAnonymous();
    }

    public String toString() {
        return basis.toString();
    }

    public String toStringSmall() {
        return basis.toStringSmall();
    }

    public int getContext() {
        return context;
    }

    public Term getBasis() {
        return basis;
    }
}
