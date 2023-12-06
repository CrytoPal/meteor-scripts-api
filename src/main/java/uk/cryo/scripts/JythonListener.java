package uk.cryo.scripts;

import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.listeners.IListener;
import org.python.util.PythonInterpreter;

public class JythonListener<T> implements IListener {
    private final Class<T> target;
    private final int priority;
    private final PythonInterpreter interpreter;
    private final String function;

    public JythonListener(Class<T> target, int priority, PythonInterpreter interpreter, String function, boolean takesEventParam) {
        this.target = target;
        this.priority = priority;
        this.interpreter = interpreter;
        this.function = takesEventParam ? function + "(event)" : function + "()";
    }

    public JythonListener(Class<T> target, PythonInterpreter interpreter, String function, boolean takesEventParam) {
        this(target, EventPriority.MEDIUM, interpreter, function, takesEventParam);
    }

    @Override
    public void call(Object event) {
        try {
            interpreter.set("event", event);
            interpreter.exec(function);
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
