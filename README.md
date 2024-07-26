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

The function `parseFunctionCall` is auto generated. for the each expression you annotated with `@Type`, there is a corresponding function with name "parse + <your type name>".
