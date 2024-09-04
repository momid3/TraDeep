package com.momid

import com.momid.parser.expression.*
import com.momid.parser.find
import com.momid.parser.not
import com.momid.type.asMulti

fun main() {
    val requires = "ooosomeofrequireooo"
    val requiresType = (!"ooo")["ooo"] + require((!"sor"))["some"] + (!"ooo")["someooo"]
    find(requires, requiresType).forEach {
        println(it.asMulti()[0] is ErrorExpressionResult)
        println(it.asMulti()[1] is ErrorExpressionResult)
        println(it.asMulti()[2] is ErrorExpressionResult)
    }
    val require = parseRequiresType(requires)!!
    println(require.some.isError?.text(requires))
    val tokens = "some<ooo>___some<ooo>some<ooo>"
    val type = parseTypes(tokens)!!
    type.forEach {
        it.isOk.then {
            it.isGenericType.then {
                it.text.println()
                ("generic " + it.name.text).println()
            }
            it.isKlass.then {
                ("klass " + it.text).println()
            }
        }
        it.isError.then {
            ("not this type " + it.text).println()
        }
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
