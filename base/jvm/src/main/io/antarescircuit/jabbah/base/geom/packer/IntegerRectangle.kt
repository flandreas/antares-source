package io.antarescircuit.jabbah.base.geom.packer

class IntegerRectangle(
    var x: Int = 0,
    var y: Int = 0,
    var width: Int = 0,
    var height: Int = 0,
    var id: String = ""
) {
    val right: Int get() = x + width
    val bottom: Int get() = y + height
}