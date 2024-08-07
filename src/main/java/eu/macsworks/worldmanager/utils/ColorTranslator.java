package eu.macsworks.worldmanager.utils;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class ColorTranslator {

	public static String translate(String str) {
		final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");
		Matcher matcher = hexPattern.matcher(str);
		StringBuffer buffer = new StringBuffer(str.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			matcher.appendReplacement(buffer,
					COLOR_CHAR + "x"
							+ COLOR_CHAR + group.charAt(0)
							+ COLOR_CHAR + group.charAt(1)
							+ COLOR_CHAR + group.charAt(2)
							+ COLOR_CHAR + group.charAt(3)
							+ COLOR_CHAR + group.charAt(4)
							+ COLOR_CHAR + group.charAt(5)
			);
		}
		return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
	}

	public static String beautify(String str) {
		StringBuilder output = new StringBuilder();
		for(String splice : str.replace("_", " ").split(" ")){
			output.append(Character.toUpperCase(splice.charAt(0))).append(splice.substring(1).toLowerCase()).append(" ");
		}

		return output.toString();
	}

}
