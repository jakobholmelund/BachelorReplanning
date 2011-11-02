package jTrolog.engine;

import jTrolog.terms.Struct;
import jTrolog.terms.Term;
import jTrolog.terms.Clause;

import java.util.*;

/**
 * Map for storing clauses in the LibraryAndTheoryManager
 *
 * @author ivar.orstavik@hist.no
 */
class ClauseDatabase extends LinkedHashMap {

    public void addFirst(Object key, Object d) {
        LinkedList family = (LinkedList) get(key);
        if (family == null)
            put(key, family = new LinkedList());
        family.addFirst(d);
    }

    public void addLast(Object key, Object d) {
        LinkedList family = (LinkedList) get(key);
        if (family == null)
            put(key, family = new LinkedList());
        family.addLast(d);
    }

    public List getPredicates(Object key) {
    	LinkedList family = (LinkedList) get(key);
    	if (family == null)
            return new LinkedList();
        return family;
    }

    public List getPredicatesIterator(Object key) {
    	return (LinkedList) get(key);
    }

    public Object restore(Object s){
        return s;
    }

    public LinkedList abolish(Object key) {
        return (LinkedList) remove(key);
    }

    public Iterator iterator() {
        return new CompleteIterator(this);
    }

    public String toString() {
       StringBuffer buffer = new StringBuffer();
       for (Iterator dynamicClauses = iterator(); dynamicClauses.hasNext();) {
           Struct d = ((Clause) dynamicClauses.next()).original;
           buffer.append(d.getArg(0).toString());
           if (d.getArg(1).equals(Term.TRUE))
               buffer.append(".\n");
           else
               buffer.append(":-\n\t").append(d.getArg(1).toString()).append(".\n");
       }
       return buffer.toString();
    }

    private static class CompleteIterator implements Iterator {
        Iterator values;
        Iterator workingList;
        ClauseDatabase cdb;

        public CompleteIterator(ClauseDatabase clauseDatabase) {
            cdb = clauseDatabase;
            values = clauseDatabase.values().iterator();
        }

        public boolean hasNext() {
            if (workingList != null && workingList.hasNext())
                return true;
            if (values.hasNext()) {
                workingList = ((List) values.next()).iterator();
                return hasNext(); //start again on next workingList
            }
            return false;
        }

        public Object next() {
            Clause modified = (Clause) workingList.next();
            return cdb.restore(modified);
        }

        public void remove() {
            workingList.remove();
        }
    }
}