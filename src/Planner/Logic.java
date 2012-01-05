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
import java.util.Map;
import java.util.StringTokenizer;
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

    public Solution eval(Solution solution) {
        Map bindings = solution.getBindings();
        for(Object o : bindings.keySet()) {
            char[] array = bindings.get(o).toString().toCharArray();
            String newBinding = bindings.get(o).toString();
            //System.out.println("Trying for: " + bindings.get(o).toString());
            for(int i = 0; i < array.length; i++) {
                int num1;
                String num1Buff = "";
                int num2;
                String num2Buff = "";
                int replacer = 0;
                
                if(array[i] == '-') {
                    // Collect first number
                    int j = i - 1;
                    while((array[j] == ' ') || (array[j] == '0') || (array[j] == '1') || (array[j] == '2') || (array[j] == '3') || (array[j] == '4') || (array[j] == '5') || (array[j] == '6') || (array[j] == '7') || (array[j] == '8') || (array[j] == '9')) {
                        if((array[j] != ' ')) { 
                            num1Buff += array[j];
                        }
                        array[j] = ' ';
                        j = j - 1;
                    }
                    // Collect second number
                    j = i + 1;
                    while((array[j] == ' ') || (array[j] == '0') || (array[j] == '1') || (array[j] == '2') || (array[j] == '3') || (array[j] == '4') || (array[j] == '5') || (array[j] == '6') || (array[j] == '7') || (array[j] == '8') || (array[j] == '9')) {
                        if((array[j] != ' ')) { 
                            num2Buff += array[j];
                        }
                        array[j] = ' ';
                        j = j + 1;
                    }
                    //System.err.println("For: " + bindings.get(o).toString() + "    - 1:" + num1Buff + "  2: " + num2Buff);
                    if(num1Buff.equals("") || num2Buff.equals("")) {
                        continue;
                    }
                    num1 = Integer.parseInt(num1Buff);
                    num2 = Integer.parseInt(num2Buff);
                    replacer = num1 - num2;
                    String repString = "" + replacer;
                    for(int k = 0; k < repString.length(); k++) {
                        array[i+k] = repString.charAt(k);
                    }
                    //System.out.println("MINUS FOUND -- " + num1Buff + " - " + num2Buff + "  = " + repString);
                    newBinding = String.copyValueOf(array).replaceAll("  ", "");
                    newBinding = newBinding.replace(", ", ",");
                    newBinding = newBinding.replace(" ,", ",");
                }
                
                if(array[i] == '+') {
                    // Collect first number
                    int j = i - 1;
                    while((array[j] == ' ') || (array[j] == '1') || (array[j] == '2') || (array[j] == '3') || (array[j] == '4') || (array[j] == '5') || (array[j] == '6') || (array[j] == '7') || (array[j] == '8') || (array[j] == '9')) {
                        if((array[j] != ' ')) { 
                            num1Buff += array[j];
                        }
                        array[j] = ' ';
                        j = j - 1;
                    }
                    // Collect second number
                    j = i + 1;
                    while((array[j] == ' ') || (array[j] == '1') || (array[j] == '2') || (array[j] == '3') || (array[j] == '4') || (array[j] == '5') || (array[j] == '6') || (array[j] == '7') || (array[j] == '8') || (array[j] == '9')) {
                        if((array[j] != ' ')) { 
                            num2Buff += array[j];
                        }
                        array[j] = ' ';
                        j = j + 1;
                    }
                    //System.err.println("1:" + num1Buff + "  2: " + num2Buff);
                    if(num1Buff.equals("") || num2Buff.equals("")) {
                        continue;
                    }else{
                        num1 = Integer.parseInt(num1Buff);
                        num2 = Integer.parseInt(num2Buff);
                        replacer = num1 + num2;
                        String repString = "" + replacer;
                        for(int k = 0; k < repString.length(); k++) {
                            array[i+k] = repString.charAt(k);
                        }
                        //System.out.println("PLUS FOUND -- " + num1Buff + " + " + num2Buff + " = " + repString);
                        newBinding = String.copyValueOf(array).replaceAll("  ", "");
                        newBinding = newBinding.replace(", ", ",");
                        newBinding = newBinding.replace(" ,", ",");
                    }
                }
            }
            //System.out.println("Inserting: " + newBinding + "\n");
            newBinding = newBinding.trim();
            if(newBinding.endsWith(".")) {
                newBinding = newBinding.substring(0, newBinding.length()-1);
            }
            newBinding = newBinding.trim();
            bindings.put(o, (Term) parse(newBinding + ". "));
        }
        return solution;
    }
    
    public Solution solve(String q) {
        q = q.trim();
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q = q.trim();
        try {
            Solution info = engine.solve(q + ". ");
            return info; //eval(info);
        }
        catch (PrologException e) {
            try {
                return engine.solve("fail.");
            } catch (Throwable ex) {
                Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        
        return null;
    }

    public boolean solveboolean(String q) {
        q = q.trim();
        q = q.replace("\\+", "!");
        
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q = q.trim();
        if (Character.toString(q.charAt(0)).equals("!")) {
            try {
                q = q.substring(1, q.length());
                Solution info = engine.solve(q + ". ");
                //System.out.println("         !--> succes for: " + q + "  ? " + !info.success());
                System.out.println("      Solve NOT Bool: " + q + " success: " + info.success());
                return !info.success();
            } catch (Throwable ex) {
                return true;
                //Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                Solution info = engine.solve(q + ". ");
                //System.out.println("         --> succes for: " + q + "  ? " + !info.success());
                //System.out.println("      Solve Bool: " + q + " success: " + info.success());
                return info.success();
            } catch (Throwable ex) {
                return false;
                //Logger.getLogger(Logic.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList<Solution> solveAll(String q) {
        //System.err.println("Gotten: " + q);
        boolean not = false;
        q = q.trim();
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
            //System.err.println("Removed last element: " + q);
        }
        q = q.trim();
        if (Character.toString(q.charAt(0)).equals("!")) {
            not = true;
            q = q.substring(1, q.length());
        }
        ArrayList<Solution> infos = new ArrayList<Solution>();
        try {
            //System.err.println(q);
            Solution info = engine.solve(q + ".");
            if(not) {
                if(info.success()) {
                    infos.add(engine.solve("fail."));
                }else{
                    infos.add(engine.solve("true."));
                }
            }else{
                infos.add(info); // eval(info)
            }
            boolean done = false;
            while(!done){
                try {
                    Solution infoNext = engine.solveNext();
                    
                    if(not) {
                        if(infoNext.success()) {
                            infos.add(engine.solve("fail."));
                        }else{
                            infos.add(engine.solve("true."));
                        }
                    }else{
                        if(infoNext.success()) {
                            infos.add(infoNext); //(eval(infoNext)
                        }
                    }
                } catch(Exception e) {
                    done = true;
                }
            }
        }
        catch (Throwable ex) {
            //System.out.println("msg: " + ex.toString());
            //ex.printStackTrace();
        }
        
        //System.err.println("Infos: " + infos.toString());
        return infos;
    }

    public void set(String q) throws Throwable {
        q = q.trim();
        if(q.endsWith(".")) {
            q = q.substring(0, q.length()-1);
        }
        q = q.trim();
        
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
            //System.err.println("Exception when handling: " + q + ". Malformed predicate");
            //e.printStackTrace();
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

