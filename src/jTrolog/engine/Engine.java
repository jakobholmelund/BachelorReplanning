package jTrolog.engine;

import jTrolog.errors.PrologException;
import jTrolog.terms.*;
import jTrolog.terms.Number;
import jTrolog.parser.Parser;
import jTrolog.lib.BuiltIn;

import java.util.*;

/**
 * @author janerist@stud.ntnu.no
 * @author ivar.orstavik@hist.no
 */
class Engine {

    public static final int EVAL = 0;
    public static final int RULE = 1;
    public static final int BACK = 2;
    public static final int TRUE = 3;
    public static final int TRUE_ALL = 4;
    public static final int FALSE = 5;

    private Prolog prolog;
    BindingsTable bt;

    private int stackPos;
    private ChoicePoint[] stack;
    public static final int STARTUP_STACK_SIZE = 64;
    private int initState;
    private ChoicePoint query;

    Engine(Prolog manager, final Struct[] queryBody) throws Throwable {
        prolog = manager;
        bt = new BindingsTable();
        stack = new ChoicePoint[STARTUP_STACK_SIZE];
        for (int i = 0; i < stack.length; i++)
            stack[i] = new ChoicePoint();
        stackPos = -1;
        query = new ChoicePoint();
        query.setBody(queryBody, 0);
        initState = addToStack(query.getTODO(), 0, query);
    }

    BindingsTable runFirst() throws Throwable {
        return run(initState);
    }

    /**
     * Core of engine. Finite State Machine
     * @param state to start from
     * @return either a Number or a BindingsTable
     * @throws Throwable Exceptions that may occur running primitive predicates.
     */
    BindingsTable run(int state) throws Throwable {
        while (true) {
            switch (state) {
                case EVAL:
                    state = eval();
                    break;
                case RULE:
                    state = ruleSelect();
                    break;
                case BACK:
                    state = back();
                    break;
                case TRUE:
                    state = truue();
                    break;
                case TRUE_ALL:
                    return bt;
                default:
                    return null;
            }
        }
    }

    private int truue() throws Throwable {
        for (ChoicePoint cp = stack[stackPos]; cp != null; cp = cp.parent) {
            if (cp.hasTODO())
                //TRO complex - here, if the last rule to be added is the same rule as topOfStack which has no more TODOs or alternatives, then it should be possible to reuse topOfStack. this requires some heavy lifting adjusting the gc system
                return addToStack(cp.getTODO(), cp.bodyCtx, cp);
        }
        return TRUE_ALL;
    }

    private int back() {
        for (; stackPos >= 0; stackPos--) {
            bt.collectGarbage(stackPos);
            ChoicePoint topOfStack = stack[stackPos];
            if (topOfStack.hasAlternatives())
                return RULE;
            topOfStack.fail();
        }
        return FALSE;
    }

    private int eval() throws Throwable {
        ChoicePoint cp = stack[stackPos];
        return Prolog.evalPrimitive(cp.prim, bt.resolveArgs(cp.head, cp.headCtx)) ? TRUE : BACK;
    }

    private int ruleSelect() throws Throwable {
        ChoicePoint topOfStack = stack[stackPos];
        while (topOfStack.hasAlternatives()) {
            Clause next = topOfStack.nextAlternative();
            int newCtx = bt.getUniqueExecutionCtxID();
            if (bt.unifyBranch(topOfStack.head, next.head, topOfStack.headCtx, newCtx)) {
                topOfStack.setBody(next.tail, newCtx);
                //TRO very simple - here, if the last rule to be added is the same rule as topOfStack which has no more TODOs or alternatives, then it should be possible to reuse topOfStack.
                return addToStack(topOfStack.getTODO(), newCtx, topOfStack);
            }
            bt.collectGarbageSmall();
        }
        return BACK;
    }

