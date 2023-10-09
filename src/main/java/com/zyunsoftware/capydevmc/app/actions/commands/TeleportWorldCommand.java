package com.zyunsoftware.capydevmc.app.actions.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;

public class TeleportWorldCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Цю команду може виконати лише гравець!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 1) {
            player.sendMessage("Вам потрібно вказати ім'я світу.");
            return true;
        }

        String worldName = args[0];

        MultiverseCore multiverseCore = (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
        MultiverseWorld targetWorld = multiverseCore.getMVWorldManager().getMVWorld(worldName);
        if (targetWorld != null) {
            World world = targetWorld.getCBWorld();
            player.teleport(world.getSpawnLocation());
            player.sendMessage("Вас було телепортовано до " + worldName);
        } else {
            player.sendMessage("Світ з назвою " + worldName + " не існує.");
        }

        return true;
    }
}
