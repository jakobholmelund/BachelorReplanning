package jTrolog.engine;

import jTrolog.lib.BasicLibrary;
import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;
import jTrolog.terms.*;
import jTrolog.terms.Number;
import jTrolog.terms.Double;

import java.util.*;

/**
 * Keeps track of what variables are bound to what Terms and at which ChoicePoint this connection was made.
 *
 * now, the BindingsTable never receives anything that is not Wrapped. All Structs sent in are wrapped as a mfck.
 * the unifyBranch is now possible to run without generating new objects when the getArg is called.
 *
 * all the methods that add vars and get vars from the bidnings table is now given both Var and context (and link term and its context)
 * a problem is that the stackMap needs to be subsumed into the bindings table since it needs to retract the links done by a current owner.
 * I don't think I can remove the current owner as a separate map since it provides a rollback function. What I can do is stringToStructList WrapVars for it only and then
 * move these into and out of the bindingsMap
 *
 * @author janerist@stud.ntnu.no
 * @author ivar.orstavik@hist.no
 */
public class BindingsTable {

    private int currentExecCtx = 0;

    private LinkTable links = new LinkTable();

    int uniqueExecutionCtxID = 1;
    int[] firstInCtx = new int[10000];
    private GarbageCan gc = new GarbageCan(links);

    public int getUniqueExecutionCtxID() {
        if(links.getWidth() == uniqueExecutionCtxID)
            links.doubleWidth(uniqueExecutionCtxID * 2);
        return uniqueExecutionCtxID++;
    }

    public void expandLinkTable(int newSize) {
        gc.doubleTrashCanWidth(newSize);
    }

    final void collectGarbage(final int execCtx) {
        uniqueExecutionCtxID = firstInCtx[execCtx];
        currentExecCtx = execCtx;
        gc.collectGarbageLinks(execCtx);
    }

    final void collectGarbageSmall() {
        uniqueExecutionCtxID--;
        gc.collectGarbageLinks(currentExecCtx);
    }

    final void setCurrentExecCtx(final int execCtx) {
        currentExecCtx = execCtx;
        if (execCtx >= firstInCtx.length)
            firstInCtx = LinkTable.expandArray(firstInCtx, execCtx*2);
        firstInCtx[execCtx] = uniqueExecutionCtxID;
    }

    //todo 1. perhaps it would be smart to keep track of the left and right terms/ctxs that the unification process works on.
    //todo    if this is done, then whenever a unification fails, one could display exactly what two terms did not match.
    //todo    but this is perhaps better done in the Engine, since one here might like to view the entire terms..
    public boolean unify(Term t0, Term t1) {
        int oneCtx = t0 instanceof Wrapper ? ((Wrapper) t0).getContext() : 0;
        int twoCtx = t1 instanceof Wrapper ? ((Wrapper) t1).getContext() : 0;
        return unifyBranch(unWrap(t0), unWrap(t1), oneCtx, twoCtx);
    }

    //ca. 500

    /**
     * @param one
     * @param two
     * @param oneCtx
     * @param twoCtx
     * @return true if the two terms are unified, false otherwise
     */
    boolean unifyBranch(Term one, Term two, int oneCtx, int twoCtx) {
        while (one.type == Term.VAR) {
            Var vOne = (Var) one;
            //if the two varaiables are equal, then return true
            if (oneCtx == twoCtx && vOne.equals(two))           //if (oneCtx == twoCtx && two instanceof Var && vOne.nrInStruct == ((Var) two).nrInStruct )
                return true;
            //else, if the left variable is not bound, bind it
            final int vNr = vOne.nrInStruct;
            Term link = links.getTerm(vNr, oneCtx);
            if (link == null)
                return setLink(vNr, oneCtx, two, twoCtx);
            //else, Var one is bound. If it is bound to a Var, then the loop will continue updating the on Var, else it will continue down the method
            oneCtx = links.getCtx(vNr, oneCtx);
            one = link;
        }
        while (two.type == Term.VAR){
            final int vNr = ((Var) two).nrInStruct;
            Term link = links.getTerm(vNr, twoCtx);
            if(link == null)
                return setLink(vNr, twoCtx, one, oneCtx);           //var two is unbound, bind it against one
            twoCtx = links.getCtx(vNr, twoCtx);
            two = link;
        }
        //from now on, neither branch is a Var
        if (one.type >= Term.STRUCT && two.type >= Term.STRUCT) {
            Struct a = (Struct) one;
            Struct b = (Struct) two;
            if (a.predicateIndicator != b.predicateIndicator)
                return false;
            for (int c = 0; c < a.arity; c++)
                if (!unifyBranch(a.getArg(c), b.getArg(c), oneCtx, twoCtx))
                    return false;
            return true;
        }
        return one.type == Term.NUMBER && two.type == Term.NUMBER && BasicLibrary.number_equality_2((Number) one, (Number) two);
    }

