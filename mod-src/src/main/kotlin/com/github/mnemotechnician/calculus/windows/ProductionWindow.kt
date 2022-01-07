package com.github.mnemotechnician.calculus.windows

import arc.util.pooling.*
import arc.graphics.*
import arc.struct.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import arc.scene.event.ChangeListener.*
import mindustry.*
import mindustry.ui.*
import mindustry.gen.*
import mindustry.type.*
import mindustry.world.*
import mindustry.world.consumers.*
import mindustry.content.*
import mindustry.world.blocks.production.*
import mindustry.world.blocks.defense.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*
import com.github.mnemotechnician.calculus.util.*

open class ProductionWindow : Window() {
	
	override var name = "production"
	override var closeable = true
	var iconScale = 2f
	var fontScale = 0.75f
	
	lateinit var blocksTable: Table
	lateinit var statsTable: Table
	
	var timeScale = 1f //overdrive
	var lastItem: Item? = null
	var lastBlock: Block? = null
	var lastBlockButton: TextButton? = null
	
	lateinit var mineableItems: ObjectSet<Item>
	
	override open fun onCreate() {
		mineableItems = Vars.content.blocks().map { it.itemDrop }.asSet()
		
		table.apply {
			//items
			addTable {
				addLabel("Item").scaleFont(fontScale).row()
				
				scrollPane {
					buttonGroup {
						Vars.content.items().each {
							textButton(it.emoji(), Styles.togglet) {
								if (it != lastItem) {
									lastItem = it
									rebuildBlocks(it)
								}
							}.marginBottom(5f).scaleButtonFont(iconScale)
							
							row()
						}
					}
				}.growY()
			}.height(350f)
			
			vsplitter()
			
			//blocks. this pane is generated when the user clicks on an item
			addTable {
				addLabel("Block").scaleFont(fontScale).row()
				
				scrollPane {
					top()
					blocksTable = this
				}.growY()
			}.growY()
			
			vsplitter()
			
			//stats. generated when the user clicks on a block.
			addTable {
				addLabel("stats").scaleFont(fontScale).row()
				//overdrives
				buttonGroup {
					textButton("X", Styles.togglet) {
						timeScale = 1f
						redoLast()
					}.scaleButtonFont(fontScale)
					
					Vars.content.blocks().each {
						if (it is OverdriveProjector) {
							textButton(it.emoji(), Styles.togglet) {
								timeScale = it.speedBoost
								redoLast()
							}.scaleButtonFont(fontScale)
							
							//if this overdrive supports phase boost, include boosted as separate entry
							if (it.hasBoost) {
								textButton("${it.emoji()} ${Items.phaseFabric.emoji()}", Styles.togglet) {
									timeScale = it.speedBoost + it.speedBoostPhase
									redoLast()
								}.scaleButtonFont(fontScale)
							}
						}
					}
				}
				.growX().visible { lastBlock != null }
				
				hsplitter()
				
				scrollPane {
					top().defaults().growX()
					statsTable = this
				}.grow()
			}.growY()
		}
	}
	
	/** Rebuilds blocks pane, adding all blocks that produce the providen item */
	open fun rebuildBlocks(item: Item) {
		blocksTable.clearChildren()
		statsTable.clearChildren()
		lastBlock = null
		lastBlockButton = null
		
		val isOre = item in mineableItems
		
		val group = blocksTable.buttonGroup {
			Vars.content.blocks().each { block ->
				if (isOre && block is Drill) {
					//drills
					if (item.hardness <= block.tier) {
						statsCategory(block) {
							val maxSpeed = (60f * block.size * block.size * timeScale) / (block.drillTime + block.hardnessDrillMultiplier * item.hardness)
							
							statEntry("X") {
								addLabel("Produces ${maxSpeed.toFixed(2)} ${item.emoji()}/sec").scaleFont(fontScale).row()
							}
							
							block.consumes.optionals().forEach {
								if (it is ConsumeLiquid) {
									statEntry(it.liquid.emoji()) {
										val boost = block.liquidBoostIntensity * block.liquidBoostIntensity
										addLabel("Produces ${(maxSpeed * boost).toFixed(2)} ${item.emoji()}/sec").scaleFont(fontScale)
									}
								}
							}
						}
					}
				} else if (block is GenericCrafter) {
					//crafters
					val stack =  block.outputItems?.find { it.item == item }
					if (stack != null) {
						statsCategory(block) {
							statEntry("X") {
								val maxSpeed = (60f * timeScale) / block.craftTime
								addLabel("Produces ${maxSpeed.toFixed(2 * stack.amount)} ${item.emoji()}/sec").scaleFont(fontScale).row()
								
								addLabel("Consumes:").scaleFont(fontScale).row()
								
								addTable {
									displayCons(block, maxSpeed)
								}.marginLeft(10f)
							}
						}
					}
				}
			}
		}
		
		//press the first button automatically
		group.get().childAs<TextButton>(0).fireClick()
	}
	
	/** Utility function — creates a button with block icon that reconstructs the stats table on click */
	protected inline fun Table.statsCategory(block: Block, crossinline lambda: Table.() -> Unit) {
		textButton(block.emoji(), Styles.togglet) {
			lastBlockButton = this
			
			if (lastBlock != block) {
				statsTable.clearChildren()
				lastBlock = block
				
				statsTable.addLabel("${block.emoji()} ${block.localizedName}").scaleFont(fontScale).marginBottom(50f).row()
				statsTable.lambda()
			}
		}.scaleButtonFont(iconScale).row()
	}
	
	/** Utility function — constructs a stat entry */
	protected inline fun Table.statEntry(left: String, crossinline constructor: Table.() -> Unit) = addTable {
		left()
		
		addLabel(left).scaleFont(fontScale).center()
		
		vsplitter(Color.gray)
		
		addTable {
			left()
			constructor()
		}.growX()
	}.also { it.growX().row() };
	
	/** Utility function: displays block's non-optional cons on the table */
	protected fun Table.displayCons(block: Block, maxSpeed: Float) {
		block.consumes.all().forEach {
			if (it.isOptional) return@forEach
			
			if (it is ConsumeItems) {
				it.items.forEach { addLabel("${(maxSpeed * it.amount).toFixed(2)} ${it.item.emoji()}/sec").scaleFont(fontScale).row() }
			} else if (it is ConsumeLiquid) {
				addLabel("${(maxSpeed * it.amount).toFixed(2)} ${it.liquid.emoji()}/sec").scaleFont(fontScale).row()
			}
		}
	}
	
	/** Simulates a click event on the last selected block, if present */
	protected fun redoLast() {
		lastBlock = null //otherwise it will just return to avoid unnecessary ui reconstruction
		lastBlockButton?.fireClick()
	}
	
}