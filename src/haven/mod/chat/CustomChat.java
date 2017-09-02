package haven.mod.chat;

import haven.ChatUI;
import haven.TextEntry;

import java.awt.event.KeyEvent;

public abstract class CustomChat extends ChatUI.EntryChannel {

    private String name;

    public CustomChat(boolean closable, String name) {
        super(closable);
        this.in.reqdestroy();
        this.in = new TextEntry(0, "") {

            public void activate(String text) {
                if (text.length() > 0)
                    onChat(text);
                settext("");
                hpos = history.size();
            }

            public boolean keydown(KeyEvent ev) {
                if (ev.getKeyCode() == KeyEvent.VK_UP) {
                    if (hpos > 0) {
                        if (hpos == history.size())
                            hcurrent = text;
                        rsettext(history.get(--hpos));
                    }
                    return (true);
                } else if (ev.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (hpos < history.size()) {
                        if (++hpos == history.size())
                            rsettext(hcurrent);
                        else
                            rsettext(history.get(hpos));
                    }
                    return (true);
                } else {
                    return (super.keydown(ev));
                }
            }
        };
        add(this.in);
        this.name = name;
    }

    public abstract void onChat(String text);

    @Override
    public String name() {
        return name;
    }
}
