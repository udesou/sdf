package org.metaborg.sdf2table.parsetable;

import java.io.Serializable;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class Accept extends Action implements Serializable {

	private static final long serialVersionUID = 4299391769734003915L;

	public Accept() {
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf, IParseTable pt) {
        return tf.makeAppl(tf.makeConstructor("accept", 0));
    }
    
    @Override public String toString() {        
        return "accept()";
    }

    @Override public int hashCode() {
        return "accept".hashCode();
    }

    @Override public boolean equals(Object obj) {
        return true;
    }

}
