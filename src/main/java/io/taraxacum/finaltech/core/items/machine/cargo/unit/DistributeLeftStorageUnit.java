package io.taraxacum.finaltech.core.items.machine.cargo.unit;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.taraxacum.finaltech.api.dto.ItemStackWithWrapperAmount;
import io.taraxacum.finaltech.core.items.machine.cargo.AbstractCargo;
import io.taraxacum.finaltech.core.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.core.menu.storage.OneLineStorageUnitMenu;
import io.taraxacum.finaltech.util.ItemStackUtil;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class DistributeLeftStorageUnit extends AbstractCargo {
    public DistributeLeftStorageUnit(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new OneLineStorageUnitMenu(this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        int beginSlot = 0;
        int endSlot = 0;
        int i;
        ItemStackWithWrapperAmount itemWithWrapperAmount = null;
        for(i = this.getInputSlot().length - 1; i >= 0; i--) {
            if(!ItemStackUtil.isItemNull(blockMenu.getItemInSlot(i))) {
                itemWithWrapperAmount = new ItemStackWithWrapperAmount(blockMenu.getItemInSlot(i));
                beginSlot = i;
                endSlot = i--;
                break;
            }
        }
        for(; i >= 0; i--) {
            if(ItemStackUtil.isItemNull(blockMenu.getItemInSlot(i))) {
                endSlot = i;
            } else if(ItemStackUtil.isItemSimilar(itemWithWrapperAmount, blockMenu.getItemInSlot(i))) {
                itemWithWrapperAmount.addAmount(blockMenu.getItemInSlot(i).getAmount());
                endSlot = i;
            } else {
                int amount = itemWithWrapperAmount.getAmount() / (beginSlot + 1 - endSlot);
                if(amount > 0) {
                    for(int j = beginSlot; j >= endSlot; j--) {
                        ItemStack item = ItemStackUtil.cloneItem(itemWithWrapperAmount.getItemStack());
                        item.setAmount(amount);
                        blockMenu.replaceExistingItem(j, item);
                    }
                    ItemStack item = ItemStackUtil.cloneItem(itemWithWrapperAmount.getItemStack());
                    item.setAmount(amount + (itemWithWrapperAmount.getAmount() % (beginSlot + 1 - endSlot)));
                    blockMenu.replaceExistingItem(beginSlot, item);
                }
                itemWithWrapperAmount = new ItemStackWithWrapperAmount(blockMenu.getItemInSlot(i));
                beginSlot = i;
                endSlot = i;
            }
        }
        if(beginSlot != endSlot) {
            int amount = itemWithWrapperAmount.getAmount() / (beginSlot + 1 - endSlot);
            if(amount > 0) {
                for(int j = beginSlot; j >= endSlot; j--) {
                    ItemStack item = ItemStackUtil.cloneItem(itemWithWrapperAmount.getItemStack());
                    item.setAmount(amount);
                    blockMenu.replaceExistingItem(j, item);
                }
                ItemStack item = ItemStackUtil.cloneItem(itemWithWrapperAmount.getItemStack());
                item.setAmount(amount + itemWithWrapperAmount.getAmount() % (beginSlot + 1 - endSlot));
                blockMenu.replaceExistingItem(beginSlot, item);
            }
        }
    }
}
