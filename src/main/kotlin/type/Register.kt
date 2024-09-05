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
            if (expression is RequireExpression) {
                val expressionTypeClass = generatedClasses[expression.expression] ?: throw (Throwable("this expression should have existed"))
                val expressionClassName = typeName(expressionTypeClass)
                "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" +
                        "\n" + "val isOk: " + expressionClassName + "? " + "\nget() {\n" + "return " + "if(expressionResult !is ErrorExpressionResult) {\n" + expressionTypeClass.name + "(expressionResult)\n} else {\nnull\n}\n}" +
                        "\nval isError: ErrorExpressionResult? " + "\nget() {\n" + "return " + "if(expressionResult is ErrorExpressionResult)\n {" + "expressionResult" + "\n} else {\nnull\n}\n}" +
                        "\n}"
            } else {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            }
        } else {
            if (typeClass.type == DefinedTypeClassValue.List) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult, val items: List<" + typeClass.innerType.name + "> = expressionResult.asMulti().map {\n" + typeClass.innerType.name + "(it)\n}): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex), List<" + typeClass.innerType.name + "> by items {\n" + typeClass.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else if (typeClass.type == DefinedTypeClassValue.AnyOf) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult, " + typeClass.innerTypes.joinToString(
                    ",\n"
                ) {
                    "val is" + it.name + ": " + it.name + "? = if (expressionResult.content.expression == " + it.name.decapitalize() + ") {\n" + it.name + "(expressionResult.content)\n} else {\nnull\n}"
                } + "): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else if (typeClass.type == DefinedTypeClassValue.Expression) {
                "class " + typeClass.name + "(val expressionResult: ExpressionResult): ExpressionResult(expressionResult.parser, expressionResult.expression, expressionResult.range, expressionResult.nextTokenIndex) {\n" + typeClass.innerType.properties.joinToString(
                    "\n"
                ) {
                    "val " + it.name + ": " + typeName(it.type) + "\nget() {\n" + "return " + it.type.name + "(expressionResult[\"" + it.name + "\"])\n}"
                } + "\n}"
            } else {
                "nothing for class " + typeClass.innerType
            }
        } + if (!typeClass.name.contains("anonymous", true)) {
            "\nfun parse" + typeClass.name + "(tokens: String): " + typeClass.name + "? {\nval parsed = " + "firstEval(" + typeClass.name.decapitalize() + ", " + "0" + ", " + "tokens.toList()" + ", " + "tokens.length" + ")\n" + "if (" + "parsed" + " != null) {\nreturn " + typeClass.name + "(parsed)" + "\n} else {\nreturn null\n}\n}"
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
            return typeClass.name
        } else if (typeClass.type == DefinedTypeClassValue.Expression) {
            return typeClass.name
        } else if (typeClass.type == DefinedTypeClassValue.AnyOf) {
            return typeClass.name
        }
    }
    return "nothing for class " + typeClass.name
}

var currentAnonymousType = 0

