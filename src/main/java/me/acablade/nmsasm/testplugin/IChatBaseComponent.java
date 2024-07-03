package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("net.minecraft.server.v1_8_R3.IChatBaseComponent$ChatSerializer")
public interface IChatBaseComponent {
	
	
	@NMSConstructor
	public void construct();
	
	@NMS(value = "a", callType = CallType.STATIC_METHOD)
	public Object a(String msg);

}
