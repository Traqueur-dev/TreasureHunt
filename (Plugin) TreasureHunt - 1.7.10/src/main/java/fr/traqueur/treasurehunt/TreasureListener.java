package fr.traqueur.treasurehunt;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import fr.traqueur.treasurehunt.config.ConfigurationManager;
import fr.traqueur.treasurehunt.event.TreasureState;

public class TreasureListener implements Listener {

	private TreasurePlugin plugin = TreasurePlugin.getInstance();
	private ConfigurationManager configManager = plugin.getConfigManager();
	private TreasureManager manager = plugin.getTreasureManager();
	
	
	@EventHandler
	public void onChestOpen(PlayerInteractEvent event) {
		if (!event.hasBlock()) {return;}
		Block block = event.getClickedBlock();
		Player player = event.getPlayer();
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {return;}
		if (!configManager.getConfig().getMap().contains(block.getLocation())) {return;}
		if (manager.getState() != TreasureState.PLAY) {return;}
		if (!(block.getState() instanceof Chest)) {return;}
		Chest chest = (Chest) block.getState();
		ItemStack[] contents = chest.getInventory().getContents();
		ItemStack commun = configManager.getConfig().getCristalCommun();
		ItemStack rare = configManager.getConfig().getCristalRare();
		ItemStack epic = configManager.getConfig().getCristalEpic();
		int nbCommun = 0, nbRare = 0, nbEpic = 0;
		
		for (ItemStack item : contents) {
			if (item != null) {
				if (item.getItemMeta().getDisplayName().equals(commun.getItemMeta().getDisplayName())) {
					nbCommun++;
					manager.addPoints(player, configManager.getConfig().getPointCommun());
				}
				if (item.getItemMeta().getDisplayName().equals(rare.getItemMeta().getDisplayName())) {
					nbRare++;
					manager.addPoints(player, configManager.getConfig().getPointRare());
				}
				if (item.getItemMeta().getDisplayName().equals(epic.getItemMeta().getDisplayName())) {
					nbEpic++;
					manager.addPoints(player, configManager.getConfig().getPointEpic());
				}
			}
		}
		if (nbCommun != 0) {
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de ramasser §7x" + nbCommun + (nbCommun == 1 ? " §6cristal §b§lcommun§6." : " §6cristaux §b§lcommuns§6."));
		}
		if (nbRare != 0) {
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de ramasser §7x" + nbRare + (nbRare == 1 ? " §6cristal §6§lrare§6." : " §6cristaux §6§lrares§6."));
		}
		if (nbEpic != 0) {
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de ramasser §7x" + nbEpic + (nbEpic == 1 ? " §6cristal §5§lépique§6." : " §6cristaux §5§lépiques§6."));
		}
		
		chest.getInventory().clear();
		block.setType(Material.AIR);
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!event.getInventory().getName().equals("§6Gestion §dInventaire")) {return;}
		if (!event.getCurrentItem().hasItemMeta()) {return;}
		if (!event.getCurrentItem().getItemMeta().hasDisplayName()) {return;}
		if (event.getCurrentItem().getItemMeta().getDisplayName().equals(" ") && event.getCurrentItem().getType() == Material.STAINED_GLASS_PANE) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCloseInv(InventoryCloseEvent event) {
		Inventory inv = event.getInventory();
		List<ItemStack> itemsReward = Lists.newArrayList();
		Player player = (Player) event.getPlayer();
		
		if (inv.getName().equals( "§eGestion des §6récompenses.")) {
			for (ItemStack item : inv.getContents()) {
				if (item != null) {
					itemsReward.add(item);
				}
			}
			configManager.getConfig().setItemsReward(itemsReward);
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier les §6§litems §6en récompenses.");
			return;
		}
		
		if (inv.getName().equals("§eGestion §dInventaire")) {
			//Casque
			configManager.getConfig().getInventory()[0] = inv.getContents()[0];
			//Plastron
			configManager.getConfig().getInventory()[1] = inv.getContents()[1];
			//Jambière
			configManager.getConfig().getInventory()[2] = inv.getContents()[2];
			//Bottes
			configManager.getConfig().getInventory()[3] = inv.getContents()[3];
			int j = 4;
			for (int i = 18; i < 54; i++) {
				configManager.getConfig().getInventory()[j] = inv.getContents()[i];
				j++;
			}
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier les items de §6§ll'inventaire§6.");
		}
		
	}
	
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		List<String> allowed = Lists.newArrayList();
		allowed.add("/cat leave");
		allowed.add("/cat stop");
		allowed.add("/cat infos");
		allowed.add("/cat info");
		allowed.add("/cat startnow");
		if (plugin.getTreasureManager().getLastLocations().containsKey(event.getPlayer()) && !allowed.contains(event.getMessage())) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player victim = event.getEntity();
		if (!(victim.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {return;}
		EntityDamageByEntityEvent lastDamage = (EntityDamageByEntityEvent) victim.getLastDamageCause();
		if (lastDamage.getDamager() == null) {return;}
		if (!(lastDamage.getDamager() instanceof Player)) {return;}
		Player killer = (Player) lastDamage.getDamager();
		if (!(manager.getPlayers().containsKey(killer) && manager.getPlayers().containsKey(victim))) {return;}
		int points = manager.getPlayers().get(victim);
		event.setKeepInventory(true);
		manager.addPoints(killer, points);
	}
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		if (!manager.getLastLocations().containsKey(player)) {return;}
		manager.leaveEvent(player);
		manager.getLastLocations().remove(player);
	}
	
	
}
