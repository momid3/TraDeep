package com.momid

import com.momid.parser.expression.*
import com.momid.parser.not
import com.momid.type.Type

@Type
val allowedName = condition { it.isLetter() } + some(condition { it.isLetterOrDigit() })

@Type
val parameter = spaces + allowedName["variableName"] + spaces

@Type
val parameters = splitBy(parameter, ",")

@Type
val functionCall = allowedName["functionName"] + insideOf('(', ')') {
    parameters
}["parameters"]

@Type
val fullFunctionCall = !"some" + insideOf('(', ')') {
    one(!"fun" + spaces + functionCall["functionCall"])
}["ooo"]

@Type
val klass = same(allowedName)

@Type
val genericType = cold { allowedName["name"] + !"<" + type["typeParameter"] + !">" }

@Type
val type: EachOfExpression = anyOf(genericType, klass)

@Type
val requiresType = (!"ooo")["ooo"] + require((!"sor"))["some"] + (!"ooo")["someooo"]

@Type
val types = some(require(type))
