package com.zyunsoftware.capydevmc.app.actions.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class YourPluginListener implements Listener {
   @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Перевірка, чи гравець тримає в руках кістку
        if (player.getInventory().getItemInMainHand().getType() == Material.BONE) {
            // Перевірка, чи гравець клікнув правою кнопкою миші на блоку
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (block != null) {
                    // Включення інспектора
                    // coreProtectAPI.performLookup(player, ParseResult.BLOCK, 10, block.getLocation(), null);
                    
                    // coreProtectAPI.

                    // Ваш код для дій після кліку на блок
                    // Наприклад, можна виконати дії збереження інформації про блок

                    // Вимкнення інспектора
                    // coreProtectAPI.performLookup(player, ParseResult.BLOCK, 0, null, null);
                }
            }
        }
    }
}