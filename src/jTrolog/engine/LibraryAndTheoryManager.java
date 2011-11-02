package jTrolog.engine;

import jTrolog.errors.PrologException;
import jTrolog.errors.InvalidLibraryException;
import jTrolog.parser.Parser;
import jTrolog.lib.Library;
import jTrolog.lib.BuiltIn;
import jTrolog.terms.Struct;
import jTrolog.terms.Term;
import jTrolog.terms.Clause;

import java.io.Serializable;
import java.util.*;
import java.lang.reflect.InvocationTargetException;

/**
 * This class defines the Library and Theory Manager who manages the libraries and clauses/theory often referred to as the Prolog database.
 * The theory (as a set of clauses) are stored in the ClauseDatabase which in essence is a HashMap grouped by functor/arity.
 * <p/>
 * The LibraryAndTheoryManager functions logically, as prescribed by ISO Standard 7.5.4
 * section. The effects of assertions and retractions shall not be undone if the
 * program subsequently backtracks over the assert or retract call, as prescribed
 * by ISO Standard 7.7.9 section.
 * <p/>
 * To use the LibraryAndTheoryManager one should primarily use the methods assertA, assertZ, consult, retract, abolish and find.
 * <p/>
 * <p/>
 * rewritten by:
 *
 * @author ivar.orstavik@hist.no
 */
class LibraryAndTheoryManager implements Serializable {

    private ClauseDatabase dynamicDBase;
    private ClauseDatabase allLibraryRules;

    private LinkedHashMap librariesToRules;

    private Prolog engine;
    String lastConsultedTheory;

    LibraryAndTheoryManager(Prolog vm) {
        dynamicDBase = new ClauseDatabase();
        librariesToRules = new LinkedHashMap();
        allLibraryRules = new ClauseDatabase();
        lastConsultedTheory = "";
        engine = vm;
    }


    public LibraryAndTheoryManager clone() {
        LibraryAndTheoryManager  returnManager = new LibraryAndTheoryManager(null);
        returnManager.dynamicDBase = (ClauseDatabase) this.dynamicDBase.clone();
        //returnManager.librariesToRules = (LinkedHashMap) this.librariesToRules.clone();
        //returnManager.allLibraryRules = (ClauseDatabase) this.allLibraryRules.clone();
        returnManager.lastConsultedTheory = this.lastConsultedTheory + "";
        return returnManager;
    }

    public void setEngine(Prolog engine) {
        this.engine = engine;
    }

    /**
     * inserting of a clause at the head of the dbase
     */
    void assertA(Clause clause, String index) throws PrologException {
        dynamicDBase.addFirst(index, clause);
    }

    /**
     * inserting of a clause at the end of the dbase
     */
    void assertZ(Clause clause, String index) throws PrologException {
        dynamicDBase.addLast(index, clause);
    }

    /**
     * removing from dbase the first clause with head unifying with clause
     * (m if a free substitution index and t is the first free timestamp)
     */
    Struct retract(Struct clause, String index) throws PrologException {
        LinkedList family = (LinkedList) dynamicDBase.get(index);
        if (family == null)
            return null;
        for (Iterator it = family.iterator(); it.hasNext();) {
            Clause d = (Clause) it.next();
            if (Prolog.match(clause, d.original)) {
                it.remove();
                return d.original;
            }
        }
        return null;
    }

    /**
     * removes all clauses matching the given signature from the dynamic dbase
     */
    List abolish(String index) throws PrologException {
        return dynamicDBase.abolish(index);
    }

    /**
     * registers a predicate indicator as known
     */
    public void setDynamic(String index) {
        LinkedList family = (LinkedList) dynamicDBase.get(index);
        if (family != null)
            return;
        dynamicDBase.addFirst(index, Term.emptyList);
        family = (LinkedList) dynamicDBase.get(index);
        for (Iterator it = family.iterator(); it.hasNext();){
            it.next();
            it.remove();
        }
    }

    /**
     * Returns a family of clauses with functor and arity equals
     * to the functor and arity of the term passed as a parameter
     */
    List find(String predIndicator) throws PrologException {
        List dynamics = dynamicDBase.getPredicatesIterator(predIndicator);
        return dynamics != null ? dynamics : allLibraryRules.getPredicatesIterator(predIndicator);
    }

