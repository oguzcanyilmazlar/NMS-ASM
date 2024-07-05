package me.acablade.nmsasm.testplugin.nms;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;

@NMS("nms.EntityPlayer")
public interface EntityPlayer {
	
	@NMS(callType = CallType.FIELD)
	public PlayerConnection playerConnection();
	
	
}
