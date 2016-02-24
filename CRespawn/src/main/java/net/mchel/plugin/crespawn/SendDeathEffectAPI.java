package net.mchel.plugin.crespawn;

import java.util.List;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityStatus;

/**
 * @author chelcy
 */
public class SendDeathEffectAPI {

	/**
	 * デスエフェクトを送信
	 * @param entity エフェクトの発信元
	 * @param players 見せる相手
	 * @return
	 */
	public static boolean sendDeathEffect(Entity entity , List<Player> players) {
		PacketPlayOutEntityStatus packet = new PacketPlayOutEntityStatus(((CraftEntity)entity).getHandle() , (byte)3);
		for (Player p : players) {
			sendPacket(p , packet);
		}
		return true;
	}

	@SuppressWarnings("rawtypes")
	private static void sendPacket(Player p , Packet pa) {
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(pa);
	}

}
