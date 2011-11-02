package Planner.naiveReplan;

public class Node implements Comparable<Node> {

    State s;
    Node parent;
    Action a; // The action taken to reach this state
    double g; // The cost of the path that leads to this node, from  the starting node.
    double h; // The heuristics
    double f; // h + g

    Node(State s, Node n, Action a, double g, double h) {
        this.s = s;
        this.parent = n;
        this.a = a;
        this.g = g; // Cost
        this.h = h; // Heuristic
        this.f = g + h;
    }

    @Override
    public int compareTo(Node other) {
        //System.err.println("COMPARE TO: 1");
        if (this.f != other.f) {
            //System.err.println("COMPARE TO: 2");
            return Double.compare(this.f, other.f);
        } else if (this.h != other.h) { //!this.equals(other.h)){
            //System.err.println("COMPARE TO: 3");
            return Double.compare(this.h, other.h);
        } else if (this.a == null || !this.a.name.equals(other.a.name)){
            return 1;
        } else {
            //System.err.println("ZERO ZERO ZERO ZERO!!!!!!!!!!!!!!!!!!!!!!!!");
            return 0;
        }
    }

    @Override
    public String toString() {
        return "Action: " + a + " g: " + g + " h: " + h + " f: " + f;
    }
}
