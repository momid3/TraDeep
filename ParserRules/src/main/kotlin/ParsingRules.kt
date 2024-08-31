package com.momid

import com.momid.parser.expression.*
import com.momid.type.Type

@Type
val allowedName = condition { it.isLetter() } + some(condition { it.isLetterOrDigit() })

@Type
val parameter = spaces + allowedName["variableName"] + spaces

@Type
val parameters = splitBy(parameter, ",")

@Type
val functionCall = allowedName["functionName"] + insideOf('(', ')') {
    parameters
}["parameters"]

fun insideOf(parenthesesStart: Char, parenthesesEnd: Char, expression: () -> Expression): CustomExpression {
    return CustomExpression(
        TypeInfo(expression(), MultiExpression(arrayListOf(expression()["inside"])))
    ) { tokens, startIndex, endIndex, thisExpression ->
        if (startIndex >= endIndex) {
            return@CustomExpression null
        }
        if (tokens[startIndex] != parenthesesStart) {
            return@CustomExpression null
        }
        var numberOfLefts = 1
        for (tokenIndex in startIndex + 1 until endIndex) {
            if (tokens[tokenIndex] == parenthesesStart) {
                numberOfLefts += 1
            }
            if (tokens[tokenIndex] == parenthesesEnd) {
                numberOfLefts -= 1
            }
            if (numberOfLefts == 0) {
                val evaluation = eval(expression()["inside"], startIndex + 1, tokens, (tokenIndex + 1) - 1) ?: return@CustomExpression null
                return@CustomExpression MultiExpressionResult(ExpressionResult(thisExpression, startIndex .. tokenIndex + 1), arrayListOf(evaluation))
            }
        }
        return@CustomExpression null
    }
}
