/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jTrolog.engine;

import jTrolog.errors.*;
import jTrolog.lib.BasicLibrary;
import jTrolog.lib.Library;
import jTrolog.lib.BuiltIn;
import jTrolog.parser.Parser;
import jTrolog.terms.*;
import jTrolog.terms.Number;

import java.io.Serializable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.IOException;
import java.util.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * The class representing a jTrolog Prolog machine.
 */
public class Prolog implements Serializable {

    public static final String VERSION = "2.1";// jTrolog version
    public static final int OP_LOW = 1;
    public static final int OP_HIGH = 1200;

    private LibraryAndTheoryManager libraryAndTheoryManager;        //Rule and Fact Database Manager
    private OperatorTable opTable;              //Table mapping operators
    
    private PrintStream currentPS = System.out; //the current printstream
    private Struct currentQuery;                //the last query
    private Engine currentEngine;               //the last initialized engine
    public static final Prolog defaultMachine = new DefaultProlog();
    public static final int FX = 0;
    public static final int FY = 1;
    public static final int XFX = 2;
    public static final int XFY = 3;
    public static final int YFX = 4;
    public static final int XF = 5;
    public static final int YF = 6;

    /**
     * Builds a prolog engine with default libraries:
     * BasicLibrary, ISOLibrary, IOLibrary
     */
    public Prolog() {
        this(new String[]{"jTrolog.lib.BasicLibrary",
                "jTrolog.lib.ISOLibrary",
                "jTrolog.lib.IOLibrary"});
    }

    /**
     * Builds a prolog engine with default libraries:
     * BasicLibrary, ISOLibrary, IOLibrary + the extraLib
     *
     * @param extraLib additional library to be loaded
     */
    public Prolog(String extraLib) {
        this();
        try {
            loadLibrary(extraLib);
        } catch (InvalidLibraryException e) {
            warn(e.toString());
        }
    }

    @Override
    public Prolog clone() {
        /*
            private LibraryAndTheoryManager libraryAndTheoryManager;        //Rule and Fact Database Manager
            private OperatorTable opTable;              //Table mapping operators

            private PrintStream currentPS = System.out; //the current printstream
            private Struct currentQuery;                //the last query
            private Engine currentEngine;               //the last initialized engine

         */

        Prolog clonedEngine = new Prolog();
        clonedEngine.libraryAndTheoryManager = this.libraryAndTheoryManager.clone();
        clonedEngine.libraryAndTheoryManager.setEngine(clonedEngine);
        return clonedEngine;
    }


    /**
     * Builds a prolog engine using _only_ the specified libraries as parameters
     *
     * @param libNames the (class) names of the libraries to be loaded
     */
    public Prolog(String[] libNames) {
        opTable = new OperatorTable();
        libraryAndTheoryManager = new LibraryAndTheoryManager(this);
        try {
            loadLibrary(new BuiltIn(this));
        } catch (InvalidLibraryException e) {
            warn(e.toString());
        }

        if (libNames == null)
            return;
        for (int i = 0; i < libNames.length; i++) {
            try {
                loadLibrary(libNames[i]);
            } catch (InvalidLibraryException e) {
                warn(e.toString());
            }
        }
    }

    /**
     * Starts a command line interface with jTrolog Prolog engine.
     * Builtin-, Basic-, ISO- and IO-Libraries are loaded.
     * In the future, args could be made to look for URLs with theories?
     */
    public static void main(String[] args) throws IOException {
        Prolog vm = new Prolog();
        System.out.println("jTrolog - Java Trondheim Prolog - v.2.1");
        System.out.print("?- ");
        BufferedReader consoleIn = new BufferedReader(new InputStreamReader(System.in));
        String input = "";
        String tmp;
        while ((tmp = consoleIn.readLine())!= null) {
            input += tmp;
            try {
                if (input.trim().endsWith(".")) {
                    Solution x = vm.solve(input);
                    System.out.println("\nresult: " + x);
                    System.out.println(x.bindingsToString());
                } else if (input.trim().length() == 0) {
                    Solution x = vm.solveNext();
                    System.out.println("\nresult: " + x);
                    System.out.println(x.bindingsToString());
                } 
            } catch (Throwable throwable) {
                System.out.println("Prolog error (but don't be alarmed):\n" + throwable.getMessage());
            } finally {
                input = "";
                System.out.print("?- ");
            }
        }
    }

