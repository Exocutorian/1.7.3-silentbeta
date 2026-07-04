package betaengine.event;

import net.minecraft.src.EntityPlayer;

/**
 * Posted from EntityPlayer.onUpdate() every tick, before vanilla logic runs.
 */
public class PlayerTickEvent extends Event {
	public final EntityPlayer player;

	public PlayerTickEvent(EntityPlayer player) {
		this.player = player;
	}
}