    //ca. 500

    //this method calls an almost entirely iterative method
    boolean unifyBranchTwo(Term one, Term two, int oneCtx, int twoCtx) {
        return unifyTrees(one.pos, two.pos, one.prePost, one.tree, two.tree, two.prePost, oneCtx, twoCtx);
    }

    private boolean unifyTrees(int i, int j, int[] prePostA, Term[] treeA, Term[] treeB, int[] prePostB, int oneCtx, int twoCtx) {
        bb: for (int stopValue = prePostA[i]; i < prePostA.length && prePostA[i] <= stopValue; i++, j++) {
            Term one = treeA[i], two = treeB[j];
            int tmpCtx1 = oneCtx;
            int tmpCtx2 = twoCtx;
            boolean mustMakeNewStackLevel = false;

            if (one.type == Term.VAR){// instanceof Var){
                while (one.type == Term.VAR){// instanceof Var) {
                    Var vOne = (Var) one;
                    //if the two varaiables are equal, then return true
                    if (tmpCtx1 == twoCtx && vOne.equals(two))
                        continue bb;
                    //else, if the left variable is not bound, bind it
                    Term link = links.getTerm(vOne.nrInStruct, tmpCtx1);
                    if (link == null){
                        if (!setLink(vOne.nrInStruct, tmpCtx1 , two, twoCtx))
                            return false;
                        if (two.type == Term.STRUCT)// instanceof Struct && ((Struct) two).arity >0) //we must increment j to skip two's children
                            for (int postStop = prePostB[j]; j < prePostB.length-1 && prePostB[j+1] < postStop; j++);
                        continue bb;
                    }
                    //else, Var one is bound. If it is bound to a Var, then the loop will continue updating the on Var, else it will continue down the method
                    tmpCtx1 = links.getCtx(vOne.nrInStruct, tmpCtx1);
                    one = link;
                }
                if (one.type == Term.STRUCT)// instanceof Struct && ((Struct) one).arity > 0) //todo we don't need to mustMakeNewStackLevel when the node har no children.
                    mustMakeNewStackLevel = true;
            }

            if (two.type == Term.VAR){// instanceof Var){
                while (two.type == Term.VAR){// instanceof Var){
                    Var vTwo = (Var) two;
                    Term link = links.getTerm(vTwo.nrInStruct, tmpCtx2);
                    if(link == null){
                        if(!setLink(vTwo.nrInStruct, tmpCtx2, one, tmpCtx1))
                            return false;           //var two is unbound, bind it against on
                        if (one.type == Term.STRUCT)// instanceof Struct && ((Struct) one).arity >0) //we must increment i to skip one's children
                            for (int postStop = prePostA[i]; i < prePostA.length-1 && prePostA[i+1] < postStop; i++);
                        continue bb;
                    }
                    tmpCtx2 = links.getCtx(vTwo.nrInStruct, tmpCtx2);
                    two = link;
                }
                if (two.type == Term.STRUCT)// instanceof Struct && ((Struct) two).arity > 0)
                    mustMakeNewStackLevel = true;
            }

            //todo we need to mustMakeNewStackLevel here because the ctx id changes,
            //todo the prePost and values arrays changes.
            //todo however these values only become relevant for the next check, so Atoms and Numbers do NOT mustMakeNewStackLevel
            if (mustMakeNewStackLevel){
                if (!unifyBranch(one, two, tmpCtx1, tmpCtx2))
                    return false;
                //we must increment i to skip one's and/or two's children
                if (one.type == Term.STRUCT)// instanceof Struct && ((Struct) one).arity >0)
                    for (int postStop = prePostA[i]; i < prePostA.length-1 && prePostA[i+1] < postStop; i++);
                if (two.type == Term.STRUCT)// instanceof Struct && ((Struct) two).arity >0)
                    for (int postStop = prePostB[j]; j < prePostB.length-1 && prePostB[j+1] < postStop; j++);
            }
            else if (one.type >= Term.STRUCT && two.type >= Term.STRUCT){//instanceof Struct && two instanceof Struct) {
                if (((Struct)one).predicateIndicator != ((Struct)two).predicateIndicator)
                    return false;
            } else if (one.type == Term.NUMBER && two.type == Term.NUMBER){// instanceof Number && two instanceof Number){
                if (! BasicLibrary.number_equality_2((Number) one, (Number) two))
                    return false;
            } else
                return false;
        }
        return true;
    }


