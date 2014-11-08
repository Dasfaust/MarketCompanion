package me.dasfaust.GlobalMarket;

import com.google.gson.Gson;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;

import java.util.List;

@Mod(modid = "MarketCompanion", name = "MarketCompanion", version = "2.0.0", acceptableRemoteVersions = "*")
public class MarketCompanion {

	@Instance("MarketCompanion")
	public static MarketCompanion instance = new MarketCompanion();
	
	public static MarketCompanion getInstance() {
		return instance;
	}

    public static Gson gson = new Gson();
	
	public WrappedItemStack getWrappedForgeItemStack(Object bInv, int slot) throws IllegalArgumentException {
		IInventory inv = ((IInventory) bInv);
		ItemStack stack = inv.getStackInSlot(slot);
		if (stack == null) {
			throw new IllegalArgumentException(String.format("There is no item in slot %s", slot));
		}
		return new WrappedItemStack(stack);
	}
	
	public void addToPlayerInventory(String playerName, int slot, org.bukkit.inventory.ItemStack stack) throws IllegalArgumentException {
		EntityPlayer player = findPlayer(playerName);
		if (!(stack instanceof WrappedItemStack)) {
			throw new IllegalArgumentException("ItemStack is not an instance of WrappedItemStack");
		}
		player.inventory.addItemStackToInventory(((WrappedItemStack) stack).stack);
	}
	
	public void setInventoryContents(Object bInv, org.bukkit.inventory.ItemStack[] stacks) {
		IInventory inv = ((IInventory) bInv);
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			inv.setInventorySlotContents(i, null);
		}
		for (int i = 0; i < stacks.length; i++) {
			if (stacks[i] != null) {
				if (!(stacks[i] instanceof WrappedItemStack)) {
					throw new IllegalArgumentException("ItemStack is not an instance of WrappedItemStack");
				}
				WrappedItemStack stack = ((WrappedItemStack) stacks[i]);
				inv.setInventorySlotContents(i, stack.stack);
			}
		}
	}
	
	public void setInventorySlot(Object bInv, org.bukkit.inventory.ItemStack stack, int slot) {
		if (!(stack instanceof WrappedItemStack)) {
			throw new IllegalArgumentException("ItemStack is not an instance of WrappedItemStack");
		}
		IInventory inv = ((IInventory) bInv);
		inv.setInventorySlotContents(slot, ((WrappedItemStack) stack).stack);
	}
	
	public org.bukkit.inventory.ItemStack wrap(Object itemStack) {
		return new WrappedItemStack((ItemStack) itemStack);
	}

    // There's probably a method for this :/
    private EntityPlayer findPlayer(String name) {
        for (EntityPlayer player : (List<EntityPlayer>) MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            if (player.getDisplayName().equals(name)) return player;
        }
        throw new IllegalArgumentException(String.format("No player by the name of %s found!", name));
    }
}
