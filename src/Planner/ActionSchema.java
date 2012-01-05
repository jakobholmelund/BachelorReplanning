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
import jTrolog.errors.InvalidTermException;
/**
 *
 * @author Dan True
 */
public class ActionSchema {
    public String name;
    public String format;
    public String prerequsiteString;
    public ArrayList<String> prerequisites;
    public ArrayList<String> args;
    public ArrayList<String> effects;
    //public ArrayList<String> requirements;
    public boolean expanded;
    public boolean atomic;
    
    public ActionSchema(String name, ArrayList<String> prerequsite, String format, ArrayList<String> args, ArrayList<String> effects, boolean expanded, boolean atomic) {
        this.name = name.toLowerCase();
        this.args = args;
        this.prerequisites = prerequsite; // "move(Agent, MoveDirAgent, C0, C1) :- 
        this.format = format;           //  agentAt(Agent, C0), neighbour(C0, C1, MoveDirAgent), f(C1). ";
        this.effects = effects;         // REMEMBER TO BUILD THE PREREQUISITE STRING
        //this.requirements = requirements;
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
        
        prerequsiteString += ") :- ";
        
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
    
    public ArrayList<Action> getSpecificActions(Logic logic, HashMap<String,String> arguments) {
        //System.out.println("Get specific action: " + this.name);
        //System.out.println("Args: " + arguments.toString());
        //System.out.println("STATE");
        //System.out.println(logic.getTheoryAsString());
        ArrayList<Action> possibleActions = new ArrayList<Action>();
        ArrayList<String> openPreconditions = new ArrayList<String>();
        boolean error = false;
         // push(Agent, MoveDir, MovePush, C0, C1, C2, Box) :- agentAt(Agent, C0), neighbour(C0, C1, MoveDir), boxAt(Box, C1), neighbour(C1, C2, MovePush), f(C2). 
        // For each preconditions
        for(int i = prerequisites.size()-1; i >= 0; i--) {
            String s = prerequisites.get(i);
            for(String arg : arguments.keySet()) {
                s = s.replaceAll(arg, arguments.get(arg));
            }
            ArrayList<Solution> solutions = new ArrayList<Solution>();
            try {
                solutions = logic.solveAll(s);
            }catch(Exception e) {
                error = true;
            }
            // Check if true in the given state.
            /*System.out.println("For: " + s);
            System.out.println("Error: " + error);
            System.out.println("Empty: " + solutions.isEmpty());
            if(!solutions.isEmpty()) {
                System.out.println("Success: " + solutions.get(0).success());
                System.out.println("Sol: " + solutions.get(0).toString());
            }*/
            if(!error && !solutions.isEmpty() && solutions.get(0).success()) {
                // If yes - for each solution append them to arguments and search onwards from there.
    
                Map bindings = solutions.get(0).getBindings();    
                
                //System.out.println("Bindings: " + bindings.toString());
                for(Object o : bindings.keySet()) {
                    String key = (String) o;
                    if(!arguments.containsKey(key)) {
                        arguments.put(key, bindings.get(key).toString());
                    }
                }
            }else{
                // If not, then add to open preconditions, and continue
                //System.out.println("Is empty: " + s);
                openPreconditions.add(s);
            }
            //System.out.println("Args: " + arguments.toString() + "\n");
        }
        Action a = createInstance(arguments);
        for(String s : openPreconditions) {
            s = s.replace('\\', ' ');
            s = s.replace('+', '!');
            s.trim();
            a.addOpenPrecondition(s);
        }
        possibleActions.add(a);
        return possibleActions;
    }
    
    public ArrayList<Action> get(Logic logic, HashMap<String,String> arguments) throws PrologException {
       //System.out.println("            Get called on action: " + this.name + "  with arguments: " + arguments.toString());
       //System.out.println(logic.getTheoryAsString());
       
        ArrayList<Action> actionsReturn = new ArrayList<Action>();
        
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
        //System.out.println("            Query:" + query);
        
        ArrayList<Solution> sol = logic.solveAll(query);
        //System.out.println("            Solution:" + sol.toString());
        for (Solution si : sol) {
            try {
                if(si.success()) {
                    actionsReturn.add(createInstanceFromSolution(arguments, si));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
           
        }
        return actionsReturn;
     }
    
    public Action createInstanceFromSolution(HashMap<String, String> arguments, Solution si) {
        String format = this.format;
        
        ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
        ArrayList<String> prerequisitesProp = (ArrayList<String>) this.prerequisites.clone();

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
            for(int j = 0; j < prerequisitesProp.size(); j++) {
                prerequisitesProp.set(j, prerequisitesProp.get(j).replaceAll(arg, replace));
            }
        }

        Action act = new Action(format, this.expanded, this.atomic);

        for(int i = 0; i < effectsProp.size(); i++) {
            act.addEffect(effectsProp.get(i));
        }
        for(int i = 0; i < prerequisitesProp.size(); i++) {
            act.addPrerequisites(prerequisitesProp.get(i));
        }

        //System.err.println("Action: " + act.toString());
        
        return act;
    }
    
    public Action createInstance(HashMap<String, String> arguments) {
        String format = this.format;
        
        ArrayList<String> effectsProp = (ArrayList<String>) this.effects.clone();
        ArrayList<String> prerequisitesProp = (ArrayList<String>) this.prerequisites.clone();
        //System.out.println("args:" + arguments.toString());
        
        for(String arg : this.args) {
            //System.out.println("Check for:-" + arg);
            String replace = "";
            // If bound from argument
            if(arguments.keySet().contains(arg)) {
                //System.out.println(" - in args");
                replace = arguments.get(arg).toString();
                
                //System.out.println("In: " + format + " replace: " + arg + " with: " + replace);
                format = format.replaceAll(arg, replace);
                for(int j = 0; j < effectsProp.size(); j++) {
                    effectsProp.set(j, effectsProp.get(j).replaceAll(arg, replace));
                }
                for(int j = 0; j < prerequisitesProp.size(); j++) {
                    prerequisitesProp.set(j, prerequisitesProp.get(j).replaceAll(arg, replace));
                }
            }
            //System.out.println("\n");
        }

        Action act = new Action(format, this.expanded, this.atomic);

        for(int i = 0; i < effectsProp.size(); i++) {
            act.addEffect(effectsProp.get(i));
        }
        for(int i = 0; i < prerequisitesProp.size(); i++) {
            act.addPrerequisites(prerequisitesProp.get(i));
        }

        //System.err.println("Action Created: " + act.toString());
        
        return act;
    }
}
