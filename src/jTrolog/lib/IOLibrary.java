/*
 * tuProlog - Copyright (C) 2001-2002  aliCE team at deis.unibo.it
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jTrolog.lib;

import jTrolog.engine.*;
import jTrolog.terms.*;
import jTrolog.terms.Double;
import jTrolog.terms.Number;
import jTrolog.parser.Parser;

import java.io.*;
import java.util.HashMap;
import java.util.Random;

/**
 * This class provides basic I/O predicates.
 *
 * @author ivar.orstavik@hist.no
 */
public class IOLibrary extends Library {

    private HashMap streams;

    private String in;
    private String out;

    private Random gen = new Random();

    public IOLibrary() {
        streams = new HashMap();
        in = "stdin";
        out = "stdout";
    }

    public boolean see_1(BindingsTable bt, Term arg0) throws Exception {
        if (!BasicLibrary.atom_1(bt, arg0))
            return false;

        String requested = ((Struct) arg0).name;
        //1. close already open stream if seeing a new input, then rename current in
        if (! requested.equals(in)) {
            InputStream inputStream = getInputStream(in);
            if (inputStream != null)
                inputStream.close();
            in = requested;
        }

        //2. if the requested stream is not already open, then open it
        InputStream newIn = getInputStream(in);
        if (newIn == null) {
            newIn = new FileInputStream(in);
            streams.put(in, newIn);
        }
        return true;
    }

    public boolean seen_0(BindingsTable bt) throws Exception {
        InputStream inputStream = getInputStream(in);
        if (inputStream != null)
            inputStream.close();
        in = "stdin";
        return true;
    }

    public boolean seeing_1(BindingsTable bt, Term t) {
        return bt.unify(t, new StructAtom(in));
    }

    public boolean tell_1(BindingsTable bt, Term arg0) throws Exception {
        if (!BasicLibrary.atom_1(bt, arg0))
            return false;

        String requested = ((Struct) arg0).name;
        //1. close already open stream if seeing a new input, then rename current in
        if (! requested.equals(out)) {
            OutputStream inputStream = getOutputStream(out);
            if (inputStream != null)
                inputStream.close();
            out = requested;
        }

        //2. if the requested stream is not already open, then open it
        OutputStream newOut = getOutputStream(out);
        if (newOut == null) {
            newOut = new FileOutputStream(out);
            streams.put(out, newOut);
        }
        return true;
    }

    public boolean told_0(BindingsTable bt) throws Exception {
        OutputStream outputStream = getOutputStream(out);
        if (outputStream != null)
            outputStream.close();
        out = "stdout";
        return true;
    }

    public boolean telling_1(BindingsTable bt, Term arg0) {
        return bt.unify(arg0, new StructAtom(out));
    }

    public boolean put_1(BindingsTable bt, Term arg0) throws Exception {
        if (!BasicLibrary.atom_1(bt, arg0))
            return false;

        String ch = ((Struct) arg0).name;
        if (ch.length() > 1)
            return false;
        getOutputStream(out).write((byte) ch.charAt(0));
        return true;
    }

    public boolean get0_1(BindingsTable bt, Term arg0) throws Exception {
        int ch = getInputStream(in).read();
        Term a1 = ch == -1 ? (Term)new Int(-1) : (Term)new StructAtom(Character.toString((char) ch));
        return bt.unify(arg0, a1);
    }

    public boolean get_1(BindingsTable bt, Term arg0) throws Exception {
        InputStream inputStream = getInputStream(in);
        int ch;
        do {
            ch = inputStream.read();
        } while (ch < 0x20 && ch >= 0);
        Term a1 = ch == -1 ? (Term)new Int(-1) : (Term)new StructAtom(Character.toString((char) ch));
        return bt.unify(arg0, a1);
    }

    public boolean tab_1(BindingsTable bt, Int arg) throws Exception {
        int n = arg.intValue();
        OutputStream outputStream = getOutputStream(out);
        for (int i = 0; i < n; i++)
            outputStream.write(0x20);
        return true;
    }

    public boolean read_1(BindingsTable bt, Term arg0) throws Exception {
        InputStream inputStream = getInputStream(in);
        StringBuffer res = new StringBuffer();
        boolean single_quotes = false;
        boolean double_quotes = false;
        do {
            int ch = inputStream.read();
            if (ch == -1)
                break;
            else if (ch == '\'')
                single_quotes = !single_quotes;
            else if (ch == '\"')
                double_quotes = !double_quotes;
            else if (ch == '.' && !(single_quotes || double_quotes))
                break;
            res.append((char)ch);
        } while (true);
        Term result = new Parser(res.toString(), ((Library) this).engine).nextTerm(false);
        return bt.unify(arg0, result);
    }

    public boolean write_1(BindingsTable bt, Term arg0) throws Exception {
        OutputStream outputStream = getOutputStream(out);
        outputStream.write(arg0.toString().getBytes());
        return true;
    }

    public boolean print_1(BindingsTable bt, Term arg0) throws Exception {
        OutputStream outputStream = getOutputStream(out);
        outputStream.write(Parser.removeApices(arg0.toString()).getBytes());
        return true;
    }

    public boolean nl_0(BindingsTable bt) throws Exception {
        OutputStream outputStream = getOutputStream(out);
        outputStream.write('\n');
        return true;
    }

    /**
     * reads a source text from a file.
     * <p/>
     * It's useful used with agent predicate:
     * text_from_file(File,Source), agent(Source).
     */
    public boolean text_from_file_2(BindingsTable bt, Term file_name, Term text) throws IOException {
        StringBuffer res = new StringBuffer();
        BufferedReader in;
        Object possibleInput = streams.get(Parser.removeApices(file_name.toString()));
        if (possibleInput instanceof InputStream){
            in = new BufferedReader(new InputStreamReader((InputStream) possibleInput));
        } else {
            in = new BufferedReader(new FileReader("infilename"));
        }
        String str;
        while ((str = in.readLine()) != null)
            res.append(str);
        in.close();

        return bt.unify(text, new StructAtom(res.toString()));
    }

    // miscellanea

    public boolean rand_float_1(BindingsTable bt, Term t) {
        return bt.unify(t, new Double(gen.nextFloat()));
    }

    public boolean rand_int_2(BindingsTable bt, Number argNum, Term num) {
        return bt.unify(num, new Int(gen.nextInt(argNum.intValue())));
    }

    public String getTheory() {
        return "consult(File) :- text_from_file(File,Text), add_theory(Text).\n" +
            "reconsult(File) :- text_from_file(File,Text), set_theory(Text).\n" +
            "solve_file(File,Goal) :- text_from_file(File,Text),text_term(Text,Goal),call(Goal).\n";
    }

    public static String readStream(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null)
            sb.append(line).append("\n");
        br.close();
        return sb.toString();
    }

    private OutputStream getOutputStream(String out) {
        if ("stdout".equals(out))
            return ((Library) this).engine.getPrintStream();
        return (OutputStream) streams.get(out);
    }

    private InputStream getInputStream(String in) {
        if ("stdout".equals(in))
            return System.in;
        return (InputStream) streams.get(in);
    }
}