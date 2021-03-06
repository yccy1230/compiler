package com.ecnu.compiler.component.CacheManager;

import com.ecnu.compiler.component.lexer.domain.DFA;
import com.ecnu.compiler.component.lexer.domain.RE;
import com.ecnu.compiler.component.parser.domain.CFG;
import com.ecnu.compiler.component.parser.domain.ParsingTable.LLParsingTable;
import com.ecnu.compiler.component.parser.domain.ParsingTable.LRParsingTable;
import com.ecnu.compiler.component.semantic.domain.AG;

import java.util.List;

/**
 * 保存一门语言的相关信息
 */
public class Language {
    //语言编号
    int mId;

    //RE列表,优先级高的排在列表前面
    List<RE> mREList;
    //DFA列表,优先级高的排在列表前面
    List<DFA> mDFAList;

    //CFG
    CFG mCFG;
    //CFG对应解析表
    //LL1
    LLParsingTable mLLParsingTable;
    //SLR
    LRParsingTable mSLRParsingTable;
    //LR
    LRParsingTable mLRParsingTable;
    //LALR
    LRParsingTable mLALRParsingTable;

    //AG
    AG mAG;

    public Language(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }

    public List<RE> getREList() {
        return mREList;
    }

    public void setREList(List<RE> REList) {
        mREList = REList;
    }

    public List<DFA> getDFAList() {
        return mDFAList;
    }

    public void setDFAList(List<DFA> DFAList) {
        mDFAList = DFAList;
    }

    public CFG getCFG() {
        return mCFG;
    }

    public void setCFG(CFG CFG) {
        mCFG = CFG;
    }

    public LLParsingTable getLLParsingTable() {
        return mLLParsingTable;
    }

    public void setLLParsingTable(LLParsingTable LLParsingTable) {
        mLLParsingTable = LLParsingTable;
    }

    public LRParsingTable getSLRParsingTable() {
        return mSLRParsingTable;
    }

    public void setSLRParsingTable(LRParsingTable SLRParsingTable) {
        mSLRParsingTable = SLRParsingTable;
    }

    public LRParsingTable getLRParsingTable() {
        return mLRParsingTable;
    }

    public void setLRParsingTable(LRParsingTable LRParsingTable) {
        mLRParsingTable = LRParsingTable;
    }

    public LRParsingTable getLALRParsingTable() {
        return mLALRParsingTable;
    }

    public void setLALRParsingTable(LRParsingTable LALRParsingTable) {
        mLALRParsingTable = LALRParsingTable;
    }

    public AG getAG() {
        return mAG;
    }

    public void setAG(AG AG) {
        mAG = AG;
    }
}
