package eu.macsworks.worldmanager.utils;

import eu.macsworks.worldmanager.WorldManager;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

public class Utils {

	private static long lastTickEpochMilli;
	@Getter private static double tps;

	static{
		tick();
	}

	public static void tick(){
		Bukkit.getScheduler().runTaskTimer(WorldManager.getInstance(), () -> {
			long delta = System.currentTimeMillis() - lastTickEpochMilli;
			float currentTickrate = Bukkit.getServerTickManager().getTickRate();

			tps = Math.min(currentTickrate, 1D / (delta / 1000D));

			lastTickEpochMilli = System.currentTimeMillis();
		}, 0L, 1L);
	}

	public static boolean copyDirectory(Path source, Path target) throws IOException {
		File sourceFile = source.toFile();
		if(!sourceFile.exists() || !sourceFile.isDirectory()) return false;

		File targetFile = target.toFile();
		if(targetFile.exists()) return false;

		FileUtils.copyDirectory(sourceFile, targetFile);
		return true;
	}

	public static boolean deleteDirectory(File source) throws IOException {
		if(!source.exists() || !source.isDirectory()) return false;

		for(File file : source.listFiles()){
			if(file.isDirectory()){
				deleteDirectory(file);
				continue;
			}

			file.delete();
		}

		source.delete();

		return true;
	}

	public static String getTimeString(long epochSeconds){
		if(epochSeconds == 0) return "Never";

		String output = "";
		Duration duration = Duration.ofSeconds(epochSeconds);
		if(duration.toDaysPart() > 0) {
			output += duration.toDays() + "d ";
			duration = duration.minus(duration.toDays(), ChronoUnit.DAYS);
		}

		if(duration.toHoursPart() > 0) {
			output += duration.toHours() + "h ";
			duration = duration.minus(duration.toHours(), ChronoUnit.HOURS);
		}
		if(duration.toMinutesPart() > 0) {
			output += duration.toMinutes() + "m ";
			duration = duration.minus(duration.toMinutes(), ChronoUnit.MINUTES);
		}

		if(duration.toSecondsPart() > 0) output += duration.toSeconds() + "s";
		return output;
	}

}
