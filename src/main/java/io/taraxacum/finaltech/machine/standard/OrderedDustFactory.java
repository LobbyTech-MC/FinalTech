package io.taraxacum.finaltech.machine.standard;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.taraxacum.finaltech.menu.standard.AbstractStandardMachineMenu;
import io.taraxacum.finaltech.core.operation.OrderedDustOperation;
import io.taraxacum.finaltech.menu.standard.OrderedDustFactoryMenu;
import io.taraxacum.finaltech.setup.register.FinalTechItems;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class OrderedDustFactory extends AbstractStandardMachine {
    public static final int TYPE_DIFFICULTY = 16;
    public static final int AMOUNT_DIFFICULTY = 1024;

    public OrderedDustFactory(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.registerDefaultRecipes();
    }

    @Nonnull
    @Override
    protected AbstractStandardMachineMenu setMachineMenu() {
        return new OrderedDustFactoryMenu(this.getId(), this.getItemName(), this);
    }

    @Override
    protected void tick(@Nonnull Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        BlockMenu inv = BlockStorage.getInventory(block);
        OrderedDustOperation operation = (OrderedDustOperation)this.getMachineProcessor().getOperation(block);

        for(int slot : this.getInputSlots()) {
            ItemStack inputItem = inv.getItemInSlot(slot);
            if(inputItem == null) {
                continue;
            }

            if(operation == null) {
                operation = new OrderedDustOperation();
                this.getMachineProcessor().startOperation(block, operation);
            }
            operation.addItem(inputItem);

            if(operation.isFinished()) {
                if(operation.isOrderedDust() && InvUtils.fitAll(inv.toInventory(), new ItemStack[] {FinalTechItems.ORDERED_DUST}, this.getOutputSlots())) {
                    inv.pushItem(new ItemStack(FinalTechItems.ORDERED_DUST), this.getOutputSlots());
                    this.getMachineProcessor().endOperation(block);
                    operation = null;
                } else if(InvUtils.fitAll(inv.toInventory(), new ItemStack[] {FinalTechItems.UNORDERED_DUST}, this.getOutputSlots())) {
                    inv.pushItem(new ItemStack(FinalTechItems.UNORDERED_DUST), this.getOutputSlots());
                    this.getMachineProcessor().endOperation(block);
                    operation = null;
                }
            }
            inv.consumeItem(slot, inputItem.getAmount());
        }

        if(operation == null) {
            operation = new OrderedDustOperation();
            this.getMachineProcessor().startOperation(block, operation);
        }
        CustomItemStack progress = new CustomItemStack(Material.REDSTONE, "&f完成进度",
                "&7匹配的物品种类" + operation.getTypeCount() + "/" + TYPE_DIFFICULTY,
                "&7输入的物品总数" + operation.getAmountCount() + "/" + AMOUNT_DIFFICULTY);
        inv.replaceExistingItem(22, progress);
    }

    @Override
    public void registerDefaultRecipes() {
        this.registerDescriptiveRecipe("&f制造无序尘埃",
                "",
                "&f输入至少" + TYPE_DIFFICULTY + "种物品",
                "&f输入至少" + AMOUNT_DIFFICULTY + "个物品",
                "&f满足以上两个条件时生成一个无序尘埃");
        this.registerDescriptiveRecipe("&f制造有序尘埃",
                "",
                "&f输入恰好" + TYPE_DIFFICULTY + "种物品",
                "&f输入恰好" + AMOUNT_DIFFICULTY + "个物品",
                "&f满足以上两个条件时生成一个有序尘埃");
    }
}
