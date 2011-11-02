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
package jTrolog.lib;

import jTrolog.engine.*;
import jTrolog.terms.Number;
import jTrolog.terms.*;
import jTrolog.terms.Float;
import jTrolog.parser.Parser;
import jTrolog.errors.PrologException;

import java.util.Iterator;

/**
 * This class represents a tuProlog library providing most of the built-ins
 * predicates and functors defined by ISO standard.
 *
 * Library/Theory dependency:  BasicLibrary
 *
 *
 *
 */
public class ISOLibrary extends Library {

    public boolean atom_length_2(BindingsTable bt, Struct arg, Term len) {
        if (!BasicLibrary.atom_1(bt,arg))
            return false;
        return bt.unify(len, new Int(arg.name.length()));
    }


    public boolean atom_chars_2(BindingsTable bt, Term arg0, Term arg1) throws PrologException {
        if (arg0 instanceof Var) {
            if (!(arg1 instanceof Struct))
                return false;

            Struct list = (Struct) arg1;
            if (list.equals(Term.emptyList))
                return bt.unify(arg0, new StructAtom(""));
            StringBuffer res = new StringBuffer();
            for (Iterator it = bt.structListIterator(list, true); it.hasNext();) {
                String character = it.next().toString();
                if (character.startsWith("'") && character.endsWith("'") && character.length()!=1)
                    character = character.substring(1,character.length()-1);
                res.append(character);
            }
            return bt.unify(arg0, new StructAtom(res.toString()));
        } else {
            if (!BasicLibrary.atom_1(bt,arg0))
                return false;
            Struct list = Parser.stringToStructList(((Struct) arg0).name);
            return bt.unify(arg1, bt.wrapWithID(list));
        }
    }



	public boolean char_code_2(BindingsTable bt, Term arg0, Term arg1) {
		if (arg1 instanceof Var) {
			if (BasicLibrary.atom_1(bt,arg0)) {
                String st = ((Struct) arg0).name;
				if (st.length() <= 1)
                    return bt.unify(arg1, new Int(st.charAt(0)));
			}
		} else if (arg1 instanceof Int) {
			char c = (char) ((jTrolog.terms.Number) arg1).intValue();
            return bt.unify(arg0, new StructAtom("" + c));
		}
		return false;
	}

	//

	// functors

	public Term sin_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.sin(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term cos_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.cos(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term exp_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.exp((bt.evalExpression(engine, val)).doubleValue()));
    }

