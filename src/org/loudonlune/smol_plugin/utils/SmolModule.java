package org.loudonlune.smol_plugin.utils;

import org.loudonlune.smol_plugin.SmolPlugin;

public abstract class SmolModule {
	protected SmolPlugin parent;
	
	public SmolModule(SmolPlugin parent) {
		this.parent = parent;
	}
	
	public SmolPlugin getParent() {
		return parent;
	}
	
	public abstract SmolModule register(String registeredName);
}
