package me.TheTealViper.timeoverride;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.TheTealViper.timeoverride.Utils.EnableShit;
import me.TheTealViper.timeoverride.Utils.PluginFile;
import me.TheTealViper.timeoverride.Utils.StringUtils;

public class TimeOverride extends JavaPlugin implements Listener {
	public static Plugin plugin;
	public static String LOG_PREFIX = "[TimeOverride] ";
	public static List<Month> monthList = new ArrayList<Month>();
	public static double timeCycleLength = 0d,
			daysInYear = 0d,
			secondOffset = 0d,
			minuteOffset = 0d,
			hourOffset = 0d,
			dayOffset = 0d,
			monthOffset = 0d,
			yearOffset = 0d,
			serverTicksPerTimeUnit = 0d,
			totalTicksOffset = 0d,
			timeUnitsPerMinute = 0d,
			timeUnitsPerHour = 0d;
	
	private int updateTicks = 0,
			minimumMinuteDigits = 0;
	private long startMillis = 0;
	private double daytimeStart = 0d,
			nighttimeStart = 0d;
	private boolean avoidMilitaryTime = false;
	private String bossbarText = "",
			timeCommand = "",
			timeCommandText = "";
	private BossBar bar = null;
	//Daytime naturally starts at 6000 ticks
	//Nighttime naturally starts at 18000 ticks
	//They both last 12000 ticks
	
	//These are used in the placeholders
	private Month currentMonth = null;
	private int currentYear = 0,
			currentDay = 0,
			currentHour = 0,
			currentMinute = 0,
			currentSecond = 0;
	
