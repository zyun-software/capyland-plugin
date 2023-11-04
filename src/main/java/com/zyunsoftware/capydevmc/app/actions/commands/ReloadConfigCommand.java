package com.zyunsoftware.capydevmc.app.actions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zyunsoftware.capydevmc.app.CapylandPlugin;

public class ReloadConfigCommand implements CommandExecutor {
  @Override
  public boolean onCommand(
    CommandSender sender,
    Command command,
    String label,
    String[] args
  ) {
    if (sender instanceof Player && !sender.isOp()) {
      sender.sendMessage("§cУ вас немає прав виконувати цю команду");
      return true;
    }

    CapylandPlugin.getInstance().reloadConfig();
    sender.sendMessage("§aКонфігурацію плагіну перезавантажено");

    return true;
  }
}
