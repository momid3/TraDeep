package com.momid

import com.momid.parser.expression.*

class Klass(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val name: AllowedName
get() {
return AllowedName(expressionResult["name"])
}
}
fun parseKlass(tokens: String): Klass? {
val parsed = eval(klass, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return Klass(parsed)
} else {
return null
}
}
class GenericType(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val name: AllowedName
get() {
return AllowedName(expressionResult["name"])
}
val typeParameter: Type
get() {
return Type(expressionResult["typeParameter"])
}
}
fun parseGenericType(tokens: String): GenericType? {
val parsed = eval(genericType, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return GenericType(parsed)
} else {
return null
}
}
class AllowedName(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}
fun parseAllowedName(tokens: String): AllowedName? {
val parsed = eval(allowedName, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return AllowedName(parsed)
} else {
return null
}
}
class Type(val expressionResult: ExpressionResult, val isKlass: Klass? = if (expressionResult.content.expression == klass) {
Klass(expressionResult.content)
} else {
null
},
val isGenericType: GenericType? = if (expressionResult.content.expression == genericType) {
GenericType(expressionResult.content)
} else {
null
}): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}
fun parseType(tokens: String): Type? {
val parsed = eval(type, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return Type(parsed)
} else {
return null
}
}
class Parameter(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val variableName: AllowedName
get() {
return AllowedName(expressionResult["variableName"])
}
}
fun parseParameter(tokens: String): Parameter? {
val parsed = eval(parameter, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return Parameter(parsed)
} else {
return null
}
}
class Parameters(val expressionResult: ExpressionResult, val items: List<Parameter> = expressionResult.asMulti().map {
Parameter(it)
}): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex), List<Parameter> by items {

}
fun parseParameters(tokens: String): Parameters? {
val parsed = eval(parameters, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return Parameters(parsed)
} else {
return null
}
}
class Anonymous2(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val inside: List<Parameter>
get() {
return Parameters(expressionResult["inside"])
}
}
class Anonymous0(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val inside: List<Parameter>
get() {
return Parameters(expressionResult["inside"])
}
}
class FunctionCall(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val functionName: AllowedName
get() {
return AllowedName(expressionResult["functionName"])
}
val parameters: Anonymous0
get() {
return Anonymous0(expressionResult["parameters"])
}
}
fun parseFunctionCall(tokens: String): FunctionCall? {
val parsed = eval(functionCall, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return FunctionCall(parsed)
} else {
return null
}
}
class Anonymous6(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val functionName: AllowedName
get() {
return AllowedName(expressionResult["functionName"])
}
val parameters: Anonymous0
get() {
return Anonymous0(expressionResult["parameters"])
}
}
class Anonymous5(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val inside: Anonymous6
get() {
return Anonymous6(expressionResult["inside"])
}
}
class Anonymous3(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val inside: Anonymous6
get() {
return Anonymous6(expressionResult["inside"])
}
}
class FullFunctionCall(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
val ooo: Anonymous3
get() {
return Anonymous3(expressionResult["ooo"])
}
}
fun parseFullFunctionCall(tokens: String): FullFunctionCall? {
val parsed = eval(fullFunctionCall, 0, tokens.toList(), tokens.length)
if (parsed != null) {
return FullFunctionCall(parsed)
} else {
return null
}
}