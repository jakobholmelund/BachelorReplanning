/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Planner;

import Planner.*;
import jTrolog.engine.Solution;
import jTrolog.errors.PrologException;
import jTrolog.terms.Term;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import Planner.*;
/**
 *
 * @author Dan True
 */
public class ActionStruct {
    public String name;
    public String format;
    public String prerequsiteString;
    public ArrayList<String> prerequisites;
    public ArrayList<String> args;
    public ArrayList<String> effects;
    public ArrayList<String> requirements;
    public boolean expanded;
    public boolean atomic;
    
    public ActionStruct(String name, ArrayList<String> prerequsite, String format, ArrayList<String> args, ArrayList<String> effects, ArrayList<String> requirements, boolean expanded, boolean atomic) {
        this.name = name.toLowerCase();
        this.args = args;
        this.prerequisites = prerequsite; // "move(Agent, MoveDirAgent, C0, C1) :- 
        this.format = format;           //  agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        this.effects = effects;         // REMEMBER TO BUILD THE PREREQUISITE STRING
        this.requirements = requirements;
        this.expanded = expanded;
        this.atomic = atomic;
        
        prerequsiteString = name + "(";
        boolean first1 = true;
        for(String s : args) {
            if(first1) {
                prerequsiteString += s;
                first1 = false;
            }else{
                prerequsiteString += "," + s;
            }
        }
        
        prerequsiteString += ") :-";
        
        boolean first2 = true;
        for(String s : prerequsite) {
            if(first2) {
                prerequsiteString += s;
                first2 = false;
            }else{
                prerequsiteString += "," + s;
            }
        }
        
        prerequsiteString += ". ";
        //System.out.println(prerequsiteString);
    }
    
    public ArrayList<Actions> getSpecificAction(Logic logic, HashMap<String,String> arguments) {
        System.out.println("Get specific action.");
        ArrayList<Actions> possibleActions = new ArrayList<Actions>();
         // push(Agent, MoveDir, MovePush, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDir), boxAt(Box, C1), neighbour(C1, C2, MovePush), f(C2). 
        // For each preconditions
        for(int i = prerequisites.size()-1; i >= 0; i--) {
            String s = prerequisites.get(i);
            for(String arg : arguments.keySet()) {
                s = s.replaceAll(arg, arguments.get(arg));
            }
            ArrayList<Solution> solutions = logic.solveAll(s);

            // Check if true in the given state.
            System.out.println("For: " + s);
            if(!solutions.isEmpty() && solutions.get(0).success()) {
                // If yes - for each solution append them to arguments and search onwards from there.
                //for(Solution sol : solutions) {
                Map bindings = solutions.get(0).getBindings();    
                //}
                System.out.println("Bindings: " + bindings.toString());
                for(Object o : bindings.keySet()) {
                    String key = (String) o;
                    if(!arguments.containsKey(key)) {
                        arguments.put(key, bindings.get(key).toString());
                    }
                }
            }else{
                // If not, then add to open preconditions, and continue
                System.out.println("Is empty: " + solutions.toString());
                
            }
            System.out.println("Args: " + arguments.toString() + "\n");
        }
        
        return possibleActions;
    }
    
    public ArrayList<Actions> get(Logic logic, HashMap<String,String> arguments) throws PrologException {
       //System.err.println("Get called on action: " + this.name + "  with arguments: " + arguments.toString());
       //System.err.println(logic.getTheoryAsString());
       
        ArrayList<Actions> actionsReturn = new ArrayList<Actions>();
        
        String query = this.name + "(";
        for(int i = 0; i < this.args.size(); i++) {
           //System.err.println("Check for: " + this.args.get(i));
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
        System.err.println("Solution:" + sol.toString());
        for (Solution si : sol) {
            try {
                if(si.success()) {
                    //System.err.println("Bindings: \n" + si.bindingsToString());

                    /*String format = this.format;
                    
                    ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
                    ArrayList<String> requirementsProp = (ArrayList<String>) this.requirements.clone();

                    for(String arg : this.args) {
                        //System.err.print("Check for: " + arg);
                        String replace = "";
                        // If bound from argument
                        if(arguments.containsKey(arg)) {
                            //System.err.println(" - in args");
                            replace = arguments.get(arg).toString();
                        }else{
                             // Else
                            //System.err.println(" - in bindings");    
                            replace = si.getBinding(arg).toString();
                        }
                        //System.out.println("In: " + format + " replace: " + arg + " with: " + replace);
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

                    System.err.println("Action: " + act.toString());*/
                    
                    actionsReturn.add(createInstanceFromSolution(arguments, si));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        }
        return actionsReturn;
     }
    
    public Actions createInstanceFromSolution(HashMap<String, String> arguments, Solution si) {
        String format = this.format;
        
        ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
        ArrayList<String> requirementsProp = (ArrayList<String>) this.requirements.clone();

        for(String arg : this.args) {
            //System.err.print("Check for: " + arg);
            String replace = "";
            // If bound from argument
            if(arguments.containsKey(arg)) {
                //System.err.println(" - in args");
                replace = arguments.get(arg).toString();
            }else{
                 // Else
                //System.err.println(" - in bindings");    
                replace = si.getBinding(arg).toString();
            }
            //System.out.println("In: " + format + " replace: " + arg + " with: " + replace);
            format = format.replaceAll(arg, replace);
            for(int j = 0; j < effectsProp.size(); j++) {
                effectsProp.set(j, effectsProp.get(j).replaceAll(arg, replace));
            }
            for(int j = 0; j < requirementsProp.size(); j++) {
                requirementsProp.set(j, requirementsProp.get(j).replaceAll(arg, replace));
            }
        }

        Actions act = new Actions(format, this.expanded, this.atomic);

        for(int i = 0; i < effectsProp.size(); i++) {
            act.addEffect(effectsProp.get(i));
        }
        for(int i = 0; i < requirementsProp.size(); i++) {
            act.addRequirement(requirementsProp.get(i));
        }

        //System.err.println("Action: " + act.toString());
        
        return act;
    }
    
    public Actions createInstance(HashMap<String, String> arguments) {
        String format = this.format;
        
        ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
        ArrayList<String> requirementsProp = (ArrayList<String>) this.requirements.clone();

        for(String arg : this.args) {
            //System.err.print("Check for: " + arg);
            String replace = "";
            // If bound from argument
            if(arguments.containsKey(arg)) {
                //System.err.println(" - in args");
                replace = arguments.get(arg).toString();
            }
            //System.out.println("In: " + format + " replace: " + arg + " with: " + replace);
            format = format.replaceAll(arg, replace);
            for(int j = 0; j < effectsProp.size(); j++) {
                effectsProp.set(j, effectsProp.get(j).replaceAll(arg, replace));
            }
            for(int j = 0; j < requirementsProp.size(); j++) {
                requirementsProp.set(j, requirementsProp.get(j).replaceAll(arg, replace));
            }
        }

        Actions act = new Actions(format, this.expanded, this.atomic);

        for(int i = 0; i < effectsProp.size(); i++) {
            act.addEffect(effectsProp.get(i));
        }
        for(int i = 0; i < requirementsProp.size(); i++) {
            act.addRequirement(requirementsProp.get(i));
        }

        //System.err.println("Action: " + act.toString());
        
        return act;
    }
}
