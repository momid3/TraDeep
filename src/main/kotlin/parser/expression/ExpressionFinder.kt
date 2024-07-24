package com.momid.parser.expression

class ExpressionFinder {

    var registeredExpressions = ArrayList<Expression>()

    fun registerExpressions(expressions: List<Expression>) {
        registeredExpressions.addAll(expressions)
    }

    fun start(tokens: List<Char>, findingRange: IntRange? = null): List<ExpressionResult> {
        val foundExpressions = ArrayList<ExpressionResult>()
        var currentTokenIndex = findingRange?.start ?: 0
        val maxTokenIndex = findingRange?.last ?: tokens.size
        whi@ while (true) {
            for (expression in registeredExpressions) {
                if (currentTokenIndex >= maxTokenIndex) {
                    break@whi
                }
                val expressionResult = eval(expression, currentTokenIndex, tokens, maxTokenIndex) ?: continue
                currentTokenIndex = expressionResult.nextTokenIndex
                foundExpressions.add(expressionResult)
                continue@whi
            }
//            println(
//                red("no more expressions found from here " + tokens.joinToString("")
//                    .slice(currentTokenIndex..min(currentTokenIndex + 10, maxTokenIndex - 1))) + "..."
//            )
            break
        }
        return foundExpressions
    }

    fun startDiscover(tokens: List<Char>, findingRange: IntRange? = null): List<ExpressionResult> {
        val foundExpressions = ArrayList<ExpressionResult>()
        var currentTokenIndex = findingRange?.start ?: 0
        val maxTokenIndex = findingRange?.last ?: tokens.size
        whi@ while (true) {
        for (expression in registeredExpressions) {
            if (currentTokenIndex >= maxTokenIndex) {
                break@whi
            }
            val expressionResult = eval(expression, currentTokenIndex, tokens, maxTokenIndex) ?: continue
            currentTokenIndex = expressionResult.nextTokenIndex
            foundExpressions.add(expressionResult)
            continue@whi
        }
            currentTokenIndex += 1
        }
        return foundExpressions
    }
}
