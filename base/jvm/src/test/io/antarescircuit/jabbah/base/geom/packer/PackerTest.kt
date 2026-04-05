package io.antarescircuit.jabbah.base.geom.packer

import kotlin.test.Test
import kotlin.test.assertEquals

class PackerTest {

    @Test
    fun shouldPack() {
        var id = 0
        val packer = Packer(300, 300, 0)
            .insertRectangle(100, 100, (id++).toString())
            .insertRectangle(100, 100, (id++).toString())
            .insertRectangle(100, 100, (id++).toString())
            .insertRectangle(100, 100, (id++).toString())

        val result = packer.packRectangles()

        assertEquals(4, result)
    }
}