package org.metaborg.sdf2table.grammar;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.metaborg.sdf2table.deepconflicts.Context;
import org.metaborg.sdf2table.jsglrinterfaces.ISGLRCharacters;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.collect.Lists;

public class CharacterClass extends Symbol implements ISGLRCharacters {

    private static final long serialVersionUID = 1619024888383357090L;

    public static CharacterClass maxCC =
        new CharacterClass(new CharacterClassRange(new CharacterClassNumeric(0), new CharacterClassNumeric(256)));
    public static CharacterClass emptyCC = new CharacterClass(null);

    private final Symbol cc;

    public CharacterClass(Symbol s) {
        this.cc = s;
    }

    public Symbol getCharacterClass() {
        return cc;
    }

    public static CharacterClass union(CharacterClass... ary) {
        int _min = minimum(ary);
        int _max = maximum(ary);

        // We want to find all sub ranges that are not included in {restrictions}.
        // min and max correspond to the current valid range.
        int min = -1;
        int max = -1;

        // The final restricted terminal.
        Symbol rt = null;

        // For each value of the range (plus a last iteration).
        for(int c = _min; c <= _max + 1; ++c) {
            boolean ok = false; // The value is valid or not (restricted or not).

            // if we are in the range...
            if(c <= _max) {
                ok = false;
                for(CharacterClass cc : ary) {
                    if(cc.containsCharacter(c)) {
                        ok = true; // valid.
                        break;
                    }
                }

                if(ok) { // We augment our current range (or create it if min == -1).
                    if(min == -1)
                        min = c;
                    max = c;
                }
            }

            // We are out of a valid range, but we have detected one.
            if(!ok && min != -1) {
                Symbol t;
                if(min == max)
                    t = new CharacterClassNumeric(min); // Single-value range, a numeric.
                else
                    t = new CharacterClassRange(new CharacterClassNumeric(min), new CharacterClassNumeric(max));

                if(rt == null)
                    rt = t; // it's the first, and may be the only, range.
                else
                    rt = new CharacterClassConc(rt, t); // Not the first one, we merge them.
                min = max = -1;
            }
        }

        return new CharacterClass(rt);
    }

    public static CharacterClass intersection(CharacterClass... ary) {
        int _min = minimum(ary);
        int _max = maximum(ary);

        // We want to find all sub ranges that are not included in {restrictions}.
        // min and max correspond to the current valid range.
        int min = -1;
        int max = -1;

        // The final restricted terminal.
        Symbol rt = null;
        // For each value of the range (plus a last iteration).
        for(int c = _min; c <= _max + 1; ++c) {
            boolean ok = false; // The value is valid or not (restricted or not).

            // if we are in the range...
            if(c <= _max) {
                ok = true;
                for(CharacterClass cc : ary) {
                    if(!cc.containsCharacter(c)) {
                        ok = false; // invalid.
                        break;
                    }
                }

                if(ok) { // We augment our current range (or create it if min == -1).
                    if(min == -1)
                        min = c;
                    max = c;
                }
            }

            // We are out of a valid range, but we have detected one.
            if(!ok && min != -1) {
                Symbol t;
                if(min == max)
                    t = new CharacterClassNumeric(min); // Single-value range, a numeric.
                else
                    t = new CharacterClassRange(new CharacterClassNumeric(min), new CharacterClassNumeric(max));

                if(rt == null)
                    rt = t; // it's the first, and may be the only, range.
                else
                    rt = CharacterClass.union(new CharacterClass(rt), new CharacterClass(t)).cc; // Not the first one,
                                                                                                 // we merge them.
                min = max = -1;
            }
        }

        return new CharacterClass(rt);
    }

    @Override public boolean containsCharacter(int character) {
        if(cc == null)
            return false;

        if(cc instanceof CharacterClassNumeric) {
            return ((CharacterClassNumeric) cc).contains(character);
        } else if(cc instanceof CharacterClassRange) {
            return ((CharacterClassRange) cc).contains(character);
        } else {
            return ((CharacterClassConc) cc).contains(character);
        }
    }

