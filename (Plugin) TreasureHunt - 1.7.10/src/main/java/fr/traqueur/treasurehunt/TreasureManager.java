package fr.traqueur.treasurehunt;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import fr.traqueur.treasurehunt.api.utils.Cuboid;
import fr.traqueur.treasurehunt.api.utils.ItemBuilder;
import fr.traqueur.treasurehunt.api.utils.NumberUtils;
import fr.traqueur.treasurehunt.api.utils.Utils;
import fr.traqueur.treasurehunt.api.utils.VaultUtils;
import fr.traqueur.treasurehunt.api.utils.jsons.Saveable;
import fr.traqueur.treasurehunt.config.ConfigurationManager;
import fr.traqueur.treasurehunt.event.TreasureState;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TreasureManager extends Saveable {
	
	private ConfigurationManager configManager;
	
	private TreasureState state;
	private int currentIntTask;
	
	private HashMap<Player, Integer> players;
	private HashMap<Player, Location> lastLocations;
	private HashMap<Player, ItemStack[]> lastInventories;
	
	private UUID idWinner;
	private boolean rewardHasTake;
	
	public TreasureManager(TreasurePlugin plugin) {
		super(plugin, "Treasure");
		this.state = TreasureState.FINISH;
		this.players = new HashMap<>();
		this.lastInventories = new HashMap<>();
		this.lastLocations = new HashMap<>();
		this.rewardHasTake = true;
		this.configManager = TreasurePlugin.getInstance().getConfigManager();
	}

	@Override
	public File getFile() {return null;}

	@Override
	public void loadData() {}

	@Override
	public void saveData() {}

	private void generateChests() {
		Cuboid cuboid = this.configManager.getConfig().getMap();
		int nbChests = this.configManager.getConfig().getNbChests();
		Location min = cuboid.getLowerLocation();
		Location max = cuboid.getUpperLocation();
		double pourcentEpic = configManager.getConfig().getPourcentEpic();
		double pourcentRare = configManager.getConfig().getPourcentRare();
		double pourcentCommun = configManager.getConfig().getPourcentCommun();
		
		for (int i = 0; i < nbChests; i++) {
			double x = NumberUtils.random(min.getX(), max.getX());
			double z = NumberUtils.random(min.getZ(), max.getZ());
			double y = cuboid.getWorld().getHighestBlockYAt((int) x, (int) z);
			Location chestLoc = new Location(cuboid.getWorld(), x, y, z);
			chestLoc.getBlock().setType(Material.CHEST);
			Chest chest = (Chest) chestLoc.getBlock().getState();
			Inventory inv = chest.getInventory();
			int nbItems = NumberUtils.random(1, configManager.getConfig().getNbItemMax());
			
			for (int j = 0; j < nbItems; j++) {
				int slot = NumberUtils.random(0, 26);
				double pourcent = NumberUtils.random(0d, 100d);
				
				if (pourcent <= pourcentEpic) {
					inv.setItem(slot, configManager.getConfig().getCristalEpic());
				} else if (pourcent <= pourcentEpic + pourcentRare && pourcent > pourcentEpic) {
					inv.setItem(slot, configManager.getConfig().getCristalRare());
				} else if (pourcent <= pourcentEpic + pourcentRare + pourcentCommun && pourcent > pourcentEpic + pourcentRare){
					inv.setItem(slot, configManager.getConfig().getCristalCommun());
				}
				
			}
		}
	}
	
	public void getReward(Player player) {
		Inventory inv = Bukkit.createInventory(null, 54, "§eRécompense de §6§ll'évènement");
		for (ItemStack itemStack : TreasurePlugin.getInstance().getConfigManager().getConfig().getItemsReward()) {
			inv.addItem(itemStack);
		}
		
		player.openInventory(inv);
		player.sendMessage(TreasurePlugin.getInstance().getPrefix() + "§eVous venez de récupérez vos §crécompenses§e." +
							"\n§cNe fermez pas l'inventaire sans récupérer les items, ils seront perdus.");
		VaultUtils.depositMoney(player, TreasurePlugin.getInstance().getConfigManager().getConfig().getMoneyReward());
	}
	
	public void addPoints(Player player, int points) {
		this.players.computeIfPresent(player, (p,i) -> i+points);
	}
	
	public void joinEvent(Player player) {
		ItemStack[] inv = new ItemStack[40];
		for (int i = 0; i < 36; i++) {
			inv[i] = player.getInventory().getContents()[i];
		}
		inv[36] = player.getInventory().getHelmet();
		inv[37] = player.getInventory().getChestplate();
		inv[38] = player.getInventory().getLeggings();
		inv[39] = player.getInventory().getBoots();
		
		players.put(player, 0);
		lastLocations.put(player, player.getLocation());
		lastInventories.put(player, inv);
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.teleport(TreasurePlugin.getInstance().getConfigManager().getConfig().getZoneTeleportAttente());
	}
	
	public void leaveEvent(Player player) {
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		ItemStack[] inv = lastInventories.get(player);
		
		player.teleport(lastLocations.get(player));
		for (int i = 0; i < 36; i++) {player.getInventory().setItem(i, inv[i]);}
		if (inv[36] != null) {player.getInventory().setHelmet(inv[36]);}
		if (inv[37] != null) {player.getInventory().setChestplate(inv[37]);}
		if (inv[38] != null) {player.getInventory().setLeggings(inv[38]);}
		if (inv[39] != null) {player.getInventory().setBoots(inv[39]);}
	}
	
	public void openInventoryEvent(Player player) {
		Inventory inv = Bukkit.createInventory(null, 54, "§eGestion §dInventaire");
		int[] slots = {4,5,6,7,8,9,10,11,12,13,14,15,16,17};
		for (int i : slots) {
			inv.setItem(i, new ItemBuilder(Material.STAINED_GLASS_PANE).setName(" ").setDyeColor(DyeColor.BLACK).toItemStack());
		}
		//Casque
		inv.setItem(0, configManager.getConfig().getInventory()[0]);
		//Plastron
		inv.setItem(1, configManager.getConfig().getInventory()[1]);
		//Jambière
		inv.setItem(2, configManager.getConfig().getInventory()[2]);
		//Bottes
		inv.setItem(3, configManager.getConfig().getInventory()[3]);
		int j = 4;
		for (int i = 18; i < 54; i++) {
			inv.setItem(i, configManager.getConfig().getInventory()[j]);
			j++;
		}
		
		player.openInventory(inv);
	}
	
	public void launchEvent() {
		long time = System.currentTimeMillis();
		this.generateChests();
		time = System.currentTimeMillis() - time;
		Bukkit.broadcastMessage(this.getPlugin().getPrefix() + " §eGénération des §ccoffres §aterminée§e. §7(" + time + "ms)");
	
		for (Entry<Player, Location> elem : this.getLastLocations().entrySet()) {
			this.setupInventory(elem.getKey());
			this.randomTP(elem.getKey());
		}
	}
	
	private void setupInventory(Player player) {
		ItemStack[] items = configManager.getConfig().getInventory();
		player.getInventory().clear();
		player.getInventory().setHelmet(items[0]);
		player.getInventory().setChestplate(items[1]);
		player.getInventory().setLeggings(items[2]);
		player.getInventory().setBoots(items[3]);
		for (int i = 4; i < items.length; i++) {
			if (items[i] != null) {
				player.getInventory().setItem(i-4, items[i]);
			}
		}
		
	}
	
	private void randomTP(Player player) {
		Cuboid cuboid = configManager.getConfig().getMap();
		Location min = cuboid.getLowerLocation();
		Location max = cuboid.getUpperLocation();
		double x = NumberUtils.random(min.getX(), max.getX());
		double z = NumberUtils.random(min.getZ(), max.getZ());
		double y = player.getWorld().getHighestBlockYAt((int) x, (int) z) + 1d;
		player.teleport(new Location(player.getWorld(), x, y, z));
	}
	
	public void clearMap() {
		Cuboid cuboid = configManager.getConfig().getMap();
		long time = System.currentTimeMillis();
		for (int x = cuboid.getLowerX(); x <= cuboid.getUpperX() ; x++) {
			for (int z = cuboid.getLowerZ(); z <= cuboid.getUpperZ(); z++) {
				for (int y = cuboid.getLowerY(); y < cuboid.getUpperY(); y++) {
					Location loc = new Location(cuboid.getWorld(), x, y, z);
					Block block = loc.getBlock();
					if (block.getType() == Material.CHEST) {
						Chest chest = (Chest) block.getState();
						chest.getInventory().clear();
						block.setType(Material.AIR);
					}
				}
			}
		}
		time = System.currentTimeMillis() - time;
		for (Player p : Utils.getOnlinePlayers()) {
			if (p.hasPermission("cat.start")) {
				p.sendMessage(this.getPlugin().getPrefix() + "§eMap §cnettoyée §een §7" + time + "ms§e.");
			}
		}
	}
	
	public void setClassement() {
		if (this.players.size() == 0) {return;}
		
		StringBuilder builder = new StringBuilder();
		List<Player> classement = Lists.newArrayList();
		this.players.entrySet()
		  .stream()
		  .sorted(Map.Entry.comparingByValue())
		  .forEach(e -> {
			  classement.add(e.getKey());
			  });
		this.idWinner = classement.get(classement.size()-1).getUniqueId();
		
		int size = classement.size() == 1 ? 0 : classement.size() - 1;
		for (int i = 0; i < ((classement.size() >= 10) ? 10 : classement.size()); i++) {
			Player player = classement.get(size - i);
			builder.append("§c#" + (i + 1) + " §7§l- §e" + player.getName() + " §7(§e" + this.players.get(player) + " §6points§7)\n");
		}
		Bukkit.broadcastMessage(builder.toString());
		for (int i = 0; i < classement.size(); i++) {
			Player player = classement.get(size - i);
			player.sendMessage("§aVous §eêtes §c" + (i + 1) + ((i == 0) ? "er" : "ème") + " §edans le classement.");
		}
	}
	
}