    private static int leftMostChildsPost(int i, int[] aPrePostArr) {
        int temp;
        int min = aPrePostArr[i];
        while (i++ < aPrePostArr.length && min > (temp = aPrePostArr[i]))
            min = temp;
        return min;
    }

    public Term wrapWithID(Term t) {
        return wrapWithID(t, getUniqueExecutionCtxID());
    }

    public static Term wrapWithID(Term t, int renameVarID){
        if (t instanceof Wrapper)
            return t;
        if (t instanceof Var)
            return new WrapVar((Var) t, renameVarID);
        if (t instanceof Struct && !(t instanceof StructAtom))
            return new WrapStruct((Struct) t, renameVarID);
        return t;
    }

    public static Term unWrap(Term l) {
        return l instanceof Wrapper ? ((Wrapper) l).getBasis() : l;
    }

    /**
     * Adds a link.
     * The key of the link is an ID calculated from the Variable (its position in the root parent Struct) and its context
     * The link is stored in three different maps:
     * 1. a map of the link to the Term it is linked to
     * 2. a map of the link to the context number of the Term it is linked to
     * 3. to facilitate garbage collection of unused links,
     * a map linking the context in which the binding takes place to a list of links that are bound at this execution context is also updated
     *
     * @param vNr
     * @param vCtx
     * @param link
     * @param linkCtx
     * @return true always
     */
    private boolean setLink(final int vNr, final int vCtx, final Term link, final int linkCtx) {
        //Most applications can probably turn off checkForInternalOccurencesOfVarInTerm without any consequenses by commenting out the if clause below.
        //Removing this will likely result in approx 20% speed-up. But do so only when you know your application do not need this check.
        /*
         if (checkForInternalOccurencesOfVarInTerm(vNr, vCtx, link, linkCtx))
            return false;
         */
        links.put(vNr, vCtx, link, linkCtx);
        gc.addToTrashCan(vNr, vCtx, currentExecCtx);
        return true;
    }

    /**
     * todo don't search, sort?
     * this now only gets in the original terms and the context int as values
     * @param varBeingChecked
     * @param beingCheckedsContext
     * @param t
     * @param tCtx
     */
    private boolean checkForInternalOccurencesOfVarInTerm(int varBeingChecked, int beingCheckedsContext, Term t, int tCtx) {
        while (t.type == Term.VAR) {
            final int vNr = ((Var) t).nrInStruct;
            if (beingCheckedsContext == tCtx && varBeingChecked == vNr)
                return true;
            Term link = links.getTerm(vNr, tCtx);
            if (link == null || link.type == Term.NUMBER || link.type == Term.ATOM)
                return false;
            t = link;
            tCtx = links.getCtx(vNr, tCtx);
            //if the Var was linked to a Var, the loop will run once more
            //else t is a Struct and 'if (t instanceof Struct)' below will kick into action
        }
        if (t.type == Term.STRUCT) {
            Var[] varList = ((Struct) t).getVarList();
            if (varList == null)
                return false;
            boolean sameCtx = beingCheckedsContext == tCtx;
            for (int i = 0; i < varList.length; i++) {
                Var v = varList[i];
                if (sameCtx && varBeingChecked == v.nrInStruct)
                    return true;
                Term link = links.getTerm(v.nrInStruct, tCtx);
                if (link == null || link.type == Term.NUMBER || link.type == Term.ATOM)
                    continue;
                int linkCtx = links.getCtx(v.nrInStruct, tCtx);
                return checkForInternalOccurencesOfVarInTerm(varBeingChecked, beingCheckedsContext, link, linkCtx);
            }
        }
        return false;
    }

