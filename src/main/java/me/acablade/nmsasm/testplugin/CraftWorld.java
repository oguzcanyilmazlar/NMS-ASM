package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS("CraftWorld")
public interface CraftWorld {
	
	@NMS("getHandle")
	public Object getHandle();
	
}
