package betaengine.example;

import betaengine.BetaEngine;
import betaengine.Registry;
import betaengine.event.BlockBreakEvent;
import betaengine.event.Event;
import betaengine.event.EventBus;
import betaengine.event.EventHandler;
import betaengine.mod.Mod;
import net.minecraft.src.Block;
import net.minecraft.src.Item;

/**
 * Reference mod showing the two engine primitives: registering content
 * through the Registry and reacting to game events through the EventBus.
 */
public class ExampleMod implements Mod {
	public static Block copperBlock;
	public static Item copperIngot;

	public String id() {
		return "example";
	}

	public void init() {
		copperBlock = Registry.registerBlock("copper_block", new Registry.BlockFactory() {
			public Block create(int blockId) {
				return new BlockCopper(blockId);
			}
		});

		copperIngot = Registry.registerItem("copper_ingot", new Registry.ItemFactory() {
			public Item create(int itemId) {
				return new ItemCopperIngot(itemId);
			}
		});

		EventBus.subscribe(BlockBreakEvent.class, new EventHandler() {
			public void handle(Event event) {
				BlockBreakEvent e = (BlockBreakEvent)event;
				if(e.blockId == copperBlock.blockID) {
					BetaEngine.log("Copper block broken at " + e.x + ", " + e.y + ", " + e.z);
				}
			}
		});
	}
}