    /**
     *
     * @param owner
     * @return
//     * @deprecated no longer supported. should be implemented anew if needed in f.ex. GUI or debugger.
     */
    int[][] getKeysForCp(int owner) {
        return gc.getGarbageLinks(owner);
    }

    /**
     * @param prolog
     * @param expression
     * @return result of the evaluation as a Number
     * @throws jTrolog.errors.PrologException instantiation error if one of the Variables to be evaluated is not instantiated
     *                         type_error(Evaluable, other) if a part in the evaluable Term is not evaluable
     * @throws Throwable any other exception that might be thrown by parts of the expression to be evaluated
     */
    public Number evalExpression(Prolog prolog, EvaluableTerm expression) throws PrologException, Throwable {
        if (expression instanceof Number)
            return (Number) expression;

        Term child = null;
        try {
            Struct struct = (Struct) expression;
            //chp 9 defines separate behavior for _evaluation_ of float/1 (casting of Int) that should only accept Numbers,
            //as opposed to the execution of float/1 that accepts Term.
            if (struct.predicateIndicator == Parser.floatSignature){
                child = resolve(struct.getArg(0));
                Number result = evalExpression(prolog, (EvaluableTerm) child);
                return new Double(result.doubleValue());
            }

            PrimitiveInfo prim = prolog.getPrimitiveExp(struct);
            if (prim == null)
                throw new PrologException("type_error(Evaluable, " + struct.predicateIndicator +")");

            //we have an expression that might be evaluable
            Object[] primitive_args = new Object[struct.arity +1];
            primitive_args[0] = this;
            for (int i = 0; i < primitive_args.length-1; i++){
                child = resolve(struct.getArg(i));
                primitive_args[i+1] = evalExpression(prolog, (EvaluableTerm) child);
            }
            return (Number) prim.method.invoke(prim.source, primitive_args);
        } catch (ClassCastException e) {
            if (child instanceof Var)
                throw new PrologException("instantiation_error");
            throw new PrologException("type_error(Evaluable, " + child +")");
        }
    }

    Object[] resolveArgs(Struct unresolved, int ctx) {
        Object[] primitive_args = new Object[unresolved.arity +1];
        primitive_args[0] = this;
        for (int i = 0; i < unresolved.arity; i++) {
            final Term arg = unresolved.getArg(i);
            Term link = resolveFaster(arg, ctx);
            if (secondOutOfFasterResolve == Integer.MAX_VALUE)
                primitive_args[i+1] = wrapWithID(arg, ctx);
            else
                primitive_args[i+1] = wrapWithID(link, secondOutOfFasterResolve );
        }
        return primitive_args;
    }

    public Term resolve(Term term) {
        if (term instanceof WrapVar) {
            final WrapVar wrapVar = ((WrapVar) term);
            Term link = resolveFaster(wrapVar.getBasis(), wrapVar.getContext());
            return secondOutOfFasterResolve == Integer.MAX_VALUE ? term : wrapWithID(link, secondOutOfFasterResolve);
        }
        return term;
    }

    int secondOutOfFasterResolve;
    public Term resolveFaster(Term term, int ctx) {
        secondOutOfFasterResolve = Integer.MAX_VALUE;
        while (term instanceof Var){
            int vNr = ((Var) term).nrInStruct;
            Term link = links.getTerm(vNr, ctx);
            if (link == null)
                break;
            term = link;
            ctx = secondOutOfFasterResolve = links.getCtx(vNr, ctx);
        }
        return term;
    }

