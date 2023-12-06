package uk.cryo.scripts;

import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.listeners.IListener;
import org.python.util.PythonInterpreter;
import meteordevelopment.meteorclient.systems.modules.Module;

public class JythonListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final PythonInterpreter interpreter;
    private final String function;

    private final Module module;

    public JythonListener(Module module, Class<T> target, int priority, PythonInterpreter interpreter, String function, boolean takesEventParam) {
        this.target = target;
        this.priority = priority;
        this.interpreter = interpreter;
        this.function = takesEventParam ? function + "(event)" : function + "()";
        this.module = module;
    }

    public JythonListener(Module module, Class<T> target, PythonInterpreter interpreter, String function, boolean takesEventParam) {
        this(module, target, EventPriority.MEDIUM, interpreter, function, takesEventParam);
    }

    @Override
    public void call(Object event) {
        if (module.isActive()) {
            try {
                interpreter.set("event", event);
                interpreter.exec(function);
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
