/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner;

import Planner.forward.Actions;
import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import jTrolog.terms.Term;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Dan True
 */
public class ActionStruct {
    String name;
    String prerequsite;
    String format;
    ArrayList<String> args;
    ArrayList<String> effects;
    ArrayList<String> requirements;
    
    public ActionStruct(String name, String prerequsite, String format, ArrayList<String> args, ArrayList<String> effects, ArrayList<String> requirements) {
        this.name = name.toLowerCase();
        this.args = args;
        this.prerequsite = prerequsite; // "move(Agent, MoveDirAgent, C0, C1) :- 
        this.format = format;           //  agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        this.effects = effects;
        this.requirements = requirements;
    }
    
    public ArrayList<Actions> get(Logic logic, HashMap<String,String> arguments) throws PrologException {
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        
        String query = this.name + "(";
        for(int i = 0; i < this.args.size(); i++) {
           System.err.println("Check for: " + this.args.get(i));
            // If an argument has been supplied, insert the value for it.
            if(arguments.containsKey(this.args.get(i))) {
                query += arguments.get(this.args.get(i)) + ",";
                continue;
            }
            // Else, insert it to be bound by the logic engine.    
            query += this.args.get(i) + ",";
        }
        
        if(query.endsWith(",")) {
            query = query.substring(0, query.length()-1);
        }
        query += "). ";
        System.err.println("Query:" + query);
        
        ArrayList<Solution> sol = logic.solveAll(query);
        
        for (Solution si : sol) {
            try {
                if(si.success()) {
                    System.err.println("Bindings: \n" + si.bindingsToString());
                    String MoveDirAgent = "" + si.getBinding("MoveDirAgent");
                    //System.out.println("Dir: " + MoveDirAgent);
                    MoveDirAgent = MoveDirAgent.replace("'", "");
                    String C0 = "" + si.getBinding("C0");
                    //System.out.println("C0: " + C0);
                    String C1 = "" + si.getBinding("C1");
                    //System.out.println("C1: " + C1);
                    String format = this.format;
                    
                    ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
                    ArrayList<String> requirementsProp = (ArrayList<String>) this.requirements.clone();

                    for(String arg : this.args) {
                        System.err.print("Check for: " + arg);
                        String replace = "";
                        // If bound from argument
                        if(arguments.containsKey(arg)) {
                            System.err.println(" - in args");
                            replace = arguments.get(arg).toString();
                        }else{
                             // Else
                            System.err.println(" - in bindings");    
                            replace = si.getBinding(arg).toString();
                        }
                        System.out.println("In: " + format + " replace: " + arg + " with: " + replace);
                        format = format.replaceAll(arg, replace);
                        for(int j = 0; j < effectsProp.size(); j++) {
                            effectsProp.set(j, effectsProp.get(j).replaceAll(arg, replace));
                        }
                        for(int j = 0; j < requirementsProp.size(); j++) {
                            requirementsProp.set(j, requirementsProp.get(j).replaceAll(arg, replace));
                        }
                    }

                    Actions act = new Actions(format);

                    for(int i = 0; i < effectsProp.size(); i++) {
                        act.addEffect(effectsProp.get(i));
                    }
                    for(int i = 0; i < requirementsProp.size(); i++) {
                        act.addRequirement(requirementsProp.get(i));
                    }

                    System.err.println(act.toString());

                    actionsReturn.add(act);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        }
        return actionsReturn;
     }
}
