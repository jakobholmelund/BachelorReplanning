package jTrolog.engine;

import jTrolog.terms.Struct;
import jTrolog.terms.Clause;
import jTrolog.errors.PrologException;
import jTrolog.parser.Parser;

import java.util.List;

/**
 * @author janerist@stud.ntnu.no
 * @author ivar.orstavik@hist.no
 */
class ChoicePoint {
    //CP head
    Struct head;
    int headCtx = -1;
    //CP body
    private Struct[] body;
    private int nextInBody = 0;
    private int bodySize = 0;
    int bodyCtx;

    //CP parent and cutParent
    ChoicePoint parent;
    ChoicePoint cutParent;

    //alternative rules
    List alternatives;
    int altPos;
    int alternativeCount;

    //or primitive
    PrimitiveInfo prim;

    /**
     * recycles previously used CP objects on the stack
     * @param s
     * @param ctx
     * @param parent
     */
    final void set(final Struct s, final int ctx, final ChoicePoint parent) throws PrologException {
        head = s;
        headCtx = ctx;
        this.parent = parent;
        cutParent = Parser.isSemiAndNotIf(parent.head) ? parent.parent.cutParent : this;

        altPos = 0;
        this.alternatives = null;
        this.alternativeCount = 0;

        bodyCtx = -1;

        nextInBody = 0;
        bodySize = 0;
    }

    final void setRules(final List rules) throws PrologException {
        altPos = 0;
        alternatives = rules;
        alternativeCount = alternatives.size();
    }

    final boolean hasAlternatives() {
        return altPos < alternativeCount;
    }

    final Clause nextAlternative() {
        return (Clause) alternatives.get(altPos++);
    }

    /**
     * removes any choice points (alternative rules) on the ChoicePoint.
     */
    final void cutAlternatives() {
        altPos = alternativeCount;
    }

    public String toString() {
        if (prim != null)
            return "!!"+ head + "<" + headCtx + ">" + "!!"; 
        String s2 = head == null ? "?-\n   " : head + "<" + headCtx + "> :-\n   ";
        if (body != null) {
            for (int i = 0; i < bodySize; i++) {
                if (i != 0) s2 += ",\n   ";
                Object rule = body[i];
                if (i == nextInBody) s2 += "**" + rule + "**";
                else s2 += rule;
            }
        }
        if (hasAlternatives()) {
            s2 += "\n\n                                rules:\n";
            for (int i = altPos; i < alternativeCount; i++)
                s2 += "                              " + alternatives.get(i) + "\n";
        }
        return s2;
    }

    boolean hasTODO() {
        return nextInBody < bodySize;
    }

    public void fail() {
        bodySize = 0;
        nextInBody = 0;
        parent.nextInBody--;
    }

    Struct getTODO() {
        return body[nextInBody++];
    }

    final void setBody(final Struct[] body, final int bodyCtx) {
        this.bodyCtx = bodyCtx;
        this.body = body;
        nextInBody = 0;
        bodySize = body.length;
    }

    public void reuse() {
        this.head = body[0];
        this.headCtx = bodyCtx;
        this.nextInBody = 0;
        this.altPos = 0;
    }
}
