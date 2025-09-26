package dev.thorinwasher.forgery.util;

import dev.thorinwasher.forgery.Forgery;
import org.bukkit.NamespacedKey;

public class PdcKeys {


    public static final NamespacedKey TOOL = Forgery.key("tool");
    public static final NamespacedKey FORGING_STEPS = new NamespacedKey(Forgery.NAMESPACE, "forging_steps");
    public static final NamespacedKey FORGING_MATERIAL = new NamespacedKey(Forgery.NAMESPACE, "material");
}
