package uk.cryo.scripts;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PyType;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import uk.cryo.scripts.utils.Mappings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@SuppressWarnings("CallToPrintStackTrace")
public class API extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category Scripts = new Category("Scripts");

    public static final File SCRIPTS_FOLDER = new File(MeteorClient.FOLDER, "scripts");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Scripts");
        try { Mappings.addMappings(); } catch (IOException ignored) {}
        LOG.info("Initializing Script Mappings");

        if (!SCRIPTS_FOLDER.exists()) SCRIPTS_FOLDER.mkdir();

        for (File s : SCRIPTS_FOLDER.listFiles()) {
            if (s.isFile()) {
                LOG.info("Loading " + s.getName() + " script");
                if (s.getName().contains(".py")) {
                    PythonInterpreter translationPython = new PythonInterpreter();
                    translationPython.exec("""
                            class EventHandler:
                                def __init__(self, Event):
                                    self.event = Event
                                def __call__(self, func):
                                    def wrapped_func(*args, **kwargs):
                                        result = func(*args, **kwargs)
                                        return result
                                    wrapped_func.__wrapped__ = {}
                                    wrapped_func.__wrapped__["class"] = self
                                    wrapped_func.__wrapped__["event"] = self.event
                                    wrapped_func.__wrapped__["func"] = func
                                    return wrapped_func""");
                    translationPython.set("mc", mc);
                    translationPython.exec(applyCode(s.getAbsolutePath(), FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named")));

                    Module mod = new Module(Scripts, s.getName().replace(".py", ""), "") {
                        {
                            for (PyObject name : translationPython.getLocals().asIterable()) {
                                PyObject realObject = translationPython.get(name.asString());
                                String classType = realObject.getType().getName();
                                if (classType.equals("function") && realObject.__findattr__("__wrapped__") != null) {
                                    PyObject wrapped = realObject.__getattr__("__wrapped__");
                                    if (wrapped.__finditem__("event") != null && wrapped.__finditem__("class") != null && wrapped.__finditem__("func") != null && super.isActive()) {
                                        PyObject wrapperEvent = wrapped.__getitem__(Py.newString("event"));
                                        PyObject codeObject = wrapped.__getitem__(Py.newString("func")).__getattr__("__code__");
                                        PyObject argNames = codeObject.__getattr__("co_varnames");
                                        PyString[] argNamesArray = (PyString[]) argNames.__tojava__(PyString[].class);
                                        JythonListener<?> listener = new JythonListener<>(this, ((PyType) wrapperEvent).getProxyType(), translationPython, name.asString(), argNamesArray.length >= 1);
                                        MeteorClient.EVENT_BUS.subscribe(listener);
                                    }
                                }
                            }
                        }
                        @Override
                        public void onActivate() {
                            if (translationPython.getLocals().__finditem__("onActivate") != null) {
                                try {translationPython.exec("onActivate()");} catch (Exception ignored) {}
                            }
                        }

                        @Override
                        public void onDeactivate() {
                            if (translationPython.getLocals().__finditem__("onDeactivate") != null) {
                                try {translationPython.exec("onDeactivate()");} catch (Exception ignored) {}
                            }
                        }

                        @Override
                        public boolean isActive() {
                            if (translationPython.getLocals().__finditem__("isActive") != null) {
                                try {
                                    return (boolean) translationPython.eval("isActive()").__tojava__(boolean.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.isActive();
                                }
                            } else return super.isActive();
                        }

                        @Override
                        public WWidget getWidget(GuiTheme theme) {
                            if (translationPython.getLocals().__finditem__("getWidget") != null) {
                                try {
                                    translationPython.set("theme", theme);
                                    return (WWidget) translationPython.eval("getWidget(theme)").__tojava__(WWidget.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.getWidget(theme);
                                }
                            } else return super.getWidget(theme);
                        }

                        @Override
                        public void toggle() {
                            if (translationPython.getLocals().__finditem__("toggle") != null) {
                                try {
                                    translationPython.exec("toggle()");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.toggle();
                                }
                            } else super.toggle();
                        }

                        @Override
                        public void sendToggledMsg() {
                            if (translationPython.getLocals().__finditem__("sendToggledMsg") != null) {
                                try {
                                    translationPython.exec("sendToggledMsg()");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.sendToggledMsg();
                                }
                            } else super.sendToggledMsg();
                        }

                        @Override
                        public void info(Text message) {
                            if (translationPython.getLocals().__finditem__("info") != null) {
                                try {
                                    translationPython.set("message", message);
                                    translationPython.exec("info(message)");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.info(message);
                                }
                            } else super.info(message);
                        }

                        @Override
                        public void warning(String message, Object... args) {
                            if (translationPython.getLocals().__finditem__("warning") != null) {
                                try {
                                    translationPython.set("message", message);
                                    translationPython.set("args", args);
                                    translationPython.exec("warning(message, args)");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.warning(message, args);
                                }
                            } else super.warning(message, args);
                        }

                        @Override
                        public void error(String message, Object... args) {
                            if (translationPython.getLocals().__finditem__("error") != null) {
                                try {
                                    translationPython.set("message", message);
                                    translationPython.set("args", args);
                                    translationPython.exec("error(message, args)");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.error(message, args);
                                }
                            } else super.error(message, args);
                        }

                        @Override
                        public String getInfoString() {
                            if (translationPython.getLocals().__finditem__("getInfoString") != null) {
                                try {
                                    return (String) translationPython.eval("getInfoString()").__tojava__(String.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.getInfoString();
                                }
                            } else return super.getInfoString();
                        }

                        @Override
                        public NbtCompound toTag() {
                            if (translationPython.getLocals().__finditem__("toTag") != null) {
                                try {
                                    return (NbtCompound) translationPython.eval("toTag()").__tojava__(NbtCompound.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.toTag();
                                }
                            } else return super.toTag();
                        }

                        @Override
                        public Module fromTag(NbtCompound tag) {
                            if (translationPython.getLocals().__finditem__("fromTag") != null) {
                                try {
                                    translationPython.set("tag", tag);
                                    return (Module) translationPython.eval("fromTag(tag)").__tojava__(Module.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.fromTag(tag);
                                }
                            } else return super.fromTag(tag);
                        }

                        @Override
                        public boolean equals(Object o) {
                            if (translationPython.getLocals().__finditem__("equals") != null) {
                                try {
                                    translationPython.set("o", o);
                                    return (boolean) translationPython.eval("equals(o)").__tojava__(boolean.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.equals(o);
                                }
                            } else return super.equals(o);
                        }

                        @Override
                        public int hashCode() {
                            if (translationPython.getLocals().__finditem__("hashCode") != null) {
                                try {
                                    return (int) translationPython.eval("hashCode()").__tojava__(int.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.hashCode();
                                }
                            } else return super.hashCode();
                        }

                        @Override
                        public int compareTo(@NotNull Module o) {
                            if (translationPython.getLocals().__finditem__("compareTo") != null) {
                                try {
                                    translationPython.set("o", o);
                                    return (int) translationPython.eval("compareTo(o)").__tojava__(int.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.compareTo(o);
                                }
                            } else return super.compareTo(o);
                        }
                    };
                    translationPython.set("module", mod);
                    Modules.get().add(mod);

                } else if (s.getName().contains(".lua")) {
                    Globals globals = JsePlatform.standardGlobals();
                    globals.set("mc", CoerceJavaToLua.coerce(mc));
                    globals.set("__wrapped_funcs__", new LuaTable());
                    globals.load("""
                        function EventHandler(eventFunction, event)
                            local __wrapped__ = {
                                eventFunction = eventFunction,
                                event = event
                            }
                            table.insert(wrapped_funcs, __wrapped__)
                        end""", "EventHandler").call();
                    Reader reader;
                    try {
                        reader = new BufferedReader(new FileReader(s));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    LuaValue chunk = globals.load(reader, "script");
                    chunk.call();
                    Module mod = new Module(Scripts, s.getName().replace(".lua", ""), "") {
                        {
                            LuaTable wrapped_funcs = (LuaTable) globals.get("__wrapped_funcs__");
                            for (int i = 1; i <= wrapped_funcs.length(); i++) {
                                LuaValue wrapped = wrapped_funcs.get(i);
                                LuaFunction eventFunction = wrapped.get("eventFunction").checkfunction();
                                LuaValue event = wrapped.get("event");
                                LuaJListener<?> listener = new LuaJListener<>(this, (Class<?>) CoerceLuaToJava.coerce(event, Class.class), eventFunction);
                                MeteorClient.EVENT_BUS.subscribe(listener);
                            }
                        }

                        @Override
                        public void onActivate() {
                            LuaValue function = globals.get("onActivate");
                            if (function.isfunction()) {
                                try {
                                    function.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.onActivate();
                                }
                            } else super.onActivate();
                        }

                        @Override
                        public void onDeactivate() {
                            LuaValue function = globals.get("onDeactivate");
                            if (function.isfunction()) {
                                try {
                                    function.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.onDeactivate();
                                }
                            } else super.onDeactivate();
                        }


                        @Override
                        public boolean isActive() {
                            LuaValue function = globals.get("isActive");
                            if (function.isfunction()) {
                                try {
                                    return function.invoke().arg(0).checkboolean();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.isActive();
                                }
                            } else return super.isActive();
                        }

                        @Override
                        public WWidget getWidget(GuiTheme theme) {
                            LuaValue function = globals.get("getWidget");
                            if (function.isfunction()) {
                                try {
                                    return (WWidget) CoerceLuaToJava.coerce(function.invoke(CoerceJavaToLua.coerce(theme)).arg(0), WWidget.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.getWidget(theme);
                                }
                            } else return super.getWidget(theme);
                        }

                        @Override
                        public void toggle() {
                            LuaValue function = globals.get("toggle");
                            if (function.isfunction()) {
                                try {
                                    function.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.toggle();
                                }
                            } else super.toggle();
                        }

                        @Override
                        public void sendToggledMsg() {
                            LuaValue function = globals.get("sendToggledMsg");
                            if (function.isfunction()) {
                                try {
                                    function.call();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.sendToggledMsg();
                                }
                            } else super.sendToggledMsg();
                        }

                        @Override
                        public void info(Text message) {
                            LuaValue function = globals.get("info");
                            if (function.isfunction()) {
                                try {
                                    function.call(CoerceJavaToLua.coerce(message));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.info(message);
                                }
                            } else super.info(message);
                        }

                        @Override
                        public void warning(String message, Object... args) {
                            LuaValue function = globals.get("warning");
                            if (function.isfunction()) {
                                try {
                                    function.call(CoerceJavaToLua.coerce(message), CoerceJavaToLua.coerce(args));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.warning(message, args);
                                }
                            } else super.warning(message, args);
                        }

                        @Override
                        public void error(String message, Object... args) {
                            LuaValue function = globals.get("error");
                            if (function.isfunction()) {
                                try {
                                    function.call(CoerceJavaToLua.coerce(message), CoerceJavaToLua.coerce(args));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    super.error(message, args);
                                }
                            } else super.error(message, args);
                        }

                        @Override
                        public String getInfoString() {
                            LuaValue function = globals.get("getInfoString");
                            if (function.isfunction()) {
                                try {
                                    return (String) CoerceLuaToJava.coerce(function.invoke().arg(0), String.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.getInfoString();
                                }
                            } else return super.getInfoString();
                        }

                        @Override
                        public NbtCompound toTag() {
                            LuaValue function = globals.get("toTag");
                            if (function.isfunction()) {
                                try {
                                    return (NbtCompound) CoerceLuaToJava.coerce(function.invoke().arg(0), NbtCompound.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.toTag();
                                }
                            } else return super.toTag();
                        }

                        @Override
                        public Module fromTag(NbtCompound tag) {
                            LuaValue function = globals.get("fromTag");
                            if (function.isfunction()) {
                                try {
                                    return (Module) CoerceLuaToJava.coerce(function.invoke(CoerceJavaToLua.coerce(tag)).arg(0), Module.class);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.fromTag(tag);
                                }
                            } else return super.fromTag(tag);
                        }

                        @Override
                        public boolean equals(Object o) {
                            LuaValue function = globals.get("equals");
                            if (function.isfunction()) {
                                try {
                                    return function.invoke(CoerceJavaToLua.coerce(o)).arg(0).checkboolean();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.equals(o);
                                }
                            } else return super.equals(o);
                        }

                        @Override
                        public int hashCode() {
                            LuaValue function = globals.get("hashCode");
                            if (function.isfunction()) {
                                try {
                                    return function.invoke().arg(0).checkint();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.hashCode();
                                }
                            } else return super.hashCode();
                        }

                        @Override
                        public int compareTo(@NotNull Module o) {
                            LuaValue function = globals.get("compareTo");
                            if (function.isfunction()) {
                                try {
                                    return function.invoke(CoerceJavaToLua.coerce(o)).arg(0).checkint();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return super.compareTo(o);
                                }
                            } else return super.compareTo(o);
                        }
                    };
                    globals.set("module", CoerceJavaToLua.coerce(mod));
                    Modules.get().add(mod);
                }
            }
        }
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(Scripts);
    }

    @Override
    public String getPackage() {
        return "uk.cryo.scripts";
    }

    public String applyCode(String fileName, boolean devmode) {
        String text = "";
        try {
            text = new String(Files.readAllBytes(Path.of(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String textv2 = text;

        if (!devmode) {
            for (int i = 0; i < Mappings.obfuscatedMap.size(); ) {
                if (Mappings.nonObfuscatedMap.get(i) != null) {
                    textv2 = textv2.replace(Mappings.nonObfuscatedMap.get(i), Mappings.obfuscatedMap.get(i));
                    i += 1;
                }
            }
        }
        return textv2;
    }
}
