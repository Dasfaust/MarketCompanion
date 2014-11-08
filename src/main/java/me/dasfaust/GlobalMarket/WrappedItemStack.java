package me.dasfaust.GlobalMarket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.nbt.*;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;

public class WrappedItemStack extends org.bukkit.inventory.ItemStack {

	protected ItemStack stack;
	protected WrappedItemMeta meta;
	
	protected WrappedItemStack(ItemStack stack) {
		this.stack = stack;
		meta = new WrappedItemMeta(stack);
	}
	
	@Override
	public Material getType() {
		return Material.getMaterial(Item.getIdFromItem(stack.getItem()));
	}
	
	@Override
	public int getTypeId() {
		return Item.getIdFromItem(stack.getItem());
	}
	
	@Override
	public short getDurability() {
		return (short) stack.getItemDamage();
	}
	
	@Override
	public int getMaxStackSize() {
		return this.getType().getMaxStackSize();
	}
	
	@Override
	public boolean hasItemMeta() {
		return true;
	}
	
	@Override
	public WrappedItemMeta getItemMeta() {
		return meta;
	}
	
	@Override
	public WrappedItemStack clone() {
		return new WrappedItemStack(stack.copy());
	}
	
	@Override
	public int getAmount() {
		return stack.stackSize;
	}
	
	@Override
	public void setAmount(int amount) {
		stack.stackSize = amount;
	}
	
	@Override
	public boolean equals(Object ob) {
		if (this == ob) {
            return true;
        }
        if (!(ob instanceof WrappedItemStack)) {
            return false;
        }
        WrappedItemStack s = (WrappedItemStack) ob;
        if (s.serializeJSON().equals(this.serializeJSON())) {
        	return true;
        }
        return false;
	}
	
	@Override
	public boolean setItemMeta(ItemMeta meta) {
		return true;
	}
	
	public String serializeJSON() {
		try {
            int id = Item.getIdFromItem(stack.getItem());
            int du = stack.getItemDamage();
            return MarketCompanion.gson.toJson(new SerializedStack(id, du, Base64.encodeBase64String(CompressedStreamTools.compress(stack.getTagCompound()))));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getItemName() {
		return stack.getDisplayName();
	}
	
	public static WrappedItemStack unserializeJSON(String json) {
        try {
            SerializedStack s = MarketCompanion.gson.fromJson(json, SerializedStack.class);
            ItemStack st = new ItemStack(Item.getItemById(s.id));
            st.setItemDamage(s.du);
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.decodeBase64(s.nbt));
            st.setTagCompound(CompressedStreamTools.readCompressed(in));
            in.close();
            return new WrappedItemStack(st);
        } catch(Exception e) {
            e.printStackTrace();
        }
		return null;
	}

    public class SerializedStack {

        public int id;
        public int du;
        public String nbt;

        public SerializedStack(int id,int du, String nbt) {
            this.id = id;
            this.du = du;
            this.nbt = nbt;
        }
    }

	public class WrappedItemMeta implements org.bukkit.inventory.meta.ItemMeta {

		private ItemStack stack;
		
		public WrappedItemMeta(ItemStack stack) {
			this.stack = stack;
		}
		
		@Override
		public Map<String, Object> serialize() {
			return null;
		}

		@Override
		public boolean addEnchant(Enchantment arg0, int arg1, boolean arg2) {
			return false;
		}

		@Override
		public String getDisplayName() {
			return stack.getDisplayName() == null ? "" : stack.getDisplayName();
		}

		@Override
		public int getEnchantLevel(Enchantment arg0) {
			return 0;
		}

		@Override
		public Map<Enchantment, Integer> getEnchants() {
			return new HashMap<Enchantment, Integer>();
		}

		@Override
		public List<String> getLore() {
			List<String> lore = new ArrayList<String>();
			NBTTagCompound comp = stack.getTagCompound();
			if (comp != null) {
				if (comp.hasKey("display")) {
					NBTTagCompound disp = comp.getCompoundTag("display");
					if (disp.hasKey("Lore")) {
						ImmutableList.Builder<String> loreBuilder = ImmutableList.builder();
						NBTTagList loreNbt = (NBTTagList) disp.getTag("Lore");
						for (int i = 0; i < loreNbt.tagCount(); i++) {
							String str = loreNbt.getStringTagAt(i);
							if (str != null) {
								loreBuilder.add(str);
							}
						}
						lore.addAll(loreBuilder.build());
					}
				}
			}
			return lore;
		}

		@Override
		public boolean hasConflictingEnchant(Enchantment arg0) {
			return false;
		}

		@Override
		public boolean hasDisplayName() {
			return stack.getDisplayName() != null;
		}

		@Override
		public boolean hasEnchant(Enchantment arg0) {
			return false;
		}

		@Override
		public boolean hasEnchants() {
			return false;
		}

		@Override
		public boolean hasLore() {
			NBTTagCompound comp = stack.getTagCompound();
			if (comp != null) {
				if (comp.hasKey("display")) {
					NBTTagCompound disp = comp.getCompoundTag("display");
					if (disp.hasKey("Lore")) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean removeEnchant(Enchantment arg0) {
			return false;
		}

		@Override
		public void setDisplayName(String arg0) {
			NBTTagCompound comp = stack.getTagCompound();
			NBTTagCompound disp = null;
			if (comp == null) {
				comp = new NBTTagCompound();
				stack.setTagCompound(comp);
			}
			if (!comp.hasKey("display")) {
				disp = new NBTTagCompound();
			} else {
				disp = comp.getCompoundTag("display");
			}
			disp.setTag("Name", new NBTTagString(arg0));
			comp.setTag("display", disp);
		}

		@Override
		public void setLore(List<String> arg0) {
			NBTTagCompound comp = stack.getTagCompound();
			NBTTagCompound disp = null;
			if (comp == null) {
				comp = new NBTTagCompound();
			}
			if (!comp.hasKey("display")) {
				disp = new NBTTagCompound();
			} else {
				disp = comp.getCompoundTag("display");
			}
			NBTTagList lore = new NBTTagList();
			for (String str : arg0) {
				if (str == null || str.length() == 0) {
					continue;
				}
				lore.appendTag(new NBTTagString(str));
			}
			disp.setTag("Lore", lore);
			comp.setTag("display", disp);
			stack.setTagCompound(comp);
		}
		
		@Override
		public WrappedItemMeta clone() {
			return this;
		}
	}
}
