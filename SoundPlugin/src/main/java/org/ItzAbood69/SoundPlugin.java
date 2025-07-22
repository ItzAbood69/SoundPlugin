package org.ItzAbood69;

import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class SoundPlugin extends JavaPlugin implements CommandExecutor, TabCompleter {

    private FileConfiguration config;

    @Override
    public void onEnable() {
        // Load configuration
        saveDefaultConfig();
        config = getConfig();

        // Register the commands
        this.getCommand("sound").setExecutor(this);
        this.getCommand("sound").setTabCompleter(this);
        this.getCommand("soundhelp").setExecutor(this);
        this.getCommand("soundhelp").setTabCompleter(this);
        this.getCommand("soundreload").setExecutor(this);
        this.getCommand("soundreload").setTabCompleter(this);

        getLogger().info("========================================");
        getLogger().info("SoundPlugin v" + getDescription().getVersion());
        getLogger().info("§e★ §6Made by §cItzAbood69 §e★");
        getLogger().info("Plugin has been enabled successfully!");
        getLogger().info("Config loaded: " + (config.getBoolean("settings.enable-plugin", true) ? "§aEnabled" : "§cDisabled"));
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("SoundPlugin has been disabled!");
        getLogger().info("§e★ §6Made by §cItzAbood69 §e★");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("soundhelp")) {
            return handleHelpCommand(sender);
        }

        if (command.getName().equalsIgnoreCase("sound")) {
            // Handle /sound help command
            if (args.length > 0 && args[0].equalsIgnoreCase("help")) {
                return handleHelpCommand(sender);
            }
            return handleSoundCommand(sender, args);
        }

        if (command.getName().equalsIgnoreCase("soundreload")) {
            return handleReloadCommand(sender);
        }

        return false;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("soundplugin.admin")) {
            sender.sendMessage(config.getString("messages.no-permission", "§c❌ You don't have permission to use this command!"));
            return true;
        }

        try {
            reloadConfig();
            config = getConfig();
            sender.sendMessage(config.getString("messages.config-reloaded", "§a✅ Configuration reloaded successfully!"));
            getLogger().info("Configuration reloaded by " + sender.getName());
        } catch (Exception e) {
            sender.sendMessage("§c❌ Error reloading configuration: " + e.getMessage());
            getLogger().severe("Error reloading configuration: " + e.getMessage());
        }

        return true;
    }

    private boolean handleHelpCommand(CommandSender sender) {
        if (!sender.hasPermission("soundplugin.help")) {
            sender.sendMessage(config.getString("messages.no-permission", "§c❌ You don't have permission to use this command!"));
            return true;
        }

        List<String> helpMessage = config.getStringList("messages.help-message");
        if (helpMessage.isEmpty()) {
            // Fallback help message with ads
            sender.sendMessage("§6========== §eSoundPlugin Help §6==========");
            sender.sendMessage("§b📢 Plugin by: §eItzAbood69");
            sender.sendMessage("§b🔧 Version: §e" + getDescription().getVersion());
            sender.sendMessage("");
            sender.sendMessage("§a🎵 Commands:");
            sender.sendMessage("§e/sound <sound_name> [volume] [pitch] §7- Play a sound");
            sender.sendMessage("§e/sound help §7- Show this help menu");
            sender.sendMessage("§e/soundhelp §7- Show help menu");
            sender.sendMessage("§e/soundreload §7- Reload configuration");
            sender.sendMessage("");
            sender.sendMessage("§a🔑 Permissions:");
            sender.sendMessage("§e• soundplugin.play §7- Play sounds");
            sender.sendMessage("§e• soundplugin.help §7- View help");
            sender.sendMessage("§e• soundplugin.admin §7- All permissions");
            sender.sendMessage("");
            sender.sendMessage("§a📋 Examples:");
            sender.sendMessage("§e/sound BLOCK_NOTE_BLOCK_PLING");
            sender.sendMessage("§e/sound ENTITY_CREEPER_PRIMED 1.5");
            sender.sendMessage("§e/sound BLOCK_ANVIL_PLACE 0.8 0.5");
            sender.sendMessage("");
            sender.sendMessage("§d✨ §5Check out my other plugins:");
            sender.sendMessage("§d• §9ParticleMaster §7- Amazing visual effects!");
            sender.sendMessage("§d• §9WorldEditTools §7- Advanced building utilities");
            sender.sendMessage("§d• §9UltimateEconomy §7- Complete economy solution");
            sender.sendMessage("");
            sender.sendMessage("§6💖 Support my work  joining my discord server §6☕");
            sender.sendMessage("§b🌟 🌟to join search in gogel dsc.gg/ZoneMine");
            sender.sendMessage("§6=====================================");
        } else {
            // Use config help message
            for (String line : helpMessage) {
                sender.sendMessage(line
                        .replace("{version}", getDescription().getVersion())
                        .replace("{author}", getDescription().getAuthors().get(0))
                        .replace("&", "§"));
            }

            // Add ads after config message
            sender.sendMessage("");
            sender.sendMessage("§d✨ §5Check out my other plugins:");
            sender.sendMessage("§d• §9ParticleMaster §7- Amazing visual effects!");
            sender.sendMessage("§d• §9WorldEditTools §7- Advanced building utilities");
            sender.sendMessage("§d• §9UltimateEconomy §7- Complete economy solution");
            sender.sendMessage("");
            sender.sendMessage("§6💖 Support my work  joining my discord server §6☕");
            sender.sendMessage("§b🌟to join search in gogel dsc.gg/ZoneMine");
        }
        return true;
    }

    private boolean handleSoundCommand(CommandSender sender, String[] args) {
        // Check if plugin is enabled
        if (!config.getBoolean("settings.enable-plugin", true)) {
            sender.sendMessage(config.getString("messages.plugin-disabled", "§c❌ SoundPlugin is currently disabled!"));
            return true;
        }

        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(config.getString("messages.player-only", "§c❌ Only players can use this command!"));
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.hasPermission("soundplugin.play")) {
            player.sendMessage(config.getString("messages.no-permission", "§c❌ You don't have permission to use this command!"));
            player.sendMessage("§e💡 Type §6/sound help §efor more information");
            return true;
        }

        // Check if sound argument is provided
        if (args.length == 0) {
            player.sendMessage(config.getString("messages.usage", "§c❌ Usage: §e/sound <sound_name> [volume] [pitch]"));
            player.sendMessage("§e💡 Example: §6/sound BLOCK_NOTE_BLOCK_PLING 1.0 1.0");
            player.sendMessage("§e💡 Type §6/sound help §efor more information");
            return true;
        }

        String soundName = args[0].toUpperCase();
        float volume = (float) config.getDouble("settings.default-volume", 1.0);
        float pitch = (float) config.getDouble("settings.default-pitch", 1.0);
        float maxVolume = (float) config.getDouble("settings.max-volume", 2.0);
        float maxPitch = (float) config.getDouble("settings.max-pitch", 2.0);
        float minPitch = (float) config.getDouble("settings.min-pitch", 0.5);

        // Parse volume if provided
        if (args.length >= 2) {
            try {
                volume = Float.parseFloat(args[1]);
                if (volume < 0.0f || volume > maxVolume) {
                    player.sendMessage(config.getString("messages.invalid-volume", "§c❌ Volume must be between 0.0 and " + maxVolume));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(config.getString("messages.invalid-volume-format", "§c❌ Invalid volume! Use a number between 0.0 and " + maxVolume));
                return true;
            }
        }

        // Parse pitch if provided
        if (args.length >= 3) {
            try {
                pitch = Float.parseFloat(args[2]);
                if (pitch < minPitch || pitch > maxPitch) {
                    player.sendMessage(config.getString("messages.invalid-pitch", "§c❌ Pitch must be between " + minPitch + " and " + maxPitch));
                    return true;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(config.getString("messages.invalid-pitch-format", "§c❌ Invalid pitch! Use a number between " + minPitch + " and " + maxPitch));
                return true;
            }
        }

        // Check if sound is blocked
        List<String> blockedSounds = config.getStringList("settings.blocked-sounds");
        if (blockedSounds.contains(soundName)) {
            player.sendMessage(config.getString("messages.sound-blocked", "§c❌ This sound is blocked by the server!"));
            return true;
        }

        // Try to find and play the sound
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, volume, pitch);

            String successMessage = config.getString("messages.sound-played", "§a🎵 Playing sound: §e{sound} §a(Volume: §e{volume}§a, Pitch: §e{pitch}§a)");
            player.sendMessage(successMessage
                    .replace("{sound}", soundName)
                    .replace("{volume}", String.valueOf(volume))
                    .replace("{pitch}", String.valueOf(pitch))
                    .replace("&", "§"));

            // Log if enabled
            if (config.getBoolean("settings.log-sounds", false)) {
                getLogger().info(player.getName() + " played sound: " + soundName + " (Volume: " + volume + ", Pitch: " + pitch + ")");
            }
        } catch (IllegalArgumentException e) {
            String notFoundMessage = config.getString("messages.sound-not-found", "§c❌ Sound '§e{sound}§c' not found!");
            player.sendMessage(notFoundMessage.replace("{sound}", soundName).replace("&", "§"));
            player.sendMessage("§e💡 Use tab completion to see available sounds");
            player.sendMessage("§e💡 Type §6/sound help §efor more information");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("sound")) {
            if (args.length == 1) {
                // Add "help" to tab completion
                completions.add("help");

                // Tab complete sound names
                List<String> soundNames = new ArrayList<>();
                for (Sound sound : Sound.values()) {
                    soundNames.add(sound.name());
                }
                StringUtil.copyPartialMatches(args[0], soundNames, completions);
            } else if (args.length == 2 && !args[0].equalsIgnoreCase("help")) {
                // Tab complete volume values from config
                double maxVolume = config.getDouble("settings.max-volume", 2.0);
                List<String> volumes = new ArrayList<>();
                for (int i = 1; i <= (int)(maxVolume * 10); i++) {
                    volumes.add(String.valueOf(i / 10.0));
                }
                StringUtil.copyPartialMatches(args[1], volumes, completions);
            } else if (args.length == 3 && !args[0].equalsIgnoreCase("help")) {
                // Tab complete pitch values from config
                double minPitch = config.getDouble("settings.min-pitch", 0.5);
                double maxPitch = config.getDouble("settings.max-pitch", 2.0);
                List<String> pitches = new ArrayList<>();
                for (int i = (int)(minPitch * 10); i <= (int)(maxPitch * 10); i++) {
                    pitches.add(String.valueOf(i / 10.0));
                }
                StringUtil.copyPartialMatches(args[2], pitches, completions);
            }
        } else if (command.getName().equalsIgnoreCase("soundhelp")) {
            // No tab completions needed for help command
            return completions;
        } else if (command.getName().equalsIgnoreCase("soundreload")) {
            // No tab completions needed for reload command
            return completions;
        }

        Collections.sort(completions);
        return completions;
    }
}