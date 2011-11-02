package jTrolog.parser;

import jTrolog.errors.InvalidTermException;
import jTrolog.engine.*;
import jTrolog.terms.Number;
import jTrolog.terms.Double;
import jTrolog.terms.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This class defines a parser of prolog terms and sentences.
 * <p/>
 * BNF for jTrolog part 2: Parser
 * term ::= expr(1200)
 * expr(n) ::= exprC(n-1) { op(xfx,n) expr(n-1) |
 *                           op(xfy,n) expr(n) |
 *                           op(xf,n) |
 *                           op(yfx,n) expr(n-1) |
 *                           op(yf,n) }*
 * // exprC is called parseLeftSide in the code
 * exprC(n) ::= '-' integer | '-' float |
 *              op( fx,n ) exprA(n-1) |
 *              op( fy,n ) exprA(n) |
 *              exprA(n)
 * exprA(0) ::= integer |
 *              float |
 *              atom |
 *              variable |
 *              atom'(' expr(1200) { ',' expr(1200) }* ')' |
 *              '[' [ expr(1200) { ',' expr(1200) }* [ '|' expr(1200) ] ] ']' |
 *              '(' { expr(1200) }* ')'
 *              '{' { expr(1200) }* '}'
 * op(type,n) ::= atom | { symbol }+
 */
public class Parser implements Serializable {
    int dqFlag = 0;
    private static final int DQ_ATOMS = 0;
    private static final int DQ_CHARS = 1;
    private static final int DQ_CODES = 2;

    public static final String floatSignature = "float/1".intern();
    public static final String listSignature = "'.'/2".intern();
    public static final String commaSignature = "','/2".intern();
    public static final String cutSignature = "!/0".intern();
    public static final String singleClauseSignature = "':-'/1".intern();
    public static final String doubleClauseSignature = "':-'/2".intern();
    public static final String semiColonSignature = "';'/2".intern();
    public static final String ifSignature = "'->'/2".intern();
    public static final String callSignature = "call/1".intern();
    public static final String throwSignature = "throw/1".intern();
    public static final String catchSignature = "catch/3".intern();
    public static final String trueSignature = "true/0".intern();
    public static final String failSignature = "fail/0".intern();

    private Tokenizer tokenizer;
    private Prolog engine;
    private List variableList;

    /**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public Parser(InputStream theoryText, Prolog p) {
        this(p, new Tokenizer(new BufferedReader(new InputStreamReader(theoryText))));
    }

    /**
     * creating a Parser specifing how to handle operators
     * and what text to parse
     */
    public Parser(String theoryText, Prolog engine) {
        this(engine, new Tokenizer(theoryText));
    }

    /**
     * creating a parser with default operator interpretation
     */
    public Parser(String theoryText) {
        this(null, new Tokenizer(theoryText));
    }

    /**
     * creating a parser with default operator interpretation
     */
    public Parser(InputStream theoryText) {
        this(null, new Tokenizer(new BufferedReader(new InputStreamReader(theoryText))));
    }

    private Parser(Prolog p, Tokenizer lexer){
        tokenizer = lexer;
        variableList = new ArrayList();
        if (p == null) {
            engine = Prolog.defaultMachine;
        } else {
            engine = p;
            Term dqFlag = p.getFlagValue("double_quotes");
            if (dqFlag != null){
                if ("chars".equals(dqFlag.toString()))
                    this.dqFlag = DQ_CHARS;
                else if ("codes".equals(dqFlag.toString()))
                    this.dqFlag = DQ_CODES;
                else
                    this.dqFlag = DQ_ATOMS;
            }
        }
    }

//  user interface

    public Iterator iterator() throws InvalidTermException {
        return new TermIterator(this);
    }

    /**
     * Parses next term from the stream built on string.
     * @param endNeeded <tt>true</tt> if it is required to parse the end token
     * (a period), <tt>false</tt> otherwise.
     * @throws InvalidTermException if a syntax error is found.
     */
    public Term nextTerm(boolean endNeeded) throws InvalidTermException {
        try {
            variableList.clear();
            Token t = tokenizer.readToken();
            if (t.isEOF())
                return null;

            tokenizer.unreadToken(t);
            Term term = expr(Prolog.OP_HIGH, false);
            if (term == null)
            	throw new InvalidTermException("The parser is unable to finish.");

            if (endNeeded && ! tokenizer.readToken().isType('.'))
                throw new InvalidTermException("The term " + term + " is not ended with a period.");
            return term;
        } catch (IOException ex) {
            throw new InvalidTermException("An I/O error occured.");
        }
    }

