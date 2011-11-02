package jTrolog.terms;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Term class - root prolog data type
 *
 * @see jTrolog.terms.Struct
 * @see Var
 * @see jTrolog.terms.Number
 * @author ivar.orstavik@hist.no
 */
public abstract class Term implements java.io.Serializable {

    public int type;

    public final static int VAR = 1;
    public final static int NUMBER = 2;
    public final static int STRUCT = 3;
    public final static int ATOM = 4;

    // important atoms to be used only once
    public static final Term TRUE = new StructAtom("true".intern());
    public static final Term FALSE = new StructAtom("false".intern());
    public static final StructAtom emptyList = new StructAtom("[]");
    public static final Iterator iterator = new LinkedList().iterator();

    //Tree data Jens Teubner prepost style:
    //http://www-db.in.tum.de/~grust/teaching/ws0506/XML-DB/db-supp-xml-03.pdf   page 17
    public int pos = 0;

    public Term[] tree;
    public int[] prePost;

    protected Term() {
        tree = new Term[]{this};
        prePost = new int[]{0};
    }

    public abstract String toStringSmall();
}