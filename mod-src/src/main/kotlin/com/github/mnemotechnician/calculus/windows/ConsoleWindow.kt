package com.github.mnemotechnician.calculus.windows

import arc.util.*
import arc.graphics.*
import arc.struct.*
import arc.scene.actions.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import mindustry.*
import mindustry.ui.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*

class ConsoleWindow : Window() {
	
	override var name = "console"
	override var closeable = true

	lateinit var field: TextField
	var log = "console result will appear here"
	
	override fun onCreate() {
		table.apply {
			addTable {
				field = textField("").width(300f).get()
				field.removeInputDialog();
				field.setMessageText("js script goes here");
				
				textButton(">>") {
					try {
						Log.info("JS \$ ${field.text}")
						val result = Vars.mods.scripts.runConsole(field.text)
						Log.info("> $result")
						log = result
					} catch (e: Throwable) {
						val result = e.stackTraceToString()
						Log.err(e)
						log = result
					}
				}.row()
			}.growX().row()
			
			scrollPane {
				left().top()
				
				addLabel({ log }).top().left().growX().height(250f)
			}.grow()
		}.margin(5f)
	}
	
}