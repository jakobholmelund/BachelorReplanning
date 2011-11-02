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

import jTrolog.engine.Prolog;
import jTrolog.terms.Term;

import java.io.Serializable;

/**
 *
 * This abstract class is the base class for developing
 * built-in libraries, which can be dynamically
 * loaded by prolog objects.
 * <p>
 * Each library can expose to engine:
 * <ul>
 * <li> a theory (as a string assigned to theory field)
 * <li> primitives: methods whose signature is
 *       boolean|void|Term  name_arity(BindingsTable bt, Term arg0, Term arg1, ...)
 *      is considered a built-in primitive provided by the library
 * <li> synonyms for primitives
 * </ul>
 */
public abstract class Library implements Serializable {

    /** prolog core which loaded the library */
    protected Prolog engine;

    /**
     * @return the library name (Library class name is default)
     */
    public String getName() {
        return getClass().getName();
    }

    /**
     * @return the theory provided with the library (an empty theory is default).
     */
    public String getTheory() {
        return "";
    }

    /**
     * Defines a map for synonyms for primitives.
     * String primitive name -> String[]{synonym name, another synonym name, ..}.
     */
    public String[] getSynonym(String primitiveName) {
        return null;
    }

    public void setEngine(Prolog en) {
        engine = en;
    }

    /**
     * method invoked by prolog engine when library is
     * going to be removed
     */
    public void dismiss() {}

    /**
     * method invoked when the engine is going
     * to demonstrate a goal
     */
    public void onSolveBegin(Term goal) {}

    /**
     * method invoked when the engine has
     * finished a demostration
     */
    public void onSolveEnd() {}
}