package dev.melncat.limitlesstps;

import dev.melncat.limitlesstps.listener.TickListener;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class LimitlessTps extends JavaPlugin {

	@Override
	public void onEnable() {
		Server bukkitServer = Bukkit.getServer();
		try {
			Object nmsServer = bukkitServer.getClass().getMethod("getServer").invoke(bukkitServer);
			Class<?> serverClass = nmsServer.getClass().getSuperclass();
			List<Method> methods = Arrays.stream(serverClass.getDeclaredMethods())
				.filter(x -> Modifier.isPublic(x.getModifiers()) && x.getReturnType() == long.class)
				.toList();
			if (methods.size() != 1)
				throw new RuntimeException("Multiple methods found: " +
					methods.stream().map(Method::getName).collect(Collectors.joining(", ")));
			var methodVal = methods.get(0).invoke(nmsServer);
			List<Field> fields = Arrays.stream(serverClass.getDeclaredFields())
				.filter(x -> {
					if (x.getType() != long.class) return false;
					if (!Modifier.isPrivate(x.getModifiers())) return false;
					x.setAccessible(true);
					try {
						return x.get(nmsServer).equals(methodVal);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				})
				.toList();
			if (fields.size() != 1)
				throw new RuntimeException("Multiple fields found: " +
					fields.stream().map(Field::getName).collect(Collectors.joining(", ")));
			var allFields = Arrays.asList(serverClass.getDeclaredFields());
			var secondField = allFields.get(allFields.indexOf((fields.get(0))) + 2);
			Bukkit.getPluginManager().registerEvents(new TickListener(nmsServer, fields.get(0)), this);

		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
