package gui.RouteFinder;

public class Node implements Comparable<Node> {

    long curPosition;
    Node parent;
    double g; // The cost of the path that leads to this node, from  the starting node.
    double h; // The heuristics
    double f; // h + g

    Node(long s, Node n, double g, double h) {
        this.curPosition = s;
        this.parent = n;
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
        return "Newpos: " + curPosition + " g: " + g + " h: " + h + " f: " + f;
    }
}
