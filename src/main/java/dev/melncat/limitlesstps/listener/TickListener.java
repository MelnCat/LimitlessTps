package dev.melncat.limitlesstps.listener;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;

public class TickListener implements Listener {
	private final Object nmsServer;
	private final Field nextTickField;
	public TickListener(Object nmsServer, Field nextTickField) {
		this.nmsServer = nmsServer;
		this.nextTickField = nextTickField;
	}
	@EventHandler
	private void onTickEnd(ServerTickEndEvent event) throws IllegalAccessException {
		nextTickField.set(nmsServer, 0);
	}
}
