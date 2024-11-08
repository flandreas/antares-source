package ch.scorpion.antares.view.analog.engine

/** Info about each row/column of the matrix for simplification purposes. */
class RowInfo {

	enum class Type {
		Normal,
		Constant,
		Equal
	}

	var type: Type = Type.Normal

	var nodeEq = 0
	var mapCol = 0
	var mapRow = 0
	var value = 0.0

	// Row's right side changes
	var rsChanges = false

	// Row's left side changes
	var lsChanges = false

	// Row is not needed in matrix
	var dropRow = false
}