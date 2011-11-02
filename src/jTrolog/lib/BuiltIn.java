/*
 * tuProlog - Copyright (C) 2001-2007 aliCE team at deis.unibo.it
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
package jTrolog.lib;

import jTrolog.errors.InvalidLibraryException;
import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;
import jTrolog.engine.*;
import jTrolog.terms.*;
import jTrolog.terms.Number;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * Library of built-in predicates
 *
 * @author Alex Benini
 * @author janerist@stud.ntnu.no
 * @author ivar.orstavik@hist.no
 */
public class BuiltIn extends Library {

    private Prolog engine;

    public BuiltIn(Prolog mediator) {
        engine = mediator;
    }

    /**
     * Defines a map for synonyms for primitives.
     * String primitive name -> String[]{synonym name, another synonym name, ..}.
     */
    public String[] getSynonym(String primitive) {
        if (primitive.equals("cut"))
            return new String[]{"!"};
        if (primitive.equals("unify"))
            return new String[]{"="};
        if (primitive.equals("deunify"))
            return new String[]{"\\="};
        if (primitive.equals("comma"))
            return new String[]{","};
        if (primitive.equals("solve"))
            return new String[]{"initialization"};
        if (primitive.equals("assertz"))
            return new String[]{"assert"};
        if (primitive.equals("copy"))
            return new String[]{"copy_term"};
        return null;
    }

    /**
     * Primitives
     */
    public boolean comma_2(BindingsTable bt, Term arg0, Term arg1) {
        throw new RuntimeException("','/2 method is hardcoded in the Engine, do not use Buitlin.comma_2!");
    }

    public static boolean cut_0(BindingsTable bt) {
        throw new RuntimeException("!/0 method is hardcoded in engine, do not use Buitlin.cut_0!");
    }

    public static boolean fail_0(BindingsTable bt) {
        return false;
    }

    public static boolean true_0(BindingsTable bt) {
        return true;
    }

    public static boolean call_1(BindingsTable bt, Term arg1) {
        throw new RuntimeException("call/1 method is hardcoded in engine, do not use Buitlin.call_1!");
    }

    public static boolean halt_0(BindingsTable bt) throws PrologException {
        throw new PrologException("halt()");
    }

    public boolean halt_1(BindingsTable bt, jTrolog.terms.Number arg0) throws PrologException {
        throw new PrologException("halt("+arg0.intValue() +")");
    }

    public boolean $debug_1(BindingsTable bt, Term arg0) {
        System.out.println("debug=== " + bt.variant(arg0));
        return true;
    }

    public boolean $debug_3(BindingsTable bt, Term arg0, Term arg2, Term arg3) {
        System.out.println("debug0=== " + bt.variant(arg0));
        System.out.println("debug1=== " + bt.variant(arg2));
        System.out.println("debug2=== " + bt.variant(arg3));
        return true;
    }

    public boolean asserta_1(BindingsTable bt, Term arg0) throws PrologException {
        arg0 = bt.variant(arg0);
        engine.assertA(convertTermToClause(arg0));
        return true;
    }

    public boolean assertz_1(BindingsTable bt, Term arg0) throws PrologException {
        arg0 = bt.variant(arg0);
        engine.assertZ(convertTermToClause(arg0));
        return true;
    }

    public boolean $retract_1(BindingsTable bt, Struct arg0) throws PrologException {
        arg0 = (Struct) bt.variant(arg0);
        arg0 = convertTermToClause(arg0).original;
        Struct sarg0 = (Struct) bt.wrapWithID(arg0);
        Struct retractedClause = engine.retract(sarg0);
        // if clause to retract found -> retract + true
        if (retractedClause != null) {
            bt.unify(retractedClause, arg0);
            return true;
        }
        return false;
    }

    /**
     * see 8.9.4 p.81 in ISO standard
     *
     * @param predicateIndicator
     * @return true
     * @throws PrologException in case of errors
     */
    public boolean abolish_1(BindingsTable bt, Struct predicateIndicator) throws PrologException {
        engine.abolish(isPredicateIndicator(predicateIndicator, bt));
        return true;
    }

    /**
     * Registers the existence predicate indicator in the dynamic data base.
     * If such a predicate is later requested, but none exists, the search will not throw an error, but return false.
     * see ISO spec on abolish for details on errors.
     *
     * @param predicateIndicator
     * @return true
     * @throws PrologException in case of bad input
     */
    public boolean dynamic_1(BindingsTable bt, Struct predicateIndicator) throws PrologException {
        engine.setDynamicPredicateIndicator(isPredicateIndicator(predicateIndicator, bt));
        return true;
    }

    /*
      * loads a engine library, given its java
      * class name
      */
    public boolean load_library_1(BindingsTable bt, Struct arg0) throws InvalidLibraryException {
        if (!BasicLibrary.atom_1(bt, arg0))
            return false;
        engine.loadLibrary(arg0.name);
        return true;
    }

