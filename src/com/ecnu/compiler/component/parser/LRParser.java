package com.ecnu.compiler.component.parser;

import com.ecnu.compiler.component.parser.base.Parser;
import com.ecnu.compiler.component.parser.domain.Symbol;
import com.ecnu.compiler.component.parser.domain.TD;
import com.ecnu.compiler.component.storage.SymbolTable;

/**
 * Bottom-Up Parsing, general LR
 *
 * @author Michael Chen
 * @date 2018-05-01 19:58
 */
public class LRParser extends Parser {
    @Override
    public TD<Symbol> getSyntaxTree(SymbolTable symbolTable) {
        return null;
    }
}
