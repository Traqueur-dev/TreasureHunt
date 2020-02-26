package fr.traqueur.treasurehunt;

import java.lang.reflect.Modifier;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;

import fr.traqueur.treasurehunt.api.utils.commands.CommandFramework;
import fr.traqueur.treasurehunt.api.utils.jsons.JsonPersist;
import fr.traqueur.treasurehunt.api.utils.jsons.adapters.ItemStackAdapter;
import fr.traqueur.treasurehunt.api.utils.jsons.adapters.LocationAdapter;
import fr.traqueur.treasurehunt.config.ConfigTask;
import fr.traqueur.treasurehunt.config.ConfigurationManager;
import lombok.Getter;
import net.minecraft.util.com.google.gson.Gson;
import net.minecraft.util.com.google.gson.GsonBuilder;

@Getter
public class TreasurePlugin extends JavaPlugin {

	private @Getter static TreasurePlugin instance;
	
	private CommandFramework framework;
	
	private TreasureManager treasureManager;
	private ConfigurationManager configManager;
	
	private List<JsonPersist> persists;
	private Gson gson;
	
	private String prefix;
	//TODO: Generation chest
	@Override
	public void onEnable() {
		TreasurePlugin.instance = this;
		this.prefix = "§7[§6TreasureHunt§7] ";
		
		this.gson = this.createGsonBuilder().create();
		this.persists = Lists.newArrayList();
		
		this.configManager = new ConfigurationManager(this);
		this.treasureManager = new TreasureManager(this);
		
		this.framework = new CommandFramework(this);
		this.framework.registerCommands(new TreasureCommand());
		this.framework.registerHelp();
		
		this.registerPersist(this.configManager);
		this.registerPersist(this.treasureManager);
		
		this.registerListener(new TreasureListener());
		
		this.loadPersists();
		
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ConfigTask(), 20, 20*30*60);
		
		super.onEnable();
	}
	
	public void registerPersist(JsonPersist persist) {
		this.persists.add(persist);
	}

	public void registerListener(Listener listener) {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(listener, this);
	}

	public void loadPersists() {
		for (JsonPersist persist : this.persists) {
			persist.loadData();
		}
	}

	public void savePersists() {
		for (JsonPersist persist : this.persists) {
			persist.saveData();
		}
	}

	private GsonBuilder createGsonBuilder() {
		GsonBuilder ret = new GsonBuilder();

		ret.setPrettyPrinting();
		ret.disableHtmlEscaping();
		ret.excludeFieldsWithModifiers(Modifier.TRANSIENT);

		ret.registerTypeHierarchyAdapter(ItemStack.class, new ItemStackAdapter(this));
		ret.registerTypeAdapter(Location.class, new LocationAdapter(this)).create();

		return ret;
	}
	
	@Override
	public void onDisable() {
		this.savePersists();
	}
}
