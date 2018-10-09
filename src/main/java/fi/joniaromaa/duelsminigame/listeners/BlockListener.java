package fi.joniaromaa.duelsminigame.listeners;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.LeavesDecayEvent;

import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.pregame.DuelsPreMinigame;

public class BlockListener implements Listener
{
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onLeavesDecayEvent(LeavesDecayEvent event)
	{
		World world = event.getBlock().getWorld();
		if (world.hasMetadata("MinigameWorld"))
		{
			Object minigame = world.getMetadata("MinigameWorld").get(0).value();
			if (minigame instanceof DuelsPreMinigame || minigame instanceof DuelsMinigame)
			{
				event.setCancelled(true);
			}
		}
	}
}
