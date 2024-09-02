package com.momid

import com.momid.parser.expression.ExpressionResult
import com.momid.parser.expression.correspondingTokensText

fun main() {
    val tokens = "some<ooo>"
    val type = parseType(tokens)!!
    type.isGenericType.then {
        it.some.text(tokens).println()
    }
    type.isKlass.then {
        it.text(tokens).println()
    }
    val text = "some(fun validate(param, otherParam, anotherParam))"
    val functionCall = parseFullFunctionCall(text)!!
    println(functionCall.ooo.inside.functionName.text(text))
    functionCall.ooo.inside.parameters.inside.forEach {
        println(it.variableName.text(text))
    }
}

fun <T> T?.then(block: (T) -> Unit) {
    if (this != null) {
        block(this)
    }
}

fun ExpressionResult.text(underlyingText: String): String {
    return this.correspondingTokensText(underlyingText.toList())
}

fun <T> T?.println() {
    println(this)
}
