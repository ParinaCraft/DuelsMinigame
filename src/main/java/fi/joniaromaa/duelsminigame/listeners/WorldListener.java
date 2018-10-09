package fi.joniaromaa.duelsminigame.listeners;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.pregame.DuelsPreMinigame;

public class WorldListener implements Listener
{
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkLoadEvent(ChunkLoadEvent event)
	{
		Chunk chunk = event.getChunk();
		
		if (Boolean.TRUE.equals(this.shouldUnloadChunk(event.getWorld(), chunk)))
		{
			chunk.unload(false, false);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChunkUnloadEvent(ChunkUnloadEvent event)
	{
		Chunk chunk = event.getChunk();
		
		if (Boolean.FALSE.equals(this.shouldUnloadChunk(event.getWorld(), chunk)))
		{
			event.setCancelled(true);
		}
	}
	
	private Boolean shouldUnloadChunk(World world, Chunk chunk)
	{
		if (world.hasMetadata("MinigameWorld"))
		{
			Object minigame = world.getMetadata("MinigameWorld").get(0).value();
			if (minigame instanceof DuelsPreMinigame)
			{
				return ((DuelsPreMinigame)minigame).shouldUnloadChunk(chunk);
			}
			else if (minigame instanceof DuelsMinigame)
			{
				return ((DuelsMinigame)minigame).shouldUnloadChunk(chunk);
			}
		}
		
		return null;
	}
}
