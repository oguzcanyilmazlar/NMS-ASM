package me.acablade.nmsasm.testplugin.nms;

import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS
public interface PacketPlayOutSpawnEntityLiving {
	
	@NMSConstructor
	public void create(@NMS("nms.EntityLiving") Object entity);
	
	public Object getHandle();
	
}
