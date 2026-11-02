package com.innerdimension.core;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Proxy;
import java.util.function.Function;

/** Bridges package-private factory interfaces while preserving their default methods. */
public final class InterfaceFactoryBridge {
    private InterfaceFactoryBridge() {}

    public static Object create(Class<?> factoryType, Function<Object[], Object> factory) {
        return Proxy.newProxyInstance(factoryType.getClassLoader(), new Class<?>[]{factoryType},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "equals" -> proxy == args[0];
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "toString" -> "Inner Dimension factory: " + factoryType.getName();
                            default -> throw new UnsupportedOperationException(method.toString());
                        };
                    }
                    if (method.isDefault()) {
                        // InvocationHandler.invokeDefault cannot access Minecraft's package-private
                        // ScreenConstructor. A private lookup works in its unnamed Java module.
                        return MethodHandles.privateLookupIn(method.getDeclaringClass(), MethodHandles.lookup())
                                .unreflectSpecial(method, method.getDeclaringClass())
                                .bindTo(proxy).invokeWithArguments(args == null ? new Object[0] : args);
                    }
                    if (method.getName().equals("create")) return factory.apply(args);
                    throw new UnsupportedOperationException("Unsupported factory method: " + method);
                });
    }
}
