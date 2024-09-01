package com.momid.type

import com.momid.parser.expression.*
import com.momid.parser.expression.Type
import java.io.File

fun registerTypes(types: List<Pair<Expression, String>>, file: File = File(""), packageName: String = "", expressionImports: String = ""): String {
    val generatedClasses = HashMap<Expression, TypeClass>()
    for (index in types.indices) {
        val (type, name) = types[index]
        generateTypeClassForExpression(type, name, generatedClasses, ArrayList(types))
    }

    val generatedCode = generatedClasses.map {
        val (expression, typeClass) = it
        if (typeClass !is DefinedTypeClass) {
            "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.properties.joinToString(
                "\n"
            ) {
                "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
            } + "\n}"
        } else {
            if (typeClass.type == DefinedTypeClassValue.List) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult, val items: List<" + typeClass.innerType.name + "> = expressionResult.asMulti().map {\n" + typeClass.innerType.name + "(it)\n}): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex), List<" + typeClass.innerType.name + "> by items {\n" + typeClass.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else if (typeClass.type == DefinedTypeClassValue.AnyOf) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult, " + typeClass.innerTypes.joinToString(
                    ",\n"
                ) {
                    "val is" + it.name + ": " + it.name + "? = if (expressionResult.content.expression == " + it.name.decapitalize() + ") {\n" + it.name + "(expressionResult.content)\n} else {\nnull\n}"
                } + "): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else if (typeClass.type == DefinedTypeClassValue.Expression) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.innerType.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else {
                "nothing for class " + typeClass.innerType
            }
        } + if (!typeClass.name.contains("anonymous", true)) {
            "\nfun parse" + typeClass.name + "(tokens: String): " + typeClass.name + "? {\nval parsed = " + "eval(" + typeClass.name.decapitalize() + ", " + "0" + ", " + "tokens.toList()" + ", " + "tokens.length" + ")\n" + "if (" + "parsed" + " != null) {\nreturn " + typeClass.name + "(parsed)" + "\n} else {\nreturn null\n}\n}"
        } else {
            ""
        }
    }.joinToString("\n")

    if (file.name != "") {
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val writer = file.outputStream().writer()
        writer.write(packageName + "\n")
        writer.write("\n")
        writer.write(
            """
                import com.momid.parser.expression.*
            """.trimIndent()
        )
        writer.write(expressionImports + "\n")
        writer.write(generatedCode)
        writer.flush()
        writer.close()
    }

    return generatedCode
}

fun generateClassForTypeClass(typeClass: TypeClass): String {
    return "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.properties.joinToString(
        "\n"
    ) {
        "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
    } + "\n}"
}

fun typeName(typeClass: TypeClass): String {
    if (typeClass !is DefinedTypeClass) {
        return typeClass.name
    } else {
        if (typeClass.type == DefinedTypeClassValue.List) {
            return "List<" + typeClass.innerType.name + ">"
        }
    }
    return ""
}

var currentAnonymousType = 0

