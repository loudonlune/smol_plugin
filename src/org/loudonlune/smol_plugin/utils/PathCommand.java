package org.loudonlune.smol_plugin.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.loudonlune.smol_plugin.SmolPlugin;

public class PathCommand extends SmolCommand {

	public PathCommand(SmolPlugin parent) {
		super(parent);
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
			@NotNull String[] args) {
		
		if (sender.isOp()) {
			PathModule pm = (PathModule) parent.getModuleManager().getModule("paths");
			
			Boolean toSet = !pm.isDisabled();
			if (args.length > 0) 					
				toSet = Boolean.parseBoolean(args[0]);
			
			pm.setDisabled(toSet.booleanValue());
		}
		
		return true;
	}
	
	

}
