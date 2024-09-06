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

class CustomExpression(val typeInfo: TypeInfo? = null, val condition: Parser.(tokens: List<Char>, startIndex: Int, endIndex: Int, thisExpression: CustomExpression) -> ExpressionResult?): Expression()

class OptionalExpression(val expression: Expression): Expression()

class RequireExpression(val expression: Expression): Expression()

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

/***
 * returns true if this expression is the next expression after an expression that evaluated to an ErrorExpressionResult
 */
fun Parser.hasError(expression: Expression): Boolean {
    return errors.containsValue(expression)
}

fun Parser.errorOf(expression: Expression): ExpressionResult? {
    return errors.entries.find { (error, next) ->
        next == expression
    }?.key
}

fun Parser.evaluateExpression(expression: Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun firstEval(expression: Expression, startIndex: Int, parsingTokens: List<Char>, endIndex: Int = parsingTokens.size): ExpressionResult? {
    val parser = Parser()
    parser.tokens = parsingTokens
    return parser.eval(expression, startIndex, parsingTokens, endIndex)
}

fun Parser.eval(expression: Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    if (isCurrentError) {
        errors.entries.last().setValue(expression)
        isCurrentError = false
    }
    if (startIndex >= endIndex) {
        if (expression is RecurringSome0Expression) {
            return expressionResult(expression, startIndex..startIndex)
        }
//        else {
//            return null
//        }
    }
    val expressionResult = when (expression) {
        is MultiExpression -> eval(expression, startIndex, tokens, endIndex)
        is EachOfExpression -> eval(expression, startIndex, tokens, endIndex)
        is RecurringSomeExpression -> eval(expression, startIndex, tokens, endIndex)
        is RecurringSome0Expression -> eval(expression, startIndex, tokens, endIndex)
        is CustomExpression -> eval(expression, startIndex, tokens, endIndex)
        is OptionalExpression -> eval(expression, startIndex, tokens, endIndex)
        is ColdExpression -> eval(expression.expression(), startIndex, tokens, endIndex).apply {
            if (this != null) {
                this.expression = expression
            }
        }
        is RequireExpression -> eval(expression, startIndex, tokens, endIndex)
        else -> {
            val tokensEndIndex = evaluateExpression(expression, startIndex, tokens, endIndex)
            if (tokensEndIndex != -1) {
                expressionResult(expression, startIndex .. tokensEndIndex)
            } else {
                null
            }
        }
    }

    if (hasError(expression)) {
        if (expressionResult != null) {
            if (expressionResult !is ErrorExpressionResult) {
                val currentErroredExpressionResult = errorOf(expression)
                currentErroredExpressionResult!!.range = currentErroredExpressionResult.range.first..expressionResult.range.first
                errors.remove(errorOf(expression))
                return expressionResult
            } else {
                if (startIndex + 1 >= endIndex) {
                    errors.remove(errorOf(expression))
                    return null
                } else {
                    return eval(expression, startIndex + 1, tokens, endIndex)
                }
            }
        } else {
            if (startIndex + 1 >= endIndex) {
                errors.remove(errorOf(expression))
                return null
            } else {
                return eval(expression, startIndex + 1, tokens, endIndex)
            }
        }
    }

    if (expressionResult is ErrorExpressionResult) {
        isCurrentError = true
        errors[expressionResult] = null
    }

    return expressionResult
}

fun Parser.evaluateExpression(exactExpression: ExactExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun Parser.evaluateExpression(conditionExpression: ConditionExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    if (startIndex >= endIndex) {
        return -1
    }
    if (conditionExpression.condition(tokens[startIndex])) {
        return startIndex + 1
    } else return -1
}

fun Parser.evaluateExpression(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun Parser.eval(multiExpression: MultiExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): MultiExpressionResult? {
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
                    return MultiExpressionResult(expressionResult(multiExpression, startIndex .. tokensEndIndex), expressionResults)
//                }
            }
            if (tokensEndIndex > endIndex) {
                break
            }
        }
    }
    return null
}

