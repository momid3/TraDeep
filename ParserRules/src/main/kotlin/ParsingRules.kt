package com.momid

import com.momid.parser.expression.*
import com.momid.parser.not
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

@Type
val fullFunctionCall = !"some" + insideOf('(', ')') {
    one(!"fun" + spaces + functionCall["functionCall"])
}["ooo"]

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
                evaluation.expression.name = "inside"
                return@CustomExpression MultiExpressionResult(ExpressionResult(thisExpression, startIndex .. tokenIndex + 1), arrayListOf(evaluation))
            }
        }
        return@CustomExpression null
    }
}

fun one(expression: Expression): CustomExpression {
    return CustomExpression(
        TypeInfo(
            expression,
            if (expression is MultiExpression) {
                expression.find {
                    it.name != null
                } ?: throw (Throwable("there should be one named expression but yours has none"))
            } else {
                throw (Throwable("should be a multi expression"))
            }
        )
    ) { tokens, startIndex, endIndex, thisExpression ->
        val evaluation = eval(expression, startIndex, tokens, endIndex) ?: return@CustomExpression null
        var namedExpressionResult: ExpressionResult? = null
        if (evaluation is MultiExpressionResult) {
            evaluation.forEach {
                if (it.expression.name != null) {
                    if (namedExpressionResult == null) {
                        namedExpressionResult = it
                    } else {
                        throw (Throwable("there should be only one named expression"))
                    }
                }
            }
            if (namedExpressionResult == null) {
                throw (Throwable("there should be one named expression but yours has none"))
            } else {
                return@CustomExpression namedExpressionResult!!.apply {
                    this.nextTokenIndex = evaluation.nextTokenIndex
                }
            }
        } else {
            evaluation
        }
    }
}
