import com.momid.parseFullFunctionCall
import com.momid.text
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ParserTest {
    @Test
    fun parseFunctionTest() {
        val text = "some(fun validate(param, otherParam, anotherParam))"
        val functionCall = parseFullFunctionCall(text)!!
        assertEquals(functionCall.ooo.inside.functionName.text(text), "validate")
        assertContentEquals(
            functionCall.ooo.inside.parameters.inside.map {
                it.variableName.text(text)
            },
            listOf("param", "otherParam", "anotherParam")
        )
    }
}
