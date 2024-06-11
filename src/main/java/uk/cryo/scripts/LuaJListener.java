package uk.cryo.scripts;

    import meteordevelopment.meteorclient.systems.modules.Module;
    import meteordevelopment.orbit.EventPriority;
    import meteordevelopment.orbit.listeners.IListener;
    import org.luaj.vm2.LuaFunction;
    import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class LuaJListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final LuaFunction function;

    private final Module module;

    public LuaJListener(Module module, Class<T> target, int priority, LuaFunction function) {
        this.target = target;
        this.priority = priority;
        this.function = function;
        this.module = module;
    }

    public LuaJListener(Module module, Class<T> target, LuaFunction function) {
        this(module, target, EventPriority.MEDIUM, function);
    }

    @Override
    public void call(Object event) {
        if (module.isActive()) {
            try {
                this.function.call(CoerceJavaToLua.coerce(event));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Class<T> getTarget() {
        return target;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isStatic() {
        return false;
    }
}
