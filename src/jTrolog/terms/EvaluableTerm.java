package jTrolog.terms;

/**
 * Both Structs and Number are considered potentially evaluable.
 * However, many Structs are of course not evaluable. The Builtin.eval_1
 * method throws an exception if this is the case.
 *
 * @author janerist@stud.ntnu.no
 * @author ivar.orstavik@hist.no
 */
public abstract class EvaluableTerm extends Term {
}
