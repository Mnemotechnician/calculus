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
	
	val master = MasterWindow()

	init {
		WindowManager.createWindow(master)
		
		master.addWindow("production", "calculus-factory", ProductionWindow::class.java)
		master.addWindow("calculator", "calculus-calculator", CalculatorWindow::class.java)
		master.addWindow("console", "calculus-console", ConsoleWindow::class.java)
		master.addWindow("sandbox", "calculus-crown", SandboxWindow::class.java)
		
		Events.run(EventType.ClientLoadEvent::class.java) {
			Updater.checkUpdates(this)
		}
	}
	
}
