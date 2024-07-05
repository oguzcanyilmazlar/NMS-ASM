package me.acablade.nmsasm.testplugin.nms;

import me.acablade.nmsasm.NMS;

@NMS
public interface CraftPlayer {
	
	@NMS("getHandle")
	public EntityPlayer getHandle();
	
}
