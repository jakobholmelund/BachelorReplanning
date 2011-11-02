package jTrolog.engine;

import jTrolog.parser.Parser;
import jTrolog.lib.Library;

import java.lang.reflect.Method;

/**
 * Wrapper for primitive library methods
 *
 * @author ivar.orstavik@hist.no
 */
class PrimitiveInfo {

    final Method method;
	final Library source;
	final String key;

	PrimitiveInfo(Library lib, Method m, String functor, int arity) {
        key = Parser.wrapAtom(functor) + "/" + arity;
		source = lib;
		method = m;
	}

    public String toString() {
        return "[ primitive: method "+method.getName()+" - "+source.getClass().getName()+" ]\n";
    }
}