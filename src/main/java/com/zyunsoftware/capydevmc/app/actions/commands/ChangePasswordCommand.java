package com.zyunsoftware.capydevmc.app.actions.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.zyunsoftware.capydevmc.app.DependencyInjection;
import com.zyunsoftware.capydevmc.domain.models.minecraft.MinecraftRepository;
import com.zyunsoftware.capydevmc.domain.services.AuthorizationService;
import com.zyunsoftware.capydevmc.infrastructure.utilities.ConfigUtility;

public class ChangePasswordCommand implements CommandExecutor {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if (!(sender instanceof Player)) {
      sender.sendMessage(ConfigUtility.getString("message.command.change-password.console"));
      return true;
    }

    AuthorizationService authorizationService = DependencyInjection.getAuthorizationService();
    MinecraftRepository minecraftRepository = authorizationService.getMinecraftRepository();

    minecraftRepository.selectPlayer(sender.getName());
    minecraftRepository.setArgs(args);

    authorizationService.changePassword();

    return true;
  }
}
