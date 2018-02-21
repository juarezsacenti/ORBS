package br.ufsc.lisa.sedim.core.io;

public class RDFTriple {

    private final String subject;
    private final String predicate;
    private final String object;

    public RDFTriple(String s, String p, String o) {
        this.subject = s;
        this.predicate = p;
        this.object = o;
    }

    public String getSubject() { return subject; }
    public String getPredicate() { return predicate; }
    public String getObject() { return object; }
}