    // internal parsing procedures

    private Term expr(int maxPriority, boolean commaIsEndMarker) throws InvalidTermException, IOException {

        //1. op(fx,n) expr(n-1) | op(fy,n) exprA(n) | expr0
        Term leftRes = parseLeftSide(commaIsEndMarker, maxPriority);
        //todo should minPriority come from parseLeftSide??
        int minPriority = 0;

        //2.left is followed by either xfx, xfy or xf operators, parse these
        Token operator = tokenizer.readToken();
        for (; operator.isOperator(commaIsEndMarker); operator = tokenizer.readToken()) {
            int XFX = engine.getOperatorPriority(operator.seq, Prolog.XFX);
            int XFY = engine.getOperatorPriority(operator.seq, Prolog.XFY);
            int XF = engine.getOperatorPriority(operator.seq, Prolog.XF);
            int YFX = engine.getOperatorPriority(operator.seq, Prolog.YFX);
            int YF = engine.getOperatorPriority(operator.seq, Prolog.YF);

            //check that no operator has a priority higher than permitted
            //or a lower priority than the left side expression
            if (XFX > maxPriority || XFX < Prolog.OP_LOW) XFX = -1;
            if (XFY > maxPriority || XFY < Prolog.OP_LOW) XFY = -1;
            if (XF > maxPriority || XF < Prolog.OP_LOW) XF = -1;
            if (YF < minPriority || YF > maxPriority) YF = -1;
            if (YFX < minPriority || YFX > maxPriority) YFX = -1;

            //XFX
            if (XFX >= XFY && XFX >= XF && XFX >= minPriority) {     //XFX has priority
                Term found = expr(XFX - 1, commaIsEndMarker);
                if (found != null) {
                    minPriority = XFX;
                    leftRes = new Struct(operator.seq, new Term[]{leftRes, found}, Prolog.XFX);
                    continue;
                }
            }
            //XFY
            else if (XFY >= XF && XFY >= minPriority){           //XFY has priority, or XFX has failed
                Term found = expr(XFY, commaIsEndMarker);
                if (found != null) {
                    minPriority = XFY;
                    leftRes = new Struct(operator.seq, new Term[]{leftRes, found}, Prolog.XFY);
                    continue;
                }
            }
            //XF
            else if (XF >= minPriority)                   //XF has priority, or XFX and/or XFY has failed
                return new Struct(operator.seq, new Term[]{leftRes}, Prolog.XF);

            //XFX did not have top priority, but XFY failed
            else if (XFX >= minPriority) {
                Term found = expr(XFX - 1, commaIsEndMarker);
                if (found != null) {
                    minPriority = XFX;
                    leftRes = new Struct(operator.seq, new Term[]{leftRes, found}, Prolog.XFX);
                    continue;
                }
            }
            //YFX
            else if (YFX >= YF && YFX >= Prolog.OP_LOW){
                Term found = expr(YFX-1, commaIsEndMarker);
                if (found != null) {
                    minPriority = YFX;
                    leftRes = new Struct(operator.seq, new Term[]{leftRes, found}, Prolog.YFX);
                    continue;
                }
            }
            //either YF has priority over YFX or YFX failed
            else if (YF >= Prolog.OP_LOW) {
                minPriority = YF;
                leftRes = new Struct(operator.seq, new Term[]{leftRes}, Prolog.YF);
                continue;
            }
            break;
        }
        tokenizer.unreadToken(operator);
        return leftRes;
    }

