package com.momid

import com.momid.parser.expression.*
import com.momid.parser.find
import com.momid.parser.not

fun main() {
    val optionalTypesTokens = "ooo some<ooo>"
    val optionalTypes = parseOptionalTypes(optionalTypesTokens)!!
    optionalTypes.identifier.isPresent.then {
        println(it.text)
    }
    val numbersTokens = "ooo333.888ooo"
    val numbers = parseNumbers(numbersTokens)!!
    numbers.forEach {
        it.isOk.then {
            println(it.text)
        }
        it.isError.then {
            println("is not a number " + it.text)
        }
    }
    val requires = "ooosomeofrequireooo"
    val requiresType = (!"ooo")["ooo"] + require((!"sor"))["some"] + (!"ooo")["someooo"]
    find(requires, requiresType).forEach {
        println(it.asMulti()[0] is ErrorExpressionResult)
        println(it.asMulti()[1] is ErrorExpressionResult)
        println(it.asMulti()[2] is ErrorExpressionResult)
    }
    val require = parseRequiresType(requires)!!
    println(require.some.isError?.text)
    val tokens = "some<ooo>___some<ooo>some<ooo>"
    val type = parseTypes(tokens)!!
    type.forEach {
        it.isOk.then {
            it.isGenericType.then {
                it.println()
                println("generic " + it.name.text)
            }
            it.isKlass.then {
                println("klass " + it.text)
            }
        }
        it.isError.then {
            println("not this type " + it.text)
        }
    }
    val text = "some(fun validate(param, otherParam, anotherParam))"
    val functionCall = parseFullFunctionCall(text)!!
    println(functionCall.ooo.inside.functionName.text)
    functionCall.ooo.inside.parameters.inside.forEach {
        println(it.variableName.text)
    }
}
