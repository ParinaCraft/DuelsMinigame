package fi.joniaromaa.duelsminigame.user.dataset;

import org.bukkit.Location;

import fi.joniaromaa.parinacorelibrary.api.user.dataset.UserDataStorage;
import lombok.Getter;

public class UserStartLocationDataStorage implements UserDataStorage
{
	@Getter private Location location;
	
	public UserStartLocationDataStorage(Location location)
	{
		this.location = location;
	}
}
