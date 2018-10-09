package fi.joniaromaa.duelsminigame.gui.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import fi.joniaromaa.parinacorelibrary.bukkit.builder.ItemStackBuilder;
import lombok.Getter;

public class DuelsRematchVoteTeam
{
	@Getter private final String teamName;
	@Getter private HashMap<UUID, String> members;
	
	private ItemStack voteItemStack;
	private HashSet<UUID> rematchVotes;
	
	public DuelsRematchVoteTeam(String teamName)
	{
		this.teamName = teamName;
		this.members = new HashMap<UUID, String>();
		
		this.rematchVotes = new HashSet<UUID>();
	}
	
	public void addMember(UUID uuid, String displayName)
	{
		this.members.put(uuid, displayName);
	}
	
	@SuppressWarnings("deprecation")
	public boolean voteRematch(UUID uuid)
	{
		boolean result = this.rematchVotes.add(uuid);
		if (result)
		{
			if (this.getAmountOfRematchVotes() == this.getMembersCount())
			{
				this.voteItemStack.setData(new MaterialData(Material.STAINED_GLASS_PANE, (byte)13));
			}
			else
			{
				this.voteItemStack.setData(new MaterialData(Material.STAINED_GLASS_PANE, (byte)4));
			}
		}
		
		return result;
	}
	
	@SuppressWarnings("deprecation")
	public ItemStack getVoteItemStack()
	{
		if (this.voteItemStack == null)
		{
			this.voteItemStack = ItemStackBuilder.builder().type(Material.STAINED_GLASS_PANE).data((byte)7).displayName(this.getTeamName() + " valinta").build();
		}
		
		return this.voteItemStack;
	}
	
	@SuppressWarnings("deprecation")
	public void voteNoRematch(UUID uuid)
	{
		this.voteItemStack.setData(new MaterialData(Material.STAINED_GLASS_PANE, (byte)14));
	}
	
	public int getMembersCount()
	{
		return this.members.size();
	}
	
	public int getAmountOfRematchVotes()
	{
		return this.rematchVotes.size();
	}
	
	public boolean everyoneHasVotesForRematch()
	{
		return this.getMembersCount() == this.getAmountOfRematchVotes();
	}
}
