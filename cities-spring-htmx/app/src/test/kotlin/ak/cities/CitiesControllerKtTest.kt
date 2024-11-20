package ak.cities

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class CitiesControllerKtTest {

    @Test
    fun testCreateHtml() {
        assertEquals(
            // language=HTML
            """
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Cities</title>
  </head>
  <body>
    <h1>Cities</h1>
  </body>
</html>

            """.trimIndent(), createHtml("Cities")
        )
    }
}
