package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.signal.BitWidth
import kotlin.test.Test
import kotlin.test.assertEquals

class AddressableValueConverterTest {

    @Test
    fun shouldRenderHexadecimal() {
        assertEquals("FF", AddressableValueConverter.Hexadecimal.render(255UL, BitWidth.BW_8))
        assertEquals("0F", AddressableValueConverter.Hexadecimal.render(15UL, BitWidth.BW_8))
    }

    @Test
    fun shouldParseHexadecimal() {
        assertEquals(255UL, AddressableValueConverter.Hexadecimal.parse("FF", BitWidth.BW_8))
        assertEquals(15UL, AddressableValueConverter.Hexadecimal.parse("0F", BitWidth.BW_8))
    }
}