package org.loudonlune.smol_plugin.general;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.loudonlune.smol_plugin.SmolPlugin;
import org.loudonlune.smol_plugin.utils.SmolCommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class ListEntityTypes extends SmolCommand {

	public ListEntityTypes(SmolPlugin parent) {
		super(parent);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		
		int page_num = 1;
		if (args.length > 0) {
			try {
				page_num = Integer.parseInt(args[0]);
			} catch (Exception e) {}
		}
		
		EntityType[] vs = EntityType.values();
		
		int start = page_num * 15;
		int end = (page_num + 1) * 15;
		sender.sendMessage(
				Component.text("List of EntityTypes (Page " + (page_num) + " of " + vs.length + "): ")
				.color(TextColor.color(0x690096))
			);
		
		for (int i = start; i < end; i++) {
			sender.sendMessage(
					Component.text(i + ") " + vs[i].toString())
					.color(TextColor.color(0x690096)));
		}
		
		return true;
	}
	
	

}
