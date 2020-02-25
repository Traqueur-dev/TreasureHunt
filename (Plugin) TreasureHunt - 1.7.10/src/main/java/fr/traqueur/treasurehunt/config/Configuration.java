package fr.traqueur.treasurehunt.config;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.Lists;

import fr.traqueur.treasurehunt.api.utils.Cuboid;
import fr.traqueur.treasurehunt.api.utils.ItemBuilder;
import lombok.Data;

@Data
public class Configuration {
	
	private int timeUntilEventStartInSecond;
	private int timeEventInSecond;
	private int nbChests;
	/* Zone où les joueurs sont téléportés */
	private Location zoneTeleportAttente;
	
	/* Points des cristaux */
	private int pointCommun;
	private int pointRare;
	private int pointEpic;
	
	/* Cristaux */
	private ItemStack cristalCommun;
	private ItemStack cristalRare;
	private ItemStack cristalEpic;
	
	/* Inventory */
	private ItemStack[] inventory;
	private Cuboid map;
	/* Rewards */
	private List<ItemStack> itemsReward;
	private double moneyReward;
	
	public Configuration() {
		this.timeUntilEventStartInSecond = 30;
		this.timeEventInSecond = 60*30;
		this.nbChests = 12;
		this.zoneTeleportAttente = new Location(Bukkit.getWorld("world"), 216, 90, 213);
		
		this.pointCommun = 100;
		this.pointRare = 500;
		this.pointEpic = 1000;
		
		this.cristalCommun = new ItemBuilder(Material.IRON_INGOT).setName("§bCristal Commun").toItemStack();
		this.cristalRare = new ItemBuilder(Material.GOLD_INGOT).setName("§6Cristal Rare").toItemStack();
		this.cristalEpic = new ItemBuilder(Material.DIAMOND).setName("§5Cristal Epique").toItemStack();
		
		this.inventory = new ItemStack[40];
		this.map = new Cuboid("world", 199,100,197,167,124,235);
		
		this.itemsReward = Lists.newArrayList();
		itemsReward.add(new ItemBuilder(Material.DIRT).setName("§c§lTEST").setLore("§6Cette item est un test.").toItemStack());
		this.moneyReward = 1000.0;
	}
	
}
