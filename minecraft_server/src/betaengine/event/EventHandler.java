package betaengine.event;

/**
 * A subscriber for one event type. Register via
 * {@link EventBus#subscribe(Class, EventHandler)}.
 */
public interface EventHandler {
	void handle(Event event);
}
