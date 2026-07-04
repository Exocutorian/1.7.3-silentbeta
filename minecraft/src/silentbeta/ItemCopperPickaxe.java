package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemPickaxe;

public class ItemCopperPickaxe extends ItemPickaxe {
	public ItemCopperPickaxe(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setIconIndex(Textures.item("copper_pickaxe"));
		this.setItemName("pickaxeCopper");
	}
}
