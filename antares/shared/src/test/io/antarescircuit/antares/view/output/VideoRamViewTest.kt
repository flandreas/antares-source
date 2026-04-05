package io.antarescircuit.antares.view.output

import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.model.port.DigitalPort
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.model.signal.DigitalSignalFactory.of
import io.antarescircuit.jabbah.edit.model.Size
import io.antarescircuit.jabbah.execution.SignalHandler
import io.antarescircuit.jabbah.graphics.TestRasterImage
import dev.mokkery.MockMode
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VideoRamViewTest {

    private var rasterImage: TestRasterImage? = null
    private val videoRamView: VideoRamView
    private val signalHandler: SignalHandler = mock(MockMode.autofill)

    init {
        AntaresTestRule.configure()

        videoRamView = VideoRamView(rasterImageFactory =  { w,h ->
            rasterImage = TestRasterImage(w, h)
            rasterImage!!
        })

        videoRamView.size = Size.SMALL
        videoRamView.rowsCount = 10
        videoRamView.columnsCount = 10
    }

    @Test
    fun shouldUpdateCGAImage() {
        // 1 logical pixel per memory cell. 4 physical pixels due to Size.SMALL = 2x2
        writePixel(0UL, 1UL)

        assertCGAImage()
        assertNull(rasterImage!!.getColor(2, 0))
        assertNull(rasterImage!!.getColor(0, 2))
    }

    @Test
    fun shouldFillCGAImage() {
        // 1 logical pixel per memory cell. 4 physical pixels due to Size.SMALL = 2x2
        writePixel(0UL, 1UL)

        videoRamView.fillImage()

        assertCGAImage()
    }

    private fun assertCGAImage() {
        val color1 = VideoRamColorModel.CGA_16.getColor(1)
        assertEquals(color1, rasterImage!!.getColor(0, 0))
        assertEquals(color1, rasterImage!!.getColor(1, 0))
        assertEquals(color1, rasterImage!!.getColor(0, 1))
        assertEquals(color1, rasterImage!!.getColor(1, 1))
    }

    @Test
    fun shouldUpdate4BitMonochromeImage() {
        videoRamView.colorModel = VideoRamColorModel.MONOCHROME
        videoRamView.dataWidth = BitWidth.BW_4

        // 4 logical pixels per memory cell. 64 physical pixels due to Size.SMALL = 2x2
        writePixel(0UL, 6UL) // Bit pattern 0110

        assert4BitMonochromeImage()
        assertNull(rasterImage!!.getColor(8, 0))
        assertNull(rasterImage!!.getColor(8, 1))
    }

    @Test
    fun shouldFill4BitMonochromeImage() {
        videoRamView.colorModel = VideoRamColorModel.MONOCHROME
        videoRamView.dataWidth = BitWidth.BW_4

        // 4 logical pixels per memory cell. 64 physical pixels due to Size.SMALL = 2x2
        writePixel(0UL, 6UL) // Bit pattern 0110

        videoRamView.fillImage()

        assert4BitMonochromeImage()
    }

    private fun assert4BitMonochromeImage() {
        val color0 = VideoRamColorModel.MONOCHROME.getColor(0)
        val color1 = VideoRamColorModel.MONOCHROME.getColor(1)

        // Row 0
        assertEquals(color0, rasterImage!!.getColor(0, 0))
        assertEquals(color0, rasterImage!!.getColor(1, 0))
        assertEquals(color1, rasterImage!!.getColor(2, 0))
        assertEquals(color1, rasterImage!!.getColor(3, 0))
        assertEquals(color1, rasterImage!!.getColor(4, 0))
        assertEquals(color1, rasterImage!!.getColor(5, 0))
        assertEquals(color0, rasterImage!!.getColor(6, 0))
        assertEquals(color0, rasterImage!!.getColor(7, 0))

        // Row 1
        assertEquals(color0, rasterImage!!.getColor(0, 1))
        assertEquals(color0, rasterImage!!.getColor(1, 1))
        assertEquals(color1, rasterImage!!.getColor(2, 1))
        assertEquals(color1, rasterImage!!.getColor(3, 1))
        assertEquals(color1, rasterImage!!.getColor(4, 1))
        assertEquals(color1, rasterImage!!.getColor(5, 1))
        assertEquals(color0, rasterImage!!.getColor(6, 1))
        assertEquals(color0, rasterImage!!.getColor(7, 1))
    }

    @Test
    fun shouldUpdate1BitMonochromeImage() {
        videoRamView.colorModel = VideoRamColorModel.MONOCHROME
        videoRamView.dataWidth = BitWidth.BW_1

        // 1 logical pixel per memory cell. 4 physical pixels due to Size.SMALL = 2x2
        writePixel(1UL, 1UL)
        writePixel(2UL, 0UL)

        assert1BitMonochromeImage()
    }

    @Test
    fun shouldUpdateAddressWidth() {
        videoRamView.columnsCount = 512
        videoRamView.rowsCount = 256
        videoRamView.colorModel = VideoRamColorModel.MONOCHROME
        videoRamView.dataWidth = BitWidth.BW_16

        assertEquals(13, (videoRamView.model.getInput<DigitalSignal>("A") as DigitalPort).bitWidth.width)
    }

    private fun assert1BitMonochromeImage() {
        val color0 = VideoRamColorModel.MONOCHROME.getColor(0)
        val color1 = VideoRamColorModel.MONOCHROME.getColor(1)

        // Row 0
        assertNull(rasterImage!!.getColor(0, 0))
        assertNull(rasterImage!!.getColor(1, 0))
        assertEquals(color1, rasterImage!!.getColor(2, 0))
        assertEquals(color1, rasterImage!!.getColor(3, 0))
        assertEquals(color0, rasterImage!!.getColor(4, 0))
        assertEquals(color0, rasterImage!!.getColor(5, 0))

        // Row 1
        assertNull(rasterImage!!.getColor(0, 1))
        assertNull(rasterImage!!.getColor(1, 1))
        assertEquals(color1, rasterImage!!.getColor(2, 1))
        assertEquals(color1, rasterImage!!.getColor(3, 1))
        assertEquals(color0, rasterImage!!.getColor(4, 1))
        assertEquals(color0, rasterImage!!.getColor(5, 1))
    }

    private fun writePixel(addr: ULong, value: ULong) {
        val ram = videoRamView.model

        ram.getChipSelectInput().setIncomingSignal(of(true), signalHandler)
        ram.getWriteInput().setIncomingSignal(of(true), signalHandler)
        ram.getClearInput().setIncomingSignal(of(false), signalHandler)
        ram.getAddressInput().setIncomingSignal(of(videoRamView.model.addressWidth, addr), signalHandler)
        ram.getDataPort().setIncomingSignal(of(videoRamView.dataWidth, value), signalHandler)
        ram.getClockInput()!!.setIncomingSignal(of(true), signalHandler)

        ram.act(signalHandler, ram.createActorData(ram.getClockInput()!!))
    }
}