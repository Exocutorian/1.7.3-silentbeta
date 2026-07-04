package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemHoe;

public class ItemCopperHoe extends ItemHoe {
	public ItemCopperHoe(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setIconIndex(Textures.item("copper_hoe"));
		this.setItemName("hoeCopper");
	}
}
