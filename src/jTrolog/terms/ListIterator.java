package jTrolog.terms;

import jTrolog.parser.Parser;

import java.util.Iterator;

/**
 * @author ivar.orstavik@hist.no
 */
class ListIterator implements Iterator {

    Term hereIAmBaby;

    public ListIterator(Struct origin) {
        hereIAmBaby = origin;
    }

    public boolean hasNext() {
        return hereIAmBaby != null;
    }

    public Object next() {
        if (hereIAmBaby == null)
            throw new IndexOutOfBoundsException("iterating out of list");
        if (hereIAmBaby.equals(Term.emptyList)) {
            hereIAmBaby = null;
            return Term.emptyList;
        }
        if (hereIAmBaby instanceof Struct && ((Struct) hereIAmBaby).predicateIndicator == Parser.listSignature) {
            Term timeToDeliver = ((Struct) hereIAmBaby).getArg(0);
            hereIAmBaby = ((Struct) hereIAmBaby).getArg(1);
            return timeToDeliver;
        }
        Term ImYours = hereIAmBaby;
        hereIAmBaby = null;
        return ImYours;
    }

    public void remove() {
        throw new UnsupportedOperationException("don't delete on List iteration");
    }
}
