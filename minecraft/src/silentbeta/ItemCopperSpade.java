package silentbeta;

import betaengine.Textures;
import net.minecraft.src.EnumToolMaterial;
import net.minecraft.src.ItemSpade;

public class ItemCopperSpade extends ItemSpade {
	public ItemCopperSpade(int itemId) {
		super(itemId, EnumToolMaterial.COPPER);
		this.setIconIndex(Textures.item("copper_shovel"));
		this.setItemName("shovelCopper");
	}
}
