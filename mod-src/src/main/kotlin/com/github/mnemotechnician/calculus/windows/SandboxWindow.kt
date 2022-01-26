package com.github.mnemotechnician.calculus.windows

import arc.util.*
import arc.graphics.*
import arc.struct.*
import arc.scene.actions.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import arc.scene.event.*
import mindustry.*
import mindustry.ui.*
import mindustry.gen.*
import mindustry.game.*
import mindustry.type.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.ui.*
import com.github.mnemotechnician.mkui.windows.*

class SandboxWindow : Window() {
	
	override var name = "sandbox utils"
	override var closeable = true
	
	lateinit var pager: TablePager
	
	var currentWarn: String? = null
	val multiplayerWarn = """
		WARNING: these utilities won't work on servers!
		The game logic is handled on the server side!
	""".trimIndent()
	val campaignWarn = """
		WARNING: these utilities are not available in campaign.
		I don't want players to use it for cheating.
		If you really want to, use normal console.
	""".trimIndent()
	
	override fun onCreate() {
		table.apply {
			addLabel({ currentWarn ?: "" }).color(Color.red).scaleFont(0.75f).row()
			
			pager(true) {
				pager = this
				
				//team switch
				addPage("player") {
					addLabel({ "Team: ${Vars.player.team().name}" }).growX().marginBottom(5f).row()
					
					teamSelector(Vars.player.team()) { Vars.player.team(it) }
					
					hsplitter()
					
					addLabel("Rules").row()
					
					customButton({
						addLabel({ if (Vars.state.rules.infiniteResources) "Infinite resources" else "Finite resources" }).growX()
					}) {
						Vars.state.rules.infiniteResources = !Vars.state.rules.infiniteResources
					}.growX().row()
				}
				
				addPage("spawn") {
					var currentTeam = Team.sharded
					var currentUnit: UnitType? = null
					
					teamSelector(Vars.player.team()) { currentTeam = it }
					
					hsplitter()
					
					addTable(Tex.buttonDown) {
						update { this@addTable.setColor(currentTeam.color) }
						
						scrollPane {
							Vars.content.units().each {
								val unit = it
								
								customButton({
									addImage(it.fullIcon, scaling = Scaling.bounded).size(30f)
								}) {
									currentUnit = unit
								}.fillX().apply {
									if (this@scrollPane.children.size % 6 == 0) row() //6 buttons per row
								}
							}
						}.height(120f)
					}
					
					hsplitter()
					
					addTable {
						addImage({ currentUnit?.fullIcon ?: Icon.none.region }, scaling = Scaling.bounded).size(30f)
						
						textButton("spawn", Styles.nodet) {
							currentUnit?.let {
								val u = it.create(currentTeam)
								u.set(Vars.player.x, Vars.player.y)
								u.dead = true //workaround: this doesn't allow the unit to be destroyed because of unit cap
								u.add()
								u.dead = false
							}
						}.update {
							it.setColor(if (currentUnit == null) Color.gray else Color.white)
						}.padLeft(5f)
					}
				}
			}
		}
	}
	
	override fun onUpdate() {
		currentWarn = when {
			Vars.state.isCampaign() -> campaignWarn
			
			Vars.net.client() -> multiplayerWarn
			
			else -> null
		}
		
		if (currentWarn == null) {
			pager.touchable = Touchable.enabled
			pager.color.a = 1f
		} else {
			pager.touchable = Touchable.disabled
			pager.color.a = 0.5f
		}
	}
	
	/** Utility function — creates a team selector */
	protected inline fun Table.teamSelector(selected: Team, crossinline onclick: Button.(Team) -> Unit) {
		scrollPane {
			for (i in 0..31) { //32 first teams: 256 is too many, 6 is not enough
				val team = Team.all[i]
				
				customButton({
					addImage(Tex.whiteui).color(team.color).size(30f)
				}) {
					onclick(team)
				}.fillX().apply {
					if (selected == it) fireClick() //fire click if the team has to be selected
					
					if (this@scrollPane.children.size % 6 == 0) row() //6 buttons per row
				}
			}
		}.height(120f)
	}
	
}