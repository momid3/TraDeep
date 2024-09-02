package com.momid.parser.expression

var currentExpressionId = 0

fun createExpressionId(): Int {
    currentExpressionId += 1
    return currentExpressionId
}

public open class Expression(var name: String? = null, var isValueic: Boolean = true, var id: Int = createExpressionId()) {
    override fun equals(other: Any?): Boolean {
        return other is Expression && other.id == this.id
    }

    override fun hashCode(): Int {
        return id
    }
}

class ExactExpression(val value: String) : Expression(isValueic = false) {

}

class ConditionExpression(val condition: (Char) -> Boolean): Expression()

open class MultiExpression(val expressions: ArrayList<Expression>): Expression(), List<Expression> by expressions

class RecurringExpression(val expression: Expression, val numberOfRecurring: Int): Expression()

class RecurringSomeExpression(val expression: Expression): Expression()

class RecurringSome0Expression(val expression: Expression): Expression()

class EachOfExpression(private val expressions: List<Expression>): Expression(), List<Expression> by expressions

class EachOfTokensExpression(private val tokens: List<Char>): Expression(), List<Char> by tokens

class NotExpression(val expression: Expression): Expression()

class CustomExpression(val typeInfo: TypeInfo? = null, val condition: (tokens: List<Char>, startIndex: Int, endIndex: Int, thisExpression: CustomExpression) -> ExpressionResult?): Expression()

class ColdExpression(val expression: () -> Expression): Expression()

class TypeInfo(val inputExpression: Expression, val outputExpression: Expression)

open class Type(val innerTypes: List<Expression>, val innerType: Expression = innerTypes[0]): Expression() {
    constructor(innerType: Expression) : this(listOf(innerType))
}

class ListType(innerType: Expression): Type(innerType)

class Content(innerType: Expression): Type(innerType)

class Optional(innerType: Expression): Type(innerType)

class AnyOf(vararg val anyOf: Expression): Type(anyOf[0])

interface Condition {

    public fun invoke(token: Char): Boolean
}

interface EvaluateExpression {

    fun evaluate(startIndex: Int, tokens: List<Char>): Int
}

fun evaluateExpression(expression: Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    if (startIndex >= endIndex) {
        if (expression is RecurringSome0Expression) {
            return startIndex
        }
//        else {
//            return -1
//        }
    }
    when (expression) {
        is ExactExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is ConditionExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is MultiExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is RecurringExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is RecurringSomeExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is RecurringSome0Expression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is EachOfExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is EachOfTokensExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is NotExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        is CustomExpression -> return evaluateExpression(expression, startIndex, tokens, endIndex)
        else -> throw(Throwable("unknown expression kind"))
    }
}

fun eval(expression: Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    if (startIndex >= endIndex) {
        if (expression is RecurringSome0Expression) {
            return ExpressionResult(expression, startIndex..startIndex)
        }
//        else {
//            return null
//        }
    }
    when (expression) {
        is MultiExpression -> return eval(expression, startIndex, tokens, endIndex)
        is EachOfExpression -> return eval(expression, startIndex, tokens, endIndex)
        is RecurringSomeExpression -> return eval(expression, startIndex, tokens, endIndex)
        is RecurringSome0Expression -> return eval(expression, startIndex, tokens, endIndex)
        is CustomExpression -> return eval(expression, startIndex, tokens, endIndex)
        is ColdExpression -> return eval(expression.expression(), startIndex, tokens, endIndex).apply {
            if (this != null) {
                this.expression = expression
            }
        }
        else -> {
            val tokensEndIndex = evaluateExpression(expression, startIndex, tokens, endIndex)
            if (tokensEndIndex != -1) {
                return ExpressionResult(expression, startIndex .. tokensEndIndex)
            } else {
                return null
            }
        }
    }
}

fun evaluateExpression(exactExpression: ExactExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    var exactExpressionIndex = 0
    var tokensEndIndex = startIndex
    for (index in startIndex until endIndex) {
        tokensEndIndex += 1
        if (tokens[index] == exactExpression.value[exactExpressionIndex]) {
            exactExpressionIndex += 1
            if (exactExpressionIndex == exactExpression.value.length) {
                return tokensEndIndex
            }
        } else {
            return -1
        }
    }
    return -1
}

fun evaluateExpression(conditionExpression: ConditionExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    if (startIndex >= endIndex) {
        return -1
    }
    if (conditionExpression.condition(tokens[startIndex])) {
        return startIndex + 1
    } else return -1
}

fun evaluateExpression(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    var multiExpressionIndex = 0
    var tokensEndIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(multiExpression[multiExpressionIndex], tokensEndIndex, tokens, endIndex)
        if (nextIndex == -1) {
            return -1
        } else {
            tokensEndIndex = nextIndex
            multiExpressionIndex += 1
            if (multiExpressionIndex == multiExpression.size) {
                return tokensEndIndex
            }
            if (tokensEndIndex > endIndex) {
                break
            }
        }
    }
    return -1
}

