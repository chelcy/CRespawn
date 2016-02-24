package net.mchel.plugin.crespawn;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;

/**
 * @author chelcy
 */
public class SendMessagePacketAPI {

	public static boolean sendTellrawMessage(Player player , String jsonbuilded) {
		IChatBaseComponent c = ChatSerializer.a(jsonbuilded);
		PacketPlayOutChat p = new PacketPlayOutChat(c , (byte)0);
		sendPacket(player , p);
		return true;
	}

	public static boolean sendTitleMessage(Player player , String jsonTitle , String jsonSubTitle , int fadeIn , int stay , int fadeOut) {
		sendTitleTime(player , fadeIn , stay , fadeOut);
		sendTitleMainC(player , jsonTitle);
		sendTitleSubC(player , jsonSubTitle);
		return true;
	}

	public static void sendTitleMain(Player player , String jsonTitle , int fadein , int stay , int fadeout) {
		sendTitleTime(player , fadein , stay , fadeout);
		sendTitleMainC(player , jsonTitle);
	}

	public static void sendTitleSub(Player player , String jsonSubTitle , int fadein , int stay , int fadeout) {
		sendTitleTime(player , fadein , stay , fadeout);
		sendTitleSubC(player , jsonSubTitle);
	}

	private static void sendTitleTime(Player p , int fadein , int stay , int fadeout) {
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TIMES , null , fadein , stay , fadeout);
		sendPacket(p , packet);
	}

	private static void sendTitleMainC(Player p , String jsonTitle) {
		IChatBaseComponent cmain = null;
		if (jsonTitle != null) {
			cmain = ChatSerializer.a(jsonTitle);
		} else {
			cmain = ChatSerializer.a("{text:''}");
		}
		PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.TITLE , cmain);
		sendPacket(p , packet);
	}

	private static void sendTitleSubC(Player p , String jsonSubTitle) {
		if (jsonSubTitle != null) {
			IChatBaseComponent csub = ChatSerializer.a(jsonSubTitle);
			PacketPlayOutTitle packet = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE , csub);
			sendPacket(p , packet);
		}
	}

	@SuppressWarnings("rawtypes")
	private static void sendPacket(Player pl , Packet pa) {
		((CraftPlayer)pl).getHandle().playerConnection.sendPacket(pa);
	}

}
