package com.github.mnemotechnician.calculus.windows

import arc.graphics.*
import arc.struct.*
import arc.scene.actions.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import arc.scene.event.*
import mindustry.*
import mindustry.ui.*
import mindustry.game.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.ui.*
import com.github.mnemotechnician.mkui.windows.*

class SandboxWindow : Window() {
	
	override var name = "sandbox utils"
	override var closeable = true
	
	lateinit var pager: TablePager
	
	override fun onCreate() {
		table.apply {
			TODO("sandbox window")
			
			addCollapser({ Vars.net.client() }) {
				addLabel("""
					WARNING: these utilities won't work on servers!
					The game logic is handled on the server side!
				""".trimIndent()).color(Color.red).pad(5f).row()
			}
			
			addCollapser({ Vars.state.isCampaign() }) {
				addLabel("""
					WARNING: these utilities are not available in campaign.
					I don't want players to use it for cheating.
					If you really want to, use normal console.
				""".trimIndent()).pad(5f).row()
			}
			
			pager(true) {
				pager = this
				
				//team switch
				addPage("team") {
					teamSelector({ it == Vars.player.team() }) { Vars.player.team(it) }
				}
				
				addPage("player") {
					
				}
				
				addPage("spawn") {
					
				}
			}
		}
	}
	
	override fun onUpdate() {
		if (Vars.state.isCampaign() && !Vars.net.client()) {
			pager.touchable = Touchable.enabled
			pager.color.a = 1f
		} else {
			pager.touchable = Touchable.disabled
			pager.color.a = 0.5f
		}
	}
	
	/** Utility function — created a team selector */
	protected inline fun Table.teamSelector(selected: (Team) -> Boolean, crossinline onclick: TextButton.(Team) -> Unit) {
		buttonGroup {
			Team.baseTeams.forEach {
				textButton("${it.emoji} ${it.localized()}") {
					onclick(it)
				}.color(it.color).fillX().apply {
					if (selected(it)) fireClick()
				}.row()
			}
		}
	}
	
}