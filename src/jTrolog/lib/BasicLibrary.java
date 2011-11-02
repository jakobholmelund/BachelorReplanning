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

import jTrolog.engine.*;
import jTrolog.terms.Number;
import jTrolog.terms.Float;
import jTrolog.terms.*;
import jTrolog.terms.Long;
import jTrolog.parser.Parser;
import jTrolog.errors.InvalidLibraryException;
import jTrolog.errors.PrologException;

import java.util.*;

/**
 * This class defines a set of basic built-in
 * predicates for the tuProlog engine
 *
 * Library/Theory dependency: none
 *
 *
 *
 */
public class BasicLibrary extends Library {

    //
    // meta-predicates
    //


    /**
     * sets a new theory provided as a text
     */
    public boolean set_theory_1(BindingsTable bt, Term th) {
        if (!atom_1(bt,th))
            return false;
        try {
            engine.clearTheory();
            engine.addTheory(((Struct) th).name);
            return true;
        } catch (PrologException e) {
            System.err.println("invalid theory:"+e.getMessage());
            return false;
        }
    }

    /**
     *  adds a new theory provided as a text
     */
    public boolean add_theory_1(BindingsTable bt, Term th) throws PrologException {
        if (!atom_1(bt,th))
            return false;
        ((Library) this).engine.addTheory(((Struct) th).name);
        return true;
    }

    /** gets current theory text */
    public boolean get_theory_1(BindingsTable bt, Term arg) {
        return bt.unify(arg, new StructAtom(engine.getTheory()));
    }

    public boolean load_library_2(BindingsTable bt, Struct className, Term libName) throws InvalidLibraryException {
        Library lib = engine.loadLibrary(Parser.removeApices(className.name));
        return bt.unify(libName, new StructAtom(lib.getName()));
    }

    public boolean get_operators_list_1(BindingsTable bt, Struct argument) {
        LinkedList result = new LinkedList();
        for (Iterator it = engine.getCurrentOperators(); it.hasNext();)
            result.add((it.next()));
        if (result.isEmpty())
            return bt.unify(argument, Term.emptyList);
        result.add(Term.emptyList);
        return bt.unify(argument, bt.createStructList(result));
    }

    public boolean warning_0(BindingsTable bt) {
        engine.resetWarningList();
        return true;
    }

    public boolean nowarning_0(BindingsTable bt) {
        engine.resetWarningList();
        return true;
    }

    //
    // term type inspection
    //

    public static boolean constant_1(BindingsTable bt, Term t) {
        return atomic_1(bt,t);
    }

    public static boolean number_1(BindingsTable bt, Term t) {
        return t instanceof Number;
    }

    public static boolean integer_1(BindingsTable bt, Term t) {
        return t instanceof Int;
    }

    public static boolean float_1(BindingsTable bt, Term t) {
        return t instanceof Float;
    }

    public static boolean atom_1(BindingsTable bt, Term t) {
        return t instanceof StructAtom;
    }

    public static boolean compound_1(BindingsTable bt, Term t) {
        return t instanceof Struct && ! (t instanceof StructAtom);
    }

    public static boolean list_1(BindingsTable bt, Term t) throws PrologException {
        if (t instanceof Var)
            throw new PrologException("instantiation_error");
        if (t.equals(Term.emptyList))
            return true;
        if (!(t instanceof Struct))
            return false;
        final Struct s = (Struct) t;
        if (s.predicateIndicator != Parser.listSignature)
            return false;
        //iterate the list to find the last element and run isList on that last element
        Term last = null;
        for (Iterator it = Struct.iterator(s); it.hasNext(); last = (Term) it.next()) ;
        return list_1(bt, bt.resolve(last));
    }

    public boolean var_1(BindingsTable bt, Term t) {
        return t instanceof Var;
    }

    public boolean nonvar_1(BindingsTable bt, Term t) {
        return !(t instanceof Var);
    }

    public static boolean atomic_1(BindingsTable bt, Term t) {
        return t instanceof Number || t instanceof StructAtom;
    }

    public static boolean ground_1(BindingsTable bt, Term t) {
        if (t instanceof Var)
            return false;
        if (t instanceof Number || t instanceof StructAtom)
            return true;
        //compound and thus wrapped struct
        WrapStruct wrapStruct = ((WrapStruct) t);
        Var[] childVars = wrapStruct.getVarList();
        if (childVars == null) return false; //todo no WrapStruct should have null as childVars. Should find out when and why null is returned here..
        int ctx = wrapStruct.getContext();
        for (int i = 0; i < childVars.length; i++) {
            if (!ground_1(bt, bt.resolve(new WrapVar(childVars[i], ctx))))
                return false;
        }
        return true;
    }

    public boolean $arg_3(BindingsTable bt, Int n, Struct term, Term arg) throws PrologException {
        if (!BasicLibrary.compound_1(bt, term))
            throw new PrologException("type_error(compound, " + term + ")");
        if (n.intValue() < 0)
            throw new PrologException("domain_error(not_less_than_zero, " + n + ")");

        if (n.intValue() == 0 || n.intValue() > term.arity)
            return false;

        Term nthArg = term.getArg(n.intValue() - 1);
        return bt.unify(arg, nthArg);
    }

