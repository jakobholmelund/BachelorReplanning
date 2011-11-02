package jTrolog.parser;
import java.io.Serializable;

/**
 * This class represents a token read by the prolog term tokenizer
 */
class Token implements Serializable {
	// token textual representation
	String seq;
	// token type and attribute
	int type;

    static final int ATOM = 'A';
    static final int SQ_SEQUENCE = 'S';
    static final int DQ_SEQUENCE = 'D';
    static final int OPERATOR = 'O';
    static final int FUNCTOR = 'F';

    static final int ATOM_OPERATOR = 'B';
    static final int ATOM_FUNCTOR = 'a';
    static final int OPERATOR_FUNCTOR = 'p';
    static final int SQ_FUNCTOR = 's';

    static final int VARIABLE = 'v';
    static final int EOF = 'e';
    static final int INTEGER = 'i';
    static final int FLOAT = 'f';

    public Token(String s, int t) {
        seq = s.intern();
        type = t;
    }
	
    public String getValue(){
		return seq;
	}

    public boolean isOperator(boolean commaIsEndMarker) {
        if (commaIsEndMarker && ",".equals(seq))
            return false;
        return type == OPERATOR || type == ATOM_OPERATOR || type == OPERATOR_FUNCTOR;
    }

    public boolean isFunctor() {
        return type == FUNCTOR || type == ATOM_FUNCTOR || type == SQ_FUNCTOR || type == OPERATOR_FUNCTOR;
    }
    
    public boolean isNumber() {
    	return type == INTEGER || type == FLOAT;
    }

    boolean isEOF() {
        return type == EOF;
    }

    boolean isType(int type) {
        return this.type == type;
    }

    boolean isAtom() {
        return type == ATOM ||
                type == ATOM_OPERATOR ||
                type == ATOM_FUNCTOR ||
                type == SQ_FUNCTOR ||
                type == SQ_SEQUENCE ||
                type == DQ_SEQUENCE;
    }
}