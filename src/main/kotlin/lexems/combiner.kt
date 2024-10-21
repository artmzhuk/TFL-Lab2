package lexems

import net.automatalib.automaton.fsa.CompactDFA

fun combineLexems(size: Int, nesting: Int, lexems: LexemBundle):CompactDFA<String> {
    val res = programCompute(size, nesting, lexems)
    return res
}

fun programCompute(size: Int, nesting: Int, lexems: LexemBundle):CompactDFA<String>{
    val eol = lexems.eolDFA
    val definition = definitionCompute(size, nesting, lexems)

    var res = kleeneOneOrMore(eol) // [eol]+
    res = concatenateAutomata(definition, res) // [definition][eol]+
    res = kleeneOneOrMore(res) // ([definition][eol]+)+
    val eolKleeneStar = kleeneStar(eol) // [eol]*
    res = concatenateAutomata(eolKleeneStar, res) // [eol]*([definition][eol]+)+
    return res
}

fun definitionCompute(size: Int, nesting: Int, lexems: LexemBundle):CompactDFA<String>{
    val const = lexems.constDFA
    val lbr1 = lexems.lbr1DFA
    val rbr1 = lexems.rbr1DFA
    val eol = lexems.eolDFA
    val sentence = sentenceCompute(size, nesting, lexems)

    var res = concatenateAutomata(const, lbr1) // [const] [lbr1]
    var eolKleene = kleeneStar(eol) // [eol]*
    var tmp1 = concatenateAutomata(eolKleene, sentence) // [eol]*[sentence]
    tmp1 = kleeneStar(tmp1) // ([eol]*[sentence])*
    res = concatenateAutomata(res, tmp1) // [const] [lbr1] ([eol]*[sentence])*
    res = concatenateAutomata(res, eolKleene) // [const] [lbr1] ([eol]*[sentence])*[eol]*
    res = concatenateAutomata(res, rbr1) // [const] [lbr1] ([eol]*[sentence])*[eol]*[rbr1]
    return res
}

fun sentenceCompute(size: Int, nesting: Int, lexems: LexemBundle):CompactDFA<String>{
    val equal = lexems.equalDFA
    val sep = lexems.sepDFA
    val pattern = patternCompute(size, nesting, lexems, 0, 0)
    val expression = expressionCompute(size, nesting, lexems, 0, 0)
    var res = concatenateAutomata(pattern, equal)
    res = concatenateAutomata(res, expression)
    res = concatenateAutomata(res, sep)
    return lexems.blankDFA

}

fun patternCompute(size: Int, nesting: Int, lexems: LexemBundle, currentDepth2:Int, currentDepth3:Int):CompactDFA<String>{
    var res = dfaUnion(lexems.varDFA, lexems.constDFA)
    if (currentDepth3 < nesting){
        var tmp1 = patternSeqCompute(size, nesting, lexems, currentDepth2, currentDepth3+1)
        tmp1 = concatenateAutomata(lexems.lbr3DFA, tmp1)
        tmp1= concatenateAutomata(tmp1, lexems.rbr3DFA)
        res = dfaUnion(res, tmp1)
    }
    return res
}

fun patternSeqCompute(size: Int, nesting: Int, lexems: LexemBundle, currentDepth2:Int, currentDepth3:Int):CompactDFA<String>{
    val pattern = patternCompute(size, nesting, lexems, currentDepth2, currentDepth3)
    var res = concatenateAutomata(lexems.blankDFA, pattern)
    res = kleeneStar(res)
    res = concatenateAutomata(pattern, res)
    return res
}

fun expressionCompute(size: Int, nesting: Int, lexems: LexemBundle, currentDepth2:Int, currentDepth3:Int):CompactDFA<String>{
    var res = dfaUnion(lexems.varDFA, lexems.constDFA)
    if(currentDepth3 < nesting){
        //println("expr3 depth $currentDepth3")
        var tmp1 = expressionSeqCompute(size, nesting, lexems, currentDepth2, currentDepth3+1)
        tmp1 = concatenateAutomata(lexems.lbr3DFA, tmp1)
        tmp1 = concatenateAutomata(tmp1, lexems.rbr3DFA)
        res = dfaUnion(res, tmp1)
    }
    if (currentDepth2 < nesting){
        val expressionSeq = expressionCompute(size, nesting, lexems, currentDepth2+1, currentDepth3)
        var tmp2 = concatenateAutomata(lexems.lbr2DFA, lexems.constDFA)
        tmp2 = concatenateAutomata(tmp2, lexems.blankDFA)
        tmp2 = concatenateAutomata(tmp2, expressionSeq)
        tmp2 = concatenateAutomata(tmp2, lexems.rbr3DFA)
        res = dfaUnion(res, tmp2)
    }
    return res
}

fun expressionSeqCompute(size: Int, nesting: Int, lexems: LexemBundle, currentDepth2:Int, currentDepth3:Int):CompactDFA<String>{
    val expression = expressionCompute(size, nesting, lexems, currentDepth2, currentDepth3)
    var res = concatenateAutomata(lexems.blankDFA, expression)
    res = kleeneStar(res)
    res = concatenateAutomata(expression, res)
    return res
}