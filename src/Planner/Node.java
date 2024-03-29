package Planner;

public class Node implements Comparable<Node> {

    public State s;
    public Node parent;
    public Action a; // The action taken to reach this state
    public double g; // The cost of the path that leads to this node, from  the starting node.
    public double h; // The heuristics
    public double f; // h + g

    public Node(State s, Node n, Action a, double g, double h) {
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
        } else {
            //System.err.println("ZERO ZERO ZERO ZERO!!!!!!!!!!!!!!!!!!!!!!!!");
            return 1;
        }
    }

    @Override
    public String toString() {
        return "Action: " + a + " g: " + g + " h: " + h + " f: " + f;
    }
}
