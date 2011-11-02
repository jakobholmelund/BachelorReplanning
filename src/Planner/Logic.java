/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Planner;

import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;
import jTrolog.lib.*;
import jTrolog.engine.*;
import jTrolog.terms.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Logic {

    public Prolog engine;

    public Logic(String theory) throws PrologException {
        engine = new Prolog();
        engine.addTheory(theory);
    }

    public Logic() throws PrologException {
        engine = new Prolog();
    }

    public String getTheoryAsString() {
        return engine.getTheory().toString();
    }

    public void setTheory(String theory) {
        try {
            engine.clearTheory();
            engine.addTheory(theory);
        } catch (PrologException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Solution solve(String q) {
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q.trim();
        try {
            Solution info = engine.solve(q + ". ");
            return info;
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public boolean solveboolean(String q) {
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q.trim();
        try {
            Solution info = engine.solve(q + ". ");
            return info.success();
        } catch (Throwable ex) {
            //Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    public ArrayList<Solution> solveAll(String q) {
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q.trim();

        ArrayList<Solution> infos = new ArrayList<Solution>();
        try {
            Solution info = engine.solve(q + ". ");
            infos.add(info);
            boolean done = false;
            while(!done){
                try {
                    Solution infoNext = engine.solveNext();
                    if(infoNext.success()) {
                        infos.add(infoNext);
                    }
                } catch(Exception e) {
                    done = true;
                }
            }
        } catch (Throwable ex) {
            //Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return infos;
    }

    public void set(String q) throws Throwable {
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q.trim();

        try {
            if (Character.toString(q.charAt(0)).equals("!")) {
                not(q.substring(1));
            } else {
                /*System.out.println("3: " + engine.solve("fact(1).").success());
                System.out.println("4: " + engine.solve("fact(A)."));
                System.out.println("5: " + engine.solveNext());
                System.out.println("5: " + engine.solveNext());*/
                Solution info = engine.solve(q + ".");
                if (!info.success()) {
                    Struct gotten = parse(q + ".");
                    Clause clause = BuiltIn.convertTermToClause(gotten);
                    engine.assertA(clause);
                } else {
                    // Do Nothing - predicate already asserted.
                }
            }
        } catch (Exception e) {
            System.err.println("Exception when handling: " + q + ". Malformed predicate");
        }
    }

    private void not(String q) throws Throwable {
        try {
            Struct gotten = parse(q + ".");
            Clause clause = BuiltIn.convertTermToClause(gotten);
            engine.retract(clause.original);
        } catch (Exception e) {
            System.err.println("Exception when handling: !" + q + ". Malformed predicate");
        }
    }
    
    public Struct parse(String st) {
        Parser p = new Parser(st, engine);
        //System.out.println("parser: " + p.toString());
        Term t = p.nextTerm(true);
        //System.out.println("Term: " + t.toString());
        if (!(t instanceof Struct)) {
            System.out.println("it's a struct!");
        }       //Var or Number is considered true, since they are not false?
        //return new Solution(t);
        Struct g = (Struct) t;
        //System.out.println("g: " + g.toString());
        return g;
    }

    @Override
    public Logic clone() {
        Logic logic;
        try {
            String theory = this.engine.getTheory();
            long time1 = System.currentTimeMillis();
            logic = new Logic(theory);
            //logic = new Logic();
            //logic.engine = this.engine.clone();
            long time2 = System.currentTimeMillis();
            //System.out.println("Cloned in: " + (time2 - time1) + " ms. Length: \n" + theory.length());
            
            return logic;
        } catch (PrologException ex) {
            Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

