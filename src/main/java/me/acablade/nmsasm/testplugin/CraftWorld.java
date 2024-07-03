package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS("org.bukkit.craftbukkit.v1_8_R3.CraftWorld")
public interface CraftWorld {
	
	@NMS("getHandle")
	public Object getHandle();
	
}