fun Parser.evaluateExpression(recurringExpression: RecurringExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    val recurringList = MutableList(recurringExpression.numberOfRecurring) {
        recurringExpression.expression
    }
    return evaluateExpression(MultiExpression(recurringList as ArrayList<Expression>), startIndex, tokens, endIndex)
}

fun Parser.evaluateExpression(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
    eachOfExpression.forEach {
        val tokensEndIndex = evaluateExpression(it, startIndex, tokens, endIndex)
        if (tokensEndIndex != -1) {
            return tokensEndIndex
        }
    }
    return -1
}

fun Parser.eval(eachOfExpression: EachOfExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ContentExpressionResult? {
    eachOfExpression.forEach {
        val expressionResult = eval(it, startIndex, tokens, endIndex)
        if (expressionResult != null) {
            val tokensEndIndex = expressionResult.nextTokenIndex
            if (tokensEndIndex != -1) {
                return ContentExpressionResult(expressionResult(eachOfExpression, expressionResult.range), expressionResult)
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

fun Parser.evaluateExpression(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun Parser.eval(recurringSomeExpression: RecurringSomeExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
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
        return MultiExpressionResult(expressionResult(recurringSomeExpression, startIndex..tokensEndIndex), expressionResults)
    } else {
        return null
    }
}

fun Parser.evaluateExpression(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun Parser.eval(recurringSome0Expression: RecurringSome0Expression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult {
    val expressionResults = ArrayList<ExpressionResult>()
    var numberOfRecurring = 0
    var tokensEndIndex = startIndex
    if (startIndex >= endIndex) {
        MultiExpressionResult(expressionResult(recurringSome0Expression, startIndex..startIndex), expressionResults)
    }
    while (true) {
        if (tokensEndIndex >= endIndex) {
            return MultiExpressionResult(expressionResult(recurringSome0Expression, startIndex..tokensEndIndex), expressionResults)
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
    return MultiExpressionResult(expressionResult(recurringSome0Expression, startIndex..tokensEndIndex), expressionResults)
}

fun Parser.evaluateExpression(notExpression: NotExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): Int {
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

fun Parser.eval(optionalExpression: OptionalExpression, startIndex: Int, tokens: List<Char>, endIndex: Int): ExpressionResult {
    val expressionResult = eval(optionalExpression.expression, startIndex, tokens, endIndex)
    if (expressionResult != null) {
        return expressionResult.apply {
            this.expression = optionalExpression
        }
    } else {
        return ExpressionResult(optionalExpression, startIndex..startIndex, startIndex)
    }
}

fun Parser.eval(requireExpression: RequireExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    val expressionResult = eval(requireExpression.expression, startIndex, tokens, endIndex)
    if (expressionResult == null) {
        if (startIndex >= endIndex) {
            return null
        }
        return ErrorExpressionResult(expressionResult(requireExpression, startIndex..startIndex, startIndex))
    } else {
        return expressionResult.apply {
            this.expression = requireExpression
        }
    }
}

fun Parser.eval(customExpression: CustomExpression, startIndex: Int, tokens: List<Char>, endIndex: Int = tokens.size): ExpressionResult? {
    val expressionResult = customExpression.condition(this, tokens, startIndex, endIndex, customExpression)
    return expressionResult
}


fun main() {
    val text = "hello ! what a beautiful day. how are you ?"
//    var endIndex = evaluateExpression(ExactExpression("hello"), 0, text.toList())
//    endIndex = evaluateExpression(ConditionExpression { it != 'h' }, 0, text.toList())
//    endIndex = evaluateExpression(MultiExpression(arrayListOf(ExactExpression("hello"), ConditionExpression { it != 'a' }, ExactExpression("!"))), 0, text.toList())
//    println("end index is: " + endIndex)
}
