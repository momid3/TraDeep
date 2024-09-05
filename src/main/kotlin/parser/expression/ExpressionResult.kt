package com.momid.parser.expression

open class SomeExpressionResult(val expressionResult: ExpressionResult?)

open class ExpressionResult(val parser: Parser, var expression: Expression, var range: IntRange, var nextTokenIndex: Int = range.last)

class MultiExpressionResult(val mainExpressionResult: ExpressionResult, val expressionResults: ArrayList<ExpressionResult> = arrayListOf()): List<ExpressionResult> by expressionResults, ExpressionResult(mainExpressionResult.parser, mainExpressionResult.expression, mainExpressionResult.range, mainExpressionResult.nextTokenIndex)

class ContentExpressionResult(val mainExpressionResult: ExpressionResult, val content: ExpressionResult): ExpressionResult(mainExpressionResult.parser, mainExpressionResult.expression, mainExpressionResult.range, mainExpressionResult.nextTokenIndex)

/***
 * same as ContentExpressionResult but its content may be null because after continuing no further results have been found
 */
class ContinueExpressionResult(val mainExpressionResult: ExpressionResult, val content: ExpressionResult?): ExpressionResult(mainExpressionResult.parser, mainExpressionResult.expression, mainExpressionResult.range, mainExpressionResult.nextTokenIndex)

/***
 * the result of RequireExpression when it did not find the expression
 */
class ErrorExpressionResult(expressionResult: ExpressionResult): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex)

fun Parser.expressionResult(expression: Expression, range: IntRange, nextTokenIndex: Int = range.last): ExpressionResult {
    return ExpressionResult(this, expression, range, nextTokenIndex)
}

fun Parser.ExpressionResult(expression: Expression, range: IntRange, nextTokenIndex: Int = range.last): ExpressionResult {
    return ExpressionResult(this, expression, range, nextTokenIndex)
}

//fun ExpressionResult.getRange(): IntRange? {
//    when (this) {
//        is ExpressionResult -> return this.range
//        is MultiExpressionResult -> return this.mainExpressionResult.range
////        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
//        else -> return null
//    }
//}
//
//fun ExpressionResult.getExpression(): Expression {
//    when (this) {
//        is ExpressionResult -> return this.expression
//        is MultiExpressionResult -> return this.mainExpressionResult.expression
//        else -> throw(Throwable("ExpressionResult is whether NoExpressionResult or unknown"))
//    }
//}

fun noExpressionResult(): SomeExpressionResult {
    return SomeExpressionResult(null)
}

fun SomeExpressionResult.isNoExpressionResult(): Boolean {
    return this.expressionResult == null
}
