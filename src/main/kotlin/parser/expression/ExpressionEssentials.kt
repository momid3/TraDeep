package com.momid.parser.expression

import com.momid.type.anything

val spaces by lazy {
    some0(condition { it.isWhitespace() }).apply { this.isValueic = false }
}

fun inline(multiExpression: Expression): CustomExpression {
    return CustomExpression() { tokens, startIndex, endIndex, thisExpression ->
        val inlinedExpressionResults = ArrayList<ExpressionResult>()
        val expressionResult = eval(multiExpression, startIndex, tokens, endIndex) ?: return@CustomExpression null
        if (expressionResult !is MultiExpressionResult) {
            throw(Throwable("expression " + multiExpression::class + "does not evaluate to a MultiExpressionResult"))
        }
        expressionResult.forEach {
            if (it is MultiExpressionResult) {
                inlinedExpressionResults.addAll(it.expressionResults)
            } else {
                inlinedExpressionResults.add(it)
            }
        }
        expressionResult.expressionResults.clear()
        expressionResult.expressionResults.addAll(inlinedExpressionResults)
        return@CustomExpression ContentExpressionResult(ExpressionResult(multiExpression, expressionResult.range), expressionResult)
    }
}

fun insideOf(expression: Expression, parenthesesStart: Char, parenthesesEnd: Char): CustomExpression {
    return CustomExpression() { tokens, startIndex, endIndex, thisExpression ->
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
                return@CustomExpression ContentExpressionResult(ExpressionResult(expression, startIndex .. tokenIndex + 1), ExpressionResult(expression, startIndex + 1 .. (tokenIndex + 1) - 1))
            }
        }
        return@CustomExpression null
    }
}

fun insideOf(parenthesesStart: Char, parenthesesEnd: Char, expression: Expression): CustomExpression {
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
                val evaluation = eval(expression, startIndex + 1, tokens, (tokenIndex + 1) - 1)
                return@CustomExpression ContinueExpressionResult(ExpressionResult(expression, startIndex .. tokenIndex + 1), evaluation)
            }
        }
        return@CustomExpression null
    }
}

fun insideOf(parenthesesStart: Char, parenthesesEnd: Char, expression: () -> Expression): CustomExpression {
    return CustomExpression(
        TypeInfo(expression(), AnyOf(expression()))
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
                val evaluation = eval(expression(), startIndex + 1, tokens, (tokenIndex + 1) - 1)
                return@CustomExpression ContinueExpressionResult(ExpressionResult(thisExpression, startIndex .. tokenIndex + 1), evaluation)
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

fun matchesFully(expression: Expression, tokenSlice: List<Char>): Boolean {
    return evaluateExpression(expression, 0, tokenSlice) == tokenSlice.size
}

fun Expression.andAlso(vararg otherExpressions: Expression): CustomExpression {
    return CustomExpression { tokens, startIndex, endIndex, thisExpression ->
        val evaluation = eval(this, startIndex, tokens, endIndex) ?: return@CustomExpression null
        for (expression in otherExpressions) {
            eval(expression, startIndex, tokens, endIndex)?.nextTokenIndex ?: return@CustomExpression null
        }
        return@CustomExpression evaluation
    }
}