    public boolean $functor_3(BindingsTable bt, Term term, Term name, Term arity) throws PrologException {
        Int maxArity = (Int) engine.getFlagValue("max_arity");

        if (term instanceof Var) {
            if (name instanceof Var || arity instanceof Var)
                throw new PrologException("instantiation_error");

            if (!BasicLibrary.atomic_1(bt, name))
                throw new PrologException("type_error(atomic, " + name + ")");

            if (!(arity instanceof Int))
                throw new PrologException("type_error(integer, " + arity + ")");

            if (((Int) arity).longValue() > maxArity.intValue())
                throw new PrologException("representation_error(max_arity)");

            if (((Int) arity).intValue() < 0)
                throw new PrologException("domain_error(not_less_than_zero, " + arity + ")");

            if (!BasicLibrary.atom_1(bt, name) && ((Int) arity).intValue() > 0)
                throw new PrologException("type_error(atom, " + name + ")");

            if (BasicLibrary.atomic_1(bt, name) && ((Int) arity).intValue() == 0)
                return bt.unify(term, name);

            Struct newList = (WrapStruct) bt.wrapWithID(Parser.createListContainingAnyVars(((Int) arity).intValue() + 1));
            bt.unify(newList.getArg(0), name);
            return $tofromlist_2(bt, term, newList);
        }

        if (BasicLibrary.atomic_1(bt, term))
            return bt.unify(term, name) && bt.unify(arity, new Int(0));

        if (BasicLibrary.compound_1(bt, term)) {
            StructAtom a = new StructAtom(((Struct) term).name);
            return bt.unify(name, a) && bt.unify(arity, new Int(((Struct) term).arity));
        }
        return false;
    }

    public boolean $tofromlist_2(BindingsTable bt, Term structIn, Term listIn) throws PrologException {
        if (structIn instanceof Number || structIn instanceof StructAtom) {
            Struct newList = (WrapStruct) bt.wrapWithID(Parser.createListContainingAnyVars(1));
            bt.unify(newList.getArg(0), structIn);
            return bt.unify(newList, listIn);
        }
        if (structIn instanceof Struct) {
            Struct struct = (Struct) structIn;
            WrapStruct medium = (WrapStruct) bt.wrapWithID(Parser.createListContainingAnyVars(struct.arity + 1));
            Iterator it = bt.structListIterator(medium, false);
            //unify the name of the struct with the first in the medium list
            bt.unify((Term) it.next(), new StructAtom(struct.name));
            //unify the children of the Struct with the rest of the medium list
            for (int i = 0; i < struct.arity; i++)
                bt.unify((Var) it.next(), struct.getArg(i));
            //try to unify the medium list generated from the struct with listIn
            return bt.unify(medium, listIn);
        }
        if (structIn instanceof Var) {
            if (listIn instanceof Var)
                throw new PrologException("instantiation_error");
            if (!list_1(bt, listIn))
                throw new PrologException("type_error(list, " + listIn + ")");
            if (listIn.equals(Term.emptyList))
                throw new PrologException("domain_error(non_empty_list, " + listIn + ")");
            Struct list = (Struct) listIn;

            Term head = bt.resolve(list.getArg(0));
            if (head instanceof Var)
                throw new PrologException("instantiation_error");
            if (!BasicLibrary.atom_1(bt, head))
                throw new PrologException("type_error(atom, " + head + ")");

            Term tail = bt.resolve(list.getArg(1));
            if (tail instanceof Struct) {
                LinkedList terms = new LinkedList();
                for (Iterator it = bt.structListIterator((Struct) tail, true); it.hasNext();)
                    terms.add(it.next());
                if (terms.isEmpty())
                    return bt.unify(structIn, head);
                if (terms.size() > ((Int) engine.getFlagValue("max_arity")).intValue())
                    throw new PrologException("representation_error(max_arity)");

                Term[] argValuesToBeLinked = (Term[]) terms.toArray(new Term[0]);
                Term[] variableArgs = new Term[argValuesToBeLinked.length];
                for (int i = 0; i < argValuesToBeLinked.length; i++)
                    variableArgs[i] = new Var("_", i+1);
                WrapStruct wrapStructRes = (WrapStruct) bt.wrapWithID(new Struct(head.toString(), variableArgs));
                for (int i = 0; i < argValuesToBeLinked.length; i++)
                    bt.unify(wrapStructRes.getArg(i), argValuesToBeLinked[i]);
                return bt.unify(structIn, wrapStructRes);
            } else {
                Struct newStruct = new Struct(head.toString(), new Term[]{new Var("_", 1)});
                WrapStruct wrapStructRes = (WrapStruct) bt.wrapWithID(newStruct);
                bt.unify(wrapStructRes.getArg(0), tail);
                return bt.unify(structIn, wrapStructRes);
            }
        }
        return false;
    }

    //
    // term/espression comparison
    //

    public Term eval_1(BindingsTable bt, EvaluableTerm structIn) throws Throwable {
        return bt.evalExpression(engine, structIn);
    }

    public boolean is_2(BindingsTable bt, Term structIn, EvaluableTerm listIn) throws Throwable {
        return bt.unify(structIn, bt.evalExpression(engine, listIn));
    }

