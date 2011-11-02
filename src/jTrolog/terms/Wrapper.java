package jTrolog.terms;

import jTrolog.terms.Term;
import jTrolog.engine.BindingsTable;

import java.util.Iterator;

/**
 * @author ivar.orstavik@hist.no
 */
public interface Wrapper {

    int getContext();

    Term getBasis();

    static class WrappedIterator implements Iterator {

        private Iterator underlyingIt;
        private int ctx;

        WrappedIterator(Iterator orig, int ctx){
            underlyingIt = orig;
            this.ctx = ctx;
        }

        public boolean hasNext() {
            return underlyingIt.hasNext();
        }

        public Object next() {
            return BindingsTable.wrapWithID((Term) underlyingIt.next(), ctx);
        }

        public void remove() {
            throw new UnsupportedOperationException("dont remove on Prolog lists");
        }
    }
}