    /**
     * Consults a theory.
     *
     * @param theory theory to add
     * @throws PrologException
     */
    void consult(String theory) throws PrologException  {
        lastConsultedTheory = theory;
        // iterate all clauses in theory and assert them
        for (Iterator it = new Parser(theory, engine).iterator(); it.hasNext();) {
            Struct d = (Struct) it.next();
            if (runDirective(d))
                continue;
//            d = BuiltIn.convertTermToClause(d);
            Clause cl = BuiltIn.convertTermToClause(d);
            assertZ(cl, cl.head.predicateIndicator);
        }
    }

    /**
     * Clears the clause dbase.
     */
    void clear() {
        dynamicDBase.clear();
    }

    private boolean runDirective(Struct c) {
        if (c.predicateIndicator != Parser.singleClauseSignature)
            return false;
        Term dir = c.getArg(0);
        if (!(dir instanceof Struct))
            return false;
        try {
            try {
                PrimitiveInfo directive = engine.getPrimitive((Struct) dir);
                if (directive == null)
                    engine.warn("The directive " + ((Struct) dir).predicateIndicator + " is unknown.");
                else {
                    Object[] args = new Object[((Struct) dir).arity +1];
                    args[0] = new BindingsTable();
                    for (int i = 0; i < ((Struct) dir).arity; i++)
                        args[i+1] =  ((Struct) dir).getArg(i);
                    Prolog.evalPrimitive(directive, args);
                }
            } catch (InvocationTargetException e) {
                Throwable cause = e;
                while (cause instanceof InvocationTargetException)
                    cause = cause.getCause();
                throw cause;
            }
        }catch (Throwable cause) {
            engine.warn("An exception has been thrown during the execution of the " +
                    ((Struct) dir).predicateIndicator + " directive.\n" + cause.getMessage());
        }
        return true;
    }

    /**
     * Gets current theory
     *
     * @param onlyDynamic if true, fetches only dynamic clauses
     */
    public String getTheory(boolean onlyDynamic) {
        if (onlyDynamic)
            return dynamicDBase.toString();
        return dynamicDBase.toString() + "\n\n" + allLibraryRules.toString();
    }

    /**
     * Gets last consulted theory
     *
     * @return last theory
     */
    String getLastConsultedTheory() {
        return lastConsultedTheory;
    }

    boolean isLibraryRule(String key) {
        return allLibraryRules.containsKey(key);
    }

    /**
     * do not use remove on this one. Will stringToStructList problems
     * @return an iterator of all the dynamic predicate indicators in the Database.
     */
    Iterator dynamicPredicateIndicators() {
        return dynamicDBase.keySet().iterator();
    }

    Library consultLib(Library lib) throws PrologException {

        ClauseDatabase theoryAssociated = new ClauseDatabase();
        for (Iterator it = new Parser(lib.getTheory(), engine).iterator(); it.hasNext();) {
            Struct d = (Struct) it.next();
            if (runDirective(d))
                continue;
            Clause cl = BuiltIn.convertTermToClause(d);
            theoryAssociated.addLast(cl.head.predicateIndicator, cl);
            allLibraryRules.addLast(cl.head.predicateIndicator, cl);
        }
        librariesToRules.put(lib, theoryAssociated);
        return lib;
    }

    Library unconsultLib(Library lib) {
        lib.dismiss();
        ClauseDatabase removed = (ClauseDatabase) librariesToRules.remove(lib);
        for (Iterator it = removed.keySet().iterator(); it.hasNext();) {
            String predInfo = (String) it.next();
            List toBeRemoved = (List) removed.get(predInfo);
            List parentList = allLibraryRules.getPredicates(predInfo);
            for (int i = 0; i < toBeRemoved.size(); i++)
                parentList.remove(toBeRemoved.get(i));
        }
        return lib;
    }

    Library getLibrary(String name) {
        for (Iterator it = getCurrentLibraries(); it.hasNext(); ) {
            Library lib = (Library) it.next();
            if (name.equals(lib.getName()))
                return lib;
        }
        return null;
    }

    /**
     * @return the names of the libraries currently loaded
     */
    Iterator getCurrentLibraries() {
        return librariesToRules.keySet().iterator();
    }
}