fun eval(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): MultiExpressionResult? {
    val expressionResults = ArrayList<ExpressionResult>()
    var multiExpressionIndex = 0
    var tokensEndIndex = startIndex
    while (true) {
        val evaluationResult = eval(multiExpression[multiExpressionIndex], tokensEndIndex, tokens, endIndex)
        if (evaluationResult == null) {
            return null
        } else {

            val nextIndex = evaluationResult.nextTokenIndex
            val expression = multiExpression[multiExpressionIndex]
            if (expression.isValueic) {
                expressionResults.add(evaluationResult)
            }

            tokensEndIndex = nextIndex
            multiExpressionIndex += 1
            if (multiExpressionIndex == multiExpression.size) {
//                if (expressionResults.isEmpty()) {
//                    throw(Throwable("multiExpression subs should not be empty"))
////                    return ExpressionResult(multiExpression, startIndex .. endIndex)
//                } else {
                    return MultiExpressionResult(ExpressionResult(multiExpression, startIndex .. tokensEndIndex), expressionResults)
//                }
            }
            if (tokensEndIndex > endIndex) {
                break
            }
        }
    }
    return null
}

fun evaluateExpression(recurringExpression: RecurringExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    val recurringList = MutableList(recurringExpression.numberOfRecurring) {
        recurringExpression.expression
    }
    return evaluateExpression(MultiExpression(recurringList as ArrayList<Expression>), startIndex, tokens, endIndex)
}

fun evaluateExpression(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    eachOfExpression.forEach {
        val tokensEndIndex = evaluateExpression(it, startIndex, tokens, endIndex)
        if (tokensEndIndex != -1) {
            return tokensEndIndex
        }
    }
    return -1
}

fun eval(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ContentExpressionResult? {
    eachOfExpression.forEach {
        val expressionResult = eval(it, startIndex, tokens, endIndex)
        if (expressionResult != null) {
            val tokensEndIndex = expressionResult.nextTokenIndex
            if (tokensEndIndex != -1) {
                return ContentExpressionResult(ExpressionResult(eachOfExpression, expressionResult.range), expressionResult)
            }
        }
    }
    return null
}

fun evaluateExpression(eachOfTokensExpression: EachOfTokensExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    if (startIndex >= endIndex) {
        return -1
    }
    eachOfTokensExpression.forEach {
        if (tokens[startIndex] == it) {
            return startIndex + 1
        }
    }
    return -1
}

fun evaluateExpression(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    var numberOfRecurring = 0
    var tokensEndIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(recurringSomeExpression.expression, tokensEndIndex, tokens, endIndex)
        if (nextIndex == -1) {
            break
        } else {
            tokensEndIndex = nextIndex
            if (tokensEndIndex <= endIndex) {
                numberOfRecurring += 1
            } else {
                break
            }
        }
    }
    if (numberOfRecurring > 0) {
        return tokensEndIndex
    } else {
        return -1
    }
}

fun eval(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    val expressionResults = ArrayList<ExpressionResult>()
    var numberOfRecurring = 0
    var tokensEndIndex = startIndex
    while (true) {
        val expressionResult = eval(recurringSomeExpression.expression, tokensEndIndex, tokens, endIndex) ?: break
        tokensEndIndex = expressionResult.nextTokenIndex
        if (tokensEndIndex <= endIndex) {
            numberOfRecurring += 1
            expressionResults.add(expressionResult)
        } else {
            break
        }
    }
    if (numberOfRecurring > 0) {
        return MultiExpressionResult(ExpressionResult(recurringSomeExpression, startIndex..tokensEndIndex), expressionResults)
    } else {
        return null
    }
}

fun evaluateExpression(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    var numberOfRecurring = 0
    var tokensEndIndex = startIndex
    while (true) {
        val nextIndex = evaluateExpression(recurringSome0Expression.expression, tokensEndIndex, tokens, endIndex)
        if (nextIndex == -1) {
            break
        } else {
            tokensEndIndex = nextIndex
            if (tokensEndIndex <= endIndex) {
                numberOfRecurring += 1
            } else {
                break
            }
        }
    }
    return tokensEndIndex
}

fun eval(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult {
    val expressionResults = ArrayList<ExpressionResult>()
    var numberOfRecurring = 0
    var tokensEndIndex = startIndex
    if (startIndex >= endIndex) {
        MultiExpressionResult(ExpressionResult(recurringSome0Expression, startIndex..startIndex), expressionResults)
    }
    while (true) {
        if (tokensEndIndex >= endIndex) {
            return MultiExpressionResult(ExpressionResult(recurringSome0Expression, startIndex..tokensEndIndex), expressionResults)
        }
        val expressionResult = eval(recurringSome0Expression.expression, tokensEndIndex, tokens, endIndex) ?: break
        tokensEndIndex = expressionResult.nextTokenIndex
        if (tokensEndIndex <= endIndex) {
            numberOfRecurring += 1
            expressionResults.add(expressionResult)
        } else {
            break
        }
    }
    return MultiExpressionResult(ExpressionResult(recurringSome0Expression, startIndex..tokensEndIndex), expressionResults)
}

fun evaluateExpression(notExpression: NotExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    if (startIndex >= endIndex) {
        return startIndex
    }
    val tokensEndIndex = eval(notExpression.expression, startIndex, tokens, endIndex)
    if (tokensEndIndex == null) {
        return startIndex
    } else {
        return -1
    }
}

fun eval(customExpression: CustomExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    val expressionResult = customExpression.condition(tokens, startIndex, endIndex, customExpression)
    return expressionResult
}


fun main() {
    val text = "hello ! what a beautiful day. how are you ?"
    var endIndex = evaluateExpression(ExactExpression("hello"), 0, text.toList())
    endIndex = evaluateExpression(ConditionExpression { it != 'h' }, 0, text.toList())
    endIndex = evaluateExpression(MultiExpression(arrayListOf(ExactExpression("hello"), ConditionExpression { it != 'a' }, ExactExpression("!"))), 0, text.toList())
    println("end index is: " + endIndex)
}
