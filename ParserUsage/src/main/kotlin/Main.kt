package com.momid

import com.momid.parser.expression.*
import com.momid.parser.find
import com.momid.parser.not
import com.momid.type.asMulti

fun main() {
    val requires = "ooosomeofrequireooo"
    val requiresType = (!"ooo")["ooo"] + require((!"sor")["some"]) + (!"ooo")["someooo"]
    find(requires, requiresType).forEach {
        println(it.asMulti()[0] is ErrorExpressionResult)
        println(it.asMulti()[1] is ErrorExpressionResult)
        println(it.asMulti()[2] is ErrorExpressionResult)
    }
    val tokens = "some<ooo>"
    val type = parseType(tokens)!!
    type.isGenericType.then {
        it.name.text(tokens).println()
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
