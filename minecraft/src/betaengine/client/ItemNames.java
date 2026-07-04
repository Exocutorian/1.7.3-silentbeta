package betaengine.client;

import betaengine.Registry;
import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.StatCollector;

/**
 * Human-readable item names for tooltips and the held-item popup.
 * Lookup order: vanilla lang file, engine registry name ("copper_ingot" ->
 * "Copper Ingot"), then the unlocalized key prettified as a last resort.
 */
public final class ItemNames {
	private ItemNames() {
	}

	public static String display(ItemStack stack) {
		String key = stack.getItemName();
		if(key != null && key.length() > 0) {
			String localized = StatCollector.translateToLocal(key + ".name");
			if(localized != null && localized.trim().length() > 0 && !localized.equals(key + ".name")) {
				return localized;
			}
		}

		Item item = Item.itemsList[stack.itemID];
		String registryName = item == null ? null : Registry.nameOf(item);
		if(registryName == null && stack.itemID < Block.blocksList.length && Block.blocksList[stack.itemID] != null) {
			registryName = Registry.nameOf(Block.blocksList[stack.itemID]);
		}

		if(registryName != null) {
			return prettifySnakeCase(registryName);
		}

		if(key == null || key.length() == 0) {
			return "???";
		}

		int dot = key.lastIndexOf('.');
		return prettifyCamelCase(dot >= 0 ? key.substring(dot + 1) : key);
	}

	private static String prettifySnakeCase(String name) {
		StringBuilder out = new StringBuilder(name.length());
		boolean wordStart = true;

		for(int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if(c == '_') {
				out.append(' ');
				wordStart = true;
			} else {
				out.append(wordStart ? Character.toUpperCase(c) : c);
				wordStart = false;
			}
		}

		return out.toString();
	}

	private static String prettifyCamelCase(String name) {
		StringBuilder out = new StringBuilder(name.length() + 4);

		for(int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if(i == 0) {
				out.append(Character.toUpperCase(c));
			} else {
				if(Character.isUpperCase(c)) {
					out.append(' ');
				}
				out.append(c);
			}
		}

		return out.toString();
	}
}
