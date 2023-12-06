package uk.cryo.scripts;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.orbit.EventHandler;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.python.util.PythonInterpreter;
import org.slf4j.Logger;

import java.io.*;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class API extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category Scripts = new Category("Scripts");

    public static final File SCRIPTS_FOLDER = new File(MeteorClient.FOLDER, "scripts");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Scripts");

        if (!SCRIPTS_FOLDER.exists()) SCRIPTS_FOLDER.mkdir();

        PythonInterpreter translationPython = new PythonInterpreter();

        for (File s : SCRIPTS_FOLDER.listFiles()) {
            LOG.info("Loading " + s.getName() + " script");
            if (s.getName().contains(".py")) {

                translationPython.execfile(s.getAbsolutePath());

                Module mod = new Module(Scripts, s.getName().replace(".py", ""), "") {

                    @Override
                    public void onActivate() {
                        try {
                            translationPython.exec("onActivate()");
                        } catch (Exception ignored) {
                        }
                    }

                    @EventHandler
                    public void onTickEventPre(TickEvent.Pre event) {
                        try {
                            translationPython.exec("onTickPre()");
                        } catch (Exception ignored) {
                        }
                    }


                    @EventHandler
                    public void onTickEventPost(TickEvent.Post event) {
                        try {
                            translationPython.exec("onTickPost()");
                        } catch (Exception ignored) {
                        }
                    }

                    @Override
                    public void onDeactivate() {
                        try {
                            translationPython.exec("onDeactivate()");
                        } catch (Exception ignored) {
                        }
                    }

                };
                Modules.get().add(mod);

            } else if (s.getName().contains(".lua")) {
                Globals globals = JsePlatform.standardGlobals();
                globals.set("mc", CoerceJavaToLua.coerce(mc));
                globals.set("player", CoerceJavaToLua.coerce(mc.player));
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
                    public void onDeactivate(){
                        LuaValue function = globals.get("onDeactivate");
                        if (function.isfunction()) function.call();
                    }
                };
                Modules.get().add(mod);
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
}
