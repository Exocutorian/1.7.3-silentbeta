package silentbeta;

import betaengine.Textures;
import net.minecraft.src.Item;

public class ItemCopperIngot extends Item {
	public ItemCopperIngot(int itemId) {
		super(itemId);
		this.setIconIndex(Textures.item("copper_ingot"));
		this.setItemName("ingotCopper");
	}
}
