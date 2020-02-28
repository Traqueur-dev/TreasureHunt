package fr.traqueur.treasurehunt.api.utils.jsons;

import java.io.File;

import fr.traqueur.treasurehunt.TreasurePlugin;
import lombok.Getter;
import net.minecraft.util.com.google.gson.Gson;
import fr.traqueur.treasurehunt.api.utils.jsons.JsonPersist;

public abstract class Saveable implements JsonPersist {

	public boolean needDir, needFirstSave;
	
	@Getter
	private TreasurePlugin plugin;

	public Saveable(TreasurePlugin plugin, String name) {
		this(plugin, name, false, false);
		this.plugin = plugin;
	}

	public Saveable(TreasurePlugin plugin, String name, boolean needDir, boolean needFirstSave) {
		this.needDir = needDir;
		this.needFirstSave = needFirstSave;
		if (this.needDir) {
			if (this.needFirstSave) {
				this.saveData();
			} else {
				File directory = this.getFile();
				if (!directory.exists()) {
					try {
						directory.mkdir();
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			}
		}
	}

	public Gson getGson() {
		return TreasurePlugin.getInstance().getGson();
	}
}
