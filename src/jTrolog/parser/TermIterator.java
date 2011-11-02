package jTrolog.parser;

import jTrolog.terms.Term;

import java.util.NoSuchElementException;

/**
 * This class represents an iterator of terms from a string.
 *
 * @see jTrolog.terms.Term
 */
class TermIterator implements java.util.Iterator, java.io.Serializable {
	
	private Parser parser;
	private boolean hasNext;
	private Term next;

	TermIterator(Parser p) {
        parser = p;
        next = parser.nextTerm(true);
        hasNext = (next != null);
    }
	
	public Object next() {
		if (hasNext) {
			if (next == null)
                next = parser.nextTerm(true);
			hasNext = false;
			Term temp = next;
			next = null;
			return temp;
		} else if (hasNext()) {
            hasNext = false;
            Term temp = next;
            next = null;
            return temp;
        }
		throw new NoSuchElementException();
	}
	
	public boolean hasNext() {
		if (hasNext)
			return hasNext;
        next = parser.nextTerm(true);
        if (next != null)
            hasNext = true;
		return hasNext;
	}
	
	public void remove() {
		throw new UnsupportedOperationException();
	}
}