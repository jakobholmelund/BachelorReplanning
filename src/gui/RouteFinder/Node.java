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
        }else if (this.h != other.h) { //!this.equals(other.h)){
            //System.err.println("COMPARE TO: 3");
            return Double.compare(this.h, other.h);
        }else{
            return 0;
        }
        /*
         *  else if(this.parent != null && other.parent != null && this.parent.equals(other.parent)) {
            //System.err.println("ZERO ZERO ZERO ZERO!!!!!!!!!!!!!!!!!!!!!!!!");
            return 0;
         */
        
    }

    @Override
    public String toString() {
        int[] coords = coordsFor(curPosition);
        return "Newpos: (" + coords[0] + "," + coords[1] + ") g: " + g + " h: " + h + " f: " + f;
    }
    
    private int[] coordsFor(long k) {
        int x = (int)(k & 0xFFFFFFFF) - 1000000000;
        int y = (int)((k >>> 32) & 0xFFFFFFFF) - 1000000000;
        return new int[]{x,y};
    }
}