fun generateTypeClassForExpression(
    type: Expression,
    name: String,
    generatedClasses: HashMap<Expression, TypeClass>,
    types: ArrayList<Pair<Expression, String>>,
    includeInGeneratedClasses: Boolean = true
): TypeClass {
    val existing = generatedClasses[type]
    if (existing != null) {
        if (existing.name != name) {
            return generateTypeClassForExpression(
                cloneExpression(type),
                name,
                generatedClasses,
                types,
                includeInGeneratedClasses
            )
        } else {
            return existing
        }
    }
    if (type is CustomExpression) {
        println(type.condition)
    }
    if (type is RequireExpression) {
        val expressionName = expressionName(type.expression, types)
        generateTypeClassForExpression(type.expression, expressionName, generatedClasses, types, includeInGeneratedClasses)
    }
    if (type is NotExpression) {
        generateTypeClassForExpression(type.expression, name, generatedClasses, types, includeInGeneratedClasses)
    }
    if (type is MultiExpression) {
        val properties = ArrayList<TypeProperty>()
        val generatedClass = TypeClass(name, properties)
        if (existing == null) {
            if (includeInGeneratedClasses) {
                generatedClasses[type] = generatedClass
            }
        } else {
            return existing
        }
        type.forEach { expression ->
            if (expression.name != null) {
                if (generatedClasses[expression] == null) {
                    val expressionName = types.find { it.first == expression }?.second
                    if (expressionName != null) {
                        val typeClass = generateTypeClassForExpression(expression, expressionName, generatedClasses, types)
                        properties.add(TypeProperty(expression.name!!, typeClass))
                    } else {
                        val typeName = expressionName(expression, types)
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
        return generatedClass
    } else if (type is EachOfExpression) {
        val innerTypes = ArrayList<TypeClass>()
        val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(AnyOf(*(((type as EachOfExpression) as List<Expression>).toTypedArray()))), innerTypes)
        definedTypeClass.name = name
        if (includeInGeneratedClasses) {
            generatedClasses[type] = definedTypeClass
        }
        innerTypes.addAll(type.map { innerType ->
            val outputExpressionName = expressionName(innerType, types)

            val typeClass = generatedClasses[innerType] ?: generateTypeClassForExpression(
                innerType,
                outputExpressionName,
                generatedClasses,
                types
            )
            typeClass
        })
        return definedTypeClass
    } else if (type is CustomExpression) {
        if (type.typeInfo != null) {
            val outputExpression = type.typeInfo.outputExpression
            when (outputExpression) {
                is AnyOf -> {
                    val innerTypes = outputExpression.innerTypes.map { innerType ->
                        val outputExpressionName = expressionName(innerType, types)

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
                    return definedTypeClass
                }

                is Type -> {
                    val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(outputExpression), TypeClass("", listOf()))
                    definedTypeClass.name = name
                    if (includeInGeneratedClasses) {
                        generatedClasses[type] = definedTypeClass
                    }
                    val outputExpressionName = expressionName(outputExpression.innerType, types)

                    val typeClass = generatedClasses[outputExpression.innerType] ?: generateTypeClassForExpression(
                        outputExpression.innerType,
                        outputExpressionName,
                        generatedClasses,
                        types
                    )
                    definedTypeClass.innerType = typeClass
                    return definedTypeClass
                }

                else -> {
                    val definedTypeClass = DefinedTypeClass(typeToDefinedTypeClassValue(outputExpression), TypeClass("", listOf()))
                    definedTypeClass.name = name
                    if (includeInGeneratedClasses) {
                        generatedClasses[type] = definedTypeClass
                    }
                    val outputExpressionName = expressionName(outputExpression, types)

                    val typeClass = generatedClasses[outputExpression] ?: generateTypeClassForExpression(
                        outputExpression,
                        outputExpressionName,
                        generatedClasses,
                        types
                    )
                    definedTypeClass.innerType = typeClass
                    return definedTypeClass
                }
            }
        }
    } else if (type is RecurringSomeExpression) {
        val definedTypeClass = DefinedTypeClass(DefinedTypeClassValue.List, TypeClass("", listOf()))
        definedTypeClass.name = name
        if (includeInGeneratedClasses) {
            generatedClasses[type] = definedTypeClass
        }
        val outputExpressionName = expressionName(type.expression, types)

        val typeClass = generatedClasses[type.expression] ?: generateTypeClassForExpression(
            type.expression,
            outputExpressionName,
            generatedClasses,
            types
        )
        definedTypeClass.innerType = typeClass
        return definedTypeClass
    } else if (type is RecurringSome0Expression) {
        val definedTypeClass = DefinedTypeClass(DefinedTypeClassValue.List, TypeClass("", listOf()))
        definedTypeClass.name = name
        if (includeInGeneratedClasses) {
            generatedClasses[type] = definedTypeClass
        }
        val outputExpressionName = expressionName(type.expression, types)

        val typeClass = generatedClasses[type.expression] ?: generateTypeClassForExpression(
            type.expression,
            outputExpressionName,
            generatedClasses,
            types
        )
        definedTypeClass.innerType = typeClass
        return definedTypeClass
    } else if (type is ColdExpression) {
        val generatedClass = TypeClass(name, listOf())
        val existing = generatedClasses[type]
        if (existing == null) {
            generatedClasses[type] = generatedClass
        } else {
            return existing
        }
        val coldExpression = type.expression()
        val typeName = expressionName(coldExpression, types)
        val generated = generateTypeClassForExpression(coldExpression, typeName, generatedClasses, types, false)
        generated.name = name
        if (includeInGeneratedClasses) {
            generatedClasses[type] = generated
        }
        return generated
    }

    val generatedClass = TypeClass(name, listOf())
    if (existing == null) {
        if (includeInGeneratedClasses) {
            generatedClasses[type] = generatedClass
        }
        return generatedClass
    } else {
        return existing
    }
}

fun expressionName(expression: Expression, types: List<Pair<Expression, String>>): String {
    val outputExpressionType = types.find { (typesExpression, name) ->
        typesExpression == expression
    }
    val outputExpressionName = if (outputExpressionType == null) {
        val name = "Anonymous" + currentAnonymousType
        currentAnonymousType += 1
        name
    } else {
        val (expression, name) = outputExpressionType
        name
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
    var innerType: TypeClass = TypeClass("", listOf())
) : TypeClass("", listOf()) {
    constructor(type: DefinedTypeClassValue, innerType: TypeClass) : this(type, listOf(innerType))
}

enum class DefinedTypeClassValue {
    List, Content, AnyOf, Optional, Expression
}

fun cloneExpression(expression: Expression): Expression {
    return expression[expression.name ?: ""].apply {
        currentExpressionId += 1
        this.id = currentExpressionId
    }
}