    /**
     * unloads a engine library, given its java class name
     */
    public boolean unload_library_1(BindingsTable bt, Struct arg0) throws InvalidLibraryException {
        if (!BasicLibrary.atom_1(bt, arg0))
            return false;
        engine.unloadLibrary(arg0.name);
        return true;
    }

    /*
      * get flag list:  flag_list(-List)
      */
    public boolean flag_list_1(BindingsTable bt, Term arg0) {
        return bt.unify(arg0, bt.wrapWithID(engine.getPrologFlagList()));
    }


    public boolean unify_2(BindingsTable bt, Term arg0, Term arg1) {
        return bt.unify(arg0, arg1);
    }


    // \=
    public boolean deunify_2(BindingsTable bt, Term arg0, Term arg1) {
        return !bt.unify(arg0, arg1);
    }


    //unifies a new Struct list of the given length with Any vars as elements with the Var out
    public boolean newlist_2(BindingsTable bt, Int length, Var out) throws PrologException {
        int lengthInt = length.intValue();
        if (lengthInt < 0)
            throw new PrologException("domain_error(not_less_than_zero, " + length + ")");
        if (lengthInt == 0)
            return bt.unify(out, Term.emptyList);
        return bt.unify(out, (WrapStruct) bt.wrapWithID(Parser.createListContainingAnyVars(lengthInt)));
    }

    // $copy
    public boolean copy_2(BindingsTable bt, Term arg0, Term arg1) {
        return bt.unify(bt.wrapWithID(bt.variant(arg0)), arg1);
    }

    // $find
    // look for clauses whose head unifies whith arg0 and unify the list of them with arg1
    public boolean $find_2(BindingsTable bt, Struct arg0, Term arg1) throws PrologException, CloneNotSupportedException {
        String key = arg0.predicateIndicator;
        if (engine.hasPrimitive(key))
            throw new PrologException("permission_error(access, static_procedure, " + key + ")");
        LinkedList res = new LinkedList();
        List clauses = engine.find(key);
        if (clauses.isEmpty())
            return bt.unify(arg1, Term.emptyList);
        for (Iterator it = clauses.iterator(); it.hasNext();)
            res.add(((Clause) it.next()).original);
        res.add(Term.emptyList);
        return bt.unify(arg1, bt.wrapWithID(Parser.createStructList(res)));
    }

    // set_prolog_flag(+Name,@Value)
    public boolean set_prolog_flag_2(BindingsTable bt, Term arg0, Term arg1) {
        if (!BasicLibrary.atom_1(bt, arg0) && !(arg0 instanceof Struct) || !BasicLibrary.ground_1(bt, arg1))
            return false;

        String name = arg0.toString();
        Flag flag = engine.getFlag(name);//Flag) it.next();
        if (flag != null) {
            try {
                for (Iterator it2 = bt.structListIterator(flag.getValueList(), true); it2.hasNext();) {
                    Term t = (Term) it2.next();
                    if (Prolog.match(arg1, t)) {
                        flag.setValue(arg1);
                        return true;
                    }
                }
            } catch (PrologException e) {
                throw new RuntimeException("error in iterating a Struct list");
            }
        }
        return false;
    }


    // get_prolog_flag(@Name,?Value)
    public boolean get_prolog_flag_2(BindingsTable bt, Term arg0, Term arg1) {
        if (!BasicLibrary.atom_1(bt, arg0) && !(arg0 instanceof Struct))
            return false;

        String name = arg0.toString();
        Term value = engine.getFlagValue(name);
        if (value == null)
            return false;
        return bt.unify(value, arg1);
    }

    /*
      * DIRECTIVES
      */

    /**
     * op(+Precedence, +Type, +Name) defines a new operator
     * if precedence not in 0..1200 -> delete currently present op
     */
    public void op_3(BindingsTable bt, Number arg0, StructAtom arg1, StructAtom arg2) {
        String st = ((Struct) arg1).name;
        if (st.matches("[xy]?f[xy]?"))      //a tad simplified
            engine.opNew(arg2.toString(), st, arg0.intValue());
    }


    public void flag_4(BindingsTable bt, Term flagName, Struct flagSet, Term flagDefault, Term flagModifiable) {
        if (flagModifiable.equals(Term.TRUE) || flagModifiable.equals(Term.FALSE))
            engine.defineFlag(flagName.toString(), flagSet, flagDefault, flagModifiable.equals(Term.TRUE));
    }

    //todo test that this works properly.
    //todo Can alternatively use a temporary list to be run after the rest of the theories in consults have been added.
    public void solve_1(BindingsTable bt, Struct goal) throws Throwable {
        engine.solve(bt.wrapWithID(bt.variant(goal)).toString());
    }

    public void load_library_1(BindingsTable bt, Term lib) throws InvalidLibraryException {
        if (BasicLibrary.atom_1(bt, lib))
            engine.loadLibrary(((Struct) lib).name);
    }

