package com.github.mnemotechnician.calculus.windows

import arc.graphics.*
import arc.struct.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import mindustry.*
import mindustry.ui.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*

class MasterWindow : Window() {
	
	override var name = "windows"
	override var closeable = false

	lateinit var buttonsTable: Table
	val windows = ObjectMap<String, Class<out Window>>(10)
	
	override fun onCreate() {
		table.apply {
			buttonsTable = this
			
			addLabel("Press to create windows").color(Color.gray).pad(5f).row()
			
			hsplitter()
			
			table.addTable {
				windows.each { key, value ->
					createButton(key, value).fillX().row()
				}
			}
		}
	}
	
	/** Requests to add a button that allows to create new windows of specified type. The class must have a zero-argument constructor. */
	fun addWindow(name: String, window: Class<out Window>) {
		windows.put(name, window)
		
		if (::buttonsTable.isInitialized) createButton(name, window)
	}

	/** Actually creates a button */
	protected fun createButton(name: String, window: Class<out Window>): Cell<TextButton> {
		return buttonsTable.textButton(name) {
			try {
				val instance = window.newInstance()
				
				WindowManager.createWindow(instance)
			} catch (e: Exception) {
				Vars.ui.showException("Couldn't instantinate window $name!", e)
			}
		}
	}
	
}