    /*******************************************************************************************************************
     * The Rule and Fact Database System
     * The Library System
     */
    public void assertZ(Clause toBeAsserted) throws PrologException {
        String index = toBeAsserted.head.predicateIndicator;
        if (staticDBContainsPredicate(index))
            throw new PrologException("permission_error(modify, static_procedure, " + index + ")");
        libraryAndTheoryManager.assertZ(toBeAsserted, index);
    }

    public void assertA(Clause toBeAsserted) throws PrologException {
        String index = toBeAsserted.head.predicateIndicator;
        if (staticDBContainsPredicate(index))
            throw new PrologException("permission_error(modify, static_procedure, " + index + ")");
        libraryAndTheoryManager.assertA(toBeAsserted, index);
    }

    public Struct retract(Struct sarg0) throws PrologException {
        String index = ((Struct) sarg0.getArg(0)).predicateIndicator;
        if (staticDBContainsPredicate(index))
            throw new PrologException("permission_error(modify, static_procedure, " + index + ")");
        Struct struct = libraryAndTheoryManager.retract(sarg0, index);
        if (struct != null)
            warn("DELETE: " + struct + "\n");
        return struct;
    }

    public boolean staticDBContainsPredicate(String key) {
       return libraryAndTheoryManager.isLibraryRule(key) || hasPrimitive(key);
    }

    public void abolish(String predicateIndicator) throws PrologException {
        if (staticDBContainsPredicate(predicateIndicator))
            throw new PrologException("permission_error(modify, static_procedure, " + predicateIndicator + ")");
        List l = libraryAndTheoryManager.abolish(predicateIndicator);
    }

    public void setDynamicPredicateIndicator(String predicateIndicator) throws PrologException {
        if (staticDBContainsPredicate(predicateIndicator))
            throw new PrologException("permission_error(modify, static_procedure, " + predicateIndicator + ")");
        libraryAndTheoryManager.setDynamic(predicateIndicator);
    }

    public List find(String predIndicator) throws PrologException {
        List rulesFromDatabase = libraryAndTheoryManager.find(predIndicator);
        if (rulesFromDatabase == null)
            throw new PrologException("The predicate " + predIndicator + " is unknown.");
        return rulesFromDatabase;
    }

    public Iterator dynamicPredicateIndicators() {
        return libraryAndTheoryManager.dynamicPredicateIndicators();
    }

    /**
     * clears all dynamic predicates and sets the new newTheory
     *
     * @deprecated use clearTheory() + addTheory(newTheory) instead
     * @param newTheory to be set 
     * @throws PrologException if newTheory is not valid
     */
    public void setTheory(String newTheory) throws PrologException {
        libraryAndTheoryManager.clear();
        addTheory(newTheory);
    }

    /**
     * removes all dynamic predicates
     */
    public void clearTheory() {
        libraryAndTheoryManager.clear();
    }

    /**
     * @param theory to be added to the existing set of theories in the database.
     * @throws PrologException if the new theory is not valid
     * @deprecated Use assert and retract instead, except when initialising an engine on create.
     */
    public void addTheory(String theory) throws PrologException {
        libraryAndTheoryManager.consult(theory);
    }

    /**
     * @return the current theory in the Prolog machine (only dynamic)
     */
    public String getTheory() {
        return libraryAndTheoryManager.getTheory(true);
    }

    /**
     * @return the last theory to be consulted or attempted consulted as text
     */
    public String getLastConsultedTheory() {
        return libraryAndTheoryManager.getLastConsultedTheory();
    }

