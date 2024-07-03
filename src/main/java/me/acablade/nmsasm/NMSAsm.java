package me.acablade.nmsasm;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

public class NMSAsm {
	
	private static Map<Class<?>, Class<?>> registered = new HashMap<>();
	private static Map<Class<?>, Class<?>> normalToNMS = new HashMap<>();
	private static Map<String, Class<?>> simpleToNMSClass = new HashMap<>();
	
	private static ScanResult scanResult = null;
	
	public static Class<?> registerNMSClass(Class<?> clazz) {
		if(scanResult == null) {
			scanResult = new ClassGraph().enableAllInfo().acceptPackages("net.minecraft", "org.bukkit.craftbukkit").scan();
		}
		if(registered.containsKey(clazz)) throw new NMSException(String.format("%s is already registered", clazz.getCanonicalName()));
		if(!clazz.isInterface()) throw new NMSException(String.format("Class(%s) should be specified as an interface", clazz.getCanonicalName()));
		if(!clazz.isAnnotationPresent(NMS.class)) throw new NMSException(String.format("Class(%s) does not have NMS annotation", clazz.getCanonicalName()));

		String clazzName = clazz.getAnnotation(NMS.class).value();
		
		if(clazzName.isEmpty()) {
			clazzName = clazz.getSimpleName();
			
		}
		
		Class<?> wantedClass = getClass(clazzName);
		System.out.println(wantedClass.getCanonicalName());
		
		ClassWriter writer = GeneratorAdapter.newClassWriter(clazz.getName() + "$NMSImpl", Type.getInternalName(clazz));
		GeneratorAdapter.addField(Opcodes.ACC_PUBLIC, "handle", "Ljava/lang/Object;", null, null, writer);

		
		GeneratorAdapter.writeConstructor(clazz, writer);
		GeneratorAdapter.writeEmptyConstructor(clazz, writer);
		for(Method method: clazz.getMethods()) {
			if(method.isAnnotationPresent(NMSConstructor.class)) {
				try {
					
//					methodVisitor.visitVarInsn(ALOAD, 0);
//					methodVisitor.visitTypeInsn(NEW, "me/acablade/nmsasm/NMSAsm$Test");
//					methodVisitor.visitInsn(DUP);
//					methodVisitor.visitVarInsn(ALOAD, 1);
//					methodVisitor.visitVarInsn(ALOAD, 2);
//					methodVisitor.visitMethodInsn(INVOKESPECIAL, "me/acablade/nmsasm/NMSAsm$Test", "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);
//					methodVisitor.visitFieldInsn(PUTFIELD, "me/acablade/nmsasm/NMSAsm", "handle", "Ljava/lang/Object;");
					
					Constructor<?> constructor = wantedClass.getConstructor(getParameters(clazz, method).toArray(new Class<?>[0]));
					GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), getMethodDescriptor(method));
			        adapter.loadThis();
			        adapter.visitTypeInsn(Opcodes.NEW, Type.getInternalName(wantedClass));
			        adapter.dup();
			        int index = adapter.getArgIndex(0);
			        for (int i = 0; i < adapter.argumentTypes.length; ++i) {
			            Type argumentType = adapter.argumentTypes[i];
			            Parameter param = constructor.getParameters()[i];
//			            Class<?> paramType = normalToNMS.containsKey(param.getType()) ? normalToNMS.get(param.getType()) : param.getType();
			            adapter.loadInsn(argumentType, index);
			            if(!Type.getType(param.getType()).equals(argumentType)) {
			            	adapter.checkCast(Type.getType(param.getType()));
			            }
//			            Class<?> typeclz = constructor.getParameterTypes()[];
//			            if(!typeclz.isPrimitive() && !typeclz.getCanonicalName().contains("java.lang"))
//			            	adapter.checkCast(Type.getType(typeclz));
			            index += argumentType.getSize();
			        }
//			        adapter.loadArgs();
			        adapter.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(wantedClass), "<init>", Type.getConstructorDescriptor(constructor), false);
			        adapter.putField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", GeneratorAdapter.OBJECT_TYPE);
			        adapter.returnValue();
			        adapter.endMethod();
			        
			        
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			
			if(!method.isAnnotationPresent(NMS.class)) {
				
				if(method.getName().equals("getHandle") && method.getReturnType().equals(Object.class)) {
					GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, "getHandle", Type.getMethodDescriptor(method));
					adapter.loadThis();
					adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
					adapter.returnValue();
					adapter.endMethod();
				}
				
				continue;
			}
			CallType callType = method.getAnnotation(NMS.class).callType();
		
					
			if(callType == CallType.METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String methodName = method.getAnnotation(NMS.class).value();
				List<Class<?>> paramList = getParameters(clazz, method);
				try {
					
					String interfaceClass = method.getAnnotation(NMS.class).interfaceName();
					Class<?> interfaceClazz = null;
					Method declared = null;
					if(!interfaceClass.isEmpty()) {
						interfaceClazz = getClass(interfaceClass);
						declared = interfaceClazz.getDeclaredMethod(methodName, paramList.toArray(new Class<?>[0]));
					}else {
						declared = wantedClass.getDeclaredMethod(methodName, paramList.toArray(new Class<?>[0]));
					}
					adapter.loadThis();
					adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
					adapter.checkCast(Type.getType(wantedClass));
					adapter.loadArgs();
//					int i = 0;
					
//					for(Parameter param: method.getParameters()) {
//						adapter.loadArg(i);
////						adapter.visitVarInsn(Type.getType(param.getType()).getOpcode(Opcodes.IALOAD), i);
//						i++;
//					}
//					adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(wantedClass), methodName, Type.getMethodDescriptor(declared), false);
					adapter.invokeVirtual(Type.getType(wantedClass), me.acablade.nmsasm.Method.getMethod(declared));
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					throw new NMSException(String.format("(%s) Cant find the wanted method: %s", method.getName(), methodName));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
			} else if(callType == CallType.STATIC_METHOD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String methodName = method.getAnnotation(NMS.class).value();
				List<Class<?>> paramList = getParameters(clazz, method);
				try {
					Method declared = wantedClass.getDeclaredMethod(methodName, paramList.toArray(new Class<?>[0]));
					adapter.loadArg(0);
					adapter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(wantedClass), methodName, Type.getMethodDescriptor(declared), false);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					throw new NMSException(String.format("(%s) Cant find the wanted method: %s", method.getName(), methodName));
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
			} else if(callType == CallType.FIELD) {
				GeneratorAdapter adapter = GeneratorAdapter.newMethodGenerator(writer, method.getName(), Type.getMethodDescriptor(method));
				String fieldName = method.getAnnotation(NMS.class).value();
//				methodVisitor.visitVarInsn(ALOAD, 0);
//				methodVisitor.visitFieldInsn(GETFIELD, "me/acablade/bladeapi/events/GameEvent", "game", "Lme/acablade/bladeapi/IGame;");
//				methodVisitor.visitInsn(ARETURN);
				
				if(method.getReturnType() == Void.TYPE) {
					// SETTER
					
					try {
						String interfaceClass = method.getAnnotation(NMS.class).interfaceName();
						Class<?> field = null;
						if(!interfaceClass.isEmpty()) {
							Class<?> interfaceClazz = getClass(interfaceClass);
							field = interfaceClazz.getField(fieldName).getType();
						}else {
							field = wantedClass.getField(fieldName).getType();
						}
						adapter.loadThis();
						adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
						adapter.checkCast(Type.getType(wantedClass));
						adapter.loadArg(0);
						adapter.visitFieldInsn(Opcodes.PUTFIELD, Type.getInternalName(wantedClass), fieldName, Type.getDescriptor(field));
					} catch (NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					
				}else {
					// GETTER
					try {
						Class<?> field = wantedClass.getField(fieldName).getType();
						adapter.loadThis();
						adapter.getField(Type.getObjectType(Type.getInternalName(clazz) + "$NMSImpl"), "handle", Type.getObjectType("java/lang/Object"));
						adapter.checkCast(Type.getType(wantedClass));
						adapter.getField(Type.getType(wantedClass), fieldName, Type.getType(field));
					} catch (NoSuchFieldException | SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				adapter.returnValue();
				adapter.visitMaxs(20, 20);
				adapter.endMethod();
				
				
				
			}
			
						
			
		}
		
		writer.visitEnd();
		
		Class clz = GeneratedClassDefiner.INSTANCE
				.define(clazz.getClassLoader(), clazz.getName() + "$NMSImpl", writer.toByteArray());
		registered.put(clazz, clz);
		normalToNMS.put(clazz, wantedClass);
		
		return clz;
		
		
	}
	
	private static List<String> getClassesFromClassGraph(String name){
		return scanResult.getAllClassesAsMap().keySet().stream().filter(s -> s.endsWith(name)).collect(Collectors.toList());
	}
	
	private static Class<?> getClass(String name){
		return simpleToNMSClass.computeIfAbsent(name, (t) -> {
			List<String> classes = getClassesFromClassGraph(t);
			if(classes.size() != 1) throw new NMSException(String.format("%s found two of classes that have the same name, please use full name instead", t));
			try {
				return Class.forName(classes.get(0));
			} catch (ClassNotFoundException e) {
				// unreachable
				throw new NMSException(String.format("%s class not found", classes.get(0)));
			}
		});
	}
	
	public static <T> T get(Class<T> clazz, Object... objects) {
		
		Class<T> clz = (Class<T>) registered.get(clazz);
		
		for(Constructor constructor : clz.getConstructors()) {
			if(constructor.getParameterCount() == objects.length)
				try {
					return (T) constructor.newInstance(objects);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
						| InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		throw new NMSException(String.format("Constructor not found (%s)", clazz.getCanonicalName()));
	}
	
	
	private static List<Class<?>> getParameters(Class clazz, Method method){
		List<Class<?>> paramList = new ArrayList<>();
		for(Parameter param: method.getParameters()) {
			if(!param.isAnnotationPresent(NMS.class)) {
				if(normalToNMS.containsKey(param.getType())) {
					paramList.add(normalToNMS.get(param.getType()));
				}else {
					paramList.add(param.getType());
				}
				
				continue;
			}
			String paramClass = param.getAnnotation(NMS.class).value();
			paramList.add(getClass(paramClass));
			
		}
		return paramList;
	}
	
	public static String getMethodDescriptor(final Method method) {
	    StringBuilder stringBuilder = new StringBuilder();
	    stringBuilder.append('(');
	    Parameter[] parameters = method.getParameters();
	    for (Parameter parameter : parameters) {
	    	if(!parameter.isAnnotationPresent(NMS.class)) {
	    		if(normalToNMS.containsKey(parameter.getType())) {
	    			appendDescriptor(normalToNMS.get(parameter.getType()), stringBuilder);
				}else {
					appendDescriptor(parameter.getType(), stringBuilder);
				}
			}else {
				stringBuilder.append('L').append(Type.getInternalName(parameter.getType())).append(';');
			}
	      
	    }
	    stringBuilder.append(')');
	    appendDescriptor(method.getReturnType(), stringBuilder);
	    return stringBuilder.toString();
	  }
	
	
	private static void appendDescriptor(final Class<?> clazz, final StringBuilder stringBuilder) {
	    Class<?> currentClass = clazz;
	    while (currentClass.isArray()) {
	      stringBuilder.append('[');
	      currentClass = currentClass.getComponentType();
	    }
	    if (currentClass.isPrimitive()) {
	      char descriptor;
	      if (currentClass == Integer.TYPE) {
	        descriptor = 'I';
	      } else if (currentClass == Void.TYPE) {
	        descriptor = 'V';
	      } else if (currentClass == Boolean.TYPE) {
	        descriptor = 'Z';
	      } else if (currentClass == Byte.TYPE) {
	        descriptor = 'B';
	      } else if (currentClass == Character.TYPE) {
	        descriptor = 'C';
	      } else if (currentClass == Short.TYPE) {
	        descriptor = 'S';
	      } else if (currentClass == Double.TYPE) {
	        descriptor = 'D';
	      } else if (currentClass == Float.TYPE) {
	        descriptor = 'F';
	      } else if (currentClass == Long.TYPE) {
	        descriptor = 'J';
	      } else {
	        throw new AssertionError();
	      }
	      stringBuilder.append(descriptor);
	    } else {
	      stringBuilder.append('L').append(Type.getInternalName(currentClass)).append(';');
	    }
	  }

}