    /**
     * removes comma and cut.
     * Comma => the struct is set as elements in a list, commas are split up as elements in this list and commas removed
     * Cut   => run separate method
     * Primitive => evaluated
     *      else
     * Rule => rules added, sent to RULE
     */
    private int addToStack(Struct head, int headCtx, ChoicePoint parent) throws PrologException {
        ++stackPos;
        if(stackPos == stack.length)
            doubleStackSize();
        bt.setCurrentExecCtx(stackPos);
        ChoicePoint topOfStack = stack[stackPos];
        topOfStack.set(head, headCtx, parent);

        if (head.predicateIndicator == Parser.callSignature) {
            int ctx = headCtx;
            Term callArg = bt.resolveFaster(head.getArg(0), ctx);
            if (bt.secondOutOfFasterResolve != Integer.MAX_VALUE)
                ctx = bt.secondOutOfFasterResolve;
            if (callArg instanceof Number)
                throw new PrologException("type_error(callable, " + callArg + ")");
            if (callArg instanceof Var)
                throw new PrologException("instantiation_error");
            topOfStack.setBody(BuiltIn.convertTermToClauseBody2(callArg), ctx);
            return addToStack(topOfStack.getTODO(), ctx, topOfStack);
        }
        if (head.predicateIndicator == Parser.cutSignature)
            return cut(parent.cutParent);

        topOfStack.prim = prolog.getPrimitive(head);
        if (topOfStack.prim != null)
            return EVAL;
        List rules = prolog.find(head.predicateIndicator);
        topOfStack.setRules(rules);
        return RULE;
    }

    /**
     * clear all ; and , backtracking alternatives up to and including the first point in the stack that is not , or ; or !.
     */
    private int cut(final ChoicePoint cutParent) {
        for (Iterator it = stackIterator(); it.hasNext(); ) {
            ChoicePoint next = (ChoicePoint) it.next();
            next.cutAlternatives();
            if (next == cutParent)
                return TRUE;
        }
        return TRUE;
    }

    boolean hasAlternatives() {
        for (Iterator it = stackIterator(); it.hasNext();) {
            if (((ChoicePoint) it.next()).hasAlternatives())
                return true;
        }
        return false;
    }

    private void doubleStackSize() {
        final int newSize = stack.length * 2;
        ChoicePoint[] newArray = new ChoicePoint[newSize];
        System.arraycopy(stack, 0, newArray, 0, stack.length);
        for (int i = stack.length; i < newSize; i++)
            newArray[i] = new ChoicePoint();
        stack = newArray;
        bt.expandLinkTable(newSize);
    }

    StackIterator stackIterator() {
        return new StackIterator(stackPos, stack);
    }

    private static class StackIterator implements Iterator {
        int pos;
        ChoicePoint[] stack;

        public StackIterator(int start, ChoicePoint[] stack) {
            this.pos = start;
            this.stack = stack;
        }

        public boolean hasNext() {
            return pos >= 0;
        }

        public Object next() {
            return stack[pos--];
        }

        public void remove() {
            throw new RuntimeException("can't remove while iterating the stack in the engine");
        }
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        for (StackIterator it = stackIterator(); it.hasNext();){
            s.append(it.next().toString() + " \n \n");
            s.append(gc_data(it.pos+1));
        }
        s.append(query);
        return s.toString();
    }

    private String gc_data(int pos) {
        String s = "";
        int[][] gc_data = bt.getKeysForCp(pos);
        for (int i = 0; i < gc_data.length; i++) {
            int[] varNr_Ctx = gc_data[i];
            int vNr = varNr_Ctx[0];
            int vCtx = varNr_Ctx[1];
            if (vNr == 0)
                continue;
            Var vFake = new Var("V", vNr);
            s += vFake + "_" + vNr + "<" + vCtx + "> : ";
            Term link = bt.resolveFaster(vFake, vCtx);
            if (bt.secondOutOfFasterResolve != Integer.MAX_VALUE)
                s += link + "<" + bt.secondOutOfFasterResolve + ">\n";
            else
                s += "what?!\n";
        }
        return s;
    }
}