    /**
     * Loads a library.
     * If a library with the same name is already present,
     * a warning event is notified. Then, the current instance of
     * that library is discarded, and the new instance gets loaded.
     *
     * @param className of the Java class containing the Library to be loaded
     * @return the reference to the Library just loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public synchronized Library loadLibrary(String className) throws InvalidLibraryException {
        try {
            return loadLibrary((Library) Class.forName(className).newInstance());
        } catch (Exception e) {
            throw new InvalidLibraryException(className);
        }
    }

    /**
     * Loads a specific instance of a library
     * If a library of the same class is already present,
     * a warning event is notified. Then, the current instance of
     * that library is discarded, and the new instance gets loaded.
     *
     * @param lib the (Java class) name of the library to be loaded
     * @throws InvalidLibraryException if name is not a valid library
     */
    public Library loadLibrary(Library lib) throws InvalidLibraryException{
        String name = lib.getName();
        if (getLibrary(name) != null)
            throw new InvalidLibraryException("library " + name + " already loaded.");

        lib.setEngine(this);

        addPrimitives(lib);
        try {
            return libraryAndTheoryManager.consultLib(lib);
        } catch (PrologException e) {
            throw new InvalidLibraryException(lib.getName(), e);
        }
    }

    /**
     * @param name of the library to be unloaded
     * @throws InvalidLibraryException if no loaded Library has the given name
     */
    public void unloadLibrary(String name) throws InvalidLibraryException{
        Library library = getLibrary(name);
        if (library == null)
            throw new InvalidLibraryException(name);
        Library unloaded = libraryAndTheoryManager.unconsultLib(library);
        removePrimitives(unloaded);
    }

    public Library getLibrary(String name) {
        return libraryAndTheoryManager.getLibrary(name);
    }

    /**
     * @return the names of the libraries currently loaded
     */
    public Iterator getCurrentLibraries() {
        return libraryAndTheoryManager.getCurrentLibraries();
    }

    /**
     * @param ps the engine printstream.
     */
    public void setPrintStream(PrintStream ps) {
        currentPS = ps;
    }

    /**
     * @return the engine printstream.
     */
    public PrintStream getPrintStream() {
        return currentPS;
    }

    /*******************************************************************************************************************
     * The Operator System
     */

    /**
     * Gets the list of the operators currently defined
     *
     * @return the list of the operators
     */
    public synchronized Iterator getCurrentOperators() {
        return opTable.getAllOperators();
    }

    public int getOperatorPriority(String name, int type) {
        return opTable.getOperatorPriority(name, type);
    }


    public void opNew(String name, int type, int i) {
        opTable.addOperator(name, type, i);
    }

    public void opNew(String name, String type, int i) {
        if (type.equalsIgnoreCase("fx"))  opTable.addOperator(name, FX, i);
        if (type.equalsIgnoreCase("fy"))  opTable.addOperator(name, FY, i);
        if (type.equalsIgnoreCase("xfx"))  opTable.addOperator(name, XFX, i);
        if (type.equalsIgnoreCase("xfy"))  opTable.addOperator(name, XFY, i);
        if (type.equalsIgnoreCase("yfx"))  opTable.addOperator(name, YFX, i);
        if (type.equalsIgnoreCase("yf"))  opTable.addOperator(name, YF, i);
        if (type.equalsIgnoreCase("xf"))  opTable.addOperator(name, XF, i);

    }

    /*******************************************************************************************************************
     * To solve, that is the problem
     */

    /**
     * Solves a query
     *
     * @param st the string representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolutionManager
     */
    public synchronized Solution solve(String st) throws Throwable {
        Parser p = new Parser(st, this);
        Term t = p.nextTerm(true);
        if (!(t instanceof Struct))         //Var or Number is considered true, since they are not false?
            return new Solution(t);

        try {
            Struct g = (Struct) t;
            if (getPrimitiveExp(g) != null )
                return new Solution(new BindingsTable().evalExpression(this, g));
            return solve(g);
        } catch (InvocationTargetException e) {
            Throwable cause = e;
            while (cause instanceof InvocationTargetException)
                cause = cause.getCause();
            throw cause;
        }
    }

