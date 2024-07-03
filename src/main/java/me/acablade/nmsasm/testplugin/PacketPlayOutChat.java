package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;
import net.md_5.bungee.api.chat.BaseComponent;

@NMS("net.minecraft.server.v1_8_R3.PacketPlayOutChat")
public interface PacketPlayOutChat {

	
	@NMSConstructor
	public void constructor(@NMS("net.minecraft.server.v1_8_R3.IChatBaseComponent") Object baseComponent, byte b0);
	
	@NMS(callType = CallType.FIELD, value = "components")
	public void components(BaseComponent[] components);
	
	@NMS(callType = CallType.FIELD, value = "components")
	public BaseComponent[] components();
	
	public Object getHandle();
}
