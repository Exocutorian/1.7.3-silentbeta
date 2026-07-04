package betaengine.event;

/**
 * Base class for everything posted on the {@link EventBus}.
 */
public abstract class Event {
	private boolean cancelled = false;

	/** Cancellable events let handlers veto the vanilla behaviour that follows. */
	public boolean isCancellable() {
		return false;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		if(cancelled && !this.isCancellable()) {
			throw new IllegalStateException(this.getClass().getName() + " is not cancellable");
		}

		this.cancelled = cancelled;
	}
}
