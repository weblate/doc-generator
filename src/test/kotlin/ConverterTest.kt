
import com.kotlindiscord.kord.extensions.ExtensibleBot
import kotlinx.coroutines.runBlocking
import org.hyacinthbots.docgenerator.excpetions.InvalidConverterException
import org.hyacinthbots.docgenerator.generator.ConverterFormatter
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnabledIfEnvironmentVariable(named = "TEST_TOKEN", matches = ".+")
class ConverterTest {

	@BeforeAll
	fun startBot(): Unit = runBlocking {
		ExtensibleBot(System.getenv("TEST_TOKEN")) {}
	}

	@Test
	@Order(1)
	fun `test converter`(): Unit = runBlocking {
		var converter1: String? = null
		var converter2: String? = null
		var converter3: String? = null
		assertThrows<InvalidConverterException> {
			ConverterFormatter(
				"com.kotlindiscord.kord.extensions.commands.converters.impl.NotRealConverter",
				"fake"
			).formatConverter()
		}

		assertDoesNotThrow {
			converter1 = ConverterFormatter(
				"com.kotlindiscord.kord.extensions.commands.converters.impl.ChannelConverter",
				"channel"
			).formatConverter()

			converter2 = ConverterFormatter(
				"com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl.StringChoiceConverter",
				""
			).formatConverter()

			converter3 = ConverterFormatter(
				"com.kotlindiscord.kord.extensions.commands.converters.SingleToDefaultingConverter",
				"channel"
			).formatConverter()
		}

		assertEquals("Channel", converter1)
		assertEquals("String Choice", converter2)
		assertEquals("Defaulting Channel", converter3)
	}
}
