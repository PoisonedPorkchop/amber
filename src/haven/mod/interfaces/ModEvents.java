package haven.mod.interfaces;

import haven.mod.event.*;
import haven.mod.event.chat.*;
import haven.mod.event.flower.FlowerMenuCancelEvent;
import haven.mod.event.flower.FlowerMenuChooseEvent;
import haven.mod.event.flower.FlowerMenuChosenEvent;
import haven.mod.event.flower.FlowerMenuCreateEvent;
import haven.mod.event.widget.*;

public interface ModEvents {

    //Channel Events
    public void onChannelCreateEvent(ChannelCreateEvent evt);
    public void onChatCreateEvent(ChatCreateEvent evt);
    public void onEntryChannelCreateEvent(EntryChannelCreateEvent evt);
    public void onLogCreateEvent(LogCreateEvent evt);
    public void onPartyChatCreateEvent(PartyChatCreateEvent evt);
    public void onPrivateChatCreateEvent(PrivateChatCreateEvent evt);
    public void onSimpleChatCreateEvent(SimpleChatCreateEvent evt);

    //Flower Menu Events
    public void onFlowerMenuCancelEvent(FlowerMenuCancelEvent evt);
    public void onFlowerMenuChooseEvent(FlowerMenuChooseEvent evt);
    public void onFlowerMenuChosenEvent(FlowerMenuChosenEvent evt);
    public void onFlowerMenuCreateEvent(FlowerMenuCreateEvent evt);

    //Widget Events
    public void onWidgetDestroyEvent(WidgetDestroyEvent evt);
    public void onWidgetGrabKeysEvent(WidgetGrabKeysEvent evt);
    public void onWidgetMessageEvent(WidgetMessageEvent evt);
    public void onWidgetMessageEvent(WidgetPostCreateEvent evt);
    public void onWidgetPostCreateEvent(WidgetPostCreateEvent evt);
    public void onWidgetPreCreateEvent(WidgetPreCreateEvent evt);

    //Misc Events
    public void onCancellableEvent(CancellableEvent evt);
    public void onCustomMenuButtonPressEvent(CustomMenuButtonPressEvent evt);
    public void onEvent(Event evt);
    public void onInventoryCreateEvent(InventoryCreateEvent evt);
    public void onRunStateChangeEvent(RunStateChangeEvent evt);
    public void onUIMessageEvent(UIMessageEvent evt);

}
