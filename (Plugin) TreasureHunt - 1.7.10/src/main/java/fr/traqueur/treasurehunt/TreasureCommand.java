package fr.traqueur.treasurehunt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

import fr.traqueur.treasurehunt.api.utils.Cuboid;
import fr.traqueur.treasurehunt.api.utils.ItemBuilder;
import fr.traqueur.treasurehunt.api.utils.Utils;
import fr.traqueur.treasurehunt.api.utils.commands.CommandArgs;
import fr.traqueur.treasurehunt.api.utils.commands.annontations.Command;
import fr.traqueur.treasurehunt.config.ConfigurationManager;
import fr.traqueur.treasurehunt.event.TreasureEvent;
import fr.traqueur.treasurehunt.event.TreasureState;

public class TreasureCommand {

	private TreasurePlugin plugin = TreasurePlugin.getInstance();
	private TreasureManager manager = plugin.getTreasureManager();
	private ConfigurationManager configManager = plugin.getConfigManager();
	private String prefix = plugin.getPrefix();
	
	@Command(name = "cat.help", inGameOnly = true)
	public void onHelp(CommandArgs args) {
		Player player = args.getPlayer();
		player.sendMessage(Utils.LINE
				+ "§6Liste des §ccommandes§e:\n"
				+ "§c§l● §6/cat infos\n"
				+ "§c§l● §6/cat reward\n"
				+ "§c§l● §6/cat join\n"
				+ "§c§l● §6/cat leave\n"
				+ "§c§l● §6/cat change\n"
				+ "§c§l● §6/cat set\n"
				+ "§c§l● §6/cat start\n"
				+ "§c§l● §6/cat startnow\n"
				+ "§c§l● §6/cat stop\n"
				+ "§c§l● §6/cat save\n"
				+ Utils.LINE);
	}
	
	@Command(name = "cat.join", inGameOnly = true)
	public void onJoin(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() != TreasureState.WAIT) {
			player.sendMessage("§cErreur, vous ne pouvez pas rejoindre l'évènement pour le moment.");
			return;
		}
		if (manager.getPlayers().containsKey(player)) {
			player.sendMessage("§cVous avez déjà rejoins l'évènement."); 
			return;
		}
		