	public void onEnable() {
		EnableShit.handleOnEnable(this, this, "-1");
		
		deserializeConfig();
		
		bar = Bukkit.createBossBar("", BarColor.BLUE, BarStyle.SOLID);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {public void run() {
			//Calculate stuff
			int runningTicks = (int) ((System.currentTimeMillis() - startMillis) / 1000d * 20d);
			double runningTimeUnits = runningTicks / serverTicksPerTimeUnit;
			
			//Calculate other stuff
			int timeScenario = 0;
			if(runningTimeUnits % timeCycleLength > daytimeStart && !(runningTimeUnits % timeCycleLength > nighttimeStart)) { //After day start and before night start
				timeScenario = 1;
			}else if(runningTimeUnits % timeCycleLength > nighttimeStart) { //After night start
				timeScenario = 2;
			}else { //Before day start
				timeScenario = 3;
			}
			
			//Get info from calcs
			currentYear = Month.getFixedYearFromTickOffset(runningTicks);
			currentMonth = Month.getFixedMonthFromTickOffset(runningTicks);
			currentDay = Month.getFixedDayFromTickOffset(runningTicks);
			currentHour = Month.getFixedHourFromTickOffset(runningTicks);
			currentMinute = Month.getFixedMinuteFromTickOffset(runningTicks);
			currentSecond = Month.getFixedSecondFromTickOffset(runningTicks);
			
			
			//Set time of day
//			int deltaTicks = (int) (((Double.valueOf(runningTimeUnits) % Double.valueOf(timeCycleLength)) / Double.valueOf(timeCycleLength)) * 24000d);
//			for(World w : Bukkit.getWorlds())
//				w.setTime(deltaTicks);
			
			//Set time of day WORK IN PROGRESS
			double timeUnitsInDayOrNight = 0;
			double timeUnitsUnderConsideration = 0;
			int deltaTicks = 0;
			double ratiodTimeUnits = 0;
			switch(timeScenario) {
			case 1:
				timeUnitsInDayOrNight = nighttimeStart - daytimeStart;
				timeUnitsUnderConsideration = ((runningTimeUnits % timeCycleLength) - daytimeStart);
				ratiodTimeUnits = timeUnitsUnderConsideration / timeUnitsInDayOrNight;
				deltaTicks = (int) (ratiodTimeUnits * 12000d);
				for(World w : Bukkit.getWorlds())
					w.setTime(deltaTicks);
				break;
			case 2:
				timeUnitsInDayOrNight = (timeCycleLength - nighttimeStart) + daytimeStart;
				timeUnitsUnderConsideration = ((runningTimeUnits % timeCycleLength) - nighttimeStart);
				ratiodTimeUnits = timeUnitsUnderConsideration / timeUnitsInDayOrNight;
				deltaTicks = (int) (ratiodTimeUnits * 12000d);
				for(World w : Bukkit.getWorlds())
					w.setTime(12000 + deltaTicks);
				break;
			case 3:
				timeUnitsInDayOrNight = (timeCycleLength - nighttimeStart) + daytimeStart;
				timeUnitsUnderConsideration = (timeCycleLength - nighttimeStart) + ((runningTimeUnits % timeCycleLength));
				ratiodTimeUnits = timeUnitsUnderConsideration / timeUnitsInDayOrNight;
				deltaTicks = (int) (ratiodTimeUnits * 12000d);
				for(World w : Bukkit.getWorlds())
					w.setTime(12000 + deltaTicks);
				break;
			default:
				break;
			}
			
			//Update bossbar
			String message = formatMessage(bossbarText);
			if(bar == null) {
				for(Player p : Bukkit.getOnlinePlayers())
					bar.addPlayer(p);
				bar.setVisible(true);
			} else
				bar.setTitle(message);
			
		}}, 0, updateTicks);
	  }
	  
	  public void onDisable() { 
		  getServer().getConsoleSender().sendMessage(LOG_PREFIX + "TimeOverride from TheTealViper shutting down. Bshzzzzzz");
	  }
	  
	  public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		  if(label.equals("timeoverride") && sender.hasPermission("timeoverride.getoffset")) {
			  if(args.length != 1)
				  return false;
			  if(!args[0].equals("getoffset"))
				  return false;
			  
			  Date date = new Date(startMillis);
			  LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			  int year = localDate.getYear();
			  int month = localDate.getMonthValue();
			  int day = localDate.getDayOfMonth();
			  LocalTime localTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();
			  int hour = localTime.getHour();
			  int minute = localTime.getMinute();
			  int second = localTime.getSecond();
			  sender.sendMessage("The offsets should be:");
			  sender.sendMessage("Year: " + year);
			  sender.sendMessage("Month: " + month);
			  sender.sendMessage("Day: " + day);
			  sender.sendMessage("Hour: " + hour);
			  sender.sendMessage("Minute: " + minute);
			  sender.sendMessage("Second: " + second);
		  }
		  
		  return false;
	  }
	  
	  @EventHandler
	  public void onJoin(PlayerJoinEvent e) {
		  if(bar.getPlayers() == null || bar.getPlayers().isEmpty() || !bar.getPlayers().contains(e.getPlayer()))
			  bar.addPlayer(e.getPlayer());
	  }
	  
	  @EventHandler
	  public void onChat(PlayerCommandPreprocessEvent e) {
		  if(e.getMessage().equals(timeCommand)) {
			  e.getPlayer().sendMessage(formatMessage(timeCommandText));
			  e.setCancelled(true);
		  }
	  }
	  
	  public void deserializeConfig() {
		  PluginFile pf = new PluginFile(this, "config.yml");
		  PluginFile pfdata = new PluginFile(this, "data.yml");
		  if(!pfdata.contains("start_millis"))
			  pfdata.set("start_millis", System.currentTimeMillis());
		  pfdata.save();
		  
		  //Load in fast data
		  startMillis = pfdata.getLong("start_millis");
		  timeCycleLength = pf.getDouble("Time_Cycle_Length");
		  serverTicksPerTimeUnit = pf.getDouble("Server_Ticks_Per_Time_Unit");
		  avoidMilitaryTime = pf.getBoolean("Avoid_Military_Time");
		  daytimeStart = pf.getDouble("Daytime_Start");
		  nighttimeStart = pf.getDouble("Nighttime_Start");
		  updateTicks = pf.getInt("Update_Ticks");
		  bossbarText = pf.getString("Bossbar_Text");
		  timeUnitsPerMinute = pf.getDouble("Time_Units_Per_Minute");
		  timeUnitsPerHour = pf.getDouble("Time_Units_Per_Hour");
		  minimumMinuteDigits = pf.getInt("Minimum_Minute_Digits");
		  timeCommand = pf.getString("Time_Command");
		  timeCommandText = pf.getString("Time_Command_Text");
		  
		  //Load in months
		  ConfigurationSection sec = pf.getConfigurationSection("Months");
		  for(String monthName : sec.getKeys(false)) {
			  int days = sec.getInt(monthName + ".Days");
			  monthList.add(new Month(monthName, days));
			  daysInYear += days;
		  }
		  
		  //Calculate offsets
		  yearOffset = pf.getDouble("Year_Offset");
		  monthOffset = pf.getDouble("Month_Offset");
		  dayOffset = pf.getDouble("Day_Offset");
		  hourOffset = pf.getDouble("Hour_Offset");
		  minuteOffset = pf.getDouble("Minute_Offset");
		  secondOffset = pf.getDouble("Second_Offset");
		  
		  //Seconds
		  double ticksPerSecond = serverTicksPerTimeUnit;
		  totalTicksOffset += secondOffset * ticksPerSecond;
		  //Minutes
		  double ticksPerMinute = timeUnitsPerMinute * serverTicksPerTimeUnit;
		  totalTicksOffset += minuteOffset * ticksPerMinute;
		  //Hours
		  double ticksPerHour = timeUnitsPerHour * serverTicksPerTimeUnit;
		  totalTicksOffset += hourOffset * ticksPerHour;
		  //Days
		  double ticksPerDay = timeCycleLength * serverTicksPerTimeUnit;
		  totalTicksOffset += dayOffset * ticksPerDay;
		  //Months
		  for(int i = 0;i < monthOffset;i++) {
			  double ticksPerThisMonth = monthList.get(i).days * ticksPerDay;
			  totalTicksOffset += ticksPerThisMonth;
		  }
		  //Years
		  double ticksPerYear = daysInYear * ticksPerDay;
		  totalTicksOffset += yearOffset * ticksPerYear;
	  }
	  
	  public String formatMessage(String s) {
		  String replacer = "";
		  
		  replacer = "%timeoverride_year%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, currentYear + "");
		  
		  replacer = "%timeoverride_month%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, currentMonth.name + "");
		  
		  replacer = "%timeoverride_day%";
		  if(s.contains(replacer)) {
			  s = s.replace(replacer, currentDay + "");
		  }
		  
		  replacer = "%timeoverride_hour%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, getHour());
		  
		  replacer = "%timeoverride_minute%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, getMinute());
		  
		  replacer = "%timeoverride_second%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, currentSecond + "");
		  
		  replacer = "%timeoverride_ampm%";
		  if(s.contains(replacer))
			  s = s.replace(replacer, getAMPM());
		  
		  return StringUtils.makeColors(s);
	  }
	  
	  public String getHour() {
		  if(!avoidMilitaryTime)
			  return currentHour + "";
		  
		  double hoursPerDay = timeCycleLength / timeUnitsPerHour;
		  double halfTotalHours = hoursPerDay / 2d;
		  return ((int)(currentHour % halfTotalHours)) + "";
	  }
	  public String getAMPM() {
		  if(!avoidMilitaryTime)
			  return "ERROR:YouMustEnableAvoidMilitaryTime";
		  
		  double hoursPerDay = timeCycleLength / timeUnitsPerHour;
		  double halfTotalHours = hoursPerDay / 2d;
		  return currentHour > halfTotalHours ? "PM" : "AM";
	  }
	  
	  public String getMinute() {
		  String currentMinute = this.currentMinute + "";
		  if(currentMinute.length() < minimumMinuteDigits) {
			  for(int i = 0;i < minimumMinuteDigits - currentMinute.length();i++)
				  currentMinute = 0 + currentMinute;
		  }
		  return currentMinute;
	  }
	  
}
