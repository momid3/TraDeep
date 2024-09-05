package com.momid.parser.expression

import com.momid.parser.not

val spaces by lazy {
    some0(condition { it.isWhitespace() }).apply { this.isValueic = false }
}

val space by lazy {
    some(condition { it.isWhitespace() }).apply { this.isValueic = false }
}

val anything by lazy {
    some0(condition {
        true
    })
}

val newLine by lazy {
    anyOf(!"\n\r", !"\n")
}

val number by lazy {
    some(condition {
        it.isDigit()
    })
}

val fNumber by lazy {
    CustomExpression() { tokens, startIndex, endIndex, thisExpression ->
        var index = startIndex
        var hasPoint = false
        var indexOfPoint = 0
        if (index >= endIndex) {
            return@CustomExpression null
        }
        while (true) {
            if (index >= endIndex) {
                break
            }
            if (!tokens[index].isDigit()) {
                if (tokens[index] == '.') {
                    if (!hasPoint) {
                        hasPoint = true
                        indexOfPoint = index
                        if (index == endIndex - 1) {
                            break
                        }
                        if (!tokens[index + 1].isDigit()) {
                            break
                        }
                    } else {
                        index = indexOfPoint
                        break
                    }
                } else {
                    if (index == startIndex) {
                        return@CustomExpression null
                    } else {
                        break
                    }
                }
            }
            index += 1
        }

        return@CustomExpression ExpressionResult(thisExpression, startIndex..index, index)
    }
}

fun insideOf(parenthesesStart: Char, parenthesesEnd: Char, expression: () -> Expression): CustomExpression {
    return CustomExpression(
        TypeInfo(expression(), type(expression()["inside"]))
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

fun insideOfShould(parenthesesStart: Char, parenthesesEnd: Char, expression: () -> Expression): CustomExpression {
    return CustomExpression() { tokens, startIndex, endIndex, thisExpression ->
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
                val evaluation = eval(expression(), startIndex + 1, tokens, (tokenIndex + 1) - 1) ?:
                return@CustomExpression null
                return@CustomExpression ContentExpressionResult(ExpressionResult(thisExpression, startIndex .. tokenIndex + 1), evaluation)
            }
        }
        return@CustomExpression null
    }
}

fun wanting(expression: Expression, until: Expression): CustomExpression {
    return CustomExpression { tokens, startIndex, endIndex, thisExpression ->
        var nextTokenIndex = startIndex
        while (true) {
            val evaluation = eval(until, nextTokenIndex, tokens, endIndex)
            if (evaluation == null) {
                nextTokenIndex += 1
            } else {
                break
            }
            if (nextTokenIndex >= endIndex) {
                break
            }
        }
        val evaluation = eval(expression, startIndex, tokens, nextTokenIndex)
        return@CustomExpression ContinueExpressionResult(ExpressionResult(thisExpression, startIndex..nextTokenIndex), evaluation)
    }
}

fun wanting(expression: Expression): CustomExpression {
    return CustomExpression { tokens, startIndex, endIndex, thisExpression ->
        val evaluation = eval(expression, startIndex, tokens, endIndex)
        return@CustomExpression ContinueExpressionResult(ExpressionResult(expression, startIndex..endIndex), evaluation)
    }
}

/***
 * splitBy without "wanting" for each element
 */
fun splitBy(expression: Expression, splitBy: String): CustomExpression {
    return CustomExpression(
        TypeInfo(expression, ListType(expression))
    ) { tokens, startIndex, endIndex, thisExpression ->
        var nextTokenIndex = startIndex
        val subExpressionResults = ArrayList<ExpressionResult>()
        val splitExpression = ExactExpression(splitBy)

        val firstEvaluation = eval(expression, startIndex, tokens, endIndex) ?: return@CustomExpression null
        nextTokenIndex = firstEvaluation.nextTokenIndex
        subExpressionResults.add(firstEvaluation)

        while (true) {
            val splitEvaluation = eval(splitExpression, nextTokenIndex, tokens, endIndex) ?: break
            nextTokenIndex = splitEvaluation.nextTokenIndex

            val nextEvaluation = eval(expression, nextTokenIndex, tokens, endIndex) ?: return@CustomExpression null
            nextTokenIndex = nextEvaluation.nextTokenIndex
            subExpressionResults.add(nextEvaluation)
        }

        return@CustomExpression MultiExpressionResult(
            ExpressionResult(thisExpression, startIndex..nextTokenIndex),
            subExpressionResults
        )
    }
}

fun until(expression: Expression): CustomExpression {
    return wanting(anything, expression)
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
                    this.expression = thisExpression
                }
            }
        } else {
            evaluation
        }
    }
}

fun same(expression: Expression): Expression {
    return expression[expression.name ?: ""].apply {
        currentExpressionId += 1
        this.id = currentExpressionId
    }
}