    /**
     * Solves a query
     *
     * @param g the term representing the goal to be demonstrated
     * @return the result of the demonstration
     * @see SolutionManager
     */
    public synchronized Solution solve(Struct g) throws Throwable {
        onSolveBegin(g);
        currentQuery = (Struct) BuiltIn.convertTermToClauseBody(g);
        currentEngine = new Engine(this, BuiltIn.convertTermToClauseBody2(currentQuery));
        BindingsTable result = currentEngine.runFirst();
        onSolveEnd();
        return SolutionManager.prepareSolution(currentQuery, result);
    }

    /**
     * Gets next solution
     *
     * @return the result of the demonstration
     * @throws NoMorePrologSolutions if no more solutions are present
     * @see SolutionManager
     */
    public synchronized Solution solveNext() throws Throwable {
        if (currentEngine == null || !currentEngine.hasAlternatives())
            throw new NoMorePrologSolutions();
        BindingsTable result = currentEngine.run(Engine.BACK);
        onSolveEnd();
        return SolutionManager.prepareSolution(currentQuery, result);
    }

    public synchronized void onSolveBegin(Term g) {
        for (Iterator it = getCurrentLibraries(); it.hasNext();)
            ((Library) it.next()).onSolveBegin(g);
    }

    public synchronized void onSolveEnd() {
        for (Iterator it = getCurrentLibraries(); it.hasNext(); )
            ((Library) it.next()).onSolveEnd();
    }

    /**
     * Asks for the presence of open alternatives to be explored
     * in current demostration process.
     *
     * @return true if open alternatives are present
     */
    public synchronized boolean hasOpenAlternatives() throws Throwable {
        return currentEngine.hasAlternatives();
    }

