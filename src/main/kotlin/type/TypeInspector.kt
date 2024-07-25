package com.momid.type

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSFile
import com.momid.parser.expression.*
import com.momid.parser.find
import com.momid.parser.not
import java.io.File

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Type()

annotation class Property(val name: String, val type: String)

class MyProcessor(val codeGenerator: CodeGenerator, val kspLogger: KSPLogger) : SymbolProcessor {

    var invoked = false
    override fun process(resolver: Resolver): List<KSFile> {
        if (invoked) {
            return emptyList()
        }
        val registerFile = codeGenerator.createNewFile(Dependencies.ALL_FILES, "com.momid.register", "register")

//        val generatedFile = codeGenerator.createNewFile(Dependencies(false), "com.momid", "generated")

        var registerFilePackage = ""

        val generatedFile = File((resolver.getAllFiles().find {
            it.fileName == "RegisterTypes.kt"
        } ?: run {
            registerFile.close()
            throw (Throwable("there is no RegisterTypes.kt file"))
        }).also {
            registerFilePackage = it.packageName.asString()
        }.filePath).resolveSibling("generated.kt")
        generatedFile.createNewFile()
//        if (!generatedFileLocation.exists()) {
//            File("C:\\Users\\moham\\IdeaProjects\\TraDeep\\Inspection\\build\\ksp\\main\\kotlin").mkdirs()
//            File("C:\\Users\\moham\\IdeaProjects\\TraDeep\\Inspection\\build\\ksp\\main\\kotlin\\generated.kt").createNewFile()
//        }

        val expressions = ArrayList<Pair<String, String>>()
        resolver.getAllFiles().forEach { symbol ->
            val file = File(symbol.filePath)
            val declaration = file.readText()
            find(declaration, !"@Type" + until(!"val") + !"val" + spaces + until(!" ")["value"]).forEach() {
                val declaration = it["value"].correspondingTokensText(declaration.toList())
                kspLogger.info("ooo")
                kspLogger.info(declaration)
//                val writer = generatedFile.writer()
//                writer.write(declaration + "\n")
//                writer.flush()
                expressions.add(declaration to symbol.packageName.asString())
            }
//            processClass(symbol)
        }
        val imports = expressions.joinToString("\n") {
            val (name, packageName) = it
            "import " + packageName + "." + name
        }
        val writer = registerFile.writer()
        writer.write("""
            package com.momid.register
            
            import com.momid.type.registerTypes
            import java.io.File
        """.trimIndent() + "\n")
        writer.write(imports + "\n")
        writer.write("fun register() {\nprintln(registerTypes(listOf(" + expressions.joinToString(",\n") {
            val (name, filePackage) = it
            name + " to " + "\"" + name.capitalize() + "\""
        } + "), File(" + "\"\"\"" + generatedFile.absolutePath + "\"\"\"" + "), " + "\"" + "package " + registerFilePackage + "\"" + ", " + "\"\"\"" + imports + "\"\"\"" + "))\n}")
        writer.flush()
        invoked = true
        registerFile.close()
        return emptyList()
    }
}

class InspectionSymbolProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MyProcessor(environment.codeGenerator, environment.logger)
    }
}

fun main() {

}
