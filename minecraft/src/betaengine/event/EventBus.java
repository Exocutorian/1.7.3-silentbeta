package betaengine.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal static event bus.
 *
 * Dispatch walks the event's class hierarchy up to {@link Event}, so a handler
 * subscribed to Event.class sees everything, while a handler subscribed to
 * BlockBreakEvent.class only sees block breaks. Handlers run in subscription
 * order; a handler that throws is logged and skipped so one broken mod cannot
 * take the game down.
 */
public final class EventBus {
	private static final Map handlersByType = new HashMap();

	private EventBus() {
	}

	public static void subscribe(Class eventType, EventHandler handler) {
		if(eventType == null || handler == null) {
			throw new IllegalArgumentException("eventType and handler must not be null");
		}

		List handlers = (List)handlersByType.get(eventType);
		if(handlers == null) {
			handlers = new ArrayList();
			handlersByType.put(eventType, handlers);
		}

		handlers.add(handler);
	}

	/** Posts the event and returns it, so call sites can check isCancelled(). */
	public static Event post(Event event) {
		for(Class type = event.getClass(); type != null && Event.class.isAssignableFrom(type); type = type.getSuperclass()) {
			List handlers = (List)handlersByType.get(type);
			if(handlers == null) {
				continue;
			}

			for(int i = 0; i < handlers.size(); ++i) {
				EventHandler handler = (EventHandler)handlers.get(i);

				try {
					handler.handle(event);
				} catch (Throwable t) {
					System.out.println("[BetaEngine] Event handler failed for " + event.getClass().getName() + ": " + t);
					t.printStackTrace();
				}
			}
		}

		return event;
	}
}
