package com.momid

import com.momid.parser.expression.*
import com.momid.parser.not
import com.momid.type.Type

@Type
val identifier = condition { it.isLetter() } + some(condition { it.isLetterOrDigit() })

@Type
val argument = spaces + identifier["argumentName"] + spaces + !":" + spaces + type["argumentType"] + spaces

val arguments = splitBy(argument, ",")

@Type
val codeBlock = anything

@Type
val functionDeclaration = !"fun" + spaces + identifier["functionName"] + !"(" + arguments["arguments"] + !")" + spaces + insideOf('{', '}') {
    codeBlock
}["functionBody"]
