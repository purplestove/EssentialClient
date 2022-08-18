package me.senseiwells.essentialclient.utils.clientscript.impl;

import me.senseiwells.arucas.exceptions.RuntimeError;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface ScriptMaterial {
	default Identifier getId() {
		return Registry.ITEM.getId(this.asItem());
	}

	default String getTranslationKey() {
		return this.asItem().getTranslationKey();
	}

	default Item asItem() {
		Item item = Item.BLOCK_ITEMS.get(this.asBlock());
		if (item != null) {
			return item;
		}
		throw new RuntimeError("Material cannot be converted to an item");
	}

	default ItemStack asItemStack() {
		return this.asItem().getDefaultStack();
	}

	default Block asBlock() {
		Item item = this.asItem();
		if (item instanceof BlockItem blockItem) {
			return blockItem.getBlock();
		}
		throw new RuntimeError("Material cannot be converted into a block");
	}

	default BlockState asBlockState() {
		return this.asBlock().getDefaultState();
	}

	record ItemMaterial(Item item) implements ScriptMaterial {
		@Override
		public Item asItem() {
			return this.item;
		}
	}

	record BlockMaterial(Block block) implements ScriptMaterial {
		@Override
		public Identifier getId() {
			return Registry.BLOCK.getId(this.block);
		}

		@Override
		public String getTranslationKey() {
			return this.block.getTranslationKey();
		}

		@Override
		public Block asBlock() {
			return this.block;
		}
	}
}