    public boolean expression_equality_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        Number val1 = bt.evalExpression(engine, structIn);
        Number val2 = bt.evalExpression(engine, listIn);
        if (val1 instanceof Float || val2 instanceof Float)
            return Number.compareDoubleValues(val1, val2) == 0;
        return val1.equals(val2);
    }

    public boolean expression_greater_than_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        Number num0 = bt.evalExpression(engine, structIn);
        Number num1 = bt.evalExpression(engine, listIn);
        if (num0 instanceof Float || num1 instanceof jTrolog.terms.Float)
            return Number.compareDoubleValues(num0, num1) >0;
        return num0.intValue() > num1.intValue();
    }

    public boolean expression_less_or_equal_than_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return ! expression_greater_than_2(bt, structIn, listIn);
    }

    public boolean expression_less_than_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return !expression_greater_or_equal_than_2(bt, structIn, listIn);
    }

    public boolean expression_greater_or_equal_than_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return expression_greater_than_2(bt, structIn, listIn) || expression_equality_2(bt, structIn, listIn);
    }

    public static boolean term_equality_2(BindingsTable bt, Term structIn, Term listIn) {
        if (structIn instanceof Var){
            if (!(listIn instanceof Var))
                return false;
            Var v = (Var) listIn;
            if (((Var) structIn).isAnonymous() && v.isAnonymous())
                return false;
            return v.equals(structIn);
        }
        if (structIn instanceof Number){
            if (!(listIn instanceof Number))
                return false;
            return number_equality_2((Number)structIn, (Number)listIn);
        }

        if (structIn instanceof Struct){
            if (!(listIn instanceof Struct))
                return false;

            Struct ts = (Struct) listIn;
            if (((Struct) structIn).arity != ts.arity || !((Struct) structIn).name.equals(ts.name))
                return false;

            for (int i = 0;i < ((Struct) structIn).arity;i++) {
                if (!term_equality_2(null, ((Struct) structIn).getArg(i), ts.getArg(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    public static boolean number_equality_2(Number listIn, Number structIn) {
        if (listIn instanceof Int && structIn instanceof Int)
                return structIn.longValue() == listIn.longValue();
        else if (listIn instanceof Float && structIn instanceof Float)
            return Number.compareDoubleValues(structIn, listIn) == 0;
        return false;
    }

    public static boolean term_greater_than_2(BindingsTable bt, Term structIn, Term listIn) {
        if (structIn instanceof Var){
            if (!(listIn instanceof Var))
                return false;
            return structIn.toString().hashCode() > listIn.toString().hashCode();
        }

        if (structIn instanceof Struct){
            if (listIn instanceof Struct) {
                Struct ts = (Struct) listIn;
                int tarity = ts.arity;
                if (((Struct) structIn).arity < tarity)
                    return false;
                if (((Struct) structIn).arity == tarity) {
                    if (((Struct) structIn).name.equals(ts.name) ) {
                        for (int i = 0; i < ((Struct) structIn).arity; i++) {
                            if (term_greater_than_2(null, ((Struct) structIn).getArg(i), ts.getArg(i)))
                                return true;
                            if (!term_equality_2(null, ((Struct) structIn).getArg(i), ts.getArg(i)))
                                return false;
                        }
                    }
                    if (((Struct) structIn).name.compareTo(ts.name) <= 0)
                        return false;
                }
            }
            return true;
        }
        if (structIn instanceof Number){
            if (listIn instanceof Var)
                return true;
            if (listIn instanceof Struct)
                return false;
            if (!(listIn instanceof Number))
                return false;

            Number n2 = ((Number) listIn);
            Number n1 = ((Number) structIn);
            if (n1 instanceof Float || n2 instanceof Float)
                return Number.compareDoubleValues(n1, n2) > 0;
            return n1.longValue() > n2.longValue();
        }
        return false; //such as implementation specific NullTerm etc.
    }

    public static boolean term_less_than_2(BindingsTable bt, Term structIn, Term listIn) {
        return !term_greater_than_2(null, structIn, listIn) && !term_equality_2(null, structIn, listIn);
    }


    public Term expression_plus_1(BindingsTable bt, EvaluableTerm structIn) throws Throwable {
        return bt.evalExpression(engine, structIn);
    }

    public Term expression_minus_1(BindingsTable bt, EvaluableTerm listIn) throws Throwable {
        Number val0 = bt.evalExpression(engine, listIn);
        if (val0 instanceof jTrolog.terms.Long)
            return new jTrolog.terms.Long(val0.longValue() * -1);
        if (val0 instanceof Int)
            return new Int(val0.intValue() * -1);
        if (val0 instanceof jTrolog.terms.Double)
            return new jTrolog.terms.Double(val0.doubleValue() * -1);
        if (val0 instanceof jTrolog.terms.Float)
            return new jTrolog.terms.Float(val0.floatValue() * -1);
        return null;
    }

    public Term expression_bitwise_not_1(BindingsTable bt, EvaluableTerm structIn) throws Throwable {
        Number val = bt.evalExpression(engine, structIn);
        if (!(val instanceof Int))
            throw new PrologException("type_error(integer, " + val + ")");
        return new Int(~val.intValue());
    }

    public Term expression_plus_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        Number val0 = bt.evalExpression(engine, structIn);
        Number val1 = bt.evalExpression(engine, listIn);
        if (val0 instanceof Float || val1 instanceof Float)
            return new jTrolog.terms.Double(val0.doubleValue() + val1.doubleValue());
        return Number.getIntegerNumber( val0.longValue() + val1.longValue());
    }

    public Term expression_minus_2(BindingsTable bt, EvaluableTerm structIn,EvaluableTerm listIn) throws Throwable {
        Number val0 = bt.evalExpression(engine, structIn);
        Number val1 = bt.evalExpression(engine, listIn);
        if (val0 instanceof Float || val1 instanceof Float)
            return new jTrolog.terms.Double(val0.doubleValue() - val1.doubleValue());
        return Number.getIntegerNumber(val0.longValue() - val1.longValue());
    }

    public Term expression_multiply_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        Number val0 = bt.evalExpression(engine, structIn);
        Number val1 = bt.evalExpression(engine, listIn);
        if (val0 instanceof Float || val1 instanceof Float)
            return new jTrolog.terms.Double(val0.doubleValue() * val1.doubleValue());
        return Number.getIntegerNumber(val0.longValue() * val1.longValue());
    }

    public Term expression_div_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        Number val0 = bt.evalExpression(engine, structIn);
        Number val1 = bt.evalExpression(engine, listIn);
        if (val0 instanceof Float || val1 instanceof Float)
            return new jTrolog.terms.Double(val0.doubleValue()/val1.doubleValue());
        return Number.getIntegerNumber((long) (val0.doubleValue()/val1.doubleValue()));
    }

    public Term expression_integer_div_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return Number.getIntegerNumber(bt.evalExpression(engine, structIn).longValue() / bt.evalExpression(engine, listIn).longValue());
    }

    public Term expression_pow_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        double val = Math.pow(bt.evalExpression(engine, structIn).doubleValue(), bt.evalExpression(engine, listIn).doubleValue());
        return new jTrolog.terms.Double(val);
    }

    public Term expression_bitwise_shift_right_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return new Int(bt.evalExpression(engine, structIn).intValue() >> bt.evalExpression(engine, listIn).intValue());
    }

    public Term expression_bitwise_shift_left_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return new Int(bt.evalExpression(engine, structIn).intValue() << bt.evalExpression(engine, listIn).intValue());
    }

    public Term expression_bitwise_and_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return new Int(bt.evalExpression(engine, structIn).intValue() & bt.evalExpression(engine, listIn).intValue());
    }

    public Term expression_bitwise_or_2(BindingsTable bt, EvaluableTerm structIn, EvaluableTerm listIn) throws Throwable {
        return new Int(bt.evalExpression(engine, structIn).intValue() | bt.evalExpression(engine, listIn).intValue());
    }

    //
    // text/atom manipulation predicates
    //

    /**
     * bidirectional text/term conversion.
     */
    public boolean text_term_2(BindingsTable bt, Term structIn, Term listIn) {
        if (!ground_1(bt, structIn))
            return bt.unify(structIn, new StructAtom(listIn.toString()));
        Term result = new Parser(Parser.removeApices(structIn.toString()), engine).nextTerm(false);
        return bt.unify(listIn, result);
    }


    public boolean text_concat_3(BindingsTable bt, Term source1, Term source2, Term dest) {
        if (!atom_1(bt,source1) || !atom_1(bt,source2))
            return false;
        return bt.unify(dest, new StructAtom(((Struct) source1).name + ((Struct) source2).name));
    }

    public boolean num_atom_2(BindingsTable bt, Term structIn,Term listIn) {
        if (listIn instanceof Var) {
            if (structIn instanceof Number)
                return bt.unify(listIn, new StructAtom(structIn.toString()));
            return false;
        }
        if (!atom_1(bt,listIn))
            return false;
        Number num = Parser.parseNumber(Parser.removeApices(((Struct) listIn).name).trim());
        return bt.unify(structIn, num);
    }

    public String getTheory(){
        return
        //
        // operators defined by the BasicLibrary theory
        //
        "':-'(op( 1200, fx,   ':-')). \n"+
        ":- op( 1200, xfx,  ':-'). \n"+
        ":- op( 1200, fx,   '?-'). \n"+
        ":- op( 1100, xfy,  ';'). \n"+
        ":- op( 1050, xfy,  '->'). \n"+
        ":- op( 1000, xfy,  ','). \n"+
        ":- op(  900, fy,   '\\+'). \n"+
        ":- op(  900, fy,   'not'). \n"+
        //
        ":- op(  700, xfx,  '='). \n"+
        ":- op(  700, xfx,  '\\='). \n"+
        ":- op(  700, xfx,  '=='). \n"+
        ":- op(  700, xfx,  '\\=='). \n"+
        //
        ":- op(  700, xfx,  '@>'). \n"+
        ":- op(  700, xfx,  '@<'). \n"+
        ":- op(  700, xfx,  '@=<'). \n"+
        ":- op(  700, xfx,  '@>='). \n"+
        ":- op(  700, xfx,  '=:='). \n"+
        ":- op(  700, xfx,  '=\\='). \n"+
        ":- op(  700, xfx,  '>'). \n"+
        ":- op(  700, xfx,  '<'). \n"+
        ":- op(  700, xfx,  '=<'). \n"+
        ":- op(  700, xfx,  '>='). \n"+
        //
        ":- op(  700, xfx,  'is'). \n"+
        ":- op(  700, xfx,  '=..'). \n"+
        ":- op(  500, yfx,  '+'). \n"+
        ":- op(  500, yfx,  '-'). \n"+
        ":- op(  500, yfx,  '/\\'). \n"+
        ":- op(  500, yfx,  '\\/'). \n"+
        ":- op(  400, yfx,  '*'). \n"+
        ":- op(  400, yfx,  '/'). \n"+
        ":- op(  400, yfx,  '//'). \n"+
        ":- op(  400, yfx,  '>>'). \n"+
        ":- op(  400, yfx,  '<<'). \n"+
        ":- op(  400, yfx,  'rem'). \n" +
        ":- op(  400, yfx,  'mod'). \n" +
        ":- op(  200, xfx,  '**'). \n"+
        ":- op(  200, xfy,  '^'). \n"+
        ":- op(  200, fy,   '\\'). \n"+
        ":- op(  200, fy,   '-'). \n"+
        //
        // flag management
        //
        "current_prolog_flag(Name,Value) :- get_prolog_flag(Name,Value),!.\n"+
        "current_prolog_flag(Name,Value) :- flag_list(L), member(flag(Name,Value),L).\n"+
        //
        // expression/term comparison
        //
        "'=\\='(X,Y):- not expression_equality(X,Y). \n"+
        "'\\=='(X,Y):- not term_equality(X,Y).\n"+
        "'@>='(X,Y):- not term_less_than(X,Y).\n"+
        "'@=<'(X,Y):- not term_greater_than(X,Y).\n"+
        //
        // meta-predicates
        //
        "clause(H,B) :- var(H),!, '$instantiation_error'. " +
        "clause(H,B) :- number(H),!, '$type_error'(callable, H). " +
        "clause(H,B) :- number(B),!, '$type_error'(callable, B). " +
        "clause(H,B) :- '$find'(H,L), member((':-'(H,B)),L). " +

        "current_predicate(PI) :- PI = Name/Arity, " +
                                "'$all_dynamic_predicate_indicators'(Iterator), " +
                                "has_next(Iterator), " +
                                "'$current_pred_impl'(Name, Arity, Iterator). " +

        "has_next(Iterator) :- not('$has_next'(Iterator)), !, fail. " +
        "has_next(Iterator) :- true. " +
        "has_next(Iterator) :- has_next(Iterator). " +
        //
        "C -> T ; B :- C, !, T. \n" +
        "C -> T ; B :- !, B. \n" +
        "C -> T :- C, !, T. \n" +
        "A ; B :- A. \n                                                                                          "+
        "A ; B :- B. \n                                                                                          "+
        "unify_with_occurs_check(X,Y) :- X=Y.\n                                                                     "+ //todo, every check now has with_occurs_check, and with_occurs must be shut down manually..
        "current_op(Pri,Type,Name):-get_operators_list(L),member(op(Pri,Type,Name),L).\n                          "+
        "once(X) :- X.                                                                                  "+
        "repeat. \n                                                                                              " +
        "repeat        :- repeat. \n                                                                             " +
        "'\\+'(P):- not(P). \n                                                                                             "+
        "not(G) :- call(G),!,fail. \n                                                                     " +
        "not(_). \n                                                                                              " +
        //
        // All solutions predicates
        //
        "findall(Template, Goal, Instances) :- \n" +
            "new_record_key(Key), \n" +
            "findall_impl(Template, Goal, Key, L), \n" +
            "Instances = L. \n" +
        "findall_impl(Template, Goal, Key, _) :- \n" +
            "call(Goal), \n" +
            "copy(Template, CL), \n" +
            "record(Key, CL), \n" +
            "fail. \n" +
        "findall_impl(_, _, Key, Instances) :- " +
            "recorded(Key, Instances), " +
            "erase(Key). \n" +

        "bagof(Template, Goal, Instances) :- \n" +
            "free_variables_set(Goal, Template, Set), \n" +
            "Witness =.. [witness | Set], \n" +
            "iterated_goal_term(Goal, G), \n" +
            "findall(Witness + Template, G, S), \n" +
            "'$bagof_impl_a'(Witness, S, Instances). \n" +

        "'$bagof_impl_a'(_, [], _) :- !, fail. \n" +
        "'$bagof_impl_a'(WitnessIn, BigSet, Instances) :- " +
                "'$stripBagList'(WitnessIn, BigSet, Matches, RemainderSet, Variant), " +
                "'$bagof_impl_b'(WitnessIn, Instances, RemainderSet, Variant, Matches). " +

        "'$bagof_impl_b'(WitnessIn, Instances, _, Variant, Matches) :- WitnessIn = Variant, Instances = Matches. \n" +  //first success?
        "'$bagof_impl_b'(WitnessIn, Instances, RemainderSet, _, _) :- '$bagof_impl_a'(WitnessIn, RemainderSet, Instances). \n" +  //get the next from remainding bag
//
//
//
//      "bagof(Template, Goal, Instances) :- " +
//            "free_variables_set(Goal, Template, Set), " +
//            "Witness =.. [witness | Set], " +
//            "iterated_goal_term(Goal, G), " +
//            "findall(Witness + Template, G, S), " +
//            "new_record_key(SetKey), " +
//            "record(SetKey, S), " +
//            "bagof_impl_x(Witness, SetKey, Instances). " +
//
//        "bagof_impl_x(_, SetKey, _) :- " +
//                "recorded(SetKey, []), " +
//                "!, " +
//                "fail. \n" +
//        "bagof_impl_x(Witness, SetKey, Instances) :- " +
//                "recorded(SetKey, List), " +
//                "split_list(Witness, List, Instances, Rest), " +
//                "erase(SetKey), " +
//                "record(SetKey, Rest). \n" +
//        "bagof_impl_x(Witness, SetKey, Instances) :- bagof_impl_x(Witness, SetKey, Instances). " +
//
//        "split_list(Witness, [WW + TT | List], [TT | Instances], Rest) :- variant2(Witness, WW), !, split_list(Witness, List, Instances, Rest). " +
//        "split_list(Witness, [H | List], Instances, [H | Rest]) :- split_list(Witness, List, Instances, Rest). " +
//        "split_list(_, [], [], []). " +
//
        "setof(Template, Goal, Instances) :- \n" +
            "bagof(Template, Goal, List), \n" +
            "quicksort(List, '@<', OrderedList), \n" +
            "no_duplicates(OrderedList, Instances). \n" +

//        "free_variables_set(T, V, FV) :- \n" +
//            "variable_set(T, VST), \n" +
//            "existential_variables(T, EVST), \n" +
//            "variable_set(V, VSV), \n" +
//            "list_remove(VST, EVST, V1), " +
//            "list_remove(V1, VSV, V2), " +
//            "FV = V2. \n" +
//
//        "list_remove([],L2,[]). \n                                                                                    " +
//        "list_remove([E|T1], L2, L3):- member(E, L2), !, list_remove(T1,L2,L3). \n                                                         " +
//        "list_remove([E|T1], L2, [E|T3]):- list_remove(T1,L2,T3). \n                                                         " +
//
        "free_variables_set(Term, WithRespectTo, Set) :- \n" +
            "variable_set(Term, VS), \n" +
            "variable_set(WithRespectTo, VS1), \n" +
            "existential_variables(Term, EVS1), \n" +
            "'$list_diff'(VS, VS1, T), \n" +
            "'$list_diff'(T, EVS1, T2), \n" +
            "Set =T2. \n" +

        "existential_variables(Term, []) :- var(Term), !. \n" +
        "existential_variables(Term, []) :- atomic(Term), !. \n" +
        "existential_variables(V ^ G, EVS) :- variable_set(V, VS), \n" +
            "existential_variables(G, ExistentialVars), \n" +
            "append(VS, ExistentialVars, EVS). \n" +
        "existential_variables(_, []). \n" +

        "iterated_goal_term(_ ^ SubGoal, Goal) :- iterated_goal_term(SubGoal, Goal). \n" +
        "iterated_goal_term(G, G). \n" +

                //intersect not tested
        "intersect([H|A],B,[H|C]) :- member(H,B), !, intersect(A,B,C). " +
        "intersect([H|T],B,C) :- intersect(T,B,C). " +
        "intersect([],[],[]). " +

        "diff(A,B,C) :- diff_impl(A,B,A2,C). " +
        "diff_impl([H|A],B,A2,C) :- member(H,B), !, diff_impl(A,B,A2,C). " +
        "diff_impl([H|A],B,A2,[H|C]) :- diff_impl(A,B,A2,C). " +
        "diff_impl([],[H|B],A2,C) :- member(H,A2), !, diff_impl([],B,A2,C). " +
        "diff_impl([],[H|B],A2,[H|C]) :- diff_impl([],B,A2,C). " +
        "diff_impl([],[],[],[]). " +

        "no_duplicates([], []). \n" +
        "no_duplicates([H | T], L) :- member(H, T), !, no_duplicates(T, L). \n" +
        "no_duplicates([H | T], [H | L]) :- no_duplicates(T, L). \n" +
        //
        // theory management predicates
        //
        "retract(Rule) :- Rule = ':-'(Head, Body), !, clause(Head, Body), '$retract'(Rule). \n" +
        "retract(Fact) :- clause(Fact, true), '$retract'(Fact). \n" +

        "retractall(Head) :- findall(Head, clause(Head, _), L), '$retract_clause_list'(L), !. \n" +
        "'$retract_clause_list'([]). \n" +
        "'$retract_clause_list'([E | T]) :- !, '$retract'(E), '$retract_clause_list'(T). \n" +
        //
        // auxiliary predicates
        //
        "member(E,[E|_]). \n                                                                                     " +
        "member(E,[_|L]):- member(E,L). \n                                                                       " +
        "length(L, S) :- number(S), S >= 0, !, lengthN(L, S), !. \n" +
        "length(L, S) :- var(S), lengthX(L, S). \n" +
        "lengthN([],0). \n" +
        "lengthN(_, N) :- nonvar(N), N < 0, !, fail. \n" +
        "lengthN([_|L], N) :- lengthN(L,M), N is M + 1. \n" +
        "lengthX([],0). \n" +
        "lengthX([_|L], N) :- lengthX(L,M), N is M + 1. \n" +
        "append([],L2,L2). \n                                                                                    " +
        "append([E|T1],L2,[E|T2]):- append(T1,L2,T2). \n                                                         " +
        "reverse(L1,L2):- reverse0(L1,[],L2). \n                                                                 " +
        "reverse0([],Acc,Acc). \n                                                                                " +
        "reverse0([H|T],Acc,Y):- reverse0(T,[H|Acc],Y). \n                                                       " +
        "delete(E,[],[]). \n                                                                                     " +
        "delete2(E,[E|T],L):- !,delete(E,T,L). \n                                                                 " +
        "delete(E,[H|T],[H|L]):- delete2(E,T,L). \n                                                               " +
        "element(1,[E|L],E):- !. \n                                                                              " +
        "element(N,_,_):- N < 0, !, fail. \n                                                                     " +
        "element(N,[_|L],E):- M is N - 1,element(M,L,E). \n                                                      " +

        "quicksort([],Pred,[]).                             \n" +
        "quicksort([X|Tail],Pred,Sorted):-                  \n"+
        "   split(X,Tail,Pred,Small,Big),                   \n"+
        "   quicksort(Small,Pred,SortedSmall),              \n"+
        "   quicksort(Big,Pred,SortedBig),                  \n"+
        "   append(SortedSmall,[X|SortedBig],Sorted).       \n"+
        "split(_,[],_,[],[]).                               \n"+
        "split(X,[Y|Tail],Pred,Small,[Y|Big]):-             \n"+
        "   Predicate =..[Pred,X,Y],                        \n"+
        "   call(Predicate),!,                              \n"+
        "   split(X,Tail,Pred,Small,Big).                   \n"+
        "split(X,[Y|Tail],Pred,[Y|Small],Big):-             \n"+
        "   split(X,Tail,Pred,Small,Big).                   \n";
    }

    // Internal Java predicates which are part of the bagof/3 and setof/3 algorithm

    public boolean $has_next_1(BindingsTable bt, IteratorAsTerm iterator) {
        return iterator.hasNext();
    }

    public boolean $all_dynamic_predicate_indicators_1(BindingsTable bt, Term iterator) throws PrologException {
        if (iterator instanceof Var){
            Term iteratorTerm = new IteratorAsTerm(engine.dynamicPredicateIndicators());
            return bt.unify(iterator, iteratorTerm);
        }
        if (iterator instanceof IteratorAsTerm)
            return ((IteratorAsTerm) iterator).hasNext();
        throw new PrologException("$all_dynamic_predicate_indicators has a bug. contact ivar.");
    }

    public boolean $current_pred_impl_3(BindingsTable bt, Term name, Term arity, IteratorAsTerm allDynamicPIs) {
        String nextDynamicPI = (String) allDynamicPIs.next();
        String[] pred = nextDynamicPI.split("/");
        if (!bt.unify(name, new StructAtom(pred[0])))
            return false;
        return bt.unify(arity, new Int(pred[1]));
    }

//        "variable_set(T, [T]) :- var(T), !. \n" +
//        "variable_set(T, []) :- atomic(T), !. \n" +
//        "variable_set(T, List) :- T =.. [_|Args], variable_set_arguments(Args, List). \n" +
//
//        "variable_set_arguments([], []). \n" +
//        "variable_set_arguments([H|T], List) :- " +
//                "variable_set(H, SubList1), " +
//                "variable_set_arguments(T, SubList2), " +
//                "append(SubList1, SubList2, List). \n" +
    public boolean variable_set_2(BindingsTable bt, Term withVars, Term varList) {
        if (withVars instanceof Number ||withVars instanceof StructAtom)
            return bt.unify(Term.emptyList, varList);
        LinkedList l = new LinkedList();
        if (withVars instanceof Var)
            l.add(withVars);
        else
            l.addAll(Arrays.asList(((Struct) withVars).getVarList()));
        l.add(Term.emptyList);
        return bt.unify(bt.createStructList(l), varList);
    }


    //internal variable / data base map
    private HashMap map = new HashMap();

    public boolean new_record_key_1(BindingsTable bt, Var key) {
        Long newKey = new Long(key.hashCode());
        return bt.unify(key, newKey);
    }

    public boolean record_2(BindingsTable bt, Number key, Term unit) {
        LinkedList appendStorage = (LinkedList) map.get(key.toString());
        if (appendStorage == null)
            appendStorage = new LinkedList();
        appendStorage.add(BindingsTable.unWrap(unit));
        map.put(key.toString(), appendStorage);
        return true;
    }

    public boolean erase_2(BindingsTable bt, Number key, Term list) {
        LinkedList appendStorage = (LinkedList) map.remove(key.toString());
        if (appendStorage == null)
            return bt.unify(list, Term.emptyList);
        appendStorage.add(Term.emptyList);
        return bt.unify(list, bt.createStructList(appendStorage));
    }

    public boolean recorded_2(BindingsTable bt, Number key, Term list) {
        LinkedList appendStorage = (LinkedList) map.get(key.toString());
        if (appendStorage == null)
            return bt.unify(list, Term.emptyList);
        appendStorage.add(Term.emptyList);
        return bt.unify(list, bt.createStructList(appendStorage));
    }

    public boolean erase_1(BindingsTable bt, Number key) {
        map.remove(key.toString());
        return true;
    }
    //internal variable / data base map

    public boolean $stripBagList_5(BindingsTable bt, Struct witnessIn, Struct a_and_bSet, Term aSet, Term bSet, Term variant) throws PrologException {
        LinkedList results = new LinkedList();
        LinkedList remains = new LinkedList();

        int wrapID = witnessIn instanceof WrapStruct ? ((WrapStruct) witnessIn).context : bt.getUniqueExecutionCtxID();
        witnessIn = (Struct) BindingsTable.wrapWithID(bt.variant(witnessIn), wrapID);

        //1a. get a variant of the first witness+template from the inputList
        Iterator it = bt.structListIterator(a_and_bSet, true);
        Struct w_t = (Struct) it.next();

        //1b. the witness branch of the first result will be used to match other results with identical witnesses
        Struct witness1 = (Struct) variant(witnessIn, w_t.getArg(0));
        bt.unify(w_t.getArg(0), witness1);


        //1c. unify first witness with Variant output and add to result
        bt.unify(variant, witness1);
        results.add(BindingsTable.unWrap(w_t.getArg(1)));

        //2. iterate the rest of the input list and
        //   put all matching witnesses from bigset into result and the rest in remainder
        while ( it.hasNext()) {
            w_t = (Struct) it.next();
            Struct witness2 = (Struct) variant(witness1, w_t.getArg(0));
            if (term_equality_2(null, witness1, witness2)){
                bt.unify(w_t.getArg(0), witness1);
                results.add(BindingsTable.unWrap(w_t.getArg(1)));
            } else {
                remains.add(BindingsTable.unWrap(w_t));
            }
        }

        //3. unify the results lists and remains list with aSet and bSet
        if (remains.isEmpty()) {
            bt.unify(bSet, Term.emptyList);
        } else {
            remains.add(Term.emptyList);
            bt.unify(bSet, BindingsTable.wrapWithID(Parser.createStructList(remains), wrapID));
        }
        results.add(Term.emptyList);
        return bt.unify(aSet, BindingsTable.wrapWithID(Parser.createStructList(results), wrapID));
    }

    //todo not finished.. Just a hack to see if things roughly work
    //todo bug in the bag_of predicate..
    //todo I think the fault lies with variant..
    //todo I think variant constructs a Struct with Variables from various contexts..
    private static Term variant(Term former, Term latter) {
        if (BasicLibrary.atomic_1(null, latter))
            return latter;
        if (latter instanceof Var){
            if (former instanceof Var)
                return former;
            throw new RuntimeException("h");
        }
        if (latter instanceof Struct) {
            if (former instanceof Var)
                return latter;
            if (!(former instanceof Struct))
                throw new RuntimeException("h2");
            final Struct sFormer = (Struct) former;
            final Struct sLatter = (Struct) latter;
            if (sFormer.predicateIndicator != sLatter.predicateIndicator)
                throw new RuntimeException("h3");
            Term[] newChildren = new Term[sLatter.arity];
            for (int i = 0; i < sFormer.arity; i++)
                newChildren[i] = variant(BindingsTable.unWrap(sFormer.getArg(i)), BindingsTable.unWrap(sLatter.getArg(i))); //todo this is just a hack. I don't think it works at all, this needs to be fixed
            return BindingsTable.wrapWithID(new Struct(sLatter.name, newChildren), ((WrapStruct) former).context); //todo bug in context setting here..
        }
        throw new RuntimeException("h4");
    }

    public boolean $list_diff_3(BindingsTable bt, Struct main, Struct retract, Var diff) throws PrologException {
        if (retract.equals(Term.emptyList))
            return bt.unify(diff, main);
        LinkedList resultList = new LinkedList();
        outer : for (Iterator it = bt.structListIterator(main, true); it.hasNext();) {
            Term child = (Term) it.next();
            for (Iterator it2 = bt.structListIterator(retract, true); it2.hasNext();) {
                Term child2 = (Term) it2.next();
                if (BasicLibrary.term_equality_2(null,child, child2))
                    continue outer;
            }
            resultList.add(child);
        }
        if (resultList.isEmpty())
            return bt.unify(diff, Term.emptyList);
        resultList.add(Term.emptyList);
        return bt.unify(diff, bt.createStructList(resultList));
    }


    /**
     * Defines a map for synonyms for primitives.
     * String primitive name -> String[]{synonym name, another synonym name, ..}.
     */
    public String[] getSynonym(String primitive) {
        if (primitive.equals("expression_plus"))
            return new String[]{"+"};
        if (primitive.equals("expression_minus"))
            return new String[]{"-"};
        if (primitive.equals("expression_multiply"))
            return new String[]{"*"};
        if (primitive.equals("expression_div"))
            return new String[]{"/"};
        if (primitive.equals("expression_pow"))
            return new String[]{"**"};
        if (primitive.equals("expression_bitwise_shift_right"))
            return new String[]{">>"};
        if (primitive.equals("expression_bitwise_shift_left"))
            return new String[]{"<<"};
        if (primitive.equals("expression_bitwise_and"))
            return new String[]{"/\\"};
        if (primitive.equals("expression_bitwise_or"))
            return new String[]{"\\/"};
        if (primitive.equals("expression_integer_div"))
            return new String[]{"//"};
        if (primitive.equals("expression_bitwise_not"))
            return new String[]{"\\"};
        if (primitive.equals("$functor"))
            return new String[]{"functor"};
        if (primitive.equals("$arg"))
            return new String[]{"arg"};
        if (primitive.equals("$tofromlist"))
            return new String[]{"=.."};
        if (primitive.equals("expression_equality"))
            return new String[]{"=:="};
        if (primitive.equals("expression_greater_than"))
            return new String[]{">"};
        if (primitive.equals("expression_less_than"))
            return new String[]{"<"};
        if (primitive.equals("expression_greater_or_equal_than"))
            return new String[]{">="};
        if (primitive.equals("expression_less_or_equal_than"))
            return new String[]{"=<"};
        if (primitive.equals("term_equality"))
            return new String[]{"=="};
        if (primitive.equals("term_greater_than"))
            return new String[]{"@>"};
        if (primitive.equals("term_less_than"))
            return new String[]{"@<"};
        return null;
    }

}