[![](https://jitpack.io/v/oguzcanyilmazlar/NMS-ASM.svg)](https://jitpack.io/#oguzcanyilmazlar/NMS-ASM)

made for fun

grabbed main classes from Revxrsal/EventBus (GeneratedClassDefiner, GeneratorAdapter, Method)

spaghetti code, i know.

accessing entity player example:

<h5 a><strong><code>CraftPlayer.java</code></strong></h5>

``` java
import me.acablade.nmsasm.NMS;

@NMS("org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer")
public interface CraftPlayer {
	
	@NMS("getHandle")
	public Object getHandle();
	
	@NMS("sendMessage")
	public void sendMessage(String message);

}

```

And then you use it as:

``` java
CraftPlayer craftPlayer = NMSAsm.get(CraftPlayer.class, player);
Object entityPlayer = craftPlayer.getHandle();
craftPlayer.sendMessage("Hi there!!");
```

Dont forget to register the classes such as

``` java
NMSAsm.registerNMSClass(CraftPlayer.class);
```
in onEnable.

See more examples in me/acablade/nmsasm/testplugin