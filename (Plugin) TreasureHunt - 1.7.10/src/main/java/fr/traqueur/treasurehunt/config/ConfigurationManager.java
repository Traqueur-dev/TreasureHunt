package fr.traqueur.treasurehunt.config;

import java.io.File;
import java.lang.reflect.Type;

import fr.traqueur.treasurehunt.TreasurePlugin;
import fr.traqueur.treasurehunt.api.utils.jsons.DiscUtil;
import fr.traqueur.treasurehunt.api.utils.jsons.Saveable;
import lombok.Getter;
import net.minecraft.util.com.google.common.reflect.TypeToken;

public class ConfigurationManager extends Saveable {

	private @Getter Configuration config;
	
	public ConfigurationManager(TreasurePlugin plugin) {
		super(plugin, "Configuration");
		this.config = new Configuration();
	}

	@Override
	public File getFile() {return new File(this.getPlugin().getDataFolder(), "config.json");}

	@Override
	public void loadData() {
		String content = DiscUtil.readCatch(this.getFile());
		if (content != null) {
			Type type = new TypeToken<Configuration>() {private static final long serialVersionUID = 1L;}.getType();
			Configuration config = this.getGson().fromJson(content, type);
			this.config = config;
		}
	}

	@Override
	public void saveData() {DiscUtil.writeCatch(this.getFile(), this.getGson().toJson(config));}

}
