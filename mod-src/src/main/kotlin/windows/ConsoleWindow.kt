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
	var log = StringBuilder(500).append("console output will appear here\n")
	
	override fun onCreate() {
		table.apply {
			addTable {
				field = textField("").width(300f).get()
				field.removeInputDialog();
				field.setMessageText("js script goes here");
				
				textButton(">>") {
					try {
						Log.info("JS \$ ${field.text}")
						log.append("[blue]JS $[] ").append(field.text).append('\n')
						
						val result = Vars.mods.scripts.runConsole(field.text)
						Log.info("> $result")
						log.append("[yellow]>[] ").append(result).append('\n')
					} catch (e: Throwable) {
						val result = e.stackTraceToString()
						Log.err(e)
						log.append("[red]ERROR >[] ").append(result).append('\n')
					}
				}.row()
			}.growX().row()
			
			addTable {
				scrollPane {
					left().top()
					
					it.setForceScroll(true, true)
					
					addLabel({ log }).grow().color(Color.lightGray).top().left()
				}.grow()
			}.fillX().height(300f)
		}.margin(5f)
	}
	
}
