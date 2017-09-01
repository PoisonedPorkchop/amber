package haven.mod.event.widget;

import haven.mod.event.CancellableEvent;

public class WidgetPreCreateEvent extends CancellableEvent {

    protected int id;
    protected String type;
    protected int parent;
    protected Object[] pargs;
    protected Object[] cargs;

    public WidgetPreCreateEvent(int id, String type, int parent, Object[] pargs, Object... cargs)
    {
        this.id = id;
        this.type = type;
        this.parent = parent;
        this.pargs = pargs;
        this.cargs = cargs;
    }

    @Override
    protected void initialization() throws Exception {
        /**iterator.insertGap(40,17);
        iterator.writeByte(187,40);
        iterator.writeByte(0,41);
        iterator.writeByte(classid,42);
        iterator.writeByte(89, 43);
        iterator.writeByte(27,44);
        iterator.writeByte(44,45);
        iterator.writeByte(29,46);
        iterator.writeByte(25, 47);
        iterator.writeByte(4, 48);
        iterator.writeByte(25, 49);
        iterator.writeByte(5,50);
        iterator.writeByte(183, 51);
        iterator.writeByte(0,52);
        iterator.writeByte(constructid,53);
        iterator.writeByte(182, 54);
        iterator.writeByte(0, 55);
        iterator.writeByte(callid,56);
        CtClass uiClass = ClassPool.getDefault().get("haven.UI");
        uiClass.getDeclaredMethod("");

        MethodEdit.editMethodAddEvent(uiClass);*/
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public Object[] getParentArgs() {
        return pargs;
    }

    public void setParentArgs(Object[] pargs) {
        this.pargs = pargs;
    }

    public Object[] getChildArgs() { return cargs; }

    public void setChildArgs(Object[] cargs) {
        this.cargs = cargs;
    }
}
