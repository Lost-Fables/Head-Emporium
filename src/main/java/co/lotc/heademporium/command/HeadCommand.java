package co.lotc.heademporium.command;

import co.lotc.core.bukkit.menu.MenuAction;
import co.lotc.core.bukkit.menu.MenuAgent;
import co.lotc.core.bukkit.menu.icon.Button;
import co.lotc.core.bukkit.menu.icon.Icon;
import co.lotc.core.command.annotate.Arg;
import co.lotc.core.command.annotate.Cmd;
import co.lotc.core.command.annotate.Default;
import co.lotc.heademporium.HeadEmporium;
import co.lotc.heademporium.shop.ShopCategory;
import org.apache.commons.lang.WordUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class HeadCommand extends BaseCommand {

	@Cmd(value="Add a head texture or the currently held texture to a category.")
	public void add(CommandSender sender,
					@Arg(value="Category Name", description="Name of the category to retexture.")ShopCategory category,
					@Arg(value="Name of the head")String name,
					@Arg(value="Price", description="Price of the head rounded to two decimal places.")float price,
					@Arg(value="Base64 String", description="The value code for a head texture.")@Default(value="")String texture) {
		if (texture.equalsIgnoreCase("")) {
			texture = null;
		}

		String newTexture = texture;
		if (newTexture == null) {
			if (sender instanceof Player) {
				newTexture = HeadEmporium.getTextureFromPlayer((Player) sender);
				if (newTexture == null) {
					msg(HeadEmporium.PREFIX + "Please specify a texture value or hold a head in your hand.");
					return;
				}
			} else {
				msg(HeadEmporium.ALT_COLOR + "Please specify a texture value.");
				return;
			}
		}

		try {
			String newName = name.replaceAll("_", " ");
			Icon icon = HeadEmporium.getHeadShop().getAsShopIcon(newTexture, WordUtils.capitalizeFully(newName), price);
			HeadEmporium.getShopDb().setToken(-1, category.getName(), WordUtils.capitalizeFully(newName), newTexture, price);
			category.addHead(icon);
			msg(HeadEmporium.PREFIX + "Successfully added new head to the " + HeadEmporium.ALT_COLOR + category.getName() + HeadEmporium.PREFIX + " category.");
		} catch (Exception e) {
			msg(HeadEmporium.PREFIX + "Error adding head.");
			if (HeadEmporium.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	@Cmd(value="Delete a head by it's texture, or by holding a copy, within a category.")
	public void delete(CommandSender sender,
					   @Arg(value="Category Name", description="Name of the category to retexture.")ShopCategory category,
					   @Arg(value="Base64 String", description="The value code for a head texture.")@Default(value="")String texture) {
		if (texture.equalsIgnoreCase("")) {
			texture = null;
		}

		String newTexture = texture;
		if (newTexture == null) {
			if (sender instanceof Player) {
				newTexture = HeadEmporium.getTextureFromPlayer((Player) sender);
				if (newTexture == null) {
					msg(HeadEmporium.PREFIX + "Please specify a texture value or hold a head in your hand.");
					return;
				}
			} else {
				msg(HeadEmporium.ALT_COLOR + "Please specify a texture value.");
				return;
			}
		}

		try {
			ArrayList<Integer> ids = HeadEmporium.getShopDb().getTokenIDsByTexture(newTexture);
			for (int i : ids) {
				if (HeadEmporium.getShopDb().getToken(Integer.toString(i), "CATEGORY").equalsIgnoreCase(category.getName())) {
					HeadEmporium.getShopDb().removeToken(i);
				}
			}
			HeadEmporium.getHeadShop().refreshShop();
		} catch (Exception e) {
			msg(HeadEmporium.PREFIX + "Error adding head.");
			if (HeadEmporium.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

	@Cmd(value="Delete a head by it's texture, or by holding a copy.")
	public void fulldelete(CommandSender sender,
					   @Arg(value="Base64 String", description="The value code for a head texture.")@Default(value="")String texture) {
		if (texture.equalsIgnoreCase("")) {
			texture = null;
		}

		String newTexture = texture;
		if (newTexture == null) {
			if (sender instanceof Player) {
				newTexture = HeadEmporium.getTextureFromPlayer((Player) sender);
				if (newTexture == null) {
					msg(HeadEmporium.PREFIX + "Please specify a texture value or hold a head in your hand.");
					return;
				}
			} else {
				msg(HeadEmporium.ALT_COLOR + "Please specify a texture value.");
				return;
			}
		}

		try {
			ArrayList<Integer> ids = HeadEmporium.getShopDb().getTokenIDsByTexture(newTexture);
			for (int i : ids) {
				HeadEmporium.getShopDb().removeToken(i);
			}
			HeadEmporium.getHeadShop().refreshShop();
		} catch (Exception e) {
			msg(HeadEmporium.PREFIX + "Error adding head.");
			if (HeadEmporium.DEBUGGING) {
				e.printStackTrace();
			}
		}
	}

}
