package fr.traqueur.treasurehunt.event;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.traqueur.treasurehunt.TreasureManager;
import fr.traqueur.treasurehunt.TreasurePlugin;
import fr.traqueur.treasurehunt.event.TreasureState;
import lombok.Setter;

public class TreasureEvent implements Runnable {

	private static TreasureManager manager = TreasurePlugin.getInstance().getTreasureManager();
	private static String prefix = TreasurePlugin.getInstance().getPrefix();

	private @Setter static int timeUntilStart;
	private static int timeEvent;

	public TreasureEvent(int timeUntilStart) {
		TreasureEvent.timeUntilStart = timeUntilStart;
		TreasureEvent.timeEvent = TreasurePlugin.getInstance().getConfigManager().getConfig().getTimeEventInSecond();
	}

	@Override
	public void run() {
		if (manager.getState() == TreasureState.WAIT) {
			if (TreasureEvent.timeUntilStart % 60 == 0 && TreasureEvent.timeUntilStart >= 60) {
				Bukkit.broadcastMessage(
						prefix + "§6L'évènement se lance dans §6§l" + TreasureEvent.timeUntilStart/60 + " §6minutes.");
			}
			if (TreasureEvent.timeUntilStart == 30) {
				Bukkit.broadcastMessage(
						prefix + "§6L'évènement se lance dans §6§l" + TreasureEvent.timeUntilStart + " §6secondes.");
			}
			if (TreasureEvent.timeUntilStart == 20) {
				Bukkit.broadcastMessage(
						prefix + "§6L'évènement se lance dans §6§l" + TreasureEvent.timeUntilStart + " §6secondes.");
			}
			if (TreasureEvent.timeUntilStart == 10) {
				Bukkit.broadcastMessage(
						prefix + "§6L'évènement se lance dans §6§l" + TreasureEvent.timeUntilStart + " §6secondes.");
			}

			if (TreasureEvent.timeUntilStart <= 5 && TreasureEvent.timeUntilStart != 0) {
				Bukkit.broadcastMessage(
						prefix + "§6L'évènement se lance dans §6§l" + TreasureEvent.timeUntilStart + (TreasureEvent.timeEvent > 1 ? " §6secondes." : " §6seconde."));
			}

			if (TreasureEvent.timeUntilStart == 0) {
				Bukkit.broadcastMessage(prefix + "§6Lancement de l'évènement §6§limminent§6.");
				manager.launchEvent();
				TreasurePlugin.getInstance().getTreasureManager().setState(TreasureState.PLAY);
			}
			if (TreasureEvent.timeUntilStart > 0) {
				TreasureEvent.timeUntilStart--;
			}
		}

		if (manager.getState() == TreasureState.PLAY) {
			if (manager.getPlayers().size() == 0) {
				Bukkit.broadcastMessage(prefix + "§6§lL'évènement §6est terminé par manque de joueurs.");
				TreasureEvent.stop();
			}
			
			if (TreasureEvent.timeEvent % (5*60) == 0 && TreasureEvent.timeEvent >= 60) {
				Bukkit.broadcastMessage(prefix + "§6Il reste §6§l" + TreasureEvent.timeEvent/60 + "§6 minutes.");
			}
			if (TreasureEvent.timeEvent == 30) {
				Bukkit.broadcastMessage(prefix + "§6Il reste §6§l" + TreasureEvent.timeEvent + "§6 secondes, avant la fin de la chasse.");
			}
			if (TreasureEvent.timeEvent == 20) {
				Bukkit.broadcastMessage(prefix + "§6Il reste §6§l" + TreasureEvent.timeEvent + "§6 secondes, avant la fin de la chasse.");
			}
			if (TreasureEvent.timeEvent == 10) {
				Bukkit.broadcastMessage(prefix + "§6Il reste §6§l" + TreasureEvent.timeEvent + "§6 secondes, avant la fin de la chasse.");
			}

			if (TreasureEvent.timeEvent <= 5 && TreasureEvent.timeEvent != 0) {
				Bukkit.broadcastMessage(prefix + "§6Il reste §6§l" + (TreasureEvent.timeEvent > 1 ? TreasureEvent.timeEvent + "§6 secondes, avant la fin de la chasse." : TreasureEvent.timeEvent + "§6 seconde, avant la fin de la chasse."));
			}
			TreasureEvent.timeEvent--;
		}
		
		if (timeEvent == 0) {
			Bukkit.broadcastMessage(prefix + "§6§lL'évènement §6est terminé. Voici le §cTOP 10§6:");
			TreasureEvent.stop();
			
		}
	}

	public static void stop() {
		TreasurePlugin.getInstance().getServer().getScheduler().cancelTask(manager.getCurrentIntTask());
		TreasurePlugin.getInstance().getTreasureManager().setState(TreasureState.FINISH);
		
		manager.setRewardHasTake(false);
		if (manager.getLastLocations().size() != 0) {
			for (Entry<Player, Location> elem : manager.getLastLocations().entrySet()) {
				manager.leaveEvent(elem.getKey());
			}
		}
		
		TreasurePlugin.getInstance().getTreasureManager().setClassement();
		TreasurePlugin.getInstance().getTreasureManager().getPlayers().clear();
		TreasurePlugin.getInstance().getTreasureManager().getLastInventories().clear();
		TreasurePlugin.getInstance().getTreasureManager().getLastLocations().clear();
		
		manager.clearMap();
	}
}
