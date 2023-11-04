package com.zyunsoftware.capydevmc.app.actions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccountCommand implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    if (!(sender instanceof Player)) {
      return false;
    }

    Player player = (Player) sender;

    

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String alias,
    @NotNull String[] args
  ) {
    Player player = (Player) sender;
    List<String> completions = new ArrayList<>();

    if (args.length == 1) {
      completions.add("статус");
      completions.add("оплатити");
      completions.add("баланс");
      completions.add("переказ");
    } else if (args.length == 2 && Stream.of("статус", "оплатити").anyMatch(args[0]::equalsIgnoreCase)) {
      completions.add("Вкажіть номер рахунку");
    } else if (args.length == 2 && args[0].equalsIgnoreCase("переказ")) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        String playerName = onlinePlayer.getName();
        if (playerName.equals(player.getName())) {
          continue;
        }
        if (playerName.toLowerCase().startsWith(args[1].toLowerCase())) {
          completions.add(playerName);
        }
      }
      if (completions.size() == 0) {
        completions.add("Вкажіть отримувача");
      }
    } else if (args.length == 3 && args[0].equalsIgnoreCase("переказ")) {
      completions.add("Вкажіть суму переказу");
    }

    return completions;
  }
}