    /**
     * Parses and returns a valid 'leftside' of an expression.
     * If the left side starts with a prefix, it consumes other expressions with a lower priority than itself.
     * If the left side does not have a prefix it must be an expr0.
     *
     * @param commaIsEndMarker used when the leftside is part of and argument list of expressions
     * @param maxPriority operators with a higher priority than this will effectivly end the expression
     * @return a wrapper of: 1. term correctly structured and 2. the priority of its root operator
     * @throws InvalidTermException
     */
    private Term parseLeftSide(boolean commaIsEndMarker, int maxPriority) throws InvalidTermException, IOException {
        //1. prefix expression
        Token f = tokenizer.readToken();
        if (f.isOperator(commaIsEndMarker) && !f.isFunctor()) {
            int FX = engine.getOperatorPriority(f.seq, Prolog.FX);
            int FY = engine.getOperatorPriority(f.seq, Prolog.FY);

            if (f.seq.equals("-")) {
                Token t = tokenizer.readToken();
                if (t.isNumber())
                    return jTrolog.terms.Number.create("-" + t.seq);
                else
                    tokenizer.unreadToken(t);
            }

            //check that no operator has a priority higher than permitted
            if (FY > maxPriority) FY = -1;
            if (FX > maxPriority) FX = -1;

            //FX has priority over FY
            if (FX >= FY && FX >= Prolog.OP_LOW){
                Term found = expr(FX-1, commaIsEndMarker);    //op(fx, n) exprA(n - 1)
                if (found != null)
                    return new Struct(f.seq, new Term[]{found}, Prolog.FX);
            }
            //FY has priority over FX, or FX has failed
            else if (FY >= Prolog.OP_LOW) {
                Term found = expr(FY, commaIsEndMarker); //op(fy,n) exprA(1200)  or   op(fy,n) expr(n)
                if (found != null)
                    return new Struct(f.seq, new Term[]{found}, Prolog.FY);
            }
            //FY has priority over FX, but FY failed
            else if (FX >= Prolog.OP_LOW) {
                Term found = expr(FX-1, commaIsEndMarker);    //op(fx, n) exprA(n - 1)
                if (found != null)
                    return new Struct(f.seq, new Term[]{found}, Prolog.FX);
            }
        }
        tokenizer.unreadToken(f);
        //2. expr0
        return expr0();
    }

    /**
     * exprA(0) ::= integer |
     *              float |
     *              variable |
     *              atom |
     *              atom( expr(1200) { , exprA(1200) }* ) |
     *              '[' expr(1200) { , expr(1200) }* [ | exprA(1200) ] ']' |
     *              '{' [ exprA(1200) ] '}' |
     *              '(' exprA(1200) ')'
     */
    private Term expr0() throws InvalidTermException, IOException {
        Token t1 = tokenizer.readToken();

        if (t1.isType(Token.INTEGER))
            return Int.create(t1.seq);

        if (t1.isType(Token.FLOAT))
            return new Double(t1.seq);

        if (t1.isType(Token.VARIABLE)){
            int pos = variableList.indexOf(t1.seq);
            if (pos != -1 && t1.seq != Var.ANY)
                return new Var(t1.seq, pos+1);
            variableList.add(t1.seq);
            return new Var(t1.seq, variableList.size());
        }

        if (t1.isFunctor()) {
            String functor = t1.seq;
            Token t = tokenizer.readToken();   //reading left par
            LinkedList l = new LinkedList();
            do {
                l.add(expr(Prolog.OP_HIGH, true));              //adding argument
                t = tokenizer.readToken();
                if (")".equals(t.seq))          //if right par, return
                    return new Struct(functor, (Term[]) l.toArray(new Term[0]));
            } while (",".equals(t.seq));        //if comma, read next arg
            throw new InvalidTermException("Error in argument list syntax.\n" +
                    "Token: " + t + " not expected at line " + tokenizer.lineno() + ".");
        }

        if (t1.isType(Token.DQ_SEQUENCE)) {
            if (dqFlag == Parser.DQ_ATOMS)  //DQ as atoms
                return new StructAtom(t1.seq);
            if (dqFlag == Parser.DQ_CHARS)  //DQ as char list
                return stringToStructList(t1.seq);
            if (dqFlag == Parser.DQ_CODES) {  //DQ as int list
                char[] chars = t1.seq.toCharArray();
                int[] codes = new int[chars.length];
                for (int i = 0; i < chars.length; i++)
                    codes[i] = chars[i];
                return intsToStructList(codes);
            }
        }

        if (t1.isAtom())
            return new StructAtom(t1.seq);

        //todo review handling of ( ... ). Error in test set and it should have consequences for how toString in Struct performs for operators that have strange priorities
        if (t1.isType('(')) {
            Term term = expr(Prolog.OP_HIGH, false);
            if (tokenizer.readToken().isType(')'))
                return term;
            throw new InvalidTermException("Missing right parenthesis: ("+term + " -> here <-");
        }

        if (t1.isType('[')) {
            Token t2 = tokenizer.readToken();
            if (t2.isType(']'))
                return Term.emptyList;
            tokenizer.unreadToken(t2);

            LinkedList elems = new LinkedList();
            do {
                elems.add(expr(Prolog.OP_HIGH, true));
                t2 = tokenizer.readToken();
                if ("|".equals(t2.seq)){
                    elems.add(expr(Prolog.OP_HIGH, true));
                    t2 = tokenizer.readToken();
                    if ("]".equals(t2.seq))
                        return createStructList(elems);
                    throw new InvalidTermException("Missing ']' after: "+ elems.getLast());
                }
                if ("]".equals(t2.seq)) {
                    elems.add(Term.emptyList);
                    return createStructList(elems);
                }
            } while(",".equals(t2.seq));
            throw new InvalidTermException("Error in list syntax after: "+ elems.getLast());
        }

        if (t1.isType('{')) {
            Token t2 = tokenizer.readToken();
            if (t2.isType('}'))
                return new StructAtom("{}");

            tokenizer.unreadToken(t2);
            Term arg = expr(Prolog.OP_HIGH, false);
            t2 = tokenizer.readToken();
            if (t2.isType('}'))
                return new Struct("{}",new Term[]{arg});
            throw new InvalidTermException("Missing right braces: {"+arg + " -> here <-");
        }

        throw new InvalidTermException("The following token could not be identified: "+t1.seq);
    }

