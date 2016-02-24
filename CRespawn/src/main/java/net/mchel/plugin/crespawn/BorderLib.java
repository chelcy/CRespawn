package net.mchel.plugin.crespawn;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder.EnumWorldBorderAction;
import net.minecraft.server.v1_8_R3.WorldBorder;

/**
 * @author chelcy
 */
public class BorderLib {

	private static BorderLib instance;
	public BorderLib() {
		instance = this;
	}

	public static BorderLib getInstance() {
		return instance;
	}

	/**
	 * ボーダーの境界を送ります
	 * @param p
	 * @param distance 警告距離
	 * @param oldradius
	 * @param afterradius
	 * @param fadetime
	 */
	public void sendBorderRed(Player p , int distance , double oldradius , double afterradius , long fadetime , int time) {
		//わーるどぼーだー
		WorldBorder border = new WorldBorder();
		//中心
		border.setCenter(p.getLocation().getX(), p.getLocation().getZ());
		//警告距離
		border.setWarningDistance(distance);
		//秒数
		border.setWarningTime(time);
		//可動
		border.transitionSizeBetween(oldradius , afterradius , fadetime);
		PacketPlayOutWorldBorder packetborder = new PacketPlayOutWorldBorder(border , EnumWorldBorderAction.INITIALIZE);
		((CraftPlayer)p).getHandle().playerConnection.sendPacket(packetborder);
	}

	/**
	 * 赤いエフェクトを発生させます
	 * @param p
	 */
	public void sendBorder(Player p) {
		sendBorderRed(p , 1200000 , 200000D , 200000D , 0 , 999999999);
	}

	/**
	 * 赤いエフェクトを除去します
	 * @param p
	 */
	public void removeBorder(Player p) {
		sendBorderRed(p , 0 , 200000D , 200000D , 0 , 15);
	}

	/**
	 * 赤いエフェクトをフェードアウト(→消す)
	 * @param p
	 */
	public void fadeoutBorder(Player p) {
		sendBorderRed(p , 0 , 200000D , 1250000D , 5000 , 15);
	}

	/**
	 * 赤いエフェクトをフェードイン(→表示)
	 * @param p
	 */
	public void fadeinBorder(Player p) {
		sendBorderRed(p , 1000000 , 1000000 , 1000D , 5000 , 15);
	}

}
