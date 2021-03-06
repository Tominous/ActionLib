package me.sraldeano.actionlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import me.sraldeano.actionlib.util.AddonUtil;
import me.sraldeano.actionlib.util.LoadUtil;
import me.sraldeano.actionlib.util.ReflectionUtil;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ActionLib extends JavaPlugin{
    protected static List<Action> addonActions = new ArrayList<>();
    protected static List<Action> actions = new ArrayList<>();
    protected static Map<String, Action> actionMap = new HashMap<>();
    protected static Map<String, Class<? extends Action>> actionClassMap = new HashMap<>();
    public static Permission perms = null;
    public static ActionLib plugin;
    public Economy eco = null;
    
    @Override
    public void onEnable() {
        plugin = this;
        Set<Class<?>> actionClasses = ReflectionUtil.getClasses(getFile(), "me.sraldeano.actionlib.action");
        for (Class<?> actionClass : actionClasses) {
            Class<? extends Action> newClass = (Class<? extends Action>) actionClass;
            Action action = null;
            try {
                action = newClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                ex.printStackTrace();
            }
            ActionManager.registerAction(action, false);
        }
        new AddonUtil().loadAddons();
        getServer().getLogger().info("ActionLib was loaded successfully.");
        getServer().getPluginCommand("actionlib").setExecutor(new CommandManager());
        saveDefaultConfig();
        LoadUtil.registerEvents();
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            setupVault();
            setupPermissions();
        }
    }

    @Override
    public void onDisable() {
        getServer().getLogger().fine("ActionLib was unloaded successfully.");
    }


    public static List<Action> getActions() {
        return actions;
    }

    public static List<Action> getAddonActions() {
        return addonActions;
    }
    
    public static Action getAction(String action) {
        try {
            return actionClassMap.get(action).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Class<? extends Action> getActionClass(String actionName) {
        return actionMap.get(actionName).getClass();
    }
    
    public boolean setupVault() {
        RegisteredServiceProvider<Economy> economyProvider = this.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            eco = economyProvider.getProvider();
        }

        return (eco != null);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
}
