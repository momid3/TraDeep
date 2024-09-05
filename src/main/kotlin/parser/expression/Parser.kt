package com.momid.parser.expression

class Parser() {
    var tokens: List<Char>? = null

    val errors = HashMap<ExpressionResult, Expression?>()
    var isCurrentError: Boolean = false
}
