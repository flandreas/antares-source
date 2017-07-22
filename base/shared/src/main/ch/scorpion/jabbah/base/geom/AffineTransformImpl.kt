package ch.scorpion.jabbah.base.geom

import ch.scorpion.jabbah.base.Math

/**
 * A Kotlin implementation of the [AffineTransform] interface.
 *
 * Used when compiling to the JavaScript target environment. When compiling to the JVM target environment,
 * the original JDK class should be used.
 *
 * See https://docs.oracle.com/javase/8/docs/api/java/awt/geom/AffineTransform.html
 */
data class AffineTransformImpl (
        var m00: Double = 0.0, var m10: Double = 0.0,
        var m01: Double = 0.0, var m11: Double = 0.0,
        var m02: Double = 0.0, var m12: Double = 0.0) : AffineTransform {

    var type: Int = TYPE_UNKNOWN
    var state: Int = APPLY_IDENTITY

    init {
        updateState()
    }

    /** Constructs an [AffineTransformImpl] that represents the identity.*/
    constructor(): this(m00 = 1.0, m11 = 1.0)
    @Suppress("unused") constructor(t: AffineTransformImpl): this(t.m00, t.m10, t.m01, t.m11, t.m02, t.m12)

    companion object {
        val TYPE_UNKNOWN = -1
        val TYPE_IDENTITY = 0
        val TYPE_TRANSLATION = 1
        val TYPE_QUADRANT_ROTATION = 8
        val TYPE_GENERAL_ROTATION = 16

        val APPLY_IDENTITY = 0
        val APPLY_TRANSLATE = 1
        val APPLY_SCALE = 2
        val APPLY_SHEAR = 4

        val HI_SHIFT = 3
        val HI_IDENTITY = APPLY_IDENTITY shl HI_SHIFT
        val HI_TRANSLATE = APPLY_TRANSLATE shl HI_SHIFT
        val HI_SCALE = APPLY_SCALE shl HI_SHIFT
        val HI_SHEAR = APPLY_SHEAR shl HI_SHIFT

        val rot90conversion = intArrayOf(
                /* IDENTITY => */        APPLY_SHEAR,
                /* TRANSLATE (TR) => */  APPLY_SHEAR or APPLY_TRANSLATE,
                /* SCALE (SC) => */      APPLY_SHEAR,
                /* SC | TR => */         APPLY_SHEAR or APPLY_TRANSLATE,
                /* SHEAR (SH) => */      APPLY_SCALE,
                /* SH | TR => */         APPLY_SCALE or APPLY_TRANSLATE,
                /* SH | SC => */         APPLY_SHEAR or APPLY_SCALE,
                /* SH | SC | TR => */    APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE
        )
    }

    /** ---- [AffineTransform] interface */

    override fun getMatrix(): DoubleArray {
        return doubleArrayOf(m00, m10, m01, m11, m02, m12)
    }

    override val scaleX: Double get() = m00

    override val scaleY: Double get() = m11

    override val translateX: Double get() = m02

    override val translateY: Double get() = m12

    override val shearX: Double get() = m01

    override val shearY: Double get() = m10

    override val determinant: Double get() = m00 * m11 - m01 * m10

    override fun setToIdentity() {
        m00 = 1.0
        m11 = 1.0
        m10 = 0.0
        m01 = 0.0
        m02 = 0.0
        m12 = 0.0
        state = APPLY_IDENTITY
        type = TYPE_IDENTITY
    }

    override fun setToTranslation(tx: Double, ty: Double) {
        m00 = 1.0
        m10 = 0.0
        m01 = 0.0
        m11 = 1.0
        m02 = tx
        m12 = ty
        if (tx != 0.0 || ty != 0.0) {
            state = APPLY_TRANSLATE
            type = TYPE_TRANSLATION
        } else {
            state = APPLY_IDENTITY
            type = TYPE_IDENTITY
        }
    }

    override fun setToRotation(theta: Double, anchorX: Double, anchorY: Double) {
        setToRotation(theta)
        val sin = m10
        val oneMinusCos = 1.0 - m00
        m02 = anchorX * oneMinusCos + anchorY * sin
        m12 = anchorY * oneMinusCos - anchorX * sin
        if (m02 != 0.0 || m12 != 0.0) {
            state = state or APPLY_TRANSLATE
            type = type or TYPE_TRANSLATION
        }
    }

    fun setToRotation(theta: Double) {
        var sin = Math.sin(theta)
        val cos: Double
        if (sin == 1.0 || sin == -1.0) {
            cos = 0.0
            state = APPLY_SHEAR
            type = TYPE_QUADRANT_ROTATION
        } else {
            cos = Math.cos(theta)
            if (cos == -1.0) {
                sin = 0.0
                state = APPLY_SCALE
                type = TYPE_QUADRANT_ROTATION
            } else if (cos == 1.0) {
                sin = 0.0
                state = APPLY_IDENTITY
                type = TYPE_IDENTITY
            } else {
                state = APPLY_SHEAR or APPLY_SCALE
                type = TYPE_GENERAL_ROTATION
            }
        }
        m00 = cos
        m10 = sin
        m01 = -sin
        m11 = cos
        m02 = 0.0
        m12 = 0.0
    }

    override fun translate(tx: Double, ty: Double) {
        when (state) {
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> {
                m02 += tx * m00 + ty * m01
                m12 += tx * m10 + ty * m11
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR or APPLY_SCALE
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            APPLY_SHEAR or APPLY_SCALE -> {
                m02 = tx * m00 + ty * m01
                m12 = tx * m10 + ty * m11
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            APPLY_SHEAR or APPLY_TRANSLATE -> {
                m02 += ty * m01
                m12 += tx * m10
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            APPLY_SHEAR -> {
                m02 = ty * m01
                m12 = tx * m10
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SHEAR or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            APPLY_SCALE or APPLY_TRANSLATE -> {
                m02 += tx * m00
                m12 += ty * m11
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE
                    if (type != TYPE_UNKNOWN) {
                        type -= TYPE_TRANSLATION
                    }
                }
                return
            }
            APPLY_SCALE -> {
                m02 = tx * m00
                m12 = ty * m11
                if (m02 != 0.0 || m12 != 0.0) {
                    state = APPLY_SCALE or APPLY_TRANSLATE
                    type = type or TYPE_TRANSLATION
                }
                return
            }
            APPLY_TRANSLATE -> {
                m02 += tx
                m12 += ty
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_IDENTITY
                    type = TYPE_IDENTITY
                }
                return
            }
            APPLY_IDENTITY -> {
                m02 = tx
                m12 = ty
                if (tx != 0.0 || ty != 0.0) {
                    state = APPLY_TRANSLATE
                    type = TYPE_TRANSLATION
                }
                return
            }
        }
    }

    override fun scale(sx: Double, sy: Double) {
        var state = this.state
        when (state) {
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE, APPLY_SHEAR or APPLY_SCALE -> {
                m00 *= sx
                m11 *= sy
                m01 *= sy
                m10 *= sx
                if (m01 == 0.0 && m10 == 0.0) {
                    state = state and APPLY_TRANSLATE
                    if (m00 == 1.0 && m11 == 1.0) {
                        this.type = if (state == APPLY_IDENTITY)
                            TYPE_IDENTITY
                        else
                            TYPE_TRANSLATION
                    } else {
                        state = state or APPLY_SCALE
                        this.type = TYPE_UNKNOWN
                    }
                    this.state = state
                }
                return
            }
        /* NOBREAK */
            APPLY_SHEAR or APPLY_TRANSLATE, APPLY_SHEAR -> {
                m01 *= sy
                m10 *= sx
                if (m01 == 0.0 && m10 == 0.0) {
                    state = state and APPLY_TRANSLATE
                    if (m00 == 1.0 && m11 == 1.0) {
                        this.type = if (state == APPLY_IDENTITY)
                            TYPE_IDENTITY
                        else
                            TYPE_TRANSLATION
                    } else {
                        state = state or APPLY_SCALE
                        this.type = TYPE_UNKNOWN
                    }
                    this.state = state
                }
                return
            }
            APPLY_SCALE or APPLY_TRANSLATE, APPLY_SCALE -> {
                m00 *= sx
                m11 *= sy
                if (m00 == 1.0 && m11 == 1.0) {
                    state = state and APPLY_TRANSLATE
                    this.state = state
                    this.type = if (state == APPLY_IDENTITY)
                        TYPE_IDENTITY
                    else
                        TYPE_TRANSLATION
                } else {
                    this.type = TYPE_UNKNOWN
                }
                return
            }
            APPLY_TRANSLATE, APPLY_IDENTITY -> {
                m00 = sx
                m11 = sy
                if (sx != 1.0 || sy != 1.0) {
                    this.state = state or APPLY_SCALE
                    this.type = TYPE_UNKNOWN
                }
                return
            }
        }
    }

    override fun rotate(theta: Double) {
        val sin = Math.sin(theta)
        if (sin == 1.0) {
            rotate90()
        } else if (sin == -1.0) {
            rotate270()
        } else {
            val cos = Math.cos(theta)
            if (cos == -1.0) {
                rotate180()
            } else if (cos != 1.0) {
                var M0: Double = m00
                var M1: Double = m01
                m00 = cos * M0 + sin * M1
                m01 = -sin * M0 + cos * M1
                M0 = m10
                M1 = m11
                m10 = cos * M0 + sin * M1
                m11 = -sin * M0 + cos * M1
                updateState()
            }
        }
    }

    override fun transform(ptSrc: Point2D): Point2D {
        val ptDst = Point2D()
        // Copy source coords into local variables in case src == dst
        val x = ptSrc.x
        val y = ptSrc.y
        when (state) {
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> {
                ptDst.setLocation(x * m00 + y * m01 + m02,
                        x * m10 + y * m11 + m12)
                return ptDst
            }
            APPLY_SHEAR or APPLY_SCALE -> {
                ptDst.setLocation(x * m00 + y * m01, x * m10 + y * m11)
                return ptDst
            }
            APPLY_SHEAR or APPLY_TRANSLATE -> {
                ptDst.setLocation(y * m01 + m02, x * m10 + m12)
                return ptDst
            }
            APPLY_SHEAR -> {
                ptDst.setLocation(y * m01, x * m10)
                return ptDst
            }
            APPLY_SCALE or APPLY_TRANSLATE -> {
                ptDst.setLocation(x * m00 + m02, y * m11 + m12)
                return ptDst
            }
            APPLY_SCALE -> {
                ptDst.setLocation(x * m00, y * m11)
                return ptDst
            }
            APPLY_TRANSLATE -> {
                ptDst.setLocation(x + m02, y + m12)
                return ptDst
            }
            APPLY_IDENTITY -> {
                ptDst.setLocation(x, y)
                return ptDst
            }
            else -> return ptSrc
        }
    }

    override fun transform(ptSrc: Point2D, ptDst: Point2D?): Point2D {
        val result: Point2D = ptDst ?: Point2D()
        // Copy source coords into local variables in case src == dst
        val x = ptSrc.x
        val y = ptSrc.y
        when (state) {
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> {
                result.setLocation(x * m00 + y * m01 + m02, x * m10 + y * m11 + m12)
                return result
            }
            APPLY_SHEAR or APPLY_SCALE -> {
                result.setLocation(x * m00 + y * m01, x * m10 + y * m11)
                return result
            }
            APPLY_SHEAR or APPLY_TRANSLATE -> {
                result.setLocation(y * m01 + m02, x * m10 + m12)
                return result
            }
            APPLY_SHEAR -> {
                result.setLocation(y * m01, x * m10)
                return result
            }
            APPLY_SCALE or APPLY_TRANSLATE -> {
                result.setLocation(x * m00 + m02, y * m11 + m12)
                return result
            }
            APPLY_SCALE -> {
                result.setLocation(x * m00, y * m11)
                return result
            }
            APPLY_TRANSLATE -> {
                result.setLocation(x + m02, y + m12)
                return result
            }
            APPLY_IDENTITY -> {
                result.setLocation(x, y)
                return result
            }
            else -> {
                throw IllegalStateException("missing case in transform state switch")
            }
        }
    }

    override fun inverseTransform(ptSrc: Point2D): Point2D {
        val ptDst = Point2D()
        // Copy source coords into local variables in case src == dst
        var x = ptSrc.x
        var y = ptSrc.y
        when (state) {
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> {
                x -= m02
                y -= m12
                val det = m00 * m11 - m01 * m10
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw NonInvertibleTransformException("Determinant is " + det)
                }
                ptDst.setLocation((x * m11 - y * m01) / det,
                        (y * m00 - x * m10) / det)
                return ptDst
            }
        /* NOBREAK */
            APPLY_SHEAR or APPLY_SCALE -> {
                val det = m00 * m11 - m01 * m10
                if (Math.abs(det) <= Double.MIN_VALUE) {
                    throw NonInvertibleTransformException("Determinant is " + det)
                }
                ptDst.setLocation((x * m11 - y * m01) / det, (y * m00 - x * m10) / det)
                return ptDst
            }
            APPLY_SHEAR or APPLY_TRANSLATE -> {
                x -= m02
                y -= m12
                if (m01 == 0.0 || m10 == 0.0) {
                    throw NonInvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(y / m10, x / m01)
                return ptDst
            }
        /* NOBREAK */
            APPLY_SHEAR -> {
                if (m01 == 0.0 || m10 == 0.0) {
                    throw NonInvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(y / m10, x / m01)
                return ptDst
            }
            APPLY_SCALE or APPLY_TRANSLATE -> {
                x -= m02
                y -= m12
                if (m00 == 0.0 || m11 == 0.0) {
                    throw NonInvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(x / m00, y / m11)
                return ptDst
            }
        /* NOBREAK */
            APPLY_SCALE -> {
                if (m00 == 0.0 || m11 == 0.0) {
                    throw NonInvertibleTransformException("Determinant is 0")
                }
                ptDst.setLocation(x / m00, y / m11)
                return ptDst
            }
            APPLY_TRANSLATE -> {
                ptDst.setLocation(x - m02, y - m12)
                return ptDst
            }
            APPLY_IDENTITY -> {
                ptDst.setLocation(x, y)
                return ptDst
            }
            else -> return ptDst
        }
    }

    override fun concatenate(Tx: AffineTransform) {

        check(Tx is AffineTransformImpl)
        if (Tx !is AffineTransformImpl) {
            // enforce smart cast
            return
        }

        var M0: Double
        var M1: Double
        val T00: Double = Tx.m00
        val T01: Double
        val T10: Double
        val T11: Double = Tx.m11
        val T02: Double = Tx.m02
        val T12: Double = Tx.m12
        val mystate = state
        val txstate = Tx.state

        when (txstate shl HI_SHIFT or mystate) {

        /* ---------- Tx == IDENTITY cases ---------- */
            HI_IDENTITY or APPLY_IDENTITY, HI_IDENTITY or APPLY_TRANSLATE, HI_IDENTITY or APPLY_SCALE, HI_IDENTITY or APPLY_SCALE or APPLY_TRANSLATE, HI_IDENTITY or APPLY_SHEAR, HI_IDENTITY or APPLY_SHEAR or APPLY_TRANSLATE, HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE, HI_IDENTITY or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> return

        /* ---------- this == IDENTITY cases ---------- */
            HI_SHEAR or HI_SCALE or HI_TRANSLATE or APPLY_IDENTITY -> {
                m01 = Tx.m01
                m10 = Tx.m10
                m00 = Tx.m00
                m11 = Tx.m11
                m02 = Tx.m02
                m12 = Tx.m12
                state = txstate
                type = Tx.type
                return
            }
        /* NOBREAK */
            HI_SCALE or HI_TRANSLATE or APPLY_IDENTITY -> {
                m00 = Tx.m00
                m11 = Tx.m11
                m02 = Tx.m02
                m12 = Tx.m12
                state = txstate
                type = Tx.type
                return
            }
        /* NOBREAK */
            HI_TRANSLATE or APPLY_IDENTITY -> {
                m02 = Tx.m02
                m12 = Tx.m12
                state = txstate
                type = Tx.type
                return
            }
            HI_SHEAR or HI_SCALE or APPLY_IDENTITY -> {
                m01 = Tx.m01
                m10 = Tx.m10
                m00 = Tx.m00
                m11 = Tx.m11
                state = txstate
                type = Tx.type
                return
            }
        /* NOBREAK */
            HI_SCALE or APPLY_IDENTITY -> {
                m00 = Tx.m00
                m11 = Tx.m11
                state = txstate
                type = Tx.type
                return
            }
            HI_SHEAR or HI_TRANSLATE or APPLY_IDENTITY -> {
                m02 = Tx.m02
                m12 = Tx.m12
                m01 = Tx.m01
                m10 = Tx.m10
                m11 = 0.0
                m00 = m11
                state = txstate
                type = Tx.type
                return
            }
        /* NOBREAK */
            HI_SHEAR or APPLY_IDENTITY -> {
                m01 = Tx.m01
                m10 = Tx.m10
                m11 = 0.0
                m00 = m11
                state = txstate
                type = Tx.type
                return
            }

        /* ---------- Tx == TRANSLATE cases ---------- */
            HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE, HI_TRANSLATE or APPLY_SHEAR or APPLY_SCALE, HI_TRANSLATE or APPLY_SHEAR or APPLY_TRANSLATE, HI_TRANSLATE or APPLY_SHEAR, HI_TRANSLATE or APPLY_SCALE or APPLY_TRANSLATE, HI_TRANSLATE or APPLY_SCALE, HI_TRANSLATE or APPLY_TRANSLATE -> {
                translate(Tx.m02, Tx.m12)
                return
            }

        /* ---------- Tx == SCALE cases ---------- */
            HI_SCALE or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE, HI_SCALE or APPLY_SHEAR or APPLY_SCALE, HI_SCALE or APPLY_SHEAR or APPLY_TRANSLATE, HI_SCALE or APPLY_SHEAR, HI_SCALE or APPLY_SCALE or APPLY_TRANSLATE, HI_SCALE or APPLY_SCALE, HI_SCALE or APPLY_TRANSLATE -> {
                scale(Tx.m00, Tx.m11)
                return
            }

        /* ---------- Tx == SHEAR cases ---------- */
            HI_SHEAR or APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE, HI_SHEAR or APPLY_SHEAR or APPLY_SCALE -> {
                T01 = Tx.m01
                T10 = Tx.m10
                M0 = m00
                m00 = m01 * T10
                m01 = M0 * T01
                M0 = m10
                m10 = m11 * T10
                m11 = M0 * T01
                type = TYPE_UNKNOWN
                return
            }
            HI_SHEAR or APPLY_SHEAR or APPLY_TRANSLATE, HI_SHEAR or APPLY_SHEAR -> {
                m00 = m01 * Tx.m10
                m01 = 0.0
                m11 = m10 * Tx.m01
                m10 = 0.0
                state = mystate xor (APPLY_SHEAR or APPLY_SCALE)
                type = TYPE_UNKNOWN
                return
            }
            HI_SHEAR or APPLY_SCALE or APPLY_TRANSLATE, HI_SHEAR or APPLY_SCALE -> {
                m01 = m00 * Tx.m01
                m00 = 0.0
                m10 = m11 * Tx.m10
                m11 = 0.0
                state = mystate xor (APPLY_SHEAR or APPLY_SCALE)
                type = TYPE_UNKNOWN
                return
            }
            HI_SHEAR or APPLY_TRANSLATE -> {
                m00 = 0.0
                m01 = Tx.m01
                m10 = Tx.m10
                m11 = 0.0
                state = APPLY_TRANSLATE or APPLY_SHEAR
                type = TYPE_UNKNOWN
                return
            }
        }

        // If Tx has more than one attribute, it is not worth optimizing
        // all of those cases...
        T01 = Tx.m01
        T10 = Tx.m10
        when (mystate) {
            APPLY_SHEAR or APPLY_SCALE -> {
                state = mystate or txstate
                M0 = m00
                M1 = m01
                m00 = T00 * M0 + T10 * M1
                m01 = T01 * M0 + T11 * M1
                m02 += T02 * M0 + T12 * M1

                M0 = m10
                M1 = m11
                m10 = T00 * M0 + T10 * M1
                m11 = T01 * M0 + T11 * M1
                m12 += T02 * M0 + T12 * M1
                type = TYPE_UNKNOWN
                return
            }
        /* NOBREAK */
            APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE -> {
                M0 = m00
                M1 = m01
                m00 = T00 * M0 + T10 * M1
                m01 = T01 * M0 + T11 * M1
                m02 += T02 * M0 + T12 * M1
                M0 = m10
                M1 = m11
                m10 = T00 * M0 + T10 * M1
                m11 = T01 * M0 + T11 * M1
                m12 += T02 * M0 + T12 * M1
                type = TYPE_UNKNOWN
                return
            }

            APPLY_SHEAR or APPLY_TRANSLATE, APPLY_SHEAR -> {
                M0 = m01
                m00 = T10 * M0
                m01 = T11 * M0
                m02 += T12 * M0

                M0 = m10
                m10 = T00 * M0
                m11 = T01 * M0
                m12 += T02 * M0
            }

            APPLY_SCALE or APPLY_TRANSLATE, APPLY_SCALE -> {
                M0 = m00
                m00 = T00 * M0
                m01 = T01 * M0
                m02 += T02 * M0

                M0 = m11
                m10 = T10 * M0
                m11 = T11 * M0
                m12 += T12 * M0
            }

            APPLY_TRANSLATE -> {
                m00 = T00
                m01 = T01
                m02 += T02

                m10 = T10
                m11 = T11
                m12 += T12
                state = txstate or APPLY_TRANSLATE
                type = TYPE_UNKNOWN
                return
            }
        }
        updateState()
    }


    /** ---- [AffineTransformImpl] */

    private fun updateState() {
        if (m01 == 0.0 && m10 == 0.0) {
            if (m00 == 1.0 && m11 == 1.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_IDENTITY
                    type = TYPE_IDENTITY
                } else {
                    state = APPLY_TRANSLATE
                    type = TYPE_TRANSLATION
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SCALE
                    type = TYPE_UNKNOWN
                } else {
                    state = APPLY_SCALE or APPLY_TRANSLATE
                    type = TYPE_UNKNOWN
                }
            }
        } else {
            if (m00 == 0.0 && m11 == 0.0) {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR
                    type = TYPE_UNKNOWN
                } else {
                    state = APPLY_SHEAR or APPLY_TRANSLATE
                    type = TYPE_UNKNOWN
                }
            } else {
                if (m02 == 0.0 && m12 == 0.0) {
                    state = APPLY_SHEAR or APPLY_SCALE
                    type = TYPE_UNKNOWN
                } else {
                    state = APPLY_SHEAR or APPLY_SCALE or APPLY_TRANSLATE
                    type = TYPE_UNKNOWN
                }
            }
        }
    }

    private fun rotate90() {
        var M0 = m00
        m00 = m01
        m01 = -M0
        M0 = m10
        m10 = m11
        m11 = -M0
        var state = rot90conversion[this.state]
        if (state and (APPLY_SHEAR or APPLY_SCALE) == APPLY_SCALE && m00 == 1.0 && m11 == 1.0) {
            state -= APPLY_SCALE
        }
        this.state = state
        type = TYPE_UNKNOWN
    }

    private fun rotate180() {
        m00 = -m00
        m11 = -m11
        val state = this.state
        if (state and APPLY_SHEAR != 0) {
            // If there was a shear, then this rotation has no
            // effect on the state.
            m01 = -m01
            m10 = -m10
        } else {
            // No shear means the SCALE state may toggle when
            // m00 and m11 are negated.
            if (m00 == 1.0 && m11 == 1.0) {
                this.state = state and APPLY_SCALE.inv()
            } else {
                this.state = state or APPLY_SCALE
            }
        }
        type = TYPE_UNKNOWN
    }

    private fun rotate270() {
        var M0 = m00
        m00 = -m01
        m01 = M0
        M0 = m10
        m10 = -m11
        m11 = M0
        var state = rot90conversion[this.state]
        if (state and (APPLY_SHEAR or APPLY_SCALE) == APPLY_SCALE && m00 == 1.0 && m11 == 1.0) {
            state -= APPLY_SCALE
        }
        this.state = state
        type = TYPE_UNKNOWN
    }
}
