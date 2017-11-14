package lol.lolpany.postamatik;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class Component implements Runnable {

    private final AtomicBoolean on;
    private final ComponentCycle runnable;

    Component(AtomicBoolean on, ComponentCycle runnable) {
        this.on = on;
        this.runnable = runnable;
    }

    public void run() {
        while (on.get()) {
            try {
                runnable.doCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}