package fi.joniaromaa.duelsminigame.listeners;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.pregame.DuelsPreMinigame;
import fi.joniaromaa.minigameframework.MinigamePlugin;
import net.minecraft.server.v1_8_R3.DamageSource;

public class PlayerListener implements Listener
{
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			Collection<UUID> limitTo = null;
			if (m instanceof DuelsPreMinigame)
			{
				limitTo = ((DuelsPreMinigame)m).getPlayersUniqueIds();
			}
			else if (m instanceof DuelsMinigame)
			{
				limitTo = ((DuelsMinigame)m).getPlayersUniqueIds();
			}
			
			if (limitTo != null)
			{
				Iterator<Player> recipients = event.getRecipients().iterator();
				while (recipients.hasNext())
				{
					Player recipient = recipients.next();
					if (!limitTo.contains(recipient.getUniqueId()))
					{
						recipients.remove();
					}
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerDeathEvent(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof DuelsMinigame)
			{
				DuelsMinigame duelsMinigame = (DuelsMinigame)m;
				
				event.getDrops().clear();
				event.setDroppedExp(0);
				event.setDeathMessage(null);
				
				duelsMinigame.makeSpectator(player);
			}
		});
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerDropItemEvent(PlayerDropItemEvent event)
	{
		Player player = event.getPlayer();
		
		MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
		{
			if (m instanceof DuelsMinigame)
			{
				DuelsMinigame duelsMinigame = (DuelsMinigame)m;
				if (!duelsMinigame.getConfig().getDuelType().canDropItems())
				{
					event.setCancelled(true);
				}
			}
		});
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();

		if (player.getLocation().getY() <= 0)
		{
			MinigamePlugin.getPlugin().getGameManager().getMinigame(player).ifPresent((m) ->
			{
				if (m instanceof DuelsMinigame)
				{
					((CraftPlayer)player).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
				}
			});
		}
	}
}
