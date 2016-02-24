package net.mchel.plugin.crespawn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.entity.EntityDismountEvent;

import net.mchel.plugin.crespawn.JsonBuilder.JSONPart;

/**
 * @author chelcy
 */
public class EListener implements Listener{

	private CRespawn cr;
	private BorderLib bl;
	public EListener(CRespawn p) {
		this.cr = p;
		bl = new BorderLib();
		p.getServer().getPluginManager().registerEvents(this, p);
	}

	//プレイヤーの死ぬ前のゲームモード
	private HashMap<Player , GameMode> playerGameMode = new HashMap<Player , GameMode>();

	//プレイヤーの状態表示
	private HashMap<Player , RespawnInfo> playerInfo = new HashMap<Player , RespawnInfo>();
	private enum RespawnInfo {
		Alive,
		CountDown,
		CountDownPrepared,
		CountAfter
	}

	//プレイヤーがダメージを受けたとき
	@EventHandler(priority = EventPriority.LOW)
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntity().getType() != EntityType.PLAYER) {
			return;
		}
		Player p = (Player)e.getEntity();
		if (p.getHealth() - e.getFinalDamage() >0) {
			return;
		}
		GameMode gm = p.getGameMode();
		if (gm != GameMode.SURVIVAL && gm != GameMode.ADVENTURE) {
			return;
		}
		e.setCancelled(true);
		e.setDamage(0);
		//周りには死んだときのエフェクトを
		List<Player> players = new ArrayList<Player>();
		for (Entity en : p.getNearbyEntities(60, 60, 60)) {
			if (en instanceof Player) {
				players.add((Player)en);
			}
		}
		SendDeathEffectAPI.sendDeathEffect(p, players);
		//ライフ
		p.setHealth(p.getMaxHealth());
		//ゲームモード取り置き
		playerGameMode.put(p, gm);
		//ゲームモード変更
		p.setGameMode(GameMode.CREATIVE);
		//赤いエフェクトを表示
		bl.sendBorder(p);
		//プレイヤー固定
		cr.fixPlayer(p);
		//周りのプレイヤーから見えないように透明化
		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.hidePlayer(p);
		}
		//タイトルのカウント開始(3秒)
		count(p , 30);
		//音
		p.playSound(p.getLocation(), Sound.BLAZE_HIT, 1F, 0.5F);
		//playerInfo更新
		playerInfo.put(p, RespawnInfo.CountDown);
		PlayerDeathEvent event = new PlayerDeathEvent(p, Arrays.asList(p.getInventory().getContents()), 0, 0, 0, 0, null);
		Bukkit.getServer().getPluginManager().callEvent(event);
	}

	//タイトルのカウント
	private void count(final Player p , final int decimin) {
		//タイトル送る
		new BukkitRunnable() {
			@Override
			public void run() {
				for (int i = decimin ; i >= 0 ; i--) {
					if (!p.isOnline()) {
						return;
					}
					if (i == 0) {
						countFinish(p);
						sendTitleText(p , 0);
					} else {
						sendTitleText(p , i);
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {}
				}
			}
		}.runTaskAsynchronously(cr);
	}

	//カウント終了時
	private void countFinish(final Player p) {
		if (playerInfo.get(p) == RespawnInfo.CountDownPrepared) {
			new BukkitRunnable() {
				@Override
				public void run() {
					respawn(p);
				}
			}.runTask(cr);
		} else if (playerInfo.get(p) == RespawnInfo.CountDown) {
			playerInfo.put(p, RespawnInfo.CountAfter);
		}
	}

	//タイトル送信
	private void sendTitleText(Player p , int decimin) {
		List<JSONPart> title = new ArrayList<JSONPart>();
		title.add(new JSONPart("You died!" , ChatColor.RED));
		List<JSONPart> sub = new ArrayList<JSONPart>();
		RespawnInfo i = playerInfo.get(p);
		double time = decimin;
		time = time / 10;
		if (i == RespawnInfo.CountDown) {
			sub.add(new JSONPart("Left click to respawn in " , ChatColor.GREEN));
			sub.add(new JSONPart(String.valueOf(time) , ChatColor.AQUA));
			sub.add(new JSONPart("s" , ChatColor.GREEN));
		} else if (i == RespawnInfo.CountDownPrepared) {
			sub.add(new JSONPart("Respawning in " , ChatColor.GREEN));
			sub.add(new JSONPart(String.valueOf(time) , ChatColor.AQUA));
			sub.add(new JSONPart("s" , ChatColor.GREEN));
		} else if (i == RespawnInfo.CountAfter) {
			sub.add(new JSONPart("Left click to respawn" , ChatColor.GREEN));
		} else {
			sub.add(new JSONPart("" , ChatColor.RESET));
		}
		SendMessagePacketAPI.sendTitleMessage(p, JsonBuilder.JSONString(title), JsonBuilder.JSONString(sub), 0, 99999999, 0);
	}

	//リスポーン
	private void respawn(Player p) {
		Location reloc = null;
		boolean bed = false;
		if (p.getBedSpawnLocation() == null) {
			reloc = p.getWorld().getSpawnLocation();
		} else {
			reloc = p.getBedSpawnLocation();
			bed = true;
		}
		PlayerRespawnEvent e = new PlayerRespawnEvent(p, reloc, bed);
		Bukkit.getServer().getPluginManager().callEvent(e);
		playerInfo.put(p, RespawnInfo.Alive);
		cr.unfixPlayer(p);
		for (Player pl : Bukkit.getOnlinePlayers()) {
			pl.showPlayer(p);
		}
		bl.removeBorder(p);
		SendMessagePacketAPI.sendTitleMain(p, "", 0, 0, 0);
		p.setGameMode(playerGameMode.get(p));
		p.teleport(e.getRespawnLocation());
	}


	//左クリックした時
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
		Action a = e.getAction();
		if (a != Action.LEFT_CLICK_AIR && a != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		//プレイヤーの状態から
		if (!playerInfo.containsKey(p)) {
			return;
		}
		RespawnInfo i = playerInfo.get(p);
		if (i == RespawnInfo.CountDown) {
			playerInfo.put(p, RespawnInfo.CountDownPrepared);
		} else if (i == RespawnInfo.CountAfter) {
			respawn(p);
		}
	}

	//フライモード変わるの禁止
	@EventHandler
	public void onFlyModeChange(PlayerToggleFlightEvent e) {
		if (cr.isFixed(e.getPlayer())) {
			e.setCancelled(true);
		}
	}

	//ログイン時
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		for (Player pl : cr.getFixedPlayers()) {
			p.hidePlayer(pl);
		}
		if (!p.isDead()) {
			playerInfo.put(p, RespawnInfo.Alive);
		}
	}

	//ログアウト時
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			cr.unfixPlayer(p);
		}
	}

	//念のためえんちちから降りた時にアレするアレ
	@EventHandler
	public void onDismountArmorStand(EntityDismountEvent e) {
		if (e.getEntity().getType() == EntityType.PLAYER && e.getDismounted().getType() == EntityType.ARMOR_STAND && cr.containArmorStand((ArmorStand)e.getDismounted())) {
			cr.unfixPlayer((Player)e.getEntity());
		}
	}

	//ここから保護系

	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent e) {
		Player p = e.getPlayer();
		if (p != null && cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getDamager() instanceof Player) {
			Player p = (Player)e.getDamager();
			if (cr.isFixed(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onChangeBlock(EntityChangeBlockEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player)e.getEntity();
			if (cr.isFixed(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBucketFill(PlayerBucketFillEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPickup(PlayerPickupItemEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player)e.getWhoClicked();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onHanging(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player) {
			Player p = (Player)e.getRemover();
			if (cr.isFixed(p)) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onArmor(PlayerArmorStandManipulateEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onFish(PlayerFishEvent e) {
		Player p = e.getPlayer();
		if (cr.isFixed(p)) {
			e.setCancelled(true);
		}
	}




}
