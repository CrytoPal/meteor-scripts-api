package uk.cryo.scripts;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.python.core.*;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;
import uk.cryo.scripts.utils.Mappings;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class API extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category Scripts = new Category("Scripts");

    public static final File SCRIPTS_FOLDER = new File(MeteorClient.FOLDER, "scripts");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Scripts");
        Mappings.addMappings();
        LOG.info("Initializing Script Mappings");

        if (!SCRIPTS_FOLDER.exists()) SCRIPTS_FOLDER.mkdir();

        for (File s : SCRIPTS_FOLDER.listFiles()) {
            if (s.isFile()) {
                LOG.info("Loading " + s.getName() + " script");
                if (s.getName().contains(".py")) {
                    PythonInterpreter translationPython = new PythonInterpreter();
                    translationPython.exec("class EventHandler:\n" +
                        "    def __init__(self, Event):\n" +
                        "        self.event = Event\n" +
                        "    def __call__(self, func):\n" +
                        "        def wrapped_func(*args, **kwargs):\n" +
                        "            result = func(*args, **kwargs)\n" +
                        "            return result\n" +
                        "        wrapped_func.__wrapped__ = {}\n" +
                        "        wrapped_func.__wrapped__[\"class\"] = self\n" +
                        "        wrapped_func.__wrapped__[\"event\"] = self.event\n" +
                        "        wrapped_func.__wrapped__[\"func\"] = func\n" +
                        "        return wrapped_func");
                    translationPython.set("mc", mc);
                    translationPython.exec(readFileAsString(s.getAbsolutePath()));

                    Module mod = new Module(Scripts, s.getName().replace(".py", ""), "") {
                        {
                            for (PyObject name : translationPython.getLocals().asIterable()) {
                                PyObject realObject = translationPython.get(name.asString());
                                String classType = realObject.getType().getName();
                                if (classType.equals("function") && realObject.__findattr__("__wrapped__") != null) {
                                    PyObject wrapped = realObject.__getattr__("__wrapped__");
                                    if (wrapped.__finditem__("event") != null && wrapped.__finditem__("class") != null && wrapped.__finditem__("func") != null) {
                                        PyObject wrapperEvent = wrapped.__getitem__(Py.newString("event"));
                                        PyObject codeObject = wrapped.__getitem__(Py.newString("func")).__getattr__("__code__");
                                        PyObject argNames = codeObject.__getattr__("co_varnames");
                                        PyString[] argNamesArray = (PyString[]) argNames.__tojava__(PyString[].class);
                                        JythonListener listener = new JythonListener<>(((PyType) wrapperEvent).getProxyType(), translationPython, name.asString(), argNamesArray.length >= 1);
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
                    Reader reader = null;
                    try {
                        reader = new BufferedReader(new FileReader(s));
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    LuaValue chunk = globals.load(reader, "script");
                    chunk.call();
                    Module mod = new Module(Scripts, s.getName().replace(".lua", ""), "") {
                        @Override
                        public void onActivate() {
                            LuaValue function = globals.get("onActivate");
                            if (function.isfunction()) function.call();
                        }

                        @EventHandler
                        private void onTickEvent(TickEvent.Post event) {
                            LuaValue function = globals.get("onTickPost");
                            if (function.isfunction()) function.call();
                        }

                        @EventHandler
                        private void onTickEvent(TickEvent.Pre event) {
                            LuaValue function = globals.get("onTickPre");
                            if (function.isfunction()) function.call();
                        }

                        @Override
                        public void onDeactivate() {
                            LuaValue function = globals.get("onDeactivate");
                            if (function.isfunction()) function.call();
                        }
                    };
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

    public String readFileAsString(String fileName) {
        String text = "";
        try {
            text = new String(Files.readAllBytes(Path.of(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String textv2 = text;

        for (int i = 0; i < Mappings.obfuscatedMap.size();) {
            if (Mappings.nonObfuscatedMap.get(i) != null) {
                textv2 = textv2.replace(Mappings.nonObfuscatedMap.get(i), Mappings.obfuscatedMap.get(i));
                i += 1;
            }
        }
        return textv2;
    }
}
