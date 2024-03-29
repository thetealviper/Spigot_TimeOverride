package me.TheTealViper.timeoverride.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import me.TheTealViper.timeoverride.TimeOverride;

public class PluginFile extends YamlConfiguration {   
   
    private File file;
    private String defaults;
    private JavaPlugin plugin;
   
    /**
     * Creates new PluginFile, without defaults
     * @param plugin - Your plugin
     * @param fileName - Name of the file
     */
    public PluginFile(JavaPlugin plugin, String fileName) {
        this(plugin, fileName, null);
    }
   
    /**
     * Creates new PluginFile, with defaults
     * @param plugin - Your plugin
     * @param fileName - Name of the file
     * @param defaultsName - Name of the defaults
     */
    public PluginFile(JavaPlugin plugin, String fileName, String defaultsName) {
        this.plugin = plugin;
        this.defaults = defaultsName;
        this.file = new File(plugin.getDataFolder(), fileName);
        reload();
    }
   
    /**
     * Reload configuration
     */
    public void reload() {
       
        if (!file.exists()) {
           
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
               
            } catch (IOException exception) {
                exception.printStackTrace();
                Bukkit.getServer().getConsoleSender().sendMessage(TimeOverride.LOG_PREFIX + "Error while creating file " + file.getName());
            }
           
        }
       
        try {
            load(file);
           
            if (defaults != null) {
                InputStreamReader reader = new InputStreamReader(plugin.getResource(defaults));
                FileConfiguration defaultsConfig = YamlConfiguration.loadConfiguration(reader);       
               
                setDefaults(defaultsConfig);
                options().copyDefaults(true);
               
                reader.close();
                save();
            }
       
        } catch (IOException exception) {
            exception.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage(TimeOverride.LOG_PREFIX + "Error while loading file " + file.getName());
           
        } catch (InvalidConfigurationException exception) {
            exception.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage(TimeOverride.LOG_PREFIX + "Error while loading file " + file.getName());
           
        }
       
    }
   
    /**
     * Save configuration
     */
    public void save() {
       
        try {
            options().indent(2);
            save(file);
           
        } catch (IOException exception) {
            exception.printStackTrace();
            Bukkit.getServer().getConsoleSender().sendMessage(TimeOverride.LOG_PREFIX + "Error while saving file " + file.getName());
        }
       
    }
   
}