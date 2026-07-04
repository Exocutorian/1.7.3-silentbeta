package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemPickaxe;

/**
 * Mines a 3x3 plane in one hit (see the BlockBreakEvent handler in
 * SilentBeta). Durability pays for the extra blocks.
 */
public class ItemCopperHammer extends ItemPickaxe {
	public ItemCopperHammer(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setMaxDamage(600);
		this.setIconIndex(Textures.item("copper_hammer"));
		this.setItemName("hammerCopper");
	}
}
