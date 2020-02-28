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
				+ "§eListe des §ccommandes§e:\n"
				+ "§c§l● §e/cat infos\n"
				+ "§c§l● §e/cat reward\n"
				+ "§c§l● §e/cat join\n"
				+ "§c§l● §e/cat leave\n"
				+ "§c§l● §e/cat change\n"
				+ "§c§l● §e/cat set\n"
				+ "§c§l● §e/cat start\n"
				+ "§c§l● §e/cat startnow\n"
				+ "§c§l● §e/cat stop\n"
				+ "§c§l● §e/cat save\n"
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
		player.sendMessage(TreasurePlugin.getInstance().getPrefix() + "§aVous §evenez de rejoindre l'évènement.");
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
		player.sendMessage(TreasurePlugin.getInstance().getPrefix() + "§aVous §evenez de quitter l'évènement.");
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
			builder.append("§c#" + (i + 1) + " §7§l- §e" + p.getName() + " §7(§e" + players.get(p) + " §6points§7)\n");
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
		
		Bukkit.broadcastMessage(this.prefix + "§c" + player.getName() + " §evient de stopper volontairement l'évènement.");
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
		Bukkit.broadcastMessage("§7[§6Sauvegarde§7] §eSauvegarde des §cdonnées §eeffectuée par §6" + player.getName() + " §eréussie. §7("+time+"ms)");
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
					+ "§c§l● §e/cat change cristaux §6<point> <point> <point>\n"
					+ "§c§l● §e/cat change commun §6<point>\n"
					+ "§c§l● §e/cat change rare §6<point>\n"
					+ "§c§l● §e/cat change epic §6<point>\n"
					+ "§c§l● §e/cat change money §6<amount>\n"
					+ "§c§l● §e/cat change nbchest §6<amount>\n"
					+ "§c§l● §e/cat change time §6<timeInSecond>\n"
					+ "§c§l● §e/cat change waiting §6<timeInSecond>\n"
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier les §cvaleurs §edes cristaux.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cvaleur §edes cristaux communs à §a" + pointCommun + " §epoints.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cvaleur §edes cristaux rares à §a" + pointRare + " §epoints.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cvaleur §edes cristaux épiques à §a" + pointEpic + " §epoints.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cmoney §een récompense à " + money + "§e$.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier le §cnombre de coffres §edurant l'évènement à " + nbChest + "§e$.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cdurée §ede l'évènement à §a" + time + "§e secondes.");
				
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
				player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier la §cdurée d'attente §ede l'évènement à §a" + wait + "§e secondes.");
				
			} catch (Exception e) {
				player.sendMessage("§cErreur, le temps doit être un entier.");
			}
			return;
		default:
			player.sendMessage(Utils.LINE
					+ "§c§l● §e/cat change cristaux §6<point> <point> <point>\n"
					+ "§c§l● §e/cat change commun §6<point>\n"
					+ "§c§l● §e/cat change rare §6<point>\n"
					+ "§c§l● §e/cat change epic §6<point>\n"
					+ "§c§l● §e/cat change money §6<amount>\n"
					+ "§c§l● §e/cat change nbchest §6<amount>\n"
					+ "§c§l● §e/cat change time §6<time>\n"
					+ "§c§l● §e/cat change waiting §6<time>\n"
					+ Utils.LINE);
			return;
		}
		
	}
	
	@Command(name = "cat.set", permission = "cat.modify", inGameOnly = true)
	public void onSet(CommandArgs args) {
		Player player = args.getPlayer();
		if (args.length() < 1) {
			player.sendMessage(Utils.LINE
					+ "§c§l● §e/cat set commun\n"
					+ "§c§l● §e/cat set rare\n"
					+ "§c§l● §e/cat set epic\n"
					+ "§c§l● §e/cat set location\n"
					+ "§c§l● §e/cat set reward\n"
					+ "§c§l● §e/cat set inventory\n"
					+ "§c§l● §e/cat set map\n"
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
			player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier le §ccristal §b§lcommun§e.");
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
			player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier le §ccristal §6§lrare§e.");
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
			player.sendMessage(plugin.getPrefix() + "§eVous venez de modifier le §ccristal §5§lépique§e.");
			break;
		case "location":
		case "loc":
			if (args.length() > 1) {
				player.sendMessage("§cUsage: /cat set location");
				return;
			}
			
			Location loc = player.getLocation();
			configManager.getConfig().setZoneTeleportAttente(loc);
			player.sendMessage(plugin.getPrefix() + "§eVous venez de modifer la §czone de téléportation §epour la salle d'attente de l'évènement.");
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
			player.sendMessage(plugin.getPrefix() + "§eVous venez de modifer la §cmap §ede l'évènement.");
			break;
		default:
			player.sendMessage(Utils.LINE
					+ "§c§l● §e/cat set commun\n"
					+ "§c§l● §e/cat set rare\n"
					+ "§c§l● §e/cat set epic\n"
					+ "§c§l● §e/cat set location\n"
					+ "§c§l● §e/cat set reward\n"
					+ "§c§l● §e/cat set inventory\n"
					+ "§c§l● §e/cat set map\n"
					+ Utils.LINE);
			return;
		}
	}
}
