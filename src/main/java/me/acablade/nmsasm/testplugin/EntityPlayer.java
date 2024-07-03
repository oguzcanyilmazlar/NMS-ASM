package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;

@NMS("net.minecraft.server.v1_8_R3.EntityPlayer")
public interface EntityPlayer {
	
	@NMS(callType = CallType.FIELD, value = "playerConnection")
	public Object playerConnection();
	
	@NMS("nextContainerCounter")
	public int containerCounter();
	
}