    public void consult_1(BindingsTable bt, Term theory) throws FileNotFoundException, PrologException, IOException {
        FileInputStream is;
        try {
            is = new FileInputStream(Parser.removeApices(theory.toString()));
        } catch (FileNotFoundException e) {
            File dir = new File(System.getProperty("user.dir"));
            String filename = dir + File.separator + Parser.removeApices(theory.toString());
            is = new FileInputStream(filename);
        }
        engine.addTheory(IOLibrary.readStream(is));
    }

    public boolean $instantiation_error_0(BindingsTable bt) throws PrologException {
        throw new PrologException("instantiation_error");
    }

    public boolean $type_error_2(BindingsTable bt, Term typeName, Term term) throws PrologException {
        throw new PrologException("type_error(" + typeName + ", " + term + ")");
    }

    public boolean $representation_error_1(BindingsTable bt, Term maxArity) throws PrologException {
        throw new PrologException("representation_error(" + maxArity + ")");
    }

    public boolean $domain_error_zero_1(BindingsTable bt, Term term) throws Exception {
        throw new PrologException("domain_error(not_less_than_zero, " + term + ")");
    }

    /**
     * see ISO 7.6.1 and 7.6.2
     * @param arg0
     * @return
     * @throws PrologException
     */
    public static Clause convertTermToClause(Term arg0) throws PrologException {
        if (arg0 instanceof Var)
            throw new PrologException("instantiation_error");

        if (arg0 instanceof Number)
            throw new PrologException("type_error(callable, " + arg0 + ")");

        Term head, body;
        Struct cl = (Struct) arg0;
        if (cl.predicateIndicator != Parser.doubleClauseSignature){
            head = arg0;
            body = Term.TRUE;
        } else {
            head = cl.getArg(0);
            body = cl.getArg(1);
        }
        if (head instanceof Number)
            throw new PrologException("type_error(callable, " + head + ")");
        if (head instanceof Var)
            throw new PrologException("instantiation_error");

        if (body instanceof Number)
            throw new PrologException("type_error(callable, " + body + ")");
        body = convertTermToClauseBody(body);
        Struct[] body2 = convertTermToClauseBody2(body);

        return new Clause(body2, (Struct) head, new Struct(":-", new Term[]{head, body}));
    }

    //todo see 7.6.2 and table 9 in ISO
    public static Struct[] convertTermToClauseBody2(Term body) throws PrologException {
        LinkedList tmp = new LinkedList();
        while (body instanceof Struct && ((Struct) body).predicateIndicator == Parser.commaSignature){
              tmp.add(convertTermToClauseBody(((Struct) body).getArg(0))); //todo, this might turn out to be a problem if the left child is a comma...
              body = ((Struct) body).getArg(1);
        }
        tmp.add(convertTermToClauseBody(body));
        return (Struct[]) tmp.toArray(new Struct[tmp.size()]);
    }

    public static Struct convertTermToClauseBody(Term body) throws PrologException {
        if (body instanceof Var)
            return new Struct("call", new Term[]{body});
        if (body instanceof Number)
            throw new PrologException("type_error(callable, " + body + ")");
        Struct s = (Struct) body;
        String sPredIndic = s.predicateIndicator;
        if (sPredIndic == Parser.ifSignature ||
            sPredIndic == Parser.commaSignature ||
            sPredIndic == Parser.semiColonSignature ||
            sPredIndic == Parser.throwSignature ||
            sPredIndic == Parser.catchSignature ){
            Term[] args = new Term[s.arity];
            for (int i = 0; i < s.arity; i++)
                args[i] = convertTermToClauseBody(s.getArg(i));
            return new Struct(s.name, args, s.getOperatorType());
        }
        return s;
    }

    public static String isPredicateIndicator(Struct predicateIndicator, BindingsTable bt) throws PrologException {
        if (predicateIndicator.predicateIndicator != "'/'/2")
            throw new PrologException("type_error(predicate_indicator, "+predicateIndicator+")");
        Term name = bt.resolve(predicateIndicator.getArg(0));
        Term arity = bt.resolve(predicateIndicator.getArg(1));
        if (name instanceof Var || arity instanceof Var)
            throw new PrologException("instantiation_error");
        if (!(arity instanceof Int))
            throw new PrologException("type_error(integer, " + arity + ")");
        if (!(name instanceof StructAtom))
            throw new PrologException("type_error(atom, " + name + ")");
        Int arityInt = (Int) arity;
        if (arityInt.intValue() < 0)
            throw new PrologException("domain_error(not_less_than_zero, " + arityInt + ")");
        if (arityInt.longValue() > Integer.MAX_VALUE)
            throw new PrologException("representation_error(max_arity)");
        return Parser.wrapAtom(name.toString()) + "/" + arity;
    }
}