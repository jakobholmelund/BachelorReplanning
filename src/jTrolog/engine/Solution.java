package jTrolog.engine;

import jTrolog.terms.Term;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author ivar.orstavik@hist.no
 */
public class Solution {

    private static final HashMap emptyMap = new HashMap();

    private HashMap bindings;
    private Term solution;

    public Solution(Term solution) {
        this.bindings = emptyMap;
        this.solution = solution;
    }

    public Solution(HashMap bindings, Term solution) {
        this.bindings = bindings;
        this.solution = solution;
    }

    /**
     * @return true if the query was a success, false otherwise
     */
    public boolean success(){
        return solution != null;
    }

    /**
     * @return the solution to the query as a Term
     */
    public Term getSolution() {
        return solution;
    }

    /**
     * @return the link of the Variable corresponding to varName,
     *         if no Var was named varName in the query, null is returned
     *         if the Var was linked to an any Var, that any Var is returned
     */
    public Term getBinding(String varName) {
        return (Term) bindings.get(varName);
    }
	
    public Map getBindings() {
    	return bindings;
    }

    public String toString() {
        return solution == null ? "no" : solution.toString();
    }

    public String bindingsToString() {
        StringBuffer buffy = new StringBuffer();
        for (Iterator it = bindings.keySet().iterator(); it.hasNext();) {
            String variable = (String) it.next();
            Term binding = (Term) bindings.get(variable);
            buffy.append(variable).append(": ").append(binding).append("\n");
        }
        return buffy.toString();
    }
}
