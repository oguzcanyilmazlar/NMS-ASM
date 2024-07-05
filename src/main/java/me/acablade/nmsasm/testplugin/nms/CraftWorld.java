package me.acablade.nmsasm.testplugin.nms;

import me.acablade.nmsasm.NMS;

@NMS("CraftWorld")
public interface CraftWorld {
	
	@NMS("getHandle")
	public World getHandle();
	
}
