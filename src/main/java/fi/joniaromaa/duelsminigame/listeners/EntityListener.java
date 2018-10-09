package fi.joniaromaa.duelsminigame.listeners;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import fi.joniaromaa.minigameframework.MinigamePlugin;

public class EntityListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFoodLevelChangeEvent(FoodLevelChangeEvent event)
	{
		HumanEntity human = event.getEntity();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(human.getUniqueId()).ifPresent((m) ->
		{
			event.setCancelled(true); //If we cancel its not gonna set the food level so manually set it
			
			((CraftHumanEntity)event.getEntity()).getHandle().getFoodData().eat(20, 20); //Set food level and saturation
		});
	}
}