    /**
     * Matches the structure of the two original terms.
     * OBS: Variables in the original terms are not resolved.
     * If this is desired, then the terms passed in should be clonedAndResolved first.
     * <p/>
     * OBS2: no unification of variables is made.
     *
     * @param t0 first term to be matched
     * @param t1 second term to be matched
     * @return true if the structure of the two terms match
     */
    public static synchronized boolean match(Term t0, Term t1) {
        if (t0 instanceof Var || t1 instanceof Var)
            return true;

        if (t0 instanceof Number && t1 instanceof jTrolog.terms.Number)
            return BasicLibrary.number_equality_2((Number) t0, (Number) t1);

        if (t0 instanceof StructAtom && t1 instanceof StructAtom)
            return t0.equals(t1);

        if (t0 instanceof Struct && t1 instanceof Struct) {
            Struct s0 = (Struct) t0;
            Struct s1 = (Struct) t1;
            if (s0.arity != s1.arity)
                return false;
            for (int i = 0; i < s1.arity; i++) {
                if (!match(s0.getArg(i), s1.getArg(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    /*******************************************************************************************************************
     * The Primitives system
     */
    private HashMap directives = new HashMap();
    private HashMap expressions = new HashMap();

    public boolean hasPrimitive(String predicateIndiciator) {
        return directives.containsKey(predicateIndiciator) || expressions.containsKey(predicateIndiciator);
    }

    public boolean hasPrimitiveExp(String predicateIndiciator) {
        return expressions.containsKey(predicateIndiciator);
    }

    /**
     * @return the primitive matching the predicate indicator signature of the struct passed as argument.
     */
    final PrimitiveInfo getPrimitive(Struct struct) {
        return (PrimitiveInfo) directives.get(struct.predicateIndicator);
    }

    final PrimitiveInfo getPrimitiveExp(Struct struct) {
        return (PrimitiveInfo) expressions.get(struct.predicateIndicator);
    }

    private void addPrimitives(Library src) {
        List prims = getPrimitives(src);
        for (int i = 0; i < prims.size(); i++) {
            PrimitiveInfo p = (PrimitiveInfo) prims.get(i);
            if (p.method.getReturnType() == Term.class)
                expressions.put(p.key,p);
            else
                directives.put(p.key,p);
        }
    }

    private void removePrimitives(Library src) {
        List prims = getPrimitives(src);
        for (int i = 0; i < prims.size(); i++) {
            PrimitiveInfo p = (PrimitiveInfo) prims.get(i);
            directives.remove(p.key);
            expressions.remove(p.key);
        }
    }

    /**
     * 1. Every method in the library that matches the following criteria:
     *  - return type either boolean, void or Term,
     *  - the first parameter is BindingsTable, and
     *  - the method name ends with "_N" where N = number of parameters - 1.
     * are added as Primitives to the Prolog machine that loads the Library.
     *
     * 2. For each primitive added, the getSynomym(name) is also queried.
     * For each synonyms, a copy of the primitive is added to the machine
     * using the synonym name.
     *
     * @param library that is scanned for primitives
     * @return a list of PrimitiveInfo
     */
    private static List getPrimitives(Library library) {
        ArrayList result = new ArrayList();

        Method[] mlist = library.getClass().getMethods();
        methodLoop: for (int i = 0; i < mlist.length; i++) {
            Method m = mlist[i];
            String mName = m.getName();
            Class[] params = m.getParameterTypes();

            Class retType = m.getReturnType();
            if (!(retType == boolean.class || retType == Term.class || retType == void.class))
                continue;

            int index = mName.lastIndexOf('_');
            if (index == -1)
                continue;

            // retrieve and check arg number
            int arity = Integer.parseInt(mName.substring(index + 1, mName.length()));
            if (params.length - 1 != arity)
                continue;

            for (int j = 1; j < arity; j++) {
                if (!Term.class.isAssignableFrom(params[j]))
                    continue methodLoop;
            }

            String rawName = mName.substring(0, index);
            result.add(new PrimitiveInfo(library, m, rawName, arity));

            // adding synonyms
            String[] synonyms = library.getSynonym(rawName);
            if (synonyms != null){
                for (int j = 0; j < synonyms.length; j++)
                    result.add(new PrimitiveInfo(library, m, synonyms[j], arity));
            }
        }
        return result;
    }


    /*******************************************************************************************************************
     * The Warning system
     */
    List warnings = new LinkedList();

    public synchronized void resetWarningList() {
        warnings.clear();
    }

    public List getAndResetWarnings(){
        List tmp = warnings;
        warnings = new LinkedList();
        return tmp;
    }

    /**
     * @param m adds the warning message to the warnings list
     */
    public void warn(String m) {
        warnings.add(m);
    }


    /*******************************************************************************************************************
     * The Flag system
     */
    private HashMap flags = new HashMap();

    public void defineFlag(String name, Struct valueList, Term defValue, boolean modifiable) {
        flags.put(name, new Flag(name, valueList, defValue, modifiable));
    }

    public Flag getFlag(String name) {
        return (Flag) flags.get(name);
    }

    public Term getFlagValue(String name) {
        Flag flag = (Flag) flags.get(name);
        return flag == null ? null : flag.getValue();
    }

    public Term getPrologFlagList() {
        Struct flist = Term.emptyList;
        for (Iterator it = flags.values().iterator(); it.hasNext();) {
            Flag fl = (Flag) it.next();
            Term at0 = new Struct("flag",new Term[]{new StructAtom(fl.getName()), fl.getValue()});
            flist = new Struct(".", new Term[]{at0, flist});
        }
        return flist;
    }

    public static boolean evalPrimitive(PrimitiveInfo prim, Object[] primitive_args) throws Throwable {
        Method method = prim.method;
        try {
            if (method.getReturnType() == void.class) {
                method.invoke(prim.source, primitive_args);
                return true;
            }
            return ((Boolean) method.invoke(prim.source, primitive_args)).booleanValue();
        } catch (IllegalArgumentException e) {
            Class[] expectedArgs = method.getParameterTypes();
            for (int i = 1; i < primitive_args.length; i++) {
                Term actual = (Term) primitive_args[i];
                Class expectedClass = expectedArgs[i];
                if (expectedClass.isAssignableFrom(actual.getClass()))
                    continue;                                           //nothing wrong with this one, check next param
                if (actual instanceof Var)
                    throw new PrologException("instantiation_error");   //expected anything but a Var
                String expected = expectedClass.getName();
                expected = expected.substring(expected.lastIndexOf('.') + 1);
                throw new PrologException("type_error(" + expected + ", " + actual + ")");
            }
            throw new PrologException("WTF: Bug in system.");
        }
    }
}