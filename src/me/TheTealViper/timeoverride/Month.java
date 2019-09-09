package me.TheTealViper.timeoverride;

import org.bukkit.Bukkit;

public class Month {
	String name = "";
	int days = 0;
	
	public Month(String name, int days) {
		this.name = name;
		this.days = days;
	}
	
	public static Month getMonthFromDay(int rawDayWithoutOffset) { //This day would likely be unaltered so shift it with the offset
		double rawTickWithoutOffset = rawDayWithoutOffset * TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		Month m = getFixedMonthFromTickOffset(rawTickWithoutOffset);
		return m;
	}
	
	public static int getMonthDayFromDay(int rawDayWithoutOffset) { //This day would likely be unaltered so shift it with the offset
		double rawTickWithoutOffset = rawDayWithoutOffset * TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		int day = getFixedDayFromTickOffset(rawTickWithoutOffset);
		
		int totalDays = 0;
		for(Month m : TimeOverride.monthList) {
			if(totalDays <= day && totalDays + m.days >= day) {
				return day - totalDays;
			}else {
				totalDays += m.days;
			}
		}
		
		//If the day amount is more than a full year's worth of days, subtract a year and try again.
		if(day > totalDays)
			Bukkit.broadcastMessage("DAYS ARE MORE: " + day);
			//return getMonthDayFromDay(day - totalDays);
		
		return -1;
	}
	
	public static int getDayFromMonth(String monthName, int daysIntoMonth) {
		int totalDays = 0;
		for(Month m : TimeOverride.monthList) {
			if(m.name.equals(monthName)) {
				totalDays += daysIntoMonth;
			}else {
				totalDays += m.days;
			}
		}
		
		return totalDays;
	}
	
	public static int getYearFromDay(int rawDayWithoutOffset) {
		double rawTickWithoutOffset = rawDayWithoutOffset * TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		return getFixedYearFromTickOffset(rawTickWithoutOffset);
	}
	
	public static int getFixedYearFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double years = totalTicksWithOffset / ticksInYear;
		return (int) years;
	}
	
	public static Month getFixedMonthFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double fixedTicks = totalTicksWithOffset % ticksInYear;
		
		double fixedDays = fixedTicks / TimeOverride.serverTicksPerTimeUnit / TimeOverride.timeCycleLength;
		int passedDays = 0;
		for(Month m : TimeOverride.monthList) {
			if(passedDays <= fixedDays && passedDays + m.days >= fixedDays) {
				return m;
			}else {
				passedDays += m.days;
			}
		}
		
		return null;
	}
	
	public static int getFixedDayFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double fixedTicks = totalTicksWithOffset % ticksInYear;
		
		double fixedDays = fixedTicks / TimeOverride.serverTicksPerTimeUnit / TimeOverride.timeCycleLength;
		
		int passedDays = 0;
		for(Month m : TimeOverride.monthList) {
			if(passedDays <= fixedDays && passedDays + m.days >= fixedDays) {
				return (int) (fixedDays - passedDays);
			}else {
				passedDays += m.days;
			}
		}
		
		return (int) fixedDays;
	}
	
	public static int getFixedHourFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double fixedTicks = totalTicksWithOffset % ticksInYear;
		
		
		double ticksInDay = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInDay;
		
		double ticksInHour = TimeOverride.timeUnitsPerHour * TimeOverride.serverTicksPerTimeUnit;
		double hours = fixedTicks / ticksInHour;
		return (int) hours;
	}
	
	public static int getFixedMinuteFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double fixedTicks = totalTicksWithOffset % ticksInYear;
		
		double ticksInDay = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInDay;
		
		double ticksInHour = TimeOverride.timeUnitsPerHour * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInHour;
		
		double ticksInMinute = TimeOverride.timeUnitsPerMinute * TimeOverride.serverTicksPerTimeUnit;
		double minutes = fixedTicks / ticksInMinute;
		return (int) minutes;
	}
	
	public static int getFixedSecondFromTickOffset(double rawTickWithoutOffset) {
		double totalTicksWithOffset = rawTickWithoutOffset + TimeOverride.totalTicksOffset;
		double ticksInYear = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit * TimeOverride.daysInYear;
		double fixedTicks = totalTicksWithOffset % ticksInYear;
		
		double ticksInDay = TimeOverride.timeCycleLength * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInDay;
		
		double ticksInHour = TimeOverride.timeUnitsPerHour * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInHour;
		
		double ticksInMinute = TimeOverride.timeUnitsPerMinute * TimeOverride.serverTicksPerTimeUnit;
		fixedTicks = fixedTicks % ticksInMinute;
		
		double ticksInSecond = TimeOverride.serverTicksPerTimeUnit;
		double seconds = fixedTicks / ticksInSecond;
		return (int) seconds;
	}
}
