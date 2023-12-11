package ch.scorpion.antares.view.analog.falstad

import ch.scorpion.antares.view.analog.AnalogCircuitAnalysis
import ch.scorpion.antares.view.analog.AnalogElement
import ch.scorpion.antares.view.analog.AnalogGraphView
import ch.scorpion.antares.view.analog.falstad.RowInfo.Type.*
import ch.scorpion.jabbah.base.logger
import kotlin.math.abs

class FalstadAnalogCircuitAnalysis(
	override val circuitView: AnalogGraphView,
	val nodeList: List<CircuitNode>,
	val voltageSources: Array<AnalogElement>
) : AnalogCircuitAnalysis {

	companion object {
		private val LOG by logger(FalstadAnalogCircuitAnalysis::class)

		/**
		 * Factors matrix [circuitMatrix] into upper and lower triangular matrices
		 * by gaussian elimination. Stores in [circuitPermute] an [Int] vector of
		 * pivot indices.
		 * @return `true` if the matrix is regular
		 */
		fun luFactor(a: Array<Array<Double>>, n: Int, ipvt: Array<Int>): Boolean {
			val scaleFactors = Array(n) { 0.0 }

			// Divide each row by its largest element, keeping track of the scaling factors
			for (i in 0 until n) {
				var largest = 0.0
				for (j in 0 until n) {
					val x = abs(a[i][j])
					if (x > largest) {
						largest = x
					}
				}
				// If all zeros, it's a singular matrix
				if (largest == 0.0) {
					return false
				}
				scaleFactors[i] = 1.0 / largest
			}

			// Use Crout's method: Loop through the columns
			for (j in 0 until n) {

				// Calculate upper triangular elements for this column
				for (i in 0 until j) {
					var q = a[i][j]
					for (k in 0 until i) {
						q -= a[i][k] * a[k][j]
					}
					a[i][j] = q
				}

				// Calculate lower triangular elements for this column
				var largest = 0.0
				var largestRow = -1
				for (i in j until n) {
					var q = a[i][j]
					for (k in 0 until j) {
						q -= a[i][k] * a[k][j]
					}
					a[i][j] = q
					var x = abs(q)
					if (x >= largest) {
						largest = x
						largestRow = i
					}
				}

				// Pivoting
				if (j != largestRow) {
					for (k in 0 until n) {
						val x = a[largestRow][k]
						a[largestRow][k] = a[j][k]
						a[j][k] = x
					}
					scaleFactors[largestRow] = scaleFactors[j]
				}

				// Keep track of row interchanges
				ipvt[j] = largestRow

				// Avoid zeros
				if (a[j][j] == 0.0) {
					println("Avoided zero")
					a[j][j] = 1E-18
				}

				if (j != n - 1) {
					val m = 1.0 / a[j][j]
					for (i in j + 1 until n) {
						a[i][j] *= m
					}
				}
			}

			return true
		}

		/**
		 * Solves the set of n linear equations using a LU factorization previously
		 * performed by [luFactor].
		 */
		fun luSolve(a: Array<Array<Double>>, n: Int, ipvt: Array<Int>, b: Array<Double>) {
			var i = 0

			// Find first non-zero b element
			while (i != n) {
				val row = ipvt[i]
				val swap = b[row]
				b[row] = b[i]
				b[i] = swap
				if (swap != 0.0) {
					break
				}

				i++
			}

			val bi = i++
			while (i < n) {
				val row = ipvt[i]
				var tot = b[row]

				b[row] = b[i]
				// Forward substitution using the lower triangular matrix
				for (j in bi until i) {
					tot -= a[i][j] * b[j]
				}
				b[i] = tot

				i++
			}

			i = n - 1
			while (i >= 0) {
				var tot = b[i]

				// Back-substitution using the upper triangular matrix
				for (j in i + 1 until n) {
					tot -= a[i][j] * b[j]
				}
				b[i] = tot / a[i][i]

				i--
			}
		}
	}

	val isNonLinear = circuitView.analogElementViews.any { it.isNonLinear }

	private var matrixSize = nodeList.size - 1 + voltageSources.size

	var circuitMatrix = Array(matrixSize) { Array(matrixSize) { 0.0 } }
		private set

	var circuitRightSide = Array(matrixSize) { 0.0 }
		private set

	val origMatrix = Array(matrixSize) { Array(matrixSize) { 0.0 } }

	val origRightSide = Array(matrixSize) { 0.0 }

	private var circuitMatrixSize = matrixSize

	val circuitMatrixFullSize = matrixSize

	val circuitPermute = Array(matrixSize) { 0 }

	val rowInfo = Array(matrixSize) { RowInfo() }

	var circuitNeedsMap = false

	var stopMessage: String? = null
		private set

	private fun stop(msg: String) {
		stopMessage = msg
	}

	fun getCircuitNode(index: Int): CircuitNode = nodeList[index]

	fun startSubIteration() {
		for (i in 0 until circuitMatrixSize) {
			circuitRightSide[i] = origRightSide[i]
		}
		if (isNonLinear) {
			for (i in 0 until circuitMatrixSize) {
				for (j in 0 until circuitMatrixSize) {
					circuitMatrix[i][j] = origMatrix[i][j]
				}
			}
		}
	}

	fun stampResistor(n1: Int, n2: Int, r: Double) {
		val r0 = 1 / r
		if (r0.isNaN() || r0.isInfinite()) {
			LOG.debug("Bad resistance $r $r0")
			throw IllegalArgumentException("Bad resistance")
		}
		stampMatrix(n1, n1, r0)
		stampMatrix(n2, n2, r0)
		stampMatrix(n1, n2, -r0)
		stampMatrix(n2, n1, -r0)
	}

	fun stampVoltageSource(n1: Int, n2: Int, vs: Int, v: Double) {
		val vn = nodeList.size + vs
		stampMatrix(vn, n1, -1.0)
		stampMatrix(vn, n2, 1.0)
		stampRightSide(vn, v)
		stampMatrix(n1, vn, 1.0)
		stampMatrix(n2, vn, -1.0)
	}

	/**
	 * Stamps value x in row i, column j, meaning that a voltage change
	 * of dv in node j will increase the current into node i by x dv,
	 * unless i or j is a voltage source node.
	 */
	private fun stampMatrix(row: Int, col: Int, x: Double) {
		var i = row
		var j = col
		if (i > 0 && j > 0) {
			if (circuitNeedsMap) {
				i = rowInfo[i - 1].mapRow
				val ri = rowInfo[j - 1]
				if (ri.type == Constant) {
					circuitRightSide[i] -= x * ri.value
					return
				}
				j = ri.mapCol
			} else {
				i--
				j--
			}
			circuitMatrix[i][j] += x
		}
	}

	/**
	 * Stamps value x on the right side of row i, representing an independent
	 * current source flowing into node i.
	 */
	private fun stampRightSide(row: Int, x: Double) {
		var i = row
		if (i > 0) {
			if (circuitNeedsMap) {
				i = rowInfo[i - 1].mapRow
			} else {
				i--
			}
			circuitRightSide[i] += x
		}
	}

	fun simplify() {
		var i = 0

		i = 0
		while (i < matrixSize) {
			var j = 0
			var qm = -1
			var qp = -1
			var qv = 0.0
			val re = rowInfo[i]

			if (re.lsChanges || re.dropRow || re.rsChanges) {
				i++
				continue
			}
			var rsAdd = 0.0

			// Look for rows that can be removed
			j = 0
			while (j < matrixSize) {
				val q = circuitMatrix[i][j]
				if (rowInfo[j].type == Constant) {
					// Keep a running total of const values that have been removed already
					rsAdd -= rowInfo[j].value * q
					j++
					continue
				}
				if (q == 0.0) {
					j++
					continue
				}
				if (qp == -1) {
					qp = j
					qv = q
					j++
					continue
				}
				if (qm == -1 && q == -qv) {
					qm = j
					j++
					continue
				}
				break
			}

			if (j == matrixSize) {
				if (qp == -1) {
					stop("Matrix error")
					return
				}

				var elt = rowInfo[qp]
				if (qm == -1) {
					// We found a row with only one nonzero entry. That value is a constant.
					var k = 0
					while (elt.type == Equal && k < 100) {
						// Follow the chain
						qp = elt.nodeEq
						elt = rowInfo[qp]
						k++
					}

					if (elt.type == Equal) {
						// Break equal chains
						elt.type == Normal
						i++
						continue
					}

					if (elt.type != Equal) {
						LOG.trace("Type already ${elt.type} for $qp!")
						i++
						continue
					}

					elt.type = Constant
					elt.value = (circuitRightSide[i] + rsAdd) / qv
					rowInfo[i].dropRow = true

					// Start over from scratch
					i = 0
				} else if (circuitRightSide[i] + rsAdd == 0.0) {
					// We found a row with only two nonzero entries, and one is the negative
					// of the other. The values are equal
					if (elt.type != Normal) {
						val qq = qm
						qm = qp
						qp = qq
						elt = rowInfo[qp]
						if (elt.type != Normal) {
							// We should follow the chain here, but this hardly ever happens,
							// so it's not worth worrying about
							i++
							continue
						}
					}

					elt.type = Equal
					elt.nodeEq = qm
					rowInfo[i].dropRow = true
				}
			}

			i++
		}

		// Find size of new matrix
		var nn = 0
		i = 0
		while (i < matrixSize) {
			val elt = rowInfo[i]
			if (elt.type == Normal) {
				elt.mapCol = nn++
				i++
				continue
			}
			if (elt.type == Equal) {
				var e2: RowInfo?
				// Resolve chains of equality. 100 max steps to avoid loops.
				for (j in 0 until 100) {
					e2 = rowInfo[elt.nodeEq]
					if (e2.type != Equal) {
						break
					}
					if (i == e2.nodeEq) {
						break
					}
					elt.nodeEq = e2.nodeEq
				}
			}

			if (elt.type == Constant) {
				elt.mapCol = -1
			}

			i++
		}

		i = 0
		while (i < matrixSize) {
			val elt = rowInfo[i]
			if (elt.type == Equal) {
				val e2 = rowInfo[elt.nodeEq]
				if (e2.type == Constant) {
					// If something is equal to a const, it's a const
					elt.type = e2.type
					elt.value = e2.value
					elt.mapCol = -1
				} else {
					elt.mapCol = e2.mapCol
				}
			}

			i++
		}

		// Make the new, simplified matrix
		val newSize = nn
		val newMatrix = Array(newSize) { Array(newSize) { 0.0 } }
		val newRS = Array(newSize) { 0.0 }
		var ii = 0

		i = 0
		while (i < matrixSize) {
			val rri = rowInfo[i]
			if (rri.dropRow) {
				rri.mapRow = -1
				i++
				continue
			}
			newRS[ii] = circuitRightSide[i]
			rri.mapRow = ii
			for (j in 0 until matrixSize) {
				val ri = rowInfo[j]
				if (ri.type == Constant) {
					newRS[ii] -= ri.value * circuitMatrix[i][j]
				} else {
					newMatrix[ii][ri.mapCol] += circuitMatrix[i][j]
				}
			}
			ii++
			i++
		}

		circuitMatrix = newMatrix
		circuitRightSide = newRS
		matrixSize = newSize
		circuitMatrixSize = newSize

		i = 0
		while (i < matrixSize) {
			origRightSide[i] = circuitRightSide[i]
			for (j in 0 until matrixSize) {
				origMatrix[i][j] = circuitMatrix[i][j]
			}
			i++
		}

		circuitNeedsMap = true

		// If matrix is linear, we can do the luFactor() here instead of needing it
		// to do in every frame
		if (!circuitView.isNonLinear) {
			if (!luFactor(circuitMatrix, matrixSize, circuitPermute)) {
				stop("Singular matrix!")
			}
		}
	}

	fun luFactor(): Boolean = Companion.luFactor(circuitMatrix, matrixSize, circuitPermute)

	fun luSolve() = Companion.luSolve(circuitMatrix, matrixSize, circuitPermute, circuitRightSide)
}