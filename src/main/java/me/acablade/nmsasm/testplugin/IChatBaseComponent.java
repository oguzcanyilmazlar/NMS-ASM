package me.acablade.nmsasm.testplugin;

import me.acablade.nmsasm.CallType;
import me.acablade.nmsasm.NMS;
import me.acablade.nmsasm.NMSConstructor;

@NMS("IChatBaseComponent$ChatSerializer")
public interface IChatBaseComponent {
	
	
	@NMSConstructor
	public void construct();
	
	@NMS(value = "a", callType = CallType.STATIC_METHOD)
	public Object a(String msg);

}
