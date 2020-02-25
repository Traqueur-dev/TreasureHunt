package fr.traqueur.treasurehunt.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import lombok.Getter;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;

@SuppressWarnings("deprecation")

public class VaultUtils {

	private static @Getter Economy economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class)
			.getProvider();
	
	private static @Getter Chat chat = Bukkit.getServer().getServicesManager().getRegistration(Chat.class)
			.getProvider();
	
	public static String getPlayerPrefix(Player player) {
		return chat.getPlayerPrefix(player);
	}
	
	public static double getBalance(String user) {
		return economy.getBalance(user);
	}

	public static double getBalance(Player player) {
		return economy.getBalance(player);
	}

	public static void setBalance(Player player, double value) {
		economy.withdrawPlayer(player, value);
		economy.depositPlayer(player, value);
	}

	public static boolean has(Player player, double value) {
		return economy.has(player, value);
	}

	public static void depositMoney(Player player, double value) {
		economy.depositPlayer(player, value);
	}

	public static void depositMoney(String player, double value) {
		economy.depositPlayer(player, value);
	}

	public static void withdrawMoney(Player player, double value) {
		economy.withdrawPlayer(player, value);
	}
	
	public static boolean has(String name, int price) {
		if (economy.has(name, price)) {
			return true;
		}
		return false;
	}
}