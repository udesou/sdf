package org.metaborg.sdf2table.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import org.metaborg.sdf2table.grammar.CharacterClass;
import org.metaborg.sdf2table.grammar.ContextFreeSymbol;
import org.metaborg.sdf2table.grammar.IProduction;
import org.metaborg.sdf2table.grammar.LexicalSymbol;
import org.metaborg.sdf2table.grammar.Production;
import org.metaborg.sdf2table.grammar.Sort;
import org.metaborg.sdf2table.grammar.Symbol;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class Covenant {

    private final String filePath =
        "/Users/udesou/Documents/repositories/experiments/covenant/build/my_install_dir/bin/covenant";

    // enforce that number of productions in trees of p is > 1
    private final String inputString1;

    // enforce that number of productions in trees of q is > 1
    private final String inputString2;

    private final SCCNodes<Symbol> scc;
    private Set<IProduction> P;
    private Set<IProduction> Q;


    public Covenant(Set<IProduction> prodsP, Set<IProduction> prodsQ, SCCNodes<Symbol> scc) {
        this.scc = scc;
        this.P = prodsP;
        this.Q = prodsQ;

        inputString1 = createInputString("p");
        inputString2 = createInputString("q");
    }

    private String createInputString(String set) {
        StringBuffer grammar1 = new StringBuffer(), grammar2 = new StringBuffer();

        Set<IProduction> initialProdsG1, initialProdsG2;

        if(set.equals("p")) {
            if(P.size() > 1) {
                initialProdsG1 = createInitialProductionsEnforceHeight(P);
            } else {
                initialProdsG1 = createInitialProductions(P);
            }
            initialProdsG2 = createInitialProductions(Q);
        } else {
            initialProdsG1 = createInitialProductions(P);
            if(Q.size() > 1) {
                initialProdsG2 = createInitialProductionsEnforceHeight(Q);
            } else {
                initialProdsG2 = createInitialProductions(Q);
            }
        }

        for(IProduction iniProd : initialProdsG1) {
            grammar1.append(printProductionCovenant(iniProd));
        }

        for(IProduction iniProd : initialProdsG2) {
            grammar2.append(printProductionCovenant(iniProd));
        }

        Set<Symbol> sortsG1 = Sets.newHashSet();
        for(IProduction p : P) {
            grammar1.append(printProductionCovenant(p));
            for(Symbol s : p.rightHand()) {
                if(!((s instanceof CharacterClass) || s.toString().equals("LAYOUT?-CF")
                    || (s instanceof Sort && ((Sort) s).getType() != null))) {
                    sortsG1.add(s);
                }
            }
        }

        Set<Symbol> sortsG2 = Sets.newHashSet();
        for(IProduction q : Q) {
            grammar2.append(printProductionCovenant(q));
            for(Symbol s : q.rightHand()) {
                if(!((s instanceof CharacterClass) || s.toString().equals("LAYOUT?-CF")
                    || (s instanceof Sort && ((Sort) s).getType() != null))) {
                    sortsG2.add(s);
                }
            }
        }

        for(Symbol s : sortsG1) {
            Set<Symbol> recursiveSymbols = Sets.newHashSet(s);
            if(scc.getNodesMapping().get(s) != null) {
                recursiveSymbols.addAll(scc.getNodesMapping().get(s));
            }
            for(Symbol recSymb : recursiveSymbols) {
                grammar1.append(createProductionPlaceholder(s, recSymb));
            }

        }

        for(Symbol s : sortsG2) {
            Set<Symbol> recursiveSymbols = Sets.newHashSet(s);
            if(scc.getNodesMapping().get(s) != null) {
                recursiveSymbols.addAll(scc.getNodesMapping().get(s));
            }
            for(Symbol recSymb : recursiveSymbols) {
                grammar2.append(createProductionPlaceholder(s, recSymb));
            }
        }

        return "( " + grammar1.substring(0, grammar1.length() - 1) + " )" + " ( "
            + grammar2.substring(0, grammar2.length() - 1) + " )";

    }

    private Set<IProduction> createInitialProductions(Set<IProduction> prods) {
        Set<IProduction> result = Sets.newHashSet();

        Symbol lhs = new Sort("START_SYMB_COV");
        for(IProduction p : prods) {
            result.add(new Production(lhs, p.rightHand()));
        }

        return result;

    }

    private Set<IProduction> createInitialProductionsEnforceHeight(Set<IProduction> prods) {
        Set<IProduction> result = Sets.newHashSet();

        Symbol lhs = new Sort("START_SYMB_COV");
        for(IProduction p : prods) {

            for(int i = 0; i < p.rightHand().size(); i++) {
                Symbol s_rhs = p.rightHand().get(i);
                // if symbol is recursive
                if(s_rhs.equals(p.leftHand())
                    || (scc.getNodesMapping().get(lhs) != null && scc.getNodesMapping().get(lhs).contains(s_rhs))) {
                    result.addAll(createRecursiveProductions(p, i, prods));
                }
            }
        }

        return result;
    }

    private Set<IProduction> createRecursiveProductions(IProduction p, int pos, Set<IProduction> prods) {
        Set<IProduction> result = Sets.newHashSet();

        Symbol lhs = new Sort("START_SYMB_COV");
        List<Symbol> rhs = Lists.newArrayList();
        for(int i = 0; i < p.rightHand().size(); i++) {
            if(i != pos) {
                rhs.add(p.rightHand().get(i));
            }
        }

        for(IProduction prod : prods) {
            if(prod.equals(p))
                continue;
            List<Symbol> newRhs = Lists.newArrayList(rhs);
            newRhs.addAll(pos, prod.rightHand());
            result.add(new Production(lhs, newRhs));
        }


        return result;
    }

    private String createProductionPlaceholder(Symbol s1, Symbol s2) {
        return printNonTerminal(s1) + " -> " + "[ " + "\"#" + printNonTerminal(s2) + "\" ];";
    }

    private String printNonTerminal(Symbol s) {
        String result = "";
        if(s instanceof ContextFreeSymbol) {
            result = printNonTerminal(((ContextFreeSymbol) s).getSymbol()) + "_CF";
        } else if(s instanceof LexicalSymbol) {
            result = printNonTerminal(((LexicalSymbol) s).getSymbol()) + "_LEX";
        } else if(s instanceof Sort) {
            result = s.name();
        } else {
            System.err.println("COULD NOT PRINT NON-TERMINAL " + s + " TO COVENANT FORMAT");
        }
        return result;
    }

    private String printProductionCovenant(IProduction p) {
        String result = printNonTerminal(p.leftHand()) + " -> [";
        for(Symbol s : p.rightHand()) {
            if(s.toString().equals("LAYOUT?-CF")) {
                continue;
            }
            result += printNonTerminal(s) + " ";
        }

        return result + "];";
    }

    public String checkIntersection() {

        String result = "";
        System.out.println("|P| > 1");
        try {
            ProcessBuilder builder = new ProcessBuilder(filePath, "--input-string", inputString1, "--iter", "15");
            Process process = builder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            bufferedReader.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }

        if(result.contains("Found a solution")) {
            System.out.println("");
        }
        
        result = "";


        System.out.println("\n\n\n|Q| > 1");
        try {
            ProcessBuilder builder = new ProcessBuilder(filePath, "--input-string", inputString2, "--iter", "15");
            Process process = builder.start();
            InputStream inputStream = process.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 1);
            String line;
            while((line = bufferedReader.readLine()) != null) {
                result += line;
            }
            inputStream.close();
            bufferedReader.close();
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }
        
        if(result.contains("Found a solution")) {
            System.out.println("");
        }
        
        result = "";
        
        return null;
    }

    public SCCNodes<Symbol> getScc() {
        return scc;
    }


}