    public int getCurrentLine() {
        return tokenizer.lineno();
    }

    public static Struct createListContainingAnyVars(int lengthInt) {
        LinkedList vars = new LinkedList();
        for (int i = 0; i < lengthInt; i++)
            vars.add(new Var("_", i+1));
        vars.add(Term.emptyList);
        return createStructList(vars);
    }

    public static Struct createStructList(LinkedList complete) {
        if (complete.isEmpty())
            return Term.emptyList;
        if (complete.size() == 2)
            return new Struct(".",new Term[]{(Term)complete.getFirst(), (Term)complete.getLast()});
        if (complete.size() > 2){
            Term head = (Term) complete.removeFirst();
            return new Struct(".",new Term[]{head, createStructList(complete)});
        }
        throw new RuntimeException("omg-..");
    }

    public static Struct stringToStructList(String charList){
        Struct t = StructAtom.emptyList;
        for (int i = charList.length()-1; i >= 0; i--)
            t = new Struct(".", new Term[]{new StructAtom(Character.toString(charList.charAt(i))), t});
        return t;
    }

    public static Struct intsToStructList(int[] numbers){
        Struct t = StructAtom.emptyList;
        for (int i = numbers.length-1; i >= 0; i--)
            t = new Struct(".", new Term[]{new Int(numbers[i]), t});
        return t;
    }

    public static boolean isSemiAndNotIf(Struct struct) {
        if (struct == null || struct.predicateIndicator != semiColonSignature)
            return false;
        final Term left = struct.getArg(0);
        return !(left instanceof Struct) || ((Struct) left).predicateIndicator != ifSignature;
    }

    /**
     * @param atom
     * @return atom wrapped in 'atom' if necessary
     */
    public static String wrapAtom(final String atom) {
        return isAtom(atom) || (atom.startsWith("'") && atom.endsWith("'")) ? atom : "'" + atom + "'";
    }

    /**
     * @return true if the String could be a prolog atom
     */
    public static boolean isAtom(String s) {
        return atom.matcher(s).matches();
    }

    static private Pattern atom = Pattern.compile("(!|[a-z][a-zA-Z_0-9]*)");

    public static Number parseNumber(String s) throws InvalidTermException {
        Term t = new Parser(s).nextTerm(false);
        if (t instanceof Number)
            return (Number) t;
        throw new InvalidTermException("Term " + t + " is not a number.");
    }

    public static String removeApices(String st){
        if (st.startsWith("'") && st.endsWith("'") && st.length() > 2)
            return st.substring(1,st.length()-1);
       return st;
    }
}