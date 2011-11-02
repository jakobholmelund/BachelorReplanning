package jTrolog.terms;

import jTrolog.terms.Struct;
import jTrolog.terms.Term;
import jTrolog.terms.Wrapper;
import jTrolog.engine.BindingsTable;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author ivar.orstavik@hist.no
 */
public class WrapStruct extends Struct implements Wrapper {

    public final int context;
    private Struct basis;

    public WrapStruct(Struct struct, int renameVarID) {
        super(struct.name, struct.arity, struct.predicateIndicator);
        basis = struct;
        context = renameVarID;
    }

    public int getOperatorType() {
        return basis.getOperatorType();
    }

    public Term getArg(int i) {
        return BindingsTable.wrapWithID(basis.getArg(i), context);
    }

    public Var[] getVarList() {
        return basis.getVarList();
    }

    public int getContext() {
        return context;
    }

    public Term getBasis() {
        return basis;
    }

//    public boolean equals(Object t) {
//        return (t instanceof WrapStruct && ((WrapStruct) t).getContext() == context && ((WrapStruct) t).getBasis().equals(basis));
//    }

    public String toString() {
        return basis.toString();
    }
}
