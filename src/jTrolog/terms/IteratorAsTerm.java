package jTrolog.terms;

import java.util.Iterator;

/**
 * @author ivar.orstavik@hist.no
 */
public class IteratorAsTerm extends Term {

    Iterator impl;

    public IteratorAsTerm(Iterator impl) {
        this.impl = impl;
    }

    public boolean hasNext() {
        return impl.hasNext();
    }

    public Object next(){
        return impl.next();
    }

    public String toStringSmall() {
        return "iteratorTerm_" + hashCode();
    }
}
