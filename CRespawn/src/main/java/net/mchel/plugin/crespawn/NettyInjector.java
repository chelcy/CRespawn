package net.mchel.plugin.crespawn;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

import io.netty.channel.Channel;
import net.minecraft.server.v1_8_R3.EntityPlayer;

/**
 * @author chelcy
 */
public class NettyInjector implements Listener {

	private Plugin plugin;
	private static NettyInjector inj;
	private HashMap<PlayerChannelHandler , Player> connection = new HashMap<>();
	public NettyInjector(Plugin plugin) {
		this.plugin = plugin;
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		inj = this;
	}

	public static NettyInjector getInstance() {
		return inj;
	}

	public Player getPlayer(PlayerChannelHandler pch) {
		if (connection.containsKey(pch)) {
			return connection.get(pch);
		} else {
			return null;
		}
	}

	public void inject(Player player) throws Exception {
		EntityPlayer ep = ((CraftPlayer)player).getHandle();
		PlayerChannelHandler pch = new PlayerChannelHandler();
		Channel channel = ep.playerConnection.networkManager.channel;
		if (channel.pipeline().get(PlayerChannelHandler.class) == null) {
			channel.pipeline().addBefore("packet_handler", plugin.getName(), pch);
			connection.put(pch, player);
		}
	}

	public void remove(final Player player) throws Exception {
		EntityPlayer ep = ((CraftPlayer)player).getHandle();
		final Channel channel = ep.playerConnection.networkManager.channel;
		if (channel.pipeline().get(PlayerChannelHandler.class) != null) {
			channel.pipeline().remove(PlayerChannelHandler.class);
			for (Map.Entry<PlayerChannelHandler, Player> es : connection.entrySet()) {
				if (es.getValue().equals(player)) {
					connection.remove(es.getKey());
				}
			}
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) throws Exception {
		remove(e.getPlayer());
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent e) throws Exception {
		if (e.getPlugin().equals(plugin)) {
			for (Player p : Bukkit.getOnlinePlayers()) {
				remove(p);
			}
		}
	}



}
