package com.github.mnemotechnician.calculus.windows

import arc.struct.*
import arc.scene.ui.*
import arc.scene.ui.layout.*
import mindustry.*
import mindustry.ui.*
import mindustry.gen.*
import mindustry.type.*
import mindustry.world.*
import mindustry.world.blocks.production.*
import com.github.mnemotechnician.mkui.*
import com.github.mnemotechnician.mkui.windows.*
import com.github.mnemotechnician.calculus.util.*

open class ProductionWindow : Window() {
	
	override var name = "ratios"
	override var closeable = true
	
	lateinit var blocksTable: Table
	lateinit var statsTable: Table
	
	var iconScale = 2f
	var fontScale = 0.7f
	var lastItem: Item? = null
	var lastBlock: Block? = null
	
	lateinit var mineableItems: ObjectSet<Item>
	
	override open fun onCreate() {
		mineableItems = Vars.content.blocks().map { it.itemDrop }.asSet()
		
		table.apply {
			defaults().height(350f)
			
			scrollPane {
				addLabel("Item").scaleFont(fontScale).row()
				
				buttonGroup {
					Vars.content.items().each {
						textButton(it.emoji(), Styles.togglet) {
							if (it != lastItem) {
								lastItem = it
								rebuildBlocks(it)
							}
						}
						.marginBottom(5f).scaleButtonFont(iconScale)
						
						row()
					}
				}
			}
			
			vsplitter()
			
			scrollPane {
				top()
				addLabel("Block").scaleFont(fontScale).row()
				
				addTable {
					top()
					blocksTable = this
				}
			}.growY()
			
			vsplitter()
			
			scrollPane {
				top()
				addLabel("stats").scaleFont(fontScale).row()
				
				addTable {
					top()
					statsTable = this
				}
			}.growY()
		}
	}
	
	open fun rebuildBlocks(item: Item) {
		blocksTable.clearChildren()
		statsTable.clearChildren()
		lastBlock = null
		
		val isOre = item in mineableItems
		
		blocksTable.buttonGroup {
			Vars.content.blocks().each { block ->
				if (isOre && block is Drill) {
					//drills
					if (item.hardness <= block.tier) {
						statsCategory(block) {
							val maxSpeed = 60f / (block.drillTime + block.hardnessDrillMultiplier * item.hardness) * block.size * block.size
							addLabel("Produces ${(maxSpeed).toFixed(2)} ${item.emoji()}/sec").scaleFont(fontScale).row()
							
							//drills can be cooled - account for this. there must be a better way but I'm too lazy.
							Vars.content.liquids().each {
								if (block.consumes.consumesLiquid(it)) {
									val cooling = block.liquidBoostIntensity * block.liquidBoostIntensity
									addLabel("[${it.emoji()}] Produces ${(maxSpeed * cooling).toFixed(2)} ${item.emoji()}/sec").scaleFont(fontScale).row()
								}
							}
						}
					}
				} else if (block is GenericCrafter) {
					//crafters
					val stack =  block.outputItems?.find { it.item == item }
					if (stack != null) {
						statsCategory(block) {
							val maxSpeed = 60f / block.craftTime * stack.amount
							addLabel("Produces ${maxSpeed.toFixed(2)} ${item.emoji()}/sec").row()
							
							addLabel("Consumes:")
							
							addTable {
								//don't forget to multiply by amount
							}.marginLeft(10f)
						}
					}
				}
			}
		}
	}
	
	/** Utility function */
	protected inline fun Table.statsCategory(block: Block, crossinline lambda: Table.() -> Unit) {
		textButton(block.emoji(), Styles.togglet) {
			if (lastBlock != block) {
				statsTable.clearChildren()
				lastBlock = block
				
				statsTable.addLabel("${block.emoji()} ${block.localizedName}").scaleFont(fontScale).marginBottom(50f).row()
				statsTable.lambda()
			}
		}
		.scaleButtonFont(iconScale).row()
	}
	
}