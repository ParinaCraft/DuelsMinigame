package fi.joniaromaa.duelsminigame.gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import fi.joniaromaa.duelsminigame.DuelsPlugin;
import fi.joniaromaa.duelsminigame.game.DuelsMinigame;
import fi.joniaromaa.duelsminigame.gui.data.DuelsRematchVoteTeam;
import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import fi.joniaromaa.parinacorelibrary.bukkit.inventory.PlayerInterfaceInventory;
import net.md_5.bungee.api.ChatColor;

public class DuelRematchPlayerInterfaceInventory extends PlayerInterfaceInventory
{
	private final DuelsMinigame duelsMinigame;
	
	private final LinkedList<DuelsRematchVoteTeam> teams;
	private final HashMap<UUID, DuelsRematchVoteTeam> uuids;
	
	@SuppressWarnings("deprecation")
	public DuelRematchPlayerInterfaceInventory(DuelsMinigame duelsMinigame, LinkedList<DuelsRematchVoteTeam> teams)
	{
		super(DuelsPlugin.getPlugin(), 45, ChatColor.YELLOW + "Uusinta?");
		
		this.duelsMinigame = duelsMinigame;
		
		this.teams = teams;
		this.uuids = new HashMap<>();
		for(DuelsRematchVoteTeam team : teams)
		{
			for(UUID teamMember : team.getMembers().keySet())
			{
				this.uuids.put(teamMember, team);
			}
		}
		
		if (teams.size() == 2)
		{
			this.getInventory().setItem(12, this.teams.get(0).getVoteItemStack());
			this.getInventory().setItem(14, this.teams.get(1).getVoteItemStack());
			
			this.setItem(30, ItemStackBuilder.builder()
					.type(Material.STAINED_CLAY)
					.data((byte)13)
					.displayName(ChatColor.GREEN + "Uusinta")
					.build(), this::rematch);
			
			this.setItem(32, ItemStackBuilder.builder()
					.type(Material.STAINED_CLAY)
					.data((byte)14)
					.displayName(ChatColor.GREEN + "Ei uusintaa")
					.build(), this::noRematch);
		}
	}
	
	public void rematch(InventoryClickEvent event)
	{
		DuelsRematchVoteTeam vote = this.uuids.get(event.getWhoClicked().getUniqueId());
		if (vote != null && vote.voteRematch(event.getWhoClicked().getUniqueId()))
		{
			this.getInventory().setItem(12, this.teams.get(0).getVoteItemStack());
			this.getInventory().setItem(14, this.teams.get(1).getVoteItemStack());
			
			this.duelsMinigame.sendMessage(ChatColor.GREEN + event.getWhoClicked().getName() + " haluaa uusinnan!");
			
			for(DuelsRematchVoteTeam team : this.teams)
			{
				if (!team.everyoneHasVotesForRematch())
				{
					return;
				}
			}
			
			this.duelsMinigame.doRematch();
			
			this.close();
		}
	}
	
	public void noRematch(InventoryClickEvent event)
	{
		DuelsRematchVoteTeam vote = this.uuids.get(event.getWhoClicked().getUniqueId());
		if (vote != null)
		{
			vote.voteNoRematch(event.getWhoClicked().getUniqueId());
			
			this.duelsMinigame.sendMessage(ChatColor.RED + event.getWhoClicked().getName() + " ei halua uusintaa!");
			this.duelsMinigame.voteEnded();
			
			this.close();
		}
	}
}
