package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemAxe;

public class ItemCopperAxe extends ItemAxe {
	public ItemCopperAxe(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setIconIndex(Textures.item("copper_axe"));
		this.setItemName("hatchetCopper");
	}
}
