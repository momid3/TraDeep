package com.momid.parser

import com.momid.parser.expression.Expression
import com.momid.parser.expression.ExpressionFinder
import com.momid.parser.expression.ExpressionResult

fun find(tokens: List<Char>, expression: Expression, skip: Boolean = true): List<ExpressionResult> {
    val finder = ExpressionFinder()
    finder.registeredExpressions.add(expression)
    if (skip) {
        return finder.startDiscover(tokens)
    } else {
        return finder.start(tokens)
    }
}

fun find(tokens: String, expression: Expression, skip: Boolean = true): List<ExpressionResult> {
    val finder = ExpressionFinder()
    finder.registeredExpressions.add(expression)
    if (skip) {
        return finder.startDiscover(tokens.toList())
    } else {
        return finder.start(tokens.toList())
    }
}

fun main() {
    find("someooosome", !"ooo").forEach {
        println(it.range)
    }
}