		manager.joinEvent(player);
		player.sendMessage(TreasurePlugin.getInstance().getPrefix() + "§aVous §6venez de rejoindre l'évènement.");
	}
	
	@Command(name = "cat.leave", inGameOnly = true)
	public void onLeave(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() != TreasureState.WAIT) {
			player.sendMessage("§cErreur, vous ne pouvez pas quitter un évènement pour le moment.");
			return;
		}
		if (!manager.getPlayers().containsKey(player)) {
			player.sendMessage("§cVous n'êtes pas dans l'évènement."); 
			return;
		}
		
		manager.leaveEvent(player);
		manager.getPlayers().remove(player);
		manager.getLastInventories().remove(player);
		manager.getLastLocations().remove(player);
		player.sendMessage(TreasurePlugin.getInstance().getPrefix() + "§aVous §6venez de quitter l'évènement.");
	}
	
	@Command(name = "cat.infos", aliases = {"cat.info"}, inGameOnly = true)
	public void onInfo(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() != TreasureState.PLAY) {
			player.sendMessage("§cErreur, vous ne pouvez pas voir les informations lorsqu'il n'y a pas d'évènement.");
			return;
		}
		HashMap<Player, Integer> players = manager.getPlayers();
		List<Player> top = Lists.newArrayList();
		players.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(e -> top.add(e.getKey()));
		StringBuilder builder = new StringBuilder();
		builder.append("§cClassement §eprovisoire:\n");
		int size = top.size() == 1 ? 0 : top.size() - 1;
		for (int i = 0; i < ((top.size() >= 5) ? 5 : top.size()); i++) {
			Player p = top.get(size - i);
			builder.append("§6§l#" + (i + 1) + " §7§l- §e" + p.getName() + " §7(§6" + players.get(p) + " §6§lpoints§7)\n");
		}
		
		player.sendMessage(Utils.LINE + builder.toString() + Utils.LINE);
		
	}
	
	@Command(name = "cat.start", permission = "cat.start", inGameOnly = true)
	public void onStart(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() != TreasureState.FINISH) {
			player.sendMessage("§cErreur, vous ne pouvez pas lancer un évènement pour le moment.");
			return;
		}
		
		int timeUntilStart = plugin.getConfigManager().getConfig().getTimeUntilEventStartInSecond();
		manager.setState(TreasureState.WAIT);
		int task = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new TreasureEvent(timeUntilStart), 0, 20);
		manager.setCurrentIntTask(task);
		Bukkit.broadcastMessage(
				prefix + "§6L'évènement Chasse aux trésors commencera dans §6§l" + timeUntilStart/60 + " §6minutes."
						+ "\n§6Faîtes la commande §c§l/cat join §6pour rejoindre l'événement");
	}
	
	@Command(name = "cat.startnow", permission = "cat.startnow", inGameOnly = true)
	public void onStartNow(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() != TreasureState.WAIT) {
			player.sendMessage("§cErreur, vous ne pouvez pas lancer un évènement pour le moment.");
			return;
		}
		TreasureEvent.setTimeUntilStart(0);
	}
	
	@Command(name = "cat.stop", permission = "cat.stop", inGameOnly = true)
	public void onStop(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.getState() == TreasureState.FINISH) {
			player.sendMessage("§cErreur, vous ne pouvez pas stoper un évènement qui n'existe pas.");
			return;
		}
		
		Bukkit.broadcastMessage(this.prefix + "§c" + player.getName() + " §6vient de stoper volontairement l'évènement.");
		TreasurePlugin.getInstance().getServer().getScheduler().cancelTask(manager.getCurrentIntTask());
		TreasurePlugin.getInstance().getTreasureManager().setState(TreasureState.FINISH);
		
		manager.setRewardHasTake(false);
		if (manager.getPlayers().size() != 0) {
			for (Entry<Player, Integer> elem : manager.getPlayers().entrySet()) {
				manager.leaveEvent(elem.getKey());
			}
		}

		TreasurePlugin.getInstance().getTreasureManager().getPlayers().clear();
		TreasurePlugin.getInstance().getTreasureManager().getLastInventories().clear();
		TreasurePlugin.getInstance().getTreasureManager().getLastLocations().clear();
		manager.setCurrentIntTask(0);
		manager.clearMap();
	}
	
	@Command(name = "cat.save", permission = "cat.save", inGameOnly = true)
	public void onSave(CommandArgs args) {
		Player player = args.getPlayer();
		long time = System.currentTimeMillis();
		plugin.savePersists();
		time = System.currentTimeMillis() - time;
		Bukkit.broadcastMessage(this.prefix +"§6Sauvegarde des §6§ldonnées §6effectuée par §6§l" + player.getName() + " §6réussie. §7("+time+"ms)");
	}
	
	@Command(name = "cat.reward", inGameOnly = true)
	public void onReward(CommandArgs args) {
		Player player = args.getPlayer();
		if (manager.isRewardHasTake()) {
			player.sendMessage("§cErreur, il n'y a pas de récompense à récupérer pour le moment.");
			return;
		}
		if (manager.getIdWinner() != player.getUniqueId()) {
			player.sendMessage("§cErreur, ce n'est pas vous le gagnant de l'évènement.");
			return;
		}
		
		manager.setRewardHasTake(true);
		manager.getReward(player);
	}
	
	@Command(name = "cat.change", permission = "cat.modify", inGameOnly = true)
	public void onChange(CommandArgs args) {
		Player player = args.getPlayer();
		if (args.length() < 1) {
			player.sendMessage(Utils.LINE
					+ "§c§l● §6/cat change cristaux §6§l<point> <point> <point>\n"
					+ "§c§l● §6/cat change commun §6§l<point>\n"
					+ "§c§l● §6/cat change rare §6§l<point>\n"
					+ "§c§l● §6/cat change epic §6§l<point>\n"
					+ "§c§l● §6/cat change money §6§l<amount>\n"
					+ "§c§l● §6/cat change nbchest §6§l<amount>\n"
					+ "§c§l● §6/cat change time §6§l<timeInSecond>\n"
					+ "§c§l● §6/cat change waiting §6§l<timeInSecond>\n"
					+ Utils.LINE);
			return;
		}
		
		String subCommand = args.getArgs(0);
		int pointCommun,pointRare,pointEpic, nbChest, time, wait;
		double money;
		switch (subCommand) {
		case "cristaux":
			if (args.length() < 4) {
				player.sendMessage("§cUsage: /cat change cristaux <point> <point> <point>");
				return;
			}
			
			try {
				pointCommun = Integer.parseInt(args.getArgs(1));
				pointRare = Integer.parseInt(args.getArgs(2));
				pointEpic = Integer.parseInt(args.getArgs(3));
				
				configManager.getConfig().setPointCommun(pointCommun);
				configManager.getConfig().setPointRare(pointRare);
				configManager.getConfig().setPointEpic(pointEpic);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier les §6§lvaleurs §6des cristaux.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, les valeurs des points doit être des entiers.");
			}
			
			return;
		case "commun":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change commun <point>");
				return;
			}
			
			try {
				pointCommun = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setPointCommun(pointCommun);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§lvaleur §edes cristaux communs à §a" + pointCommun + " §6points.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, les valeurs des points d'avoir être un entier.");
			}
			return;
		case "rare":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change rare <point>");
				return;
			}
			
			try {
				pointRare = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setPointRare(pointRare);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§lvaleur §6des cristaux rares à §a" + pointRare + " §6points.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, les valeurs des points doit être un entier.");
			}
			return;
		case "epic":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change epic <point>");
				return;
			}
			
			try {
				pointEpic = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setPointRare(pointEpic);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§lvaleur §6des cristaux épiques à §a" + pointEpic + " §6points.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, les valeurs des points doit être un entier.");
			}
			return;
		case "money":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change money <amount>");
				return;
			}
			
			try {
				money = Double.parseDouble(args.getArgs(1));
				
				configManager.getConfig().setMoneyReward(money);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§lmoney §6en récompense à " + money + "§6$.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, les valeurs de la money doit être un nombre.");
			}
			return;
		case "nbchest":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change nbchest <amount>");
				return;
			}
			
			try {
				nbChest = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setNbChests(nbChest);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier le §6§lnombre de coffres §6durant l'évènement à " + nbChest + "§6$.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, la quantité de coffres doit être un entier.");
			}
			return;
		case "time":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change time <timeInSecond>");
				return;
			}
			
			try {
				time = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setTimeEventInSecond(time);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§ldurée §6de l'évènement à §a" + time + "§6 secondes.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, le temps doit être un entier.");
			}
			return;
		case "wating":
		case "wait":
			if (args.length() < 2) {
				player.sendMessage("§cUsage: /cat change wating <timeInSecond>");
				return;
			}
			
			try {
				wait = Integer.parseInt(args.getArgs(1));
				
				configManager.getConfig().setTimeUntilEventStartInSecond(wait);
				player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier la §6§ldurée d'attente §6de l'évènement à §a" + wait + "§6 secondes.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, le temps doit être un entier.");
			}
			return;
		default:
			player.sendMessage(Utils.LINE
					+ "§c§l● §6/cat change cristaux §6§l<point> <point> <point>\n"
					+ "§c§l● §6/cat change commun §6§l<point>\n"
					+ "§c§l● §6/cat change rare §6§l<point>\n"
					+ "§c§l● §6/cat change epic §6§l<point>\n"
					+ "§c§l● §6/cat change money §6§l<amount>\n"
					+ "§c§l● §6/cat change nbchest §6§l<amount>\n"
					+ "§c§l● §6/cat change time §6§l<time>\n"
					+ "§c§l● §6/cat change waiting §6§l<time>\n"
					+ Utils.LINE);
			return;
		}
		
	}
	
	@Command(name = "cat.set", permission = "cat.modify", inGameOnly = true)
	public void onSet(CommandArgs args) {
		Player player = args.getPlayer();
		if (args.length() < 1) {
			player.sendMessage(Utils.LINE
					+ "§c§l● §6/cat set commun\n"
					+ "§c§l● §6/cat set rare\n"
					+ "§c§l● §6/cat set epic\n"
					+ "§c§l● §6/cat set location\n"
					+ "§c§l● §6/cat set reward\n"
					+ "§c§l● §6/cat set inventory\n"
					+ "§c§l● §6/cat set map\n"
					+ Utils.LINE);
			return;
		}
		ItemStack item, newCristal;
		String subCommand = args.getArgs(0);
		switch (subCommand) {
		case "commun":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set commun");
				return;
			}
			item = player.getItemInHand();
			if (item.getType() == Material.AIR) {
				player.sendMessage("§cErreur, vous devez tenir le nouveau cristal en main.");
				return;
			}
			newCristal = new ItemBuilder(item).setName("§bCristal Commun").toItemStack();
			this.configManager.getConfig().setCristalCommun(newCristal);
			player.getInventory().setItemInHand(null);
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier le §6§lcristal §b§lcommun§e.");
			break;
		case "rare":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set rare");
				return;
			}
			item = player.getItemInHand();
			if (item.getType() == Material.AIR) {
				player.sendMessage("§cErreur, vous devez tenir le nouveau cristal en main.");
				return;
			}
			newCristal = new ItemBuilder(item).setName("§6Cristal Rare").toItemStack();
			this.configManager.getConfig().setCristalRare(newCristal);
			player.getInventory().setItemInHand(null);
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier le §6§lcristal §6§lrare§e.");
			break;
		case "epic":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set epic");
				return;
			}
			item = player.getItemInHand();
			if (item.getType() == Material.AIR) {
				player.sendMessage("§cErreur, vous devez tenir le nouveau cristal en main.");
				return;
			}
			newCristal = new ItemBuilder(item).setName("§5Cristal Epique").toItemStack();
			this.configManager.getConfig().setCristalEpic(newCristal);
			player.getInventory().setItemInHand(null);
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifier le §6§lcristal §5§lépique§e.");
			break;
		case "location":
		case "loc":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set location");
				return;
			}
			
			Location loc = player.getLocation();
			configManager.getConfig().setZoneTeleportAttente(loc);
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifer la §6§lzone de téléportation §6pour la salle d'attente de l'évènement.");
			return;
		case "reward":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set reward");
				return;
			}
			Inventory inv = Bukkit.createInventory(null, 54, "§eGestion des §6récompenses.");
			for (ItemStack itemStack : TreasurePlugin.getInstance().getConfigManager().getConfig().getItemsReward()) {
				if (itemStack != null) {
					inv.addItem(itemStack);
				}
			}
			player.openInventory(inv);
			break;
		case "inventory":
		case "inv":
		case "inventaire":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set inventory");
				return;
			}
			manager.openInventoryEvent(player);
			break;
		case "map":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set map");
				return;
			}
			Selection selection = ((WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit")).getSelection(player);
			Location minimumPoint = new Location(player.getWorld(),
			                (double) selection.getMinimumPoint().getBlockX(), (double) selection.getMinimumPoint().getBlockY(),
			                (double) selection.getMinimumPoint().getBlockZ());
			Location maximumPoint = new Location(player.getWorld(),
			                (double) selection.getMaximumPoint().getBlockX(), (double) selection.getMaximumPoint().getBlockY(),
			                (double) selection.getMaximumPoint().getBlockZ());
			
			configManager.getConfig().setMap(new Cuboid(minimumPoint, maximumPoint));
			player.sendMessage(plugin.getPrefix() + "§6Vous venez de modifer la §6§lmap §6de l'évènement.");
			break;
		default:
			player.sendMessage(Utils.LINE
					+ "§c§l● §6/cat set commun\n"
					+ "§c§l● §6/cat set rare\n"
					+ "§c§l● §6/cat set epic\n"
					+ "§c§l● §6/cat set location\n"
					+ "§c§l● §6/cat set reward\n"
					+ "§c§l● §6/cat set inventory\n"
					+ "§c§l● §6/cat set map\n"
					+ Utils.LINE);
			return;
		}
	}
}
