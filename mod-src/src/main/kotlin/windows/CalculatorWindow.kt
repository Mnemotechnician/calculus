package com.github.mnemotechnician.calculus.windows

import arc.graphics.*
import arc.struct.*
import arc.scene.actions.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import mindustry.*
import mindustry.ui.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*

class CalculatorWindow : Window() {
	
	override var name = "calculator"
	override var closeable = true
	
	lateinit var resultLabel: Label
	
	var expression = StringBuilder(50)
	var result = ""
	
	override fun onCreate() {
		table.apply {
			addTable {
				//expression field
				addLabel({ expression }).scaleFont(1.3f).growX().maxHeight(400f).row()
				
				//result field
				resultLabel = addLabel({ result }).color(Color.gray).growX().maxHeight(200f).get()
			}.growX().padLeft(5f).padRight(5f)
			
			hsplitter(Color.gray, 10f)
			
			//buttons
			addTable {
				fun Table.literal(char: Char) {
					textButton(char.toString()) {
						expression.append(char)
						updateDisplay()
					}
				}
				
				defaults().fill()
				
				textButton("[red]CA") { expression.clear(); updateDisplay() }
				textButton("[yellow]<") { if (expression.length > 0) expression.deleteAt(expression.length - 1); updateDisplay() }
				literal('/')
				literal('*')
				
				row()
				
				literal('1')
				literal('2')
				literal('3')
				literal('-')
				
				row()
				
				literal('4')
				literal('5')
				literal('6')
				literal('+')
				
				row()
				
				literal('7')
				literal('8')
				literal('9')
				literal('(')
				
				row()
				
				literal('.')
				literal('0')
				textButton("[green]=") { evaluate() }
				literal(')')
			}.grow()
		}
	}
	
	/** Evaluates the current expression and writes the value to $result */
	fun updateDisplay() {
		try {
			if (expression.length == 0) {
				result = "0.0"
				return
			}
			
			val r = Vars.mods.scripts.runConsole("+($expression)").toString().toDouble() //toDouble ensures the result is valid or throws an exception if it isn't
			
			if (r.isNaN()) {
				result = "< ? >"
			} else {
				result = r.toString()
			}
		} catch (e: Throwable) {
			result = "< error >"
		}
	}
	
	/** Evaluates the current expression and, in case of success, assigns the result to $expression */
	fun evaluate() {
		updateDisplay()
		
		if (!result.startsWith('<')) { //< something > = error
			expression.clear()
			expression.append(result)
			result = ""
		} else {
			//make the field blink
			resultLabel.addAction(Actions.sequence(
				Actions.color(Color.red),
				Actions.color(Color.gray, 0.4f)
			))
		}
	}
	
}