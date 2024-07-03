package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.NMS;

@NMS("CraftLivingEntity")
public interface CraftLivingEntity {
	
	@NMS("getHandle")
	public Object getHandle();

}
