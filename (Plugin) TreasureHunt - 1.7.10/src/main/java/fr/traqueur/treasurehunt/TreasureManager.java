package fr.traqueur.treasurehunt;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import fr.traqueur.treasurehunt.api.utils.ItemBuilder;
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
	
	private List<Player> classement;
	private UUID idWinner;
	private boolean rewardHasTake;
	
	public TreasureManager(TreasurePlugin plugin) {
		super(plugin, "Treasure");
		this.state = TreasureState.FINISH;
		this.players = new HashMap<>();
		this.lastInventories = new HashMap<>();
		this.lastLocations = new HashMap<>();
		this.rewardHasTake = true;
		this.classement = Lists.newArrayList();
		this.configManager = TreasurePlugin.getInstance().getConfigManager();
	}

	@Override
	public File getFile() {return null;}

	@Override
	public void loadData() {}

	@Override
	public void saveData() {}

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
		int lastPoints = this.players.get(player);
		this.players.remove(player);
		this.players.put(player, lastPoints+points);
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
		this.lastLocations.remove(player);
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
	
	public void setupInventory(Player player) {
		ItemStack[] items = configManager.getConfig().getInventory();
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
	
	public void setClassement() {
		if (this.players.size() == 0) {return;}
		StringBuilder builder = new StringBuilder();
		this.players.entrySet()
		  .stream()
		  .sorted(Map.Entry.comparingByValue())
		  .forEach(e -> {
			  this.classement.add(e.getKey());
			  });
		this.idWinner = this.classement.get(this.classement.size()-1).getUniqueId();
		
		int size = this.classement.size() == 1 ? 0 : this.classement.size() - 1;
		for (int i = 0; i < ((this.classement.size() >= 10) ? 10 : this.classement.size()); i++) {
			Player player = this.classement.get(size - i);
			builder.append("§c#" + (i + 1) + " §7§l- §e" + player.getName() + " §7(§e" + this.players.get(player) + " §6points§7)\n");
		}
		Bukkit.broadcastMessage(builder.toString());
		for (int i = 0; i < this.classement.size(); i++) {
			Player player = this.classement.get(size - i);
			player.sendMessage("§aVous §eêtes §c" + (i + 1) + ((i == 0) ? "er" : "ème") + " §edans le classement.");
		}
	}
	
}
