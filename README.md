# TraDeep
TraDeep is parser library for Kotlin. It let's you define the parser grammar in pure Kotlin like parser combinators, and generates corresponding classes for the parsed tree that you can access, like parser generators.
## Setup
To use TraDeep, you must define your parser grammar in a separate module, and use it to parse your data in the main (or any other module). Here are the steps to set it up:
1. Add dependency to TraDeep. Also add `JitPack` as your repository if haven't already.
```kotlin
repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") } // add this
}

dependencies {
    implementation(kotlin("stdlib-jdk8")) // add this
    implementation("com.github.momid3:TraDeep:0.3.8") // add this
}
```

2. Create a new module (file -> new -> module). Let's call it `ParserRules`.
3. Add dependency to TraDeep and Kotlin KSP and add `JitPack` as repository in your new module.

```kotlin
plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.23" // add this
}

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") } // add this
}

dependencies {
    implementation(kotlin("stdlib-jdk8")) // add this
    implementation("com.github.momid3:TraDeep:0.3.8") // add this
    ksp("com.github.momid3:TraDeep:0.3.8") // add this
}
```
4. Add dependency to your new module from your root module (or any other module you want to use your parser from).
```kotlin
dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("com.github.momid3:TraDeep:0.3.8")
    implementation(project(":ParserRules")) // add this
}
```
5. In your new module, create a new Kotlin file and name it `RegisterTypes.kt` and add the following as its content.
```kotlin
package com.yourpackage // replace with your actual package

import com.momid.register.register

fun main() {
    register()
}

```

## How to use
Define your parsing rules in the separate module you created.
Let's say we want to parse a function call (for simplicity, we assume every provided parameter is an identifier).
```kotlin
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
```

now, run the `RegisterTypes.kt` file you've created initially. This will generate the corresponding classes to your types. you have to do this every time you change your parsing rules.
And that's it. your AST will be created automatically.

In your root module, parse your data:

```kotlin
fun main() {
    val text = "validate(param, otherParam, anotherParam)"
    val functionCall = parseFunctionCall(text)!!
    println(functionCall.functionName.text(text))
    functionCall.parameters.inside.forEach {
        println(it.variableName.text(text))
    }
}
```

The function `parseFunctionCall` is auto generated. for the each expression you annotated with `@Type`, there is a corresponding function with name "parse + \<your type name\>".
## Introduction
In TraDeep, parsing rules are called `expressions`. every parsing rule is a subclass of the `Expression` class and similarly, every resulting object in the resulting AST, is a subclass of `ExpressionResult`.
These are the classes you interact with the most. `ExpressionResult` contains two important properties that you can access. `expression`, which is the `Expression` that it matched against, and `range` which is the IntRange of the start and end index of the expression.
There are built-in functions that help you create your parsing rules by combining them or even creating your own custom expressions that execute arbitrary kotlin code when matching expressions.
### Common expression functions and operators
`!`: The not operator is applied to strings and simply indicates a parsing rule that matches against them. for example, !"sweets" will match against the word "sweets". It *does not* negate.

`+`: The plus operator appends an expression to (the right of) this expression. for example, `!"I like " + !"sweets"` will create a new expression that will match against "I like sweets".

`some()`: This function takes an expression and returns an expression that will look for multiple subsequent recurrences of the expression. for example `some(!"sweets")` will match against both "sweets" and "sweetssweets" but not "sweets sweets".

`some0()`: Same as `some()` but will match if there was zero or more subsequent ocurrences. So `!"sweets" + spaces + !"sweets"` will match for both "sweetssweets" and "sweets sweets". (`spaces` is one of the built-in expressions you can use. It will look for zero or more 
whitespaces)

`anyOf()`: It will match if any of the provided parameters to it are present. It will start from the leftmost parameter and if satisfied, will discard the remaining expressions. The resulting AST object of this will contain nullable properties with the names of the provided to this function. So you can know which one did it match based on nullability, and then access the nested expression.

`CustomExpression()`: CustomExpression is a subclass of Expression and lets you create an expression with custom logic. It takes a lambda as parameter and inside it you can access the current index of where the TraDeep evaluator currently is within the text being parsed, and the end token until which you're allowed to access.
