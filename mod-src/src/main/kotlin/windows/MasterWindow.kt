package com.github.mnemotechnician.calculus.windows

import arc.*
import arc.util.*
import arc.graphics.*
import arc.graphics.g2d.*
import arc.struct.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import mindustry.*
import mindustry.ui.*
import mindustry.gen.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*

class MasterWindow : Window() {
	
	override var name = "master window"
	override var closeable = false

	lateinit var buttonsTable: Table
	var windows = Seq<WindowEntry>(10)
	
	//can't bother making these static
	val unpressedColor = Color.royal.cpy().mul(0.65f)
	val pressedColor = unpressedColor.cpy().mul(0.65f)
	
	override fun onCreate() {
		table.apply {
			addLabel("Press to create windows").color(Color.gray).pad(5f).row()
			
			hsplitter()
			
			table.addTable {
				buttonsTable = this
				
				windows.each {
					createButton(it.name, it.iconName, it.window)
				}
			}
		}
	}
	
	/** Requests to add a button that allows to create new windows of specified type. The class must have a zero-argument constructor. */
	fun addWindow(name: String, iconName: String? = null, window: Class<out Window>) {
		windows.add(WindowEntry(window, name, iconName))
		
		if (::buttonsTable.isInitialized) createButton(name, iconName, window)
	}

	/** Actually creates a button */
	protected fun createButton(name: String, iconName: String? = null, window: Class<out Window>): Cell<Button> {
		return buttonsTable.customButton(button@ {
			addStack {
				this += Image(Tex.whiteui).apply {
					setFillParent(true)
				}.update {
					child(0).setColor(if (this@button.isPressed) pressedColor else unpressedColor)
				}
				
				this += createTable {
					center().setFillParent(true)
					margin(5f)
					
					if (iconName != null) {
						addImage(Core.atlas.find(iconName)).size(80f).row()
					}
					
					addLabel(name).scaleFont(0.7f)
				}
			}.grow()
		}, Styles.nonet) {
			try {
				val instance = window.newInstance()
				
				WindowManager.createWindow(instance)
			} catch (e: Exception) {
				Vars.ui.showException("Couldn't instantinate window $name!", e)
			}
		}.also {
			if (buttonsTable.children.size % 2 == 0) it.row()
		}.size(120f).margin(5f)
	}
	
	data class WindowEntry(val window: Class<out Window>, val name: String, val iconName: String?)
	
}
