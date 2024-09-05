package com.momid

import com.momid.parser.expression.*

class Anonymous6(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: CodeBlock
        get() {
            return CodeBlock(expressionResult["inside"])
        }
}

class Identifier(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

fun parseIdentifier(tokens: String): Identifier? {
    val parsed = firstEval(identifier, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Identifier(parsed)
    } else {
        return null
    }
}

class Anonymous4(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: CodeBlock
        get() {
            return CodeBlock(expressionResult["inside"])
        }
}

class Argument(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val argumentName: Identifier
        get() {
            return Identifier(expressionResult["argumentName"])
        }
    val argumentType: Type
        get() {
            return Type(expressionResult["argumentType"])
        }
}

fun parseArgument(tokens: String): Argument? {
    val parsed = firstEval(argument, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Argument(parsed)
    } else {
        return null
    }
}

class AllowedName(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

fun parseAllowedName(tokens: String): AllowedName? {
    val parsed = firstEval(allowedName, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return AllowedName(parsed)
    } else {
        return null
    }
}

class Parameter(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val variableName: AllowedName
        get() {
            return AllowedName(expressionResult["variableName"])
        }
}

fun parseParameter(tokens: String): Parameter? {
    val parsed = firstEval(parameter, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Parameter(parsed)
    } else {
        return null
    }
}

class Parameters(
    val expressionResult: ExpressionResult, val items: List<Parameter> = expressionResult.asMulti().map {
        Parameter(it)
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex),
    List<Parameter> by items {

}

fun parseParameters(tokens: String): Parameters? {
    val parsed = firstEval(parameters, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Parameters(parsed)
    } else {
        return null
    }
}

class Anonymous9(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: Parameters
        get() {
            return Parameters(expressionResult["inside"])
        }
}

class Anonymous7(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: Parameters
        get() {
            return Parameters(expressionResult["inside"])
        }
}

class FunctionCall(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val functionName: AllowedName
        get() {
            return AllowedName(expressionResult["functionName"])
        }
    val parameters: Anonymous7
        get() {
            return Anonymous7(expressionResult["parameters"])
        }
}

fun parseFunctionCall(tokens: String): FunctionCall? {
    val parsed = firstEval(functionCall, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return FunctionCall(parsed)
    } else {
        return null
    }
}

class Anonymous13(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val functionName: AllowedName
        get() {
            return AllowedName(expressionResult["functionName"])
        }
    val parameters: Anonymous7
        get() {
            return Anonymous7(expressionResult["parameters"])
        }
}

class Anonymous12(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: Anonymous13
        get() {
            return Anonymous13(expressionResult["inside"])
        }
}

class Anonymous10(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val inside: Anonymous13
        get() {
            return Anonymous13(expressionResult["inside"])
        }
}

class FullFunctionCall(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val ooo: Anonymous10
        get() {
            return Anonymous10(expressionResult["ooo"])
        }
}

fun parseFullFunctionCall(tokens: String): FullFunctionCall? {
    val parsed = firstEval(fullFunctionCall, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return FullFunctionCall(parsed)
    } else {
        return null
    }
}

class Klass(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

fun parseKlass(tokens: String): Klass? {
    val parsed = firstEval(klass, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Klass(parsed)
    } else {
        return null
    }
}

class GenericType(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
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
    val parsed = firstEval(genericType, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return GenericType(parsed)
    } else {
        return null
    }
}

class Type(
    val expressionResult: ExpressionResult,
    val isGenericType: GenericType? = if (expressionResult.content.expression == genericType) {
        GenericType(expressionResult.content)
    } else {
        null
    },
    val isKlass: Klass? = if (expressionResult.content.expression == klass) {
        Klass(expressionResult.content)
    } else {
        null
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

fun parseType(tokens: String): Type? {
    val parsed = firstEval(type, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Type(parsed)
    } else {
        return null
    }
}

class Anonymous15(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

class Anonymous19(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

class Anonymous17(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

    val isOk: Anonymous19?
        get() {
            return if (expressionResult !is ErrorExpressionResult) {
                Anonymous19(expressionResult)
            } else {
                null
            }
        }
    val isError: ErrorExpressionResult?
        get() {
            return if (expressionResult is ErrorExpressionResult) {
                expressionResult
            } else {
                null
            }
        }
}

class RequiresType(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val ooo: Anonymous15
        get() {
            return Anonymous15(expressionResult["ooo"])
        }
    val some: Anonymous17
        get() {
            return Anonymous17(expressionResult["some"])
        }
    val someooo: Anonymous20
        get() {
            return Anonymous20(expressionResult["someooo"])
        }
}

fun parseRequiresType(tokens: String): RequiresType? {
    val parsed = firstEval(requiresType, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return RequiresType(parsed)
    } else {
        return null
    }
}

class Anonymous20(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

class Anonymous22(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

    val isOk: Type?
        get() {
            return if (expressionResult !is ErrorExpressionResult) {
                Type(expressionResult)
            } else {
                null
            }
        }
    val isError: ErrorExpressionResult?
        get() {
            return if (expressionResult is ErrorExpressionResult) {
                expressionResult
            } else {
                null
            }
        }
}

class Types(
    val expressionResult: ExpressionResult, val items: List<Anonymous22> = expressionResult.asMulti().map {
        Anonymous22(it)
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex),
    List<Anonymous22> by items {

}

fun parseTypes(tokens: String): Types? {
    val parsed = firstEval(types, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Types(parsed)
    } else {
        return null
    }
}

class Anonymous24(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

class Anonymous23(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

    val isOk: Anonymous24?
        get() {
            return if (expressionResult !is ErrorExpressionResult) {
                Anonymous24(expressionResult)
            } else {
                null
            }
        }
    val isError: ErrorExpressionResult?
        get() {
            return if (expressionResult is ErrorExpressionResult) {
                expressionResult
            } else {
                null
            }
        }
}

class Numbers(
    val expressionResult: ExpressionResult, val items: List<Anonymous23> = expressionResult.asMulti().map {
        Anonymous23(it)
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex),
    List<Anonymous23> by items {

}

fun parseNumbers(tokens: String): Numbers? {
    val parsed = firstEval(numbers, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return Numbers(parsed)
    } else {
        return null
    }
}

class Anonymous2(
    val expressionResult: ExpressionResult, val items: List<Argument> = expressionResult.asMulti().map {
        Argument(it)
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex),
    List<Argument> by items {

}

class Anonymous1(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}

class CodeBlock(
    val expressionResult: ExpressionResult, val items: List<Anonymous1> = expressionResult.asMulti().map {
        Anonymous1(it)
    }
) : ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex),
    List<Anonymous1> by items {

}

fun parseCodeBlock(tokens: String): CodeBlock? {
    val parsed = firstEval(codeBlock, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return CodeBlock(parsed)
    } else {
        return null
    }
}

class FunctionDeclaration(val expressionResult: ExpressionResult) :
    ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val functionName: Identifier
        get() {
            return Identifier(expressionResult["functionName"])
        }
    val arguments: Anonymous2
        get() {
            return Anonymous2(expressionResult["arguments"])
        }
    val functionBody: Anonymous4
        get() {
            return Anonymous4(expressionResult["functionBody"])
        }
}

fun parseFunctionDeclaration(tokens: String): FunctionDeclaration? {
    val parsed = firstEval(functionDeclaration, 0, tokens.toList(), tokens.length)
    if (parsed != null) {
        return FunctionDeclaration(parsed)
    } else {
        return null
    }
}
