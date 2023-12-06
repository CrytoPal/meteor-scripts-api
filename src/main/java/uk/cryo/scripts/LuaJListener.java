package uk.cryo.scripts;

import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.listeners.IListener;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.python.util.PythonInterpreter;

public class LuaJListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final LuaValue function;
    private final boolean takesEventParam;

    public LuaJListener(Class<T> target, int priority, LuaValue function, boolean takesEventParam) {
        System.out.println(target);
        this.target = target;
        this.priority = priority;
        this.function = function;
        this.takesEventParam = takesEventParam;
    }

    public LuaJListener(Class<T> target, LuaValue function, boolean takesEventParam) {
        this(target, EventPriority.MEDIUM, function, takesEventParam);
    }

    @Override
    public void call(Object event) {
        System.out.println("luaj events are being called "+event.toString());
        try {
            if (!function.isfunction()) return;
            if (takesEventParam) function.call(CoerceJavaToLua.coerce(event));
            else function.call();
        } catch (Exception e) {
            e.printStackTrace();
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

