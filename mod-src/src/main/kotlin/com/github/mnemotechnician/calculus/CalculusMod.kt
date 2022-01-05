package com.github.mnemotechnician.calculus

import arc.*
import arc.math.*
import arc.util.*
import arc.scene.ui.*
import mindustry.*
import mindustry.mod.*
import mindustry.game.*
import mindustry.ui.*
import mindustry.ui.dialogs.*
import io.mnemotechnician.autoupdater.* //todo: change the package name of this lib
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*
import com.github.mnemotechnician.calculus.windows.*

class CalculusMod : Mod() {
	
	val production = ProductionWindow()

	init {
		WindowManager.createWindow(production)
		
		Events.run(EventType.ClientLoadEvent::class.java) {
			Updater.checkUpdates(this)
		}
	}
	
}
