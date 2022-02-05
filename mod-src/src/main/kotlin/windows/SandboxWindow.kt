package com.github.mnemotechnician.calculus.windows

import arc.*
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
import com.github.mnemotechnician.calculus.util.*

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
		If you really want to, use the normal console
		(or install mnemotechnician/newconsole)
	""".trimIndent()
	
	override fun onCreate() {
		table.apply {
			addLabel({ currentWarn ?: "" }).color(Color.red).scaleFont(0.75f).row()
			
			pager(true) {
				pager = this
				
				//team switch
				addPage("player") {
					var infiniteHealth = false
					var lastUnit: mindustry.gen.Unit? = null
					
					addLabel({ "Team: ${Vars.player.team().name}" }).growX().marginBottom(5f).row()
					
					teamSelector(Vars.player.team()) { Vars.player.team(it) }
					
					hsplitter()
					
					addLabel("Rules").row()
					
					//infinite health toggle
					customButton({
						addLabel({ if (infiniteHealth) "Invulnerable" else "Vulnerable"})
					}) {
						infiniteHealth = !infiniteHealth
						
						if (!infiniteHealth) lastUnit?.health = lastUnit!!.type.health //reset
					}.update {
						val currentUnit = Vars.player.unit()
						
						if (infiniteHealth) currentUnit.health = Float.MAX_VALUE
						
						if (lastUnit != currentUnit) {
							if (lastUnit != null && lastUnit!!.isValid()) lastUnit!!.health = lastUnit!!.type.health //reset health when player switches units
							
							lastUnit = currentUnit
						}
					}.growX().row()
					
					//infinite resources toggle
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
					
					addLabel("Spawn at:").row()
					
					//spawn position selector
					pager {
						//relative to player
						addPage("player") {
							var offX = 0f
							var offY = 0f
							
							addTable {
								addLabel("offset: x:").marginRight(5f)
								
								numberField { offX = it }
								
								addLabel(", y:").marginRight(5f)
								
								numberField { offY = it }
							}.row()
							
							spawnButton({ currentUnit }, { currentTeam }, { Vars.player.x + offX * 8 }, { Vars.player.y + offY * 8 }).row()
							
							addLabel({ "x: ${(Vars.player.x / 8 + offX).toFixed(2)}, y: ${(Vars.player.y / 8 + offX).toFixed(2)}" }).color(Color.gray)
						}
						
						addPage("absolute") {
							lateinit var fieldX: TextField
							lateinit var fieldY: TextField
							var posX = 0f
							var posY = 0f
							
							addTable {
								addLabel("position: x:").marginRight(5f)
								
								fieldX = numberField { posX = it }.get()
								
								addLabel(", y:").marginRight(5f)
								
								fieldY = numberField { posY = it }.get()
							}.row()
							
							addTable {
								spawnButton({ currentUnit }, { currentTeam }, { posX * 8 }, { posY * 8 })
								
								textButton("locate") {
									posX = Vars.player.x / 8
									posY = Vars.player.y / 8
									fieldX.setText(posX.toFixed(2))
									fieldY.setText(posY.toFixed(2))
								}
							}
						}
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
	protected inline fun Table.teamSelector(selected: Team, crossinline onclick: Button.(Team) -> Unit): Cell<ScrollPane> {
		return scrollPane {
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
	
	/** Utility function: creates a spawn button with a coordinate provider */
	protected inline fun Table.spawnButton(crossinline currentUnit: () -> UnitType?, crossinline currentTeam: () -> Team, crossinline x: () -> Float, crossinline y: () -> Float): Cell<Table> {
		return addTable {
			addImage({ currentUnit()?.fullIcon ?: Icon.none.region }, scaling = Scaling.bounded).size(30f)
			
			textButton("spawn", Styles.nodet) {
				currentUnit()?.let {
					val u = it.create(currentTeam())
					u.set(x(), y())
					u.dead = true //workaround: this won't allow the unit to get destroyed due to unit cap
					u.add()
					u.dead = false
				}
			}.update {
				it.setColor(if (currentUnit() == null) Color.gray else Color.white)
			}.padLeft(5f)
		}
	}
	
	/** Utility function: adds a number input field */
	protected inline fun Table.numberField(default: Float = 0f, crossinline onChange: (Float) -> Unit): Cell<TextField> {
		return textField(default.toString()) {
			try {
				onChange(it.toFloat())
			} catch (e: NumberFormatException) {
				setText(default.toString())
			}
		}
	}
	
}