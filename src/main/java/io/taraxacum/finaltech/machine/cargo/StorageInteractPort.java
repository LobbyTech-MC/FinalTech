package io.taraxacum.finaltech.machine.cargo;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib;
import io.github.thebusybiscuit.slimefun4.utils.itemstack.ItemStackWrapper;
import io.taraxacum.finaltech.interfaces.RecipeItem;
import io.taraxacum.finaltech.menu.AbstractMachineMenu;
import io.taraxacum.finaltech.menu.BasicLinkedStorageUnitMenu;
import io.taraxacum.finaltech.menu.StorageInteractPortMenu;
import io.taraxacum.finaltech.util.*;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author Final_ROOT
 */
public class StorageInteractPort extends AbstractCargo implements RecipeItem {
    private static final int SEARCH_LIMIT = 9;
    public static final List<MachineRecipe> RECIPE = new ArrayList<>();

    public StorageInteractPort(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
        this.registerDefaultRecipes();
    }

    @Nonnull
    @Override
    protected AbstractMachineMenu setMachineMenu() {
        return new StorageInteractPortMenu(this.getId(), this.getItemName(), this);
    }

    @Override
    protected void tick(Block block, @Nonnull SlimefunItem slimefunItem, @Nonnull Config config) {
        Block targetBlock = block.getRelative(BlockFace.UP);
        BlockMenu blockMenu = BlockStorage.getInventory(block);
        if(!BlockStorage.hasInventory(targetBlock)) {
            BlockState blockState = PaperLib.getBlockState(targetBlock, false).getState();
            if (blockState instanceof InventoryHolder) {
                Inventory inventory = ((InventoryHolder) blockState).getInventory();
                LinkedList<ItemStackWrapper> unPushItem = new LinkedList<>();
                LinkedList<ItemStackWrapper> unStackItem = new LinkedList<>();
                boolean isFull = MachineUtil.isFull(blockMenu.toInventory(), getOutputSlots());
                boolean isEmpty = MachineUtil.isEmpty(blockMenu.toInventory(), getInputSlots());
                MachineUtil.stockSlots(blockMenu, getOutputSlots());
                MachineUtil.stockSlots(blockMenu, getInputSlots());
                int pushItemAmount = 0;
                Map<ItemStack, ItemMeta> map = new HashMap<>(inventory.getSize());
                for(int i = 0; i < Math.min(inventory.getSize(), SEARCH_LIMIT); i++) {
                    ItemStack item = inventory.getItem(i);
                    if(ItemStackUtil.isItemNull(item) || !item.hasItemMeta()) {
                        continue;
                    }
                    ItemMeta itemMeta = item.getItemMeta();
                    if(StringItemUtil.canStorage(itemMeta)) {
                        map.put(item, itemMeta);
                        if(item.getAmount() == 1) {
                            pushItemAmount++;
                        }
                    }
                }
                for(Map.Entry<ItemStack, ItemMeta> entry : map.entrySet()) {
                    if(isEmpty && isFull) {
                        continue;
                    }
                    ItemMeta itemMeta = entry.getValue();
                    ItemStack stringItemStack = StringItemUtil.parseItemInCard(itemMeta);
                    ItemStackWithWrapper stringItem = stringItemStack == null ? null : new ItemStackWithWrapper(stringItemStack);
                    boolean work = true;
                    int pushCount = 0;
                    int stackCount = 0;
                    if(!isFull && entry.getKey().getAmount() == 1 && stringItem != null) {
                        for(ItemStackWrapper unWorkItem : unPushItem) {
                            if(ItemStackUtil.isItemSimilar(stringItem.getItemStackWrapper(), unWorkItem)) {
                                work = false;
                                break;
                            }
                        }
                        if(work) {
                            pushItemAmount--;
                            pushCount = StringItemUtil.pushItemFromCard(itemMeta, stringItem, blockMenu.toInventory(), getOutputSlots());
                            if(pushCount == 0) {
                                unPushItem.add(stringItem.getItemStackWrapper());
                            } else {
                                isFull = MachineUtil.isFull(blockMenu.toInventory(), getOutputSlots());
                                if(!isFull) {
                                    MachineUtil.stockSlots(blockMenu, getOutputSlots());
                                }
                            }
                            if(pushItemAmount == 0) {
                                isFull = true;
                            }
                        }
                    }
                    if(!isEmpty){
                        work = true;
                        if(stringItem != null) {
                            for(ItemStackWrapper unWorkItem : unStackItem) {
                                if(ItemStackUtil.isItemSimilar(stringItem.getItemStackWrapper(), unWorkItem)) {
                                    work = false;
                                    break;
                                }
                            }
                        }
                        if(work) {
                            stackCount = StringItemUtil.storageItemToCard(itemMeta, stringItem, entry.getKey().getAmount(), blockMenu.toInventory(), getInputSlots());
                            if(stackCount == 0) {
                                if(stringItem != null) {
                                    unStackItem.add(stringItem.getItemStackWrapper());
                                }
                            } else {
                                isEmpty = MachineUtil.isEmpty(blockMenu.toInventory(), getInputSlots());
                                if(!isEmpty) {
                                    MachineUtil.stockSlots(blockMenu, getInputSlots());
                                }
                                if(stringItem == null) {
                                    stringItem = new ItemStackWithWrapper(StringItemUtil.parseItemInCard(itemMeta));
                                }
                            }
                        }
                    }
                    if(pushCount != 0 || stackCount != 0) {
                        StringItemUtil.updateStorageCardLore(itemMeta, stringItem.getItemStack());
                        entry.getKey().setItemMeta(itemMeta);
                        StringItemUtil.updateStorageItemType(entry.getKey(), itemMeta);
                    }
                }
            }
        }
    }

    @Override
    public List<MachineRecipe> getMachineRecipes() {
        return RECIPE;
    }

    @Override
    public void registerDefaultRecipes() {
        this.registerDescriptiveRecipe("&f存取物品",
                "",
                "&f需在该机器上方放置原版容器",
                "&f并将存储卡放于前" + SEARCH_LIMIT + "个格子");
        this.registerDescriptiveRecipe("&f存入物品",
                "",
                "&f将物品输入到该机器的输入槽",
                "&f然后该机器会把物品存入到对应的存储器中");
        this.registerDescriptiveRecipe("&f取出物品",
                "",
                "&f在交互位置放置堆叠数为1的存储卡",
                "&f然后该机器会不断取出物品至自身输出槽");
        this.registerDescriptiveRecipe("&f混合存入",
                "",
                "&f该机器可以把物品存入到堆叠的存储卡中");
    }
}