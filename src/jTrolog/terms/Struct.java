package jTrolog.terms;

import jTrolog.parser.Parser;
import jTrolog.terms.EvaluableTerm;
import jTrolog.engine.Prolog;

import java.util.*;

/**
 * Struct class represents both compound prolog term
 * and atom term (considered as 0-arity compound).
 */
public class Struct extends EvaluableTerm {

    public final String name;
    public final String predicateIndicator;  /** speedup hash map operation */
    public final int arity;
    public int operatorType = -1; //see Prolog.FX, Prolog.XFX etc.

    private Term[] args;
    private Var[] varList;

    public Var[] getVarList() {
        return varList;
    }

    Struct(String n, int arity, String predIndic) {
        type = Term.STRUCT;
        name = n;
        this.arity = arity;
        predicateIndicator = predIndic;
    }

    /**
     * Struct representing an operator
     */
    public Struct(String f,Term[] argList, int opType) {
        this(f, argList);
        operatorType = opType;
    }

    /**
     * Compound struct with an array of arguments
     */
    public Struct(String f,Term[] argList) {
        this((Parser.removeApices(f)).intern(), argList.length, (Parser.wrapAtom(f) + "/" + argList.length).intern());
        args = argList;
        varList = (Var[]) getChildrenVarList(args).toArray(new Var[0]);
//        prepArrays();
    }

    /**
     * Gets the i-th element of this structure
     *
     * No bound check is done
     */
    public Term getArg(int index) {
        return args[index];
    }

    public int getOperatorType() {
        return operatorType;
    }

    private static List getChildrenVarList(Term[] args) {
        List findings = new ArrayList();
        for (int i = 0; i < args.length; i++) {
            Term arg = args[i];
            if (arg instanceof Var)
                findings.add(arg);
            else if (arg instanceof Struct) {
                Var[] childsVars = ((Struct) arg).varList;
                if (childsVars != null && childsVars.length > 0)
                    findings.addAll(Arrays.asList(childsVars));
            }
        }
        return findings;
    }

    /**
     * Gets the string representation of this structure
     *
     * Specific representations are provided for lists and atoms.
     * Names starting with upper case letter are enclosed in apices.
     */
    public String toString() {
        return predicateIndicator == Parser.listSignature ? toStringList(false) : toStringImpl(false);
    }

    public String toStringList(boolean small) {
        StringBuffer res = new StringBuffer("[");
        boolean firstPass = true;
        int stopPrinting = 10;
        for (Iterator it = iterator(this); it.hasNext();) {
            if (small){
                if (stopPrinting-- == 0){
                    Term last = (Term) it.next();
                    for (;it.hasNext(); last = (Term) it.next()) ;
                    if (last.equals(emptyList))
                        return res.append(" ... ").append("]").toString();
                    return res.append(" ... ").append("|").append(last.toStringSmall()).append("]").toString();
                }
            }
            Term child = (Term) it.next();
            if (!it.hasNext()) { //last child
                if (child.equals(emptyList))
                    return res.append(']').toString();
                return res.append('|').append(small ? child.toStringSmall() : child.toString()).append(']').toString();
            }
            if (firstPass)
                firstPass = false;
            else
                res.append(", ");
            res.append(small ? child.toStringSmall() : child.toString());
        }
        throw new RuntimeException("bug");
    }

    public String toStringSmall(){
        return predicateIndicator == Parser.listSignature ? toStringList(true) : toStringImpl(true);
    }

    private String toStringImpl(boolean small) {

        if (predicateIndicator == Parser.commaSignature){
            String a = small ? getArg(0).toStringSmall() : getArg(0).toString();
            String b = small ? getArg(1).toStringSmall() : getArg(1).toString();
            return a + name + " " + b;
        }

        int opType = getOperatorType();
        if (opType == Prolog.XFX || opType == Prolog.YFX || opType == Prolog.XFY) {
            String a = small ? getArg(0).toStringSmall() : getArg(0).toString();
            String b = small ? getArg(1).toStringSmall() : getArg(1).toString();
            return a + " " + name + " " + b;
        }
        if (opType == Prolog.XF || opType == Prolog.YF ) {
            String a = small ? getArg(0).toStringSmall() : getArg(0).toString();
            return a + " " + name;
        }
        if (opType == Prolog.FX || opType == Prolog.FY) {
            String a = small ? getArg(0).toStringSmall() : getArg(0).toString();
            return name + " " + a;
        }

        if (name.equals("{}")) {
            if (arity == 0)
                return name;
            if (arity == 1)
                return "{" + (small ? getArg(0).toStringSmall() : getArg(0).toString()) + "}";
        }
        StringBuffer res = new StringBuffer(Parser.wrapAtom(name));
        res.append("(");
        for (int i = 0;i < arity;i++) {
            Term arg = getArg(i);
            if (arg instanceof Struct && ((Struct) arg).predicateIndicator == Parser.commaSignature)
                res.append("(").append(small ? arg.toStringSmall() : arg.toString()).append(")");
            else
                res.append(small ? arg.toStringSmall() : arg.toString());
            if(i < arity -1)
                res.append(", ");
        }
        res.append(")");
        return res.toString();
    }

    public boolean equals(Object t) {
        if (!(t instanceof Struct))
            return false;
        Struct other = (Struct) t;
        if (!predicateIndicator.equals(other.predicateIndicator))
            return false;
//        return true;
        Struct s1 = (Struct) t;
        if (arity != s1.arity)
            return false;
        for (int i = 0; i < s1.arity; i++) {
            if (!getArg(i).equals(s1.getArg(i)))
                return false;
        }
        return true;
    }

    public static Iterator iterator(Struct structList) {
        final Struct origin = (Struct) (structList instanceof Wrapper ? ((Wrapper) structList).getBasis() : structList);
        Iterator it = new ListIterator(origin);
        if (structList instanceof Wrapper)
            return new Wrapper.WrappedIterator(it, ((Wrapper) structList).getContext());
        return it;
    }

    private void prepArrays() {

        //give me my sizzzzze!
        int size = 1;           //count me in
        for (int i = 0; i < arity; i++) {
            Term arg = args[i];
            if (arg instanceof Struct && ((Struct) arg).arity > 0)
                size += ((Struct) arg).prePost.length;
            else
                size++;
        }
        prePost = new int[size];
        tree = new Term[size];

        int pos = 0;

        prePost[pos] = size-1;
        tree[pos] = this;
        upDateChild(pos++);

        int position = 0;
        for (int i = 0; i < arity; i++){
            Term child = getArg(i);
            if (child instanceof Struct && ((Struct) child).arity > 0) {
                Struct cs1 = (Struct) child;
                Term[] valuesArr = cs1.tree;
                int[] prePostArr = cs1.prePost;
                int childSize = valuesArr.length;

                for (int j = 0; j < childSize; j++) {
                    this.prePost[pos] = prePostArr[j] + position;
                    this.tree[pos] = valuesArr[j];
                    upDateChild(pos++);
                }
                position += childSize;
            } else {                                                        //atoms and numbers and vars
                prePost[pos] = position++;
                tree[pos] = child;
                upDateChild(pos++);
            }
        }
    }

    private void upDateChild(int i) {
        Term child = tree[i];
        child.tree = tree;
        child.prePost = prePost;
        child.pos = i;
    }
}