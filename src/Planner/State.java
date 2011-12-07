package Planner;

import Planner.Logic;
import jTrolog.errors.PrologException;
import java.util.ArrayList;

public class State {
	
	//public ArrayList<Action> actions = new ArrayList<Action>();
        public Logic state;

       public State(Logic l) {
            this.state = l;
        }

       public State(String s) throws PrologException {
           Logic l = new Logic(s);
           this.state = l;
       }

	public boolean equals(State s) {
            return false;
	}

        public String toString() {
            return state.getTheoryAsString();
        }

}
