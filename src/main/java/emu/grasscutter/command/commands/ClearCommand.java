package emu.grasscutter.command.commands;

import static emu.grasscutter.command.CommandHelpers.*;

import emu.grasscutter.command.*;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.inventory.*;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.utils.lang.Language;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import lombok.Setter;

@Command(
        label = "clear",
        usage = {"(all|wp|art|mat) [lv<max level>] [r<max refinement>] [<max rarity>*]", "<itemId> [x<amount>]"},
        permission = "player.clearinv",
        permissionTargeted = "player.clearinv.others")
public final class ClearCommand implements CommandHandler {

    private static final Map<Pattern, BiConsumer<ClearItemParameters, Integer>> intCommandHandlers =
            Map.ofEntries(
                    Map.entry(lvlRegex, ClearItemParameters::setLvl),
                    Map.entry(refineRegex, ClearItemParameters::setRefinement),
                    Map.entry(rankRegex, ClearItemParameters::setRank),
                    Map.entry(amountRegex, ClearItemParameters::setAmount));

    private Stream<GameItem> getOther(
            ItemType type, Inventory playerInventory, ClearItemParameters param) {
        return playerInventory.getItems().values().stream()
                .filter(item -> item.getItemType() == type)
                .filter(item -> item.getItemData().getRankLevel() <= param.rank)
                .filter(item -> !item.isLocked() && !item.isEquipped());
    }

    private Stream<GameItem> getWeapons(Inventory playerInventory, ClearItemParameters param) {
        return getOther(ItemType.ITEM_WEAPON, playerInventory, param)
                .filter(item -> item.getLevel() <= param.lvl)
                .filter(item -> item.getRefinement() < param.refinement);
    }

    private Stream<GameItem> getRelics(Inventory playerInventory, ClearItemParameters param) {
        return getOther(ItemType.ITEM_RELIQUARY, playerInventory, param)
                .filter(item -> item.getLevel() <= param.lvl + 1);
    }

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        Inventory playerInventory = targetPlayer.getInventory();
        ClearItemParameters param = new ClearItemParameters();
        if (args.size() < 1) {
            sendUsageMessage(sender);
            return;
        }

        // Extract any tagged int arguments (e.g. "lv90", "x100", "r5")
        parseIntParameters(args, param, intCommandHandlers);
        String arg0 = args.get(0);
        String playerString = targetPlayer.getNickname(); // Should probably be UID instead but whatever
        int itemId = 0;
        try {
            itemId = Integer.parseInt(arg0);
        } catch (NumberFormatException e) {}
        if (itemId > 0) {
            var itemName = Language.getTextMapKey(GameData.getItemDataMap().get(itemId).getNameTextMapHash());
            if (param.amount == 0) param.setAmount(playerInventory.getItemCountById(itemId));
            if (param.amount == 0) sendUsageMessage(sender);
            else {
                playerInventory.removeItem(itemId, param.amount);
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.item", playerString, param.amount, itemName);
            }
            return;
        }

        switch (arg0) {
            case "wp" -> {
                playerInventory.removeItems(getWeapons(playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.weapons", playerString);
            }
            case "art" -> {
                playerInventory.removeItems(getRelics(playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.artifacts", playerString);
            }
            case "mat" -> {
                playerInventory.removeItems(
                        getOther(ItemType.ITEM_MATERIAL, playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.materials", playerString);
            }
            case "all" -> {
                playerInventory.removeItems(getRelics(playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.artifacts", playerString);
                playerInventory.removeItems(getWeapons(playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.weapons", playerString);
                playerInventory.removeItems(
                        getOther(ItemType.ITEM_MATERIAL, playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.materials", playerString);
                playerInventory.removeItems(
                        getOther(ItemType.ITEM_FURNITURE, playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.furniture", playerString);
                playerInventory.removeItems(
                        getOther(ItemType.ITEM_DISPLAY, playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.displays", playerString);
                playerInventory.removeItems(
                        getOther(ItemType.ITEM_VIRTUAL, playerInventory, param).toList());
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.virtuals", playerString);
                CommandHandler.sendTranslatedMessage(sender, "commands.clear.everything", playerString);
            }
        }
    }

    private static class ClearItemParameters {
        @Setter public int lvl = 1;
        @Setter public int refinement = 1;
        @Setter public int rank = 4;
        @Setter public int amount = 0;
    }
}