	public Term atan_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.atan(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term log_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.log(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term sqrt_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double(Math.sqrt(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term abs_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        Number val0 = bt.evalExpression(engine, val);
        if (val0 instanceof Float)
            return new jTrolog.terms.Double(Math.abs(val0.doubleValue()));
        return new jTrolog.terms.Int(Math.abs(val0.intValue()));
    }

	public Term sign_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        Number val0 = bt.evalExpression(engine, val);
        if (val0 instanceof Float)
            return new jTrolog.terms.Double(val0.doubleValue()>0 ? 1.0: -1.0);
        return new jTrolog.terms.Double(val0.intValue()>0 ? 1.0: -1.0);
    }

	public Term float_integer_part_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Double((long)Math.rint(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term float_fractional_part_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        double fl = bt.evalExpression(engine, val).doubleValue();
        return new jTrolog.terms.Double(Math.abs(fl-Math.rint(fl)));
    }

//	public boolean float_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
//        return engine.evalExpression(bt, val).isReal();
//    }

	public Term floor_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new Int((int) Math.floor(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term round_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new jTrolog.terms.Long(Math.round(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term truncate_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new Int((int) Math.rint(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term ceiling_1(BindingsTable bt, EvaluableTerm val) throws Throwable {
        return new Int((int) Math.ceil(bt.evalExpression(engine, val).doubleValue()));
    }

	public Term div_2(BindingsTable bt, EvaluableTerm v0, EvaluableTerm v1) throws Throwable {
        return new jTrolog.terms.Int(bt.evalExpression(engine, v0).intValue()/bt.evalExpression(engine, v1).intValue());
    }

	public Term mod_2(BindingsTable bt, EvaluableTerm v0, EvaluableTerm v1) throws Throwable {
        return new Int(bt.evalExpression(engine, v0).intValue() % bt.evalExpression(engine, v1).intValue());
    }

	public Term rem_2(BindingsTable bt, EvaluableTerm v0, EvaluableTerm v1) throws Throwable {
        return new jTrolog.terms.Double(Math.IEEEremainder(bt.evalExpression(engine, v0).doubleValue(),bt.evalExpression(engine, v1).doubleValue()));
    }

	/**
	 * library theory
	 */
	public String getTheory(){
		return
		//
		// operators defined by the ISOLibrary theory
		//
		":- op(  300, yfx,  'div'). \n"+
		":- op(  400, yfx,  'mod'). \n"+
		":- op(  400, yfx,  'rem'). \n"+
		":- op(  200, fx,   'sin'). \n"+
		":- op(  200, fx,   'cos'). \n"+
		":- op(  200, fx,   'sqrt'). \n"+
		":- op(  200, fx,   'atan'). \n"+
		":- op(  200, fx,   'exp'). \n"+
		":- op(  200, fx,   'log'). \n"+
		//
		// flags defined by the ISOLibrary theory
		//
		":- flag(bounded, [true,false], true, false).\n"+
		":- flag(max_integer, ["+ Integer.toString(Integer.MAX_VALUE) +"], "+Integer.toString(Integer.MAX_VALUE)+",false).\n"+
		":- flag(min_integer, ["+ Integer.toString(Integer.MIN_VALUE) +"], "+ Integer.toString(Integer.MIN_VALUE) +",false).\n"+
		":- flag(integer_rounding_function, [up,down], down, false).\n"+
		":- flag(char_conversion,[on,off],off,false).\n"+
		":- flag(debug,[on,off],off,false).\n"+
		":- flag(max_arity, ["+ Integer.toString(Integer.MAX_VALUE) +"], "+ Integer.toString(Integer.MAX_VALUE) +",false).\n"+
		":- flag(undefined_predicate, [error,fail,warning], fail, false).\n"+
		":- flag(double_quotes, [atom,chars,codes], atom, true).\n"+
		//
		//
		"unbound(X):-not bound(X).\n                                                                          "+
		//
		"atom_concat(F,S,R) :- atom_chars(F,FL),atom_chars(S,SL),!,append(FL,SL,RS),atom_chars(R,RS).\n          " +
		"atom_concat(F,S,R) :- atom_chars(R,RS),append(FL,SL,RS),atom_chars(F,FL),atom_chars(S,SL).\n            " +
		"atom_codes(A,L):-atom_chars(A,L1),!,chars_codes(L1,L).\n"+
		"atom_codes(A,L):-chars_codes(L1,L),atom_chars(A,L1).\n"+
		"chars_codes([],[]).\n"+
		"chars_codes([X|L1],[Y|L2]):-char_code(X,Y),chars_codes(L1,L2).\n"+
		"sub_atom(Atom,B,L,A,Sub):-atom_chars(Atom,L1),atom_chars(Sub,L2),!,sub_list(L2,L1,B),length(L2,L), length(L1,Len), A is Len - (B+L).\n"+
		"sub_atom(Atom,B,L,A,Sub):-atom_chars(Atom,L1),sub_list(L2,L1,B),atom_chars(Sub,L2),length(L2,L), length(L1,Len), A is Len - (B+L).\n"+
		"sub_list([],_,0).\n"+
		"sub_list([X|L1],[X|L2],0):- sub_list_seq(L1,L2).\n"+
		"sub_list(L1,[_|L2],N):- sub_list(L1,L2,M), N is M + 1.\n"+
		"sub_list_seq([],L).\n"+
		"sub_list_seq([X|L1],[X|L2]):-sub_list_seq(L1,L2).\n"+
		"number_chars(Number,List):-num_atom(Number,Struct),atom_chars(Struct,List),!.\n"+
		"number_chars(Number,List):-atom_chars(Struct,List),num_atom(Number,Struct).\n"+
		"number_codes(Number,List):-num_atom(Number,Struct),atom_codes(Struct,List),!.\n"+
		"number_codes(Number,List):-atom_codes(Struct,List),num_atom(Number,Struct).\n";
        //
		// ISO default
		//"current_prolog_flag(changeable_flags,[ char_conversion(on,off), debug(on,off), undefined_predicate(error,fail,warning),double_quotes(chars,codes,atom) ]).\n"+
		//"current_prolog_flag(changeable_flags,[]).\n                                                              "+
	}

    public String[] getSynonym(String primitiveName) {
        if (primitiveName.equals("ground"))
            return new String[]{"bound"};
        return null;
    }

}
