package fr.traqueur.treasurehunt.config;

import org.bukkit.Bukkit;

import fr.traqueur.treasurehunt.TreasurePlugin;

public class ConfigTask implements Runnable {
	
	private TreasurePlugin plugin = TreasurePlugin.getInstance();
	
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		plugin.savePersists();
		time = System.currentTimeMillis() - time;
		Bukkit.broadcastMessage(plugin.getPrefix() +"§6Sauvegarde des §6§ldonnées §6réussie. §7("+time+"ms)");
	}

}