    public int minimum() {

        if(cc == null)
            return Integer.MAX_VALUE;

        if(cc instanceof CharacterClassNumeric) {
            return ((CharacterClassNumeric) cc).minimum();
        } else if(cc instanceof CharacterClassRange) {
            return ((CharacterClassRange) cc).minimum();
        } else {
            return ((CharacterClassConc) cc).minimum();
        }
    }

    public int maximum() {

        if(cc == null)
            return Integer.MIN_VALUE;

        if(cc instanceof CharacterClassNumeric) {
            return ((CharacterClassNumeric) cc).maximum();
        } else if(cc instanceof CharacterClassRange) {
            return ((CharacterClassRange) cc).maximum();
        } else {
            return ((CharacterClassConc) cc).maximum();
        }
    }

    public static int minimum(CharacterClass... ary) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i < ary.length; ++i) {
            if(ary[i].minimum() < min)
                min = ary[i].minimum();
        }

        return min;
    }

    public static int maximum(CharacterClass... ary) {
        int max = Integer.MIN_VALUE;
        for(int i = 0; i < ary.length; ++i) {
            if(ary[i].maximum() > max)
                max = ary[i].maximum();
        }

        return max;
    }

    public CharacterClass difference(CharacterClass... ary) {
        if(cc == null)
            return this;

        if(cc instanceof CharacterClassNumeric) {
            return ((CharacterClassNumeric) cc).difference(ary);
        } else if(cc instanceof CharacterClassRange) {
            return ((CharacterClassRange) cc).difference(ary);
        } else {
            return ((CharacterClassConc) cc).difference(ary);
        }
    }

    @Override public String name() {
        if(cc == null)
            return "[]";
        String name = "[";
        name += cc.name() + "]";
        return name;
    }

    @Override public IStrategoTerm toAterm(ITermFactory tf) {
        if(cc == null) {
            return tf.makeAppl(tf.makeConstructor("char-class", 1), tf.makeList());
        }

        IStrategoTerm cc_aterm = cc.toAterm(tf);
        if(cc_aterm instanceof IStrategoList) {
            List<IStrategoTerm> terms = Lists.newArrayList();
            for(IStrategoTerm child : cc_aterm.getAllSubterms()) {
                terms.add(child);
            }
            return tf.makeAppl(tf.makeConstructor("char-class", 1), tf.makeList(terms));
        }
        return tf.makeAppl(tf.makeConstructor("char-class", 1), tf.makeList(cc_aterm));

    }

    public IStrategoTerm toStateAterm(ITermFactory tf) {
        if(cc == null) {
            return tf.makeList();
        }

        IStrategoTerm cc_aterm = cc.toAterm(tf);
        if(cc_aterm instanceof IStrategoList) {
            List<IStrategoTerm> terms = Lists.newArrayList();
            for(IStrategoTerm child : cc_aterm.getAllSubterms()) {
                terms.add(child);
            }
            return tf.makeList(terms);
        }
        return tf.makeList(cc_aterm);
    }
    
//    public boolean contains(int c) {
//        if (cc == null) return false;
//        return ((CharacterClass) cc).contains(c);
//    }

    @Override public IStrategoTerm toSDF3Aterm(ITermFactory tf,
        Map<Set<Context>, Integer> ctx_vals, Integer ctx_val) {
        if(cc == null) {
            return tf.makeAppl(tf.makeConstructor("CharClass", 1),
                tf.makeAppl(tf.makeConstructor("Simple", 1), tf.makeAppl(tf.makeConstructor("Absent", 0))));
        }
        return tf.makeAppl(tf.makeConstructor("CharClass", 1), tf.makeAppl(tf.makeConstructor("Simple", 1),
            tf.makeAppl(tf.makeConstructor("Present", 1), cc.toSDF3Aterm(tf, ctx_vals, ctx_val))));
    }

    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cc == null) ? 0 : cc.hashCode());
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        CharacterClass other = (CharacterClass) obj;
        if(cc == null) {
            if(other.cc != null)
                return false;
        } else if(!cc.equals(other.cc))
            return false;
        return true;
    }
}
