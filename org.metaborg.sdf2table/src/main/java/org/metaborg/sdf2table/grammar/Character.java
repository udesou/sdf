package org.metaborg.sdf2table.grammar;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Map;
import java.util.Set;

import org.metaborg.sdf2table.deepconflicts.Context;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public abstract class Character implements Serializable {

    private static final long serialVersionUID = -5026630907913651353L;

    public abstract BitSet getBitSet();
    
    public abstract String name();
    
    public abstract int hashCode();
    
    public abstract boolean contains(int c);

    public abstract boolean equals(Object s);

    public abstract IStrategoTerm toAterm(ITermFactory tf);

    public abstract IStrategoTerm toSDF3Aterm(ITermFactory tf, Map<Set<Context>, Integer> ctx_vals, Integer ctx_val);

    public abstract CharacterClass difference(CharacterClass[] ary);

}
