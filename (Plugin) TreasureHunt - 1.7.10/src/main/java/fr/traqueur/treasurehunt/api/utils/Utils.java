package fr.traqueur.treasurehunt.api.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils {

	public static String LINE = "§7§m" + StringUtils.repeat("-", 44) + "\n";

	@SuppressWarnings("deprecation")
	public static Player[] getOnlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}
}
