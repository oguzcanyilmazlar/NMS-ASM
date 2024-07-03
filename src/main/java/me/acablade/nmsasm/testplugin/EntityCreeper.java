package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.EntityCreeper")
public interface EntityCreeper {

	@NMSConstructor
	public void constructor(@NMS("net.minecraft.server.v1_8_R3.World") Object world);
	
	@NMS(value = "setPosition", interfaceName = "net.minecraft.server.v1_8_R3.Entity")
	public void pos(double x, double y, double z);
	
	@NMS(value = "getId", interfaceName = "net.minecraft.server.v1_8_R3.Entity")
	public int entityId();
	
	@NMS(value = "aK", interfaceName = "net.minecraft.server.v1_8_R3.EntityLiving", callType = CallType.FIELD)
	public void headRotation(float yaw);
	
	@NMS(value = "yaw", interfaceName = "net.minecraft.server.v1_8_R3.Entity", callType = CallType.FIELD)
	public void yaw(float yaw);
	
	@NMS(value = "pitch", interfaceName = "net.minecraft.server.v1_8_R3.Entity", callType = CallType.FIELD)
	public void pitch(float pitch);
	
	public Object getHandle();
	
}
