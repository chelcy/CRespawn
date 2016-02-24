package net.mchel.plugin.crespawn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author chelcy
 */
public class CRespawn extends JavaPlugin {

	private static CRespawn instance;
	private NettyInjector ni;

	@Override
	public void onEnable() {
		instance = this;
		ni = new NettyInjector(this);
		new EListener(this);
	}

	@Override
	public void onDisable() {
		for (Map.Entry<Player,ArmorStand> map : fixedPlayers.entrySet()) {
			map.getValue().remove();
		}
	}

	public static CRespawn getInstance() {
		return instance;
	}

	public NettyInjector getNettyInjector() {
		return ni;
	}

	private HashMap<Player , ArmorStand> fixedPlayers = new HashMap<Player , ArmorStand>();


	/**
	 * 固定中プレイヤーを返します
	 * @return
	 */
	public List<Player> getFixedPlayers() {
		List<Player> list = new ArrayList<Player>();
		for (Player p : fixedPlayers.keySet()) {
			if (p.isOnline()) {
				list.add(p);
			}
		}
		return list;
	}

	/**
	 * プレイヤー固定
	 * @param p
	 */
	public void fixPlayer(Player p) {
		Location loc = p.getLocation().add(0, -0.6, 0);
		ArmorStand stand = (ArmorStand)loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
		stand.setGravity(false);
		stand.setPassenger(p);
		stand.setMarker(true);
		stand.setVisible(false);
		stand.setCustomName("CRespawn");
		stand.setCustomNameVisible(false);
		if (!fixedPlayers.containsKey(p)) {
			fixedPlayers.put(p, stand);
		}
		try {
			ni.inject(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 固定解除
	 * @param p
	 */
	public void unfixPlayer(Player p) {
		while (fixedPlayers.containsKey(p)) {
			fixedPlayers.get(p).remove();
			fixedPlayers.remove(p);
		}
		try {
			ni.remove(p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * プレイヤーが固定状態にあるか
	 * @param p
	 * @return
	 */
	public boolean isFixed(Player p) {
		return fixedPlayers.containsKey(p);
	}

	/**
	 * アーマースタンドが入っていたら
	 * @param as
	 * @return
	 */
	public boolean containArmorStand(ArmorStand as) {
		return fixedPlayers.containsValue(as);
	}



}
