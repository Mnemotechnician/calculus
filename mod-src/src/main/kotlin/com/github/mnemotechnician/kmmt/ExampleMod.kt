package com.github.mnemotechnician.kmmt

import arc.*
import arc.scene.ui.*
import mindustry.mod.*
import mindustry.game.*
import mindustry.ui.*
import mindustry.ui.dialogs.*
import com.github.mnemotechnician.mkui.*

class ExampleMod : Mod() {

	val info = """
		This is a mod template.
		
		If you've installed this mod by accident, just uninstall it.
		This mod doesn't add anything useful
		It's just an template for mod developers.
	""".trimIndent()

	init {
		Events.on(EventType.ClientLoadEvent::class.java) {
			val dialog = BaseDialog("This is an example mod")
			dialog.closeOnBack()
			 
			dialog.cont.apply {
				addLabel(info).marginBottom(20f).row()
				
				buttonGroup {
					defaults().width(120f)
					
					textButton("info", Styles.togglet) {
						dialog.cont.childAs<Label>(0).setText(info)
					}
					
					textButton("about us", Styles.togglet) {
						dialog.cont.childAs<Label>(0).setText("""
							[Kotlin] Mindustry Mod Template by Mnemotechnician
							
							Discord: @Mnemotechnician#9967
							Github: https://github.com/Mnemotechnician
						""".trimIndent())
					}
				}.marginBottom(60f).row()
				
				textButton("close", Styles.nodet) { dialog.hide() }.width(240f)
			}
			
			dialog.show()
		}
	}
	
}
