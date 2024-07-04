package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("EntityCreeper")
public interface EntityCreeper {

	@NMSConstructor
	public void constructor(@NMS("net.minecraft.server.v1_8_R3.World") Object world);
	
	@NMS(value = "setPosition", interfaceClass = Entity.class)
	public void pos(double x, double y, double z);
	
	@NMS(value = "getId", interfaceClass = Entity.class)
	public int entityId();
	
	@NMS(value = "aK", interfaceName = "nms.EntityLiving", callType = CallType.FIELD)
	public void headRotation(float yaw);
	
	@NMS(value = "yaw", interfaceClass = Entity.class, callType = CallType.FIELD)
	public void yaw(float yaw);
	
	@NMS(value = "pitch", interfaceClass = Entity.class, callType = CallType.FIELD)
	public void pitch(float pitch);
	
	public Object getHandle();
	
}
