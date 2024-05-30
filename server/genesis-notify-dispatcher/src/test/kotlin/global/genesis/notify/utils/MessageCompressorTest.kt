package global.genesis.notify.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class MessageCompressorTest {

    @Test
    fun testUncompressed() {
        val text = "abcdefghijk"
        val messageCompressor = MessageCompressor()

        assertEquals(text, messageCompressor.compress(text))
    }

    @Test
    fun testCompressed() {
        val text = "abcdefghijk".repeat(1000)

        val messageCompressor = MessageCompressor()

        assertNotEquals(text, messageCompressor.compress(text, "gz"))
    }
}
