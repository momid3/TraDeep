package com.momid

import com.momid.parser.expression.text

fun main() {
    val text = """
        fun println(printable: List<Item>, otherParameter: Item) {
            println(printable)
        }
        """.trimIndent()
    val functionDeclaration = parseFunctionDeclaration(text)!!
    functionDeclaration.arguments.forEach {
        println(it.argumentName.text)
        println(it.argumentType.text)
    }
    println(functionDeclaration.functionName.text)
    println(functionDeclaration.functionBody.text)
}