fun generateTypeClassForExpression(
    type: Expression,
    name: String,
    generatedClasses: HashMap<Expression, TypeClass>,
    types: ArrayList<Pair<Expression, String>>
): TypeClass {
    val properties = ArrayList<TypeProperty>()
    val generatedClass = TypeClass(name, properties)
    if (type is CustomExpression) {
        println(type.condition)
    }
    if (type is MultiExpression) {
        type.forEach { expression ->
            if (expression.name != null) {
                if (generatedClasses[expression] == null) {
                    val expressionName = types.find { it.first == expression }?.second
                    if (expressionName != null) {
                        val typeClass = generateTypeClassForExpression(expression, expressionName, generatedClasses, types)
                        properties.add(TypeProperty(expression.name!!, typeClass))
                    } else {
                        val typeName = "Anonymous" + currentAnonymousType
                        types.add(Pair(expression, typeName))
                        currentAnonymousType += 1
                        val typeClass = generateTypeClassForExpression(expression, typeName, generatedClasses, types)
                        properties.add(TypeProperty(expression.name!!, typeClass))
                    }
                } else {
                    properties.add(TypeProperty(expression.name!!, generatedClasses[expression]!!))
                }
            }
        }
    } else if (type is EachOfExpression) {
        val innerTypes = type.map { innerType ->
            val outputExpressionName = expressionName(innerType, types)

            val typeClass = generatedClasses[innerType] ?: generateTypeClassForExpression(
                innerType,
                outputExpressionName,
                generatedClasses,
                types
            )
            typeClass
        }
        val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(AnyOf(*(((type as EachOfExpression) as List<Expression>).toTypedArray()))), innerTypes)
        definedTypeClass.name = name
        generatedClasses[type] = definedTypeClass
        return generatedClass
    } else if (type is CustomExpression) {
        if (type.typeInfo != null) {
            val outputExpression = type.typeInfo.outputExpression
            when (outputExpression) {
                is AnyOf -> {
                    val innerTypes = outputExpression.innerTypes.map { innerType ->
                        val outputExpressionName = expressionName(outputExpression, types)

                        val typeClass = generatedClasses[innerType] ?: generateTypeClassForExpression(
                            innerType,
                            outputExpressionName,
                            generatedClasses,
                            types
                        )
                        typeClass
                    }
                    val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(outputExpression), innerTypes)
                    definedTypeClass.name = name
                    generatedClasses[type] = definedTypeClass
                    return generatedClass
                }

                is Type -> {
                    val outputExpressionName = expressionName(outputExpression.innerType, types)

                    val typeClass = generatedClasses[outputExpression.innerType] ?: generateTypeClassForExpression(
                        outputExpression.innerType,
                        outputExpressionName,
                        generatedClasses,
                        types
                    )
                    val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(outputExpression), typeClass)
                    definedTypeClass.name = name
                    generatedClasses[type] = definedTypeClass
                    return generatedClass
                }

                else -> {
                    val outputExpressionName = expressionName(outputExpression, types)

                    val typeClass = generatedClasses[outputExpression] ?: generateTypeClassForExpression(
                        outputExpression,
                        outputExpressionName,
                        generatedClasses,
                        types
                    )
                    val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(outputExpression), typeClass)
                    definedTypeClass.name = name
                    generatedClasses[type] = definedTypeClass
                    return generatedClass
                }
            }
        }
    }
    generatedClasses[type] = generatedClass
    return generatedClass
}

fun expressionName(expression: Expression, types: List<Pair<Expression, String>>): String {
    val outputExpressionType = types.find { (typesExpression, name) ->
        typesExpression == expression
    }
    var outputExpressionName = "Anonymous" + currentAnonymousType
    currentAnonymousType += 1
    if (outputExpressionType != null) {
        val (expression, name) = outputExpressionType
        outputExpressionName = name
    }
    return outputExpressionName
}

fun typeToDefinedTypeClassValue(type: Expression): DefinedTypeClassValue {
    return when (type) {
        is ListType -> DefinedTypeClassValue.List
        is Content -> DefinedTypeClassValue.Content
        is AnyOf -> DefinedTypeClassValue.AnyOf
        is Optional -> DefinedTypeClassValue.Optional
        else -> DefinedTypeClassValue.Expression
    }
}

open class TypeClass(var name: String, val properties: List<TypeProperty>)

class TypeProperty(val name: String, val type: TypeClass)

class DefinedTypeClass(
    val type: DefinedTypeClassValue,
    val innerTypes: List<TypeClass>,
    val innerType: TypeClass = innerTypes[0]
) : TypeClass("", listOf()) {
    constructor(type: DefinedTypeClassValue, innerType: TypeClass) : this(type, listOf(innerType))
}

enum class DefinedTypeClassValue {
    List, Content, AnyOf, Optional, Expression
}

class ooo(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {

}
class some(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val name: ooo
        get() {
            return ooo(expressionResult["name"])
        }
    val value: ooo
        get() {
            return ooo(expressionResult["value"])
        }
}

class Anonymous1(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val name: ooo
        get() {
            return ooo(expressionResult["name"])
        }
    val value: ooo
        get() {
            return ooo(expressionResult["value"])
        }
}
class Anonymous0(val expressionResult: ExpressionResult, val items: List<ooo> = expressionResult.asMulti().map {
    ooo(it)
}): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex), List<ooo> by items {

}
class Split(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {
    val splitBeforeOoo: Anonymous0
        get() {
            return Anonymous0(expressionResult["splitBeforeOoo"])
        }
    val someAfterOoo: Anonymous1
        get() {
            return Anonymous1(expressionResult["someAfterOoo"])
        }
}

//val split = splitByNW(ooo, ",")["splitBeforeOoo"]

fun ExpressionResult.asMulti(): MultiExpressionResult {
    if (this is MultiExpressionResult) {
        return this
    } else {
        throw (Throwable("this expression result is not a multi expression result"))
    }
}

val anything by lazy {
    some0(condition { true })
}

fun main() {
//    println(registerTypes(listOf(Pair(some, "some"), Pair(ooo, "ooo"))))

//    println(registerTypes(listOf(Pair(ooo, "ooo"), Pair(split, "split"))))

//    find("ooo,ooo", split).forEach {
//        val split = Split(it)
//        println(split.splitBeforeOoo.items.size)
//    }
}
