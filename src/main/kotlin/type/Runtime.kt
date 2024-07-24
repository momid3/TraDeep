package com.momid.type

import java.io.File
import kotlin.io.path.writeText

fun main() {
    val file = File("content.kt")
    file.createNewFile()
    if (File("content.jar").exists()) {
        File("content.jar").delete()
    }

    file.writeText("""
        fun main() {
            println("o")
        }
    """.trimIndent())

    val process = ProcessBuilder("kotlinc.bat", "content.kt", "-include-runtime", "-d", "content.jar").redirectErrorStream(true).start()
    println(process.inputStream.reader().readText())

    if (File("content.jar").exists()) {
        val executionProcess = ProcessBuilder("java", "-jar", "content.jar").redirectErrorStream(true).start()
        println(executionProcess.inputStream.reader().readText())
    }
}

fun runFile(codeText: String): String {
    val file = File("content.kt")
    file.createNewFile()
    if (File("content.jar").exists()) {
        File("content.jar").delete()
    }

    file.writeText(codeText)

    val process = ProcessBuilder("kotlinc.bat", "content.kt", "-include-runtime", "-d", "content.jar").redirectErrorStream(true).start()
    val compilationTerminal = process.inputStream.reader().readText()
    println(compilationTerminal)

    if (File("content.jar").exists()) {
        val executionProcess = ProcessBuilder("java", "-jar", "content.jar").redirectErrorStream(true).start()
        val terminal = executionProcess.inputStream.reader().readText()
        println(terminal)
        return terminal
    } else {
        return "compilation problem occurred " + compilationTerminal
    }
}
