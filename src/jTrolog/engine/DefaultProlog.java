package jTrolog.engine;

/**
 * @author ivar.orstavik@hist.no
 */
public class DefaultProlog extends Prolog {

   public DefaultProlog() {
        super(new String[0]);
        opNew(":-", XFX, 1200);
        opNew("-->", XFX, 1200);
        opNew(":-", FX, 1200);
        opNew("?-", FX, 1200);
        opNew(";", XFY, 1100);
        opNew("->", XFY, 1050);
        opNew(",", XFY, 1000);
        opNew("\\+", FY, 900);
        opNew("not", FY, 900);
        opNew("=", XFX, 700);
        opNew("\\=", XFX, 700);
        opNew("==", XFX, 700);
        opNew("\\==", XFX, 700);
        //opNew("@==",OperatorTable.XFX,700);
        //opNew("@\\==",OperatorTable.XFX,700);
        opNew("@>", XFX, 700);
        opNew("@<", XFX, 700);
        opNew("@=<", XFX, 700);
        opNew("@>=", XFX, 700);
        opNew("=:=", XFX, 700);
        opNew("=\\=", XFX, 700);
        opNew(">", XFX, 700);
        opNew("<", XFX, 700);
        opNew("=<", XFX, 700);
        opNew(">=", XFX, 700);
        opNew("is", XFX, 700);
        opNew("==..", XFX, 700);
        //opNew("?",OperatorTable.XFX,600);
        //opNew("@",OperatorTable.XFX,550);
        opNew("+", YFX, 500);
        opNew("-", YFX, 500);
        opNew("/\\", YFX, 500);
        opNew("\\/", YFX, 500);
        opNew("*", YFX, 400);
        opNew("/", YFX, 400);
        opNew("//", YFX, 400);
        opNew(">>", YFX, 400);
        opNew("<<", YFX, 400);
        opNew("rem", YFX, 400);
        opNew("mod", YFX, 400);
        opNew("**", XFX, 200);
        opNew("^", XFY, 200);
        opNew("\\", FX, 200);
        opNew("-", FY, 200);
    }
}
