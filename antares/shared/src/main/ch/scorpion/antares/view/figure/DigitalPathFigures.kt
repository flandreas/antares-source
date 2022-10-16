package ch.scorpion.antares.view.figure

import ch.scorpion.antares.view.symbolstyle.SymbolStyle
import ch.scorpion.jabbah.base.Translations

class AndGateFigure : AbstractPathFigure(
	SymbolStyle.AND_PATH,
	Translations.getString("antares.figure.andShape"))

class OrGateFigure : AbstractPathFigure(
	SymbolStyle.OR_PATH,
	Translations.getString("antares.figure.orShape"))

class NotGateFigure : AbstractPathFigure(
	SymbolStyle.NOT_PATH,
	Translations.getString("antares.figure.notShape"))