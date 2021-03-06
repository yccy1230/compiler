package com.ecnu.compiler.component.parser.domain;

import com.ecnu.compiler.component.storage.ErrorList;
import com.ecnu.compiler.constant.StatusCode;

import java.util.*;

/**
 * 上下文无关文法
 *
 */
public class CFG {
    //非终结符映射
    private Map<Symbol, List<Integer>> mNonTerminalMap = new LinkedHashMap<>();
    //终结符集合
    private Set<Symbol> mTerminalSet = new HashSet<>();
    //产生式列表
    private List<Production> mProductions = new ArrayList<>();
    //开始符号
    private Symbol mStartSymbol = null;
    //是否构造成功
    private boolean mIsSucessfulBuild;

    public Set<Symbol> getSymbolSet(List<String> productionStrList) {
        Set<Symbol> symbolSet = new HashSet<>();
        for (String s : productionStrList) {
            String[] arr = s.split("->");
            if (arr.length != 2) {
                return null;
            }
            Symbol left = new Symbol(arr[0]);
            symbolSet.add(left);
        }
        for (String s : productionStrList) {
            String[] arr = s.split("->");
            String right = arr[1];
            String[] rightArr = right.split("\\|");
            for (String rs : rightArr) {
                String[] rRightArr = rs.split(" ");
                for (String rrs : rRightArr) {
                    boolean flag = false;
                    Symbol rightSym = new Symbol(rrs);
                    for (Symbol sym : symbolSet) {
                        if (sym.getName().equals(rightSym.getName())) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        symbolSet.add(rightSym);
                    }
                }
            }
        }
//        System.out.println("*************");
//        for (Symbol s : symbolSet) {
//            System.out.println(s.getName() + "----" + s.isTerminal());
//        }
//        System.out.println("*************");
        return symbolSet;
    }

    /**
     * 构造函数，需要传入CFG的列表
     */

    public CFG(List<String> productionStrList, ErrorList errorList) {
        mIsSucessfulBuild = true;
        Set<Symbol> mSymbolSet = getSymbolSet(productionStrList);
        if (mSymbolSet == null || mSymbolSet.isEmpty()) {
            return;
        }
        int nonTermId = 1;
        int prodId = 1;
        Set<String> stringSet = new HashSet<>();
        for (Symbol sym : mSymbolSet) {
            stringSet.add(sym.getName());
        }
        for (String item : productionStrList) {
            item = item.trim();
            String[] result = item.split("->");
            if (result.length != 2) {
                mIsSucessfulBuild = false;
                errorList.addErrorMsg("CFG格式错误", StatusCode.ERROR_INIT);
                return;
            }
            String leftStr = result[0].trim();
            List<Integer> integerList = new ArrayList<>();
            integerList.add(0);
            Symbol leftSym = null;
            if (!stringSet.contains(leftStr)) {
                mIsSucessfulBuild = false;
                errorList.addErrorMsg("CFG格式错误：无法识别的左部 \"" + leftStr + "\"，请确认输入。"
                        , StatusCode.ERROR_INIT);
                return;
            } else {
                for (Symbol sym : mSymbolSet) {
                    if (sym.getName().equals(leftStr)) {
                        leftSym = sym;
                        //只有初次执行到这里，才会把当前的左部作为开始符号
                        if (mStartSymbol == null) {
                            mStartSymbol = leftSym;
                        }
                        mTerminalSet.remove(sym);
                        sym.setTerminal(false);
                        mNonTerminalMap.put(sym, integerList);
                        break;
                    }
                }
            }
            String[] rightArr = result[1].trim().split("\\|");
            for (String rStr : rightArr) {
                List<Symbol> rightList = new ArrayList<>();
                String[] rightStrList = rStr.trim().split(" ");
                for (String s : rightStrList) {
                    s = s.trim();
                    for (Symbol sym : mSymbolSet) {
                        if (sym.getName().equals(s)) {
                            rightList.add(sym);
                            if (!mNonTerminalMap.containsKey(sym)) {
                                mTerminalSet.add(sym);
                            }
                            break;
                        }
                    }
                }
//                for (int i = 1; i <= rStr.length() && !rStr.isEmpty(); i++) {
//                    String s = rStr.substring(0, i);
//                    while (stringSet.contains(s) && !rStr.isEmpty()) {
//                        for (Symbol sym : mSymbolSet) {
//                            if (sym.getName().equals(s)) {
//                                rightList.add(sym);
//                                rStr = rStr.substring(i);
//                                if (!mNonTerminalMap.containsKey(sym)) {
//                                    mTerminalSet.add(sym);
//                                }
//                                break;
//                            }
//                        }
//                        if (i <= rStr.length()) {
//                            s = rStr.substring(0, i);
//                        } else {
//                            break;
//                        }
//                    }
//                }
                Production prod = new Production(leftSym, rightList, prodId++);
                mProductions.add(prod);
            }
        }
        setListForMap(mNonTerminalMap);
        //this.cleanLeftRecursion();
        //this.extractLeftCommonFactor();
    }

    public boolean isSucessfulBuild() {
        return mIsSucessfulBuild;
    }

    public Symbol getStartSymbol() {
        return mStartSymbol;
    }

    public Set<Symbol> getTerminalSet(){
        return mTerminalSet;
    }

    public Set<Symbol> getNonTerminalSet(){
        return mNonTerminalMap.keySet();
    }

    public Map<Symbol, List<Integer>> getNonTerminalMap() {
        return mNonTerminalMap;
    }

    public List<Production> getAllProductions() {
        return mProductions;
    }

    public void setAllProductions(List<Production> mProductions) {
        this.mProductions = mProductions;
    }

    /**
     * 获取某个非终结符对应的产生式
     * @param symbol 一个非终结符
     * @return 终结符对应的产生式
     */
    public List<Production> getProductions(Symbol symbol) {
        List<Integer> indexList = mNonTerminalMap.get(symbol);
        if (indexList == null)
            return null;
        List<Production> productions = new ArrayList<>();
        for (Integer index : indexList){
            for (Production prod : getAllProductions()) {
                if (prod.getId() == index) {
                    productions.add(prod);
                }
            }
        }
        return productions;
    }

    //给非终结符的映射配置对应的产生式
    public void setListForMap(Map<Symbol, List<Integer>> map) {
        for (Symbol sym : map.keySet()) {
            List<Integer> intList = new ArrayList<>();
            for (Production prod : mProductions) {
                if (prod.getLeft() == sym) {
                    intList.add(prod.getId());
                }
            }
            map.put(sym, intList);
        }
    }

    public void cleanImmediateLeftRecursion(Symbol symbol) {
        List<Production> productions = getProductions(symbol);
        List<List<Symbol>> alpha = new ArrayList<>();
        List<List<Symbol>> beta = new ArrayList<>();
        for (Production prod : productions) {
            List<Symbol> right = prod.getRight();
            if (symbol.equals(right.get(0))) {
                List<Symbol> list = new ArrayList<>();
                for (int i = 1; i < right.size(); i++) {
                    list.add(right.get(i));
                }
                alpha.add(list);
            } else {
                beta.add(right);
            }
        }
        if (alpha.size() == 0) {
            return;
        } else {
            removeProductionsByLeft(symbol);
            int id = getMaxId();
            List<Production> allProductions = getAllProductions();
            Symbol prime = new Symbol(symbol.getName() + "\'");
            while (getNonTerminalSet().contains(prime)) {
                prime = new Symbol(prime.getName() + "\'");
            }
            for (List<Symbol> l : beta) {
                l.add(prime);
                Production p = new Production(symbol, l, ++id);
                allProductions.add(p);
            }
            List<Integer> nonTermList = new ArrayList<>();
            for (List<Symbol> l : alpha) {
                l.add(prime);
                Production p = new Production(prime, l, ++id);
                allProductions.add(p);
                nonTermList.add(id);
            }
            List<Symbol> empty = new ArrayList<>();
            empty.add(Symbol.EMPTY_SYMBOL);
            Production p = new Production(prime, empty, ++id);
            allProductions.add(p);
            setAllProductions(allProductions);
            getNonTerminalMap().put(prime, nonTermList);
        }
        setListForMap(mNonTerminalMap);
    }

