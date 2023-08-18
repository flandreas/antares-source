package ch.scorpion.antares.hdl.expression

import ch.scorpion.antares.hdl.HDLNet

interface Expression

/** A reference to a [HDLNet]. The value of the [Expression] is the name of the [HDLNet].*/
class NetExpression(val net: HDLNet) : Expression