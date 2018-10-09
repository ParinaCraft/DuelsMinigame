package fi.joniaromaa.duelsminigame.utils;

import java.text.MessageFormat;
import java.util.TreeMap;

import net.md_5.bungee.api.ChatColor;

public class LangUtils
{
	private static final TreeMap<String, TreeMap<String, String>> textsByLang = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private static TreeMap<String, String> defaultTexts = null;
	
	public static void init(String defaultLang)
	{
		/*TreeMap<String, String> english = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		english.put("scoreboard.players", ChatColor.AQUA + "Players: " + ChatColor.GREEN + "{0}/{1}");
		english.put("scoreboard.waiting-for-players", ChatColor.AQUA + "Waiting for players");
		english.put("scoreboard.countdown-starting", ChatColor.AQUA + "Starting in " + ChatColor.GREEN + "{0}");
		english.put("scoreboard.map", ChatColor.AQUA + "Map: " + ChatColor.GREEN + "{0}");
		LangUtils.textsByLang.put("en_US", english);*/
		
		TreeMap<String, String> finnish = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		finnish.put("scoreboard.players", ChatColor.AQUA + "Pelaajia: " + ChatColor.GREEN + "{0}/{1}");
		finnish.put("scoreboard.waiting-for-players", ChatColor.AQUA + "Odotetaan pelaajia");
		finnish.put("scoreboard.countdown-starting", ChatColor.AQUA + "Alkaa " + ChatColor.GREEN + "{0}");
		finnish.put("scoreboard.map", ChatColor.AQUA + "Kartta: " + ChatColor.GREEN + "{0}");
		finnish.put("scoreboard.time-left", ChatColor.AQUA + "Aika j‰ljell‰: " + ChatColor.GREEN + "{0}");
		finnish.put("scoreboard.game-over", ChatColor.AQUA + "Peli p‰‰ttynyt");
		LangUtils.textsByLang.put("fi_FI", finnish);
		
		LangUtils.defaultTexts = LangUtils.textsByLang.get(defaultLang);
		if (LangUtils.defaultTexts == null && LangUtils.textsByLang.firstEntry() != null)
		{
			LangUtils.defaultTexts = LangUtils.textsByLang.firstEntry().getValue();
		}
	}
	
	public static String getText(String locale, String key)
	{
		TreeMap<String, String> texts = LangUtils.textsByLang.getOrDefault(locale, LangUtils.defaultTexts);
		if (texts != null)
		{
			String text = texts.get(key);
			if (text != null)
			{
				return text;
			}
		}
		
		return key;
	}
	
	public static String getText(String locale, String key, Object... params)
	{
		TreeMap<String, String> texts = LangUtils.textsByLang.getOrDefault(locale, LangUtils.defaultTexts);
		if (texts != null)
		{
			String text = texts.get(key);
			if (text != null)
			{
				return MessageFormat.format(text, params);
			}
		}

		return key;
	}
}
