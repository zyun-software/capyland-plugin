package com.zyunsoftware.capydevmc.app.actions.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ArticleCommand implements CommandExecutor, TabCompleter {
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

    ItemStack heldItem = player.getInventory().getItemInMainHand();
    if (heldItem == null) {
      player.sendMessage("§cПотрібно взяти товар у руку");
      return true;
    }

    ItemMeta itemMeta = heldItem.getItemMeta();

    List<Component> lore = itemMeta.lore();
    if (lore == null) {
      lore = new ArrayList<>();
    }

    if (args.length == 3 && args[0].equalsIgnoreCase("додати")) {
      String productCode = args[1];

      // todo: API виклик для додавання товару
      boolean canAdd = lore.size() == 0;
      if (canAdd) {
        lore.add(Component.text("§6§lТовар"));
        lore.add(Component.text(player.getName() + "#" + productCode));
        itemMeta.lore(lore);
        heldItem.setItemMeta(itemMeta);
        player.sendMessage("§aТовар успішно додано на склад");
      } else {
        player.sendMessage("§cЦей товар не можна додати на склад");
      }

      return true;
    }

    if (args.length == 1 && args[0].equalsIgnoreCase("прибрати")) {
      boolean canRemove = false;
      if (lore.size() >= 2) {
        String firstLine = LegacyComponentSerializer.legacyAmpersand()
          .serialize(lore.get(0));

        String secondLine = LegacyComponentSerializer.legacyAmpersand()
          .serialize(lore.get(1));

        if (firstLine.contains("Товар") && secondLine.contains(player.getName())) {
          canRemove = true;
        }
      }

      if (canRemove) {
        // todo: API виклик для прибирання товару
        lore.clear();
        itemMeta.lore(lore);
        heldItem.setItemMeta(itemMeta);
        player.sendMessage("§aТовар успішно прибрано з складу");
      } else {
        player.sendMessage("§cВи не можете прибрати цей товар з складу");
      }

      return true;
    }

    player.sendMessage("§cНевірна дія для товару");

    return true;
  }

  @Override
  public @Nullable List<String> onTabComplete(
    @NotNull CommandSender sender,
    @NotNull Command command,
    @NotNull String alias,
    @NotNull String[] args
  ) {
    List<String> completions = new ArrayList<>();

    if (args.length == 1) {
      completions.add("додати");
      completions.add("прибрати");
    } else if (args.length == 2 && args[0].equalsIgnoreCase("додати")) {
      completions.add("Вкажіть код товару");
    }

    return completions;
  }
}
