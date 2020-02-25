package fr.traqueur.treasurehunt.api.utils.jsons.adapters;

import fr.traqueur.treasurehunt.TreasurePlugin;
import net.minecraft.util.com.google.gson.Gson;
import net.minecraft.util.com.google.gson.TypeAdapter;

public abstract class GsonAdapter<T> extends TypeAdapter<T> {

	private TreasurePlugin plugin;

	public GsonAdapter(TreasurePlugin plugin) {
		this.plugin = plugin;
	}

	public Gson getGson() {
		return this.plugin.getGson();
	}
}