    private void removeProductionsByLeft(Symbol symbol) {
        List<Production> productions = getProductions(symbol);
        List<Production> allProductions = getAllProductions();
        allProductions.removeAll(productions);
        setAllProductions(allProductions);
    }

    public int getMaxId() {
        int max = 0;
        for (Production prod : mProductions) {
            if (prod.getId() > max) {
                max = prod.getId();
            }
        }
        return max;
    }

    public void cleanLeftRecursion() {
        Set<Symbol> nonTerminalSet = getNonTerminalSet();
        Map<Integer, Symbol> map = new HashMap<>();
        int id = 1;
        for (Symbol sym : nonTerminalSet) {
            map.put(id++, sym);
        }
        int size = nonTerminalSet.size();
        for (int i = 1; i <= size; i++) {
            Symbol Ai = map.get(i);
            for (int j = 1; j <= i - 1; j++) {
                Symbol Aj = map.get(j);
                List<Production> productions = getAllProductions();
                for (Production prod : getProductions(Ai)) {
                    List<Symbol> right = prod.getRight();
                    if (right.get(0).equals(Aj)) {
                        right.remove(0);
                        productions.remove(prod);
                        for (Production p : getProductions(Aj)) {
                            List<Symbol> r = p.getRight();
                            r.addAll(right);
                            Production newProd = new Production(Ai, r, (getMaxId() + 1));
                            productions.add(newProd);
                        }
                    }
                }
                setAllProductions(productions);
            }
            cleanImmediateLeftRecursion(Ai);
        }
        setListForMap(mNonTerminalMap);
    }

    public void extractLeftCommonFactor() {
        boolean changed = true;
        Map<Symbol, List<Integer>> tempMap = new HashMap<>();
        for (Symbol sym : getNonTerminalSet()) {
            List<Production> productions = getProductions(sym);
            if (productions.size() <= 1) continue;
            List<Symbol> prefix = getPrefix(productions);
            int size = prefix.size();
            Symbol prime = null;
            List<Integer> nonTermList = new ArrayList<>();
//        System.out.println("LCP:");
//        for (Symbol s : lcp) {
//            System.out.println(s.getName());
//        }
            if (size > 0) {
                prime = new Symbol(sym.getName() + "\'");
                while (getNonTerminalSet().contains(prime)) {
                    prime = new Symbol(prime.getName() + "\'");
                }
                for (Production prod : productions) {
                    List<Symbol> right = prod.getRight();
                    List<Symbol> subList = right.subList(0, size);
                    if (subList.equals(prefix)) {
                        mProductions.remove(prod);
                        List<Symbol> newRight = new ArrayList<>();
                        for (Symbol s : right) {
                            if (!subList.contains(s)) {
                                newRight.add(s);
                            }
                        }
                        if (newRight.isEmpty()) {
                            newRight.add(Symbol.EMPTY_SYMBOL);
                        }
                        int id = getMaxId() + 1;
                        Production newProd = new Production(prime, newRight, id);
                        mProductions.add(newProd);
                        nonTermList.add(id);
                    }
                }
                List<Symbol> newRight = new ArrayList<>(prefix);
                newRight.add(prime);
                int id = getMaxId() + 1;
                Production newProd = new Production(sym, newRight, id);
                mProductions.add(newProd);
                nonTermList.add(id);
                tempMap.put(prime, nonTermList);
            } else {
                return;
            }
        }
        mNonTerminalMap.putAll(tempMap);
    }

    private List<Symbol> getPrefix(List<Production> productions) {
        int min = 65536;
        List<Symbol> minRight = new ArrayList<>();
        for (Production prod : productions) {
            List<Symbol> right = prod.getRight();
            if (right.size() < min) {
                min = right.size();
            }
        }
        while (min > 0) {
            List<Symbol> result;
            for (Production p : productions) {
                int match = 0;
                minRight = p.getRight().subList(0, min);
                for (Production prod : productions) {
                    result = prod.getRight().subList(0, min);
                    if (result.containsAll(minRight) && minRight.containsAll(result)) {
                        match++;
                    }
                }
                if (match >= 2) {
                    return minRight;
                }
            }
            min--;
        }
        return minRight;
    }

}
