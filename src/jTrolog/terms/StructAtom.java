package jTrolog.terms;

public class StructAtom extends Struct {

    public StructAtom(String name) {
        super(name, new Term[0]);
        type = Term.ATOM;
    }

    public boolean equals(Object t) {
        return t instanceof StructAtom && name == ((StructAtom) t).name;
    }

    public String toString() {
        return name;
    }

    public String toStringSmall() {
        return name.length() < 25 ? name : name.substring(0, 23) + "..";
    }
}