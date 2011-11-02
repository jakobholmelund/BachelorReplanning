package jTrolog.terms;

/**
 * Wrapper object to store both the original Struct and its converted clause in the same object
 *
 * @author ivar.orstavik@hist.no
 */
public class Clause {

    public final Struct[] tail;
    public final Struct head;
    public final Struct original;

    public Clause(Struct[] tail, Struct head, Struct original) {
        this.tail = tail;
        this.head = head;
        this.original = original;
    }

    public String toString() {
        String s = "";
        /*String s = "Tail: ";
        for(int i = 0; i < this.tail.length; i++) {
            s += " " + this.tail[i].toString();
        }*/
        s += " Head: " + this.head.toString() + " Original: " + this.original.toString();
        return s;
    }
}
