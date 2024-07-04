package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;
import net.md_5.bungee.api.chat.BaseComponent;

@NMS("PacketPlayOutChat")
public interface PacketPlayOutChat {

	
	@NMSConstructor
	public void constructor(@NMS("IChatBaseComponent") Object baseComponent, byte b0);
	
	@NMS(callType = CallType.FIELD)
	public void components(BaseComponent[] components);
	
	@NMS(callType = CallType.FIELD)
	public BaseComponent[] components();
	
	public Object getHandle();
}
