package haven.event;

public interface ItemListener extends EventListener {
    void onItemCreate(ItemEvent e);

    void onItemGrab(ItemEvent e);

    void onItemRelease(ItemEvent e);

    void onItemDestroy(ItemEvent e);
}