    public Iterator structListIterator(Struct origin, boolean skipLastEmptyList) throws PrologException {
        if (origin.equals(Term.emptyList))
            return Term.iterator;
        if (origin.predicateIndicator != Parser.listSignature)
            throw new PrologException("type_error(list, " + origin + ")");
        return new StructListIterator(origin, skipLastEmptyList);
    }

    public Struct createStructList(Collection complete) {
        if (complete.isEmpty())
            return Term.emptyList;
        Struct res = (WrapStruct) wrapWithID(Parser.createListContainingAnyVars(complete.size() - 1));
        Iterator toBeLinked = complete.iterator();
        try {
            for (Iterator it = structListIterator(res, true); it.hasNext();) {
                Var child =  (Var) it.next();
                Term link = (Term) toBeLinked.next();
                unify(child, link);
            }
        } catch (PrologException e) {
            throw new RuntimeException("error in iterating a Struct list");
        }
        return res;
    }


    private class StructListIterator implements Iterator {

        Iterator currentListIterator;
        Term cache;
        boolean includeLastEmptyList;

        StructListIterator(Struct origin, boolean skipLastEmptyList) {
            this.includeLastEmptyList = !skipLastEmptyList;
            currentListIterator = Struct.iterator(origin);
        }

        public boolean hasNext() {
            if (cache != null)
                return true;
            if (!currentListIterator.hasNext())
                return false;
            cache = (Term) currentListIterator.next();
            cache = resolve(cache);

            if (currentListIterator.hasNext())
                return true;

            if (cache.equals(Term.emptyList)){
                if (includeLastEmptyList)
                    return true;
                cache = null;
                return false;
            }

            if (!(cache instanceof Struct && ((Struct) cache).predicateIndicator == Parser.listSignature))
                return true;

            currentListIterator = Struct.iterator((Struct) cache);
            cache = null;
            return hasNext();
        }

        public Object next() {
            if (cache == null)
                hasNext();
            if (cache == null)
                throw new NoSuchElementException();
            Term temp = cache;
            cache = null;
            return temp;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    //todo override toString();

    /**
     * See 7.1.6.1 Variants of a term in the ISO spec
     * this method resolves all the terms children. Not sure if this is in line with the spec.
     *
     * @param original such as
     *          - f(X,Y,X)
     *          - g(A,B)
     *          - P + Q
     * @return a variant of the original such as
     *          - f(_1, _2, _1)
     *          - g(_1, _2)
     *          - _1 + _2
     */
    public Term variant(Term original) {
        return new Copier(original).getFlatVariant();
    }

    public Term flatCopy(Term original, int keepCtx) {
        return new Copier(BindingsTable.wrapWithID(original, keepCtx), keepCtx).getFlatCopy();
    }

    private class Copier {
        HashMap varTable = new HashMap();
        Term original;
        Term copy;
        private int keepOriginals = -1;

        public Copier(Term root) {
            original = root;
        }

        public Copier(Term root, int ctxToKeep) {
            original = root;
            keepOriginals = ctxToKeep;
        }

        private Term copyRecursive(Term original) {
            if (original instanceof StructAtom || original instanceof Number)
                return original;
            if (original instanceof Var){
                Term link = resolve(original);
                if (link != original)
                    return copyRecursive(link);
                if (link instanceof Wrapper && ((Wrapper) link).getContext() == keepOriginals)
                    return ((Wrapper) link).getBasis();
                Var copyVar = (Var) varTable.get(link);
                if (copyVar == null)
                    varTable.put(link, copyVar = new Var("_", varTable.size()+1));
                return copyVar;
            }
            Struct sOrig = (Struct) original;
            Term[] clonedElements = new Term[sOrig.arity];
            for (int i = 0; i < clonedElements.length; i++)
                clonedElements[i] = copyRecursive(sOrig.getArg(i));
            return new Struct(sOrig.name, clonedElements, sOrig.getOperatorType());
        }

        public Term getFlatVariant() {
            if (copy != null)
                return copy;
            return copy = copyRecursive(this.original);
        }

        public Term getFlatCopy() {
            if (copy != null)
                return copy;
            return copy = copyRecursive(this.original);
        }

    }
}