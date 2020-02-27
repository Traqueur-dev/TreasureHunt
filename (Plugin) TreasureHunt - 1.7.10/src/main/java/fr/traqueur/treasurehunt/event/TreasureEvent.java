package fr.traqueur.treasurehunt.event;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.traqueur.treasurehunt.TreasureManager;
import fr.traqueur.treasurehunt.TreasurePlugin;
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
			if (TreasureEvent.timeUntilStart == 30) {
				Bukkit.broadcastMessage(
						prefix + "§eL'évènement se lance dans §c" + TreasureEvent.timeUntilStart + " §esecondes.");
			}
			if (TreasureEvent.timeUntilStart == 20) {
				Bukkit.broadcastMessage(
						prefix + "§eL'évènement se lance dans §c" + TreasureEvent.timeUntilStart + " §esecondes.");
			}
			if (TreasureEvent.timeUntilStart == 10) {
				Bukkit.broadcastMessage(
						prefix + "§eL'évènement se lance dans §c" + TreasureEvent.timeUntilStart + " §esecondes.");
			}

			if (TreasureEvent.timeUntilStart <= 5 && TreasureEvent.timeUntilStart != 0) {
				Bukkit.broadcastMessage(
						prefix + "§eL'évènement se lance dans §c" + TreasureEvent.timeUntilStart + " §esecondes.");
			}

			if (TreasureEvent.timeUntilStart == 0) {
				Bukkit.broadcastMessage(prefix + "§eLancement de l'évènement §cimminent§e.");
				manager.launchEvent();
				TreasurePlugin.getInstance().getTreasureManager().setState(TreasureState.PLAY);
			}
			if (TreasureEvent.timeUntilStart > 0) {
				TreasureEvent.timeUntilStart--;
			}
		}

		if (manager.getState() == TreasureState.PLAY) {
			if (manager.getPlayers().size() == 0) {
				Bukkit.broadcastMessage(prefix + "§cL'évènement §eest terminé par manque de joueurs.");
				TreasureEvent.stop();
			}
			
			Bukkit.broadcastMessage("" + timeEvent);
			TreasureEvent.timeEvent--;
		}
		
		if (timeEvent == 0) {
			Bukkit.broadcastMessage(prefix + "§cL'évènement §eest terminé. Voici le §aTOP 10§e:");
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
	}
}
