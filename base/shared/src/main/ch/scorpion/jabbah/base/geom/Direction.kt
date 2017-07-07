package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.Math

enum class Direction(val customName: String, val dx: Int, val dy: Int, val rotation: Rotation) {
    EAST("east", 1, 0, Rotation.R0),
    NORTH("north", 0, -1, Rotation.R90),
    WEST("west", -1, 0, Rotation.R180),
    SOUTH("south", 0, 1, Rotation.R270);

    companion object {

        private val LOG by logger()

        val ALL: Set<Direction> = setOf(EAST, NORTH, WEST, SOUTH)

        /** Returns the [Direction] with the specified custom name.*/
        fun withName(name: String): Direction {
            for (dir in Direction.values()) {
                if (dir.customName == name) {
                    return dir
                }
            }
            throw IllegalArgumentException("unknown Direction $name")
        }

        /** Returns the [Direction] with the specified x and y offsets.*/
        fun of(dx: Int, dy: Int): Direction {
            for (dir in Direction.values()) {
                if (dir.dx == dx && dir.dy == dy) {
                    return dir
                }
            }
            throw IllegalArgumentException("Cannot determine Direction for ($dx,$dy)")
        }

        /**
         * Returns the [Direction] from a first point to a second point
         * @throws IllegalArgumentException if the two points are not orthogonal, i.e. if they don't have
         * exactly one coordinate in common
         */
        fun of(p1: Point2D, p2: Point2D): Direction {
            try {
                return of(Math.signum(p2.x - p1.x).toInt(), Math.signum(p2.y - p1.y).toInt())
            } catch (e: IllegalArgumentException) {
                LOG.error("Cannot determine Direction from $p1 to $p2")
                throw e
            }
        }

        /** Returns the [Direction] that represents the specified [Rotation].*/
        fun of(rotation: Rotation): Direction {
            for (dir in Direction.values()) {
                if (dir.rotation === rotation) {
                    return dir
                }
            }
            throw IllegalArgumentException("Cannot determine Direction for Rotation $rotation")
        }

        fun oppositeSet(directions: Set<Direction> ): Set<Direction> {
            val result = mutableSetOf<Direction>()
            directions.forEach { result.add(it.opposite()) }
            return result
        }

    }

    override fun toString(): String {
        return when (this) {
            EAST -> Translations.getString("graph.property.direction.east.name")
            NORTH -> Translations.getString("graph.property.direction.north.name")
            WEST -> Translations.getString("graph.property.direction.west.name")
            SOUTH -> Translations.getString("graph.property.direction.south.name")
        }
    }

    /** Returns the next counter-clockwise [Direction].*/
    fun next(): Direction {
        return Direction.values()[(this.ordinal + 1) % 4];
    }

    fun previous(): Direction {
        return Direction.values()[(this.ordinal + 3) % 4];
    }

    /** Returns the opposite of this [Direction].*/
    fun opposite(): Direction {
        return Direction.values()[(this.ordinal + 2) % 4];
    }

    /** Returns the result of mirroring this [Direction] horizontally, i.e. mirroring at a vertical axis.*/
    fun mirrorHorizontally(): Direction {
        return when(this) {
            NORTH, SOUTH -> this
            EAST, WEST -> opposite()
        }
    }

    /** Returns the result of mirroring this [Direction] vertically, i.e. mirroring at a horizontal axis.*/
    fun mirrorVertically(): Direction {
        return when (this) {
            NORTH, SOUTH -> opposite()
            EAST, WEST -> this
        }
    }

    fun abs(): Direction {
        return of(Math.abs(dx), Math.abs(dy))
    }

    /** Multiplies this [Direction] by the specified value and returns the result as a [Point2D].*/
    fun multiply(value: Double): Point2D {
        return Point2D(dx * value, dy * value)
    }

    fun isHorizontal(): Boolean {
        return dy == 0
    }

    fun isVertical(): Boolean {
        return dx == 0
    }

    /** Determines the [Turn] that distinguish this [Direction] from the specified [Direction].*/
    fun determineTurn(newDirection: Direction): Turn {
        if (newDirection == this) {
            return Turn.NONE
        }
        if (newDirection == opposite()) {
            return Turn.AROUND
        }
        if (newDirection == next()) {
            return Turn.LEFT
        }
        return Turn.RIGHT
    }

    /** Return the new [Direction] when turning from this [Direction].*/
    fun turn(turn: Turn): Direction {
        return when(turn) {
            Turn.AROUND -> opposite()
            Turn.LEFT -> next()
            Turn.RIGHT -> previous()
            Turn.NONE -> this
        }
    }
}