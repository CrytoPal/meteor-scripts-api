# Meteor Multi Language Script API

A Meteor Addon that lets you create Meteor modules in Multiple Languages.

### What Languages do we support?

We currently only support Python and Lua, more are coming in the future though.

### How do I use it?
1. Launch up Minecraft Fabric with the Mod installed.
2. Close the game and go to your .minecraft folder.
3. Go to the Meteor-Client Folder and then go to Scripts.
4. Put your Python / Lua Script into that folder and then Relaunch the game.
5. You then should have a Category in the ClickGui that shows your modules.

### How do I code a Script?

Please use the wiki! That explains almost everything the API has to offer.


### Python

But here's an example piece of code

```python
from net.minecraft.client import MinecraftClient
from net.minecraft.text import Text

mc = MinecraftClient.getInstance()

def onTickEventPre():
    mc.player.setSprinting(True)

def onActivate():
    mc.player.sendMessage(Text.of("Just Enabled Sprint"))

def onDeactivate():
    mc.player.sendMessage(Text.of("Just Disabled Sprint"))

```

### Lua

```lua

function onActivate()
    print("Hello from Lua!")
end

function onTickPre()
     mc.player:setSprinting(true)
end

function onDeactivate()
    print("Goodbye from Lua!")
    mc.player:setSprinting(false)
end
```

Although, the code must be obfuscated through minecraft's mappings. For example

### Python

```python

from net.minecraft import class_310
from net.minecraft import class_2561

mc = class_310.method_1551()

def onTickPre():
    mc.field_1724.method_5728(True)

def onActivate():
    mc.field_1724.method_43496(class_2561.method_30163("Just Enabled Sprint"))

def onDeactivate():
    mc.field_1724.method_43496(class_2561.method_30163("Just Disabled Sprint"))
```
### Lua

```lua
function onActivate()
    print("Hello from Lua!")
end

function onTickPre()
     mc.field_1724:method_5728(true)
end

function onDeactivate()
    print("Goodbye from Lua!")
    mc.field_1724:method_5728(false)
end
```

### How does it work?
It uses Jython's Python to Java library to convert your Python code to Java code. My API then checks for any Event Handlers like (for now) Tick Events, Activate, and Deactivate.
It also uses Luaj to convert your Lua code to Java code and execture it. My API then checks for any Event Handlers like (for now) Tick Events, Activate, and Deactivate.

### Credits
- [Jython](https://www.jython.org/) for their Library.
- [Walper](https://github.com/ridglef/walper-addon) (Pegasus) for the Inspiration + code I used
- [Luaj](https://github.com/luaj/luaj) for Lua Library


