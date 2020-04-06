package fr.landel.myproxy.utils.json;

import java.io.Serializable;
import java.util.HashSet;

public class JsonArray extends HashSet<JsonObject<? extends Serializable>> implements Serializable {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -3178369799990583170L;

    private boolean opened;

    public JsonArray() {
        super();
        this.opened = true;
    }

    public void close() {
        this.opened = false;
    }

    public boolean isOpened() {
        return this.opened;
    }
}
