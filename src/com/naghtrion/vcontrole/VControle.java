package com.naghtrion.vcontrole;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.naghtrion.vcontrole.async.AsyncManager;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class VControle extends JavaPlugin implements Listener
{

    protected PermissionsManager hook = null;
    protected static Economy econ = null;
    protected static Permission perms = null;

    protected FileConfiguration language = null;
    protected boolean flatfile = true;

    protected boolean usekey_global = false;
    protected HashMap<String, String> trocou = new HashMap<>();

    protected String mysql_url = "";
    protected String mysql_user = "";
    protected String mysql_pass = "";
    protected HashMap<String, String> using_codes = new HashMap<>();


    @Override
    public void onEnable()
    {
        getLogger().log(Level.INFO, "Enabling VControle (V{0}) - Authors: SubZero0, BrineDev, Naghtrion", getDescription().getVersion());
        if (!checkVault())
        {
            super.setEnabled(false);
            getLogger().warning("Plugin Vault not found. This plugin needs Vault to work! Disabling...");
            return;
        }

        if (!setupPermissions())
        {
            super.setEnabled(false);
            getLogger().warning("Vault is not linked to any permissions plugin! Disabling...");
            return;
        }
        getLogger().info("Hooked to Vault (Permission).");

        if (!setupEconomy())
        {
            super.setEnabled(false);
            getLogger().warning("Vault is not linked to any economy plugin! Disabling...");
            return;
        }
        getLogger().info("Hooked to Vault (Economy).");

        getServer().getPluginCommand("usarkey").setExecutor(new Commands(this));
        getServer().getPluginCommand("usekey").setExecutor(new Commands(this));
        getServer().getPluginCommand("tempovip").setExecutor(new Commands(this));
        getServer().getPluginCommand("viptime").setExecutor(new Commands(this));
        getServer().getPluginCommand("vcontrole").setExecutor(new Commands(this));
        getServer().getPluginCommand("tirarvip").setExecutor(new Commands(this));
        getServer().getPluginCommand("rvip").setExecutor(new Commands(this));
        getServer().getPluginCommand("trocarvip").setExecutor(new Commands(this));
        getServer().getPluginCommand("changevip").setExecutor(new Commands(this));
        getServer().getPluginCommand("mudardias").setExecutor(new Commands(this));
        getServer().getPluginCommand("changedays").setExecutor(new Commands(this));
        getServer().getPluginCommand("darvip").setExecutor(new Commands(this));
        getServer().getPluginCommand("givevip").setExecutor(new Commands(this));
        getServer().getPluginCommand("addvip").setExecutor(new Commands(this));

        getServer().getPluginManager().registerEvents(this, this);

        hook = new PermissionsManager();

        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists())
            saveDefaultConfig();


        file = new File(getDataFolder(), "language_br.yml");
        if (!file.exists())
        {
            saveResource("language_br.yml", false);
            getLogger().info("Saved language_br.yml");
        }


        file = new File(getDataFolder(), "language_en.yml");
        if (!file.exists())
        {
            saveResource("language_en.yml", false);
            getLogger().info("Saved language_en.yml");
        }


        File lFile = new File(this.getDataFolder(), "language_" + getConfig().getString("language").trim() + ".yml");
        language = YamlConfiguration.loadConfiguration(lFile);

        if (getConfig().getBoolean("MySQL.use"))
        {
            mysql_url = "jdbc:mysql://" + getConfig().getString("MySQL.Host").trim() + ":" + getConfig().getInt("MySQL.Port") + "/" + getConfig().getString("MySQL.Database").trim() + "";
            mysql_user = getConfig().getString("MySQL.Username").trim();
            mysql_pass = getConfig().getString("MySQL.Password").trim();
            try
            {
                Connection con = DriverManager.getConnection(mysql_url, mysql_user, mysql_pass);
                flatfile = false;
                if (con == null)
                {
                    getLogger().info("Connection to MySQL failed! Changing to flatfile.");
                    flatfile = true;
                }
                else
                {
                    getLogger().info("Connected to MySQL server!");
                    Statement st = con.createStatement();
                    st.execute("CREATE TABLE IF NOT EXISTS `v_vips` (`nome` VARCHAR(30) PRIMARY KEY, `inicio` VARCHAR(11), `usando` VARCHAR(15));");
                    for (String gname : getConfig().getStringList("vip_groups"))
                    {
                        try
                        {
                            PreparedStatement pst2 = con.prepareStatement("ALTER TABLE `v_vips` ADD COLUMN `" + gname.trim() + "` VARCHAR(15) NOT NULL DEFAULT 0;");
                            pst2.execute();
                            pst2.close();
                        }
                        catch (SQLException e)
                        {
                            Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }

                    if (getConfig().getBoolean("logging.usekey"))
                        st.execute("CREATE TABLE IF NOT EXISTS `v_logs` (`comando` VARCHAR(20), `nome` VARCHAR(30), `key` VARCHAR(11) PRIMARY KEY, `data` VARCHAR(11), `grupo` VARCHAR(15), `dias` INT);");

                    st.close();
                }
                con.close();
            }
            catch (SQLException e)
            {
                getLogger().warning("Connection to MySQL failed! Changing to flatfile.");
                Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, e);
                flatfile = true;
            }
        }
        else
            getLogger().info("Using flatfile system.");

        if (flatfile && getConfig().getBoolean("logging.usekey"))
        {
            try
            {
                File file2 = new File(getDataFolder(), "log.txt");
                if (!file2.exists())
                {
                    saveResource("log.txt", false);
                    getLogger().info("Saved log.txt");
                }
            }
            catch (Exception e)
            {
                Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        AsyncManager.getInstance().start();
        usekey_global = getConfig().getBoolean("usekey_global");

        int tempo = getConfig().getInt("check_time");
        if (tempo != 0)
        {
            getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable()
            {
                @Override
                public void run()
                {
                    for (Player p : getServer().getOnlinePlayers())
                    {
                        AtualizarVIP(p);
                    }
                }
            }, 20L, 1200 * tempo);
        }
    }


    @Override
    public void onDisable()
    {
        getLogger().log(Level.INFO, "Disabling VControle (V{0}) - Authors: SubZero0, BrineDev, Naghtrion", getDescription().getVersion());
        AsyncManager.getInstance().stop();
    }


    private boolean checkVault()
    {
        Plugin pVT = Bukkit.getPluginManager().getPlugin("Vault");
        return pVT != null && pVT.isEnabled();
    }


    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
            return false;
        econ = rsp.getProvider();
        return econ != null;
    }


    private boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null)
            return false;
        perms = rsp.getProvider();
        return perms != null;
    }


    protected void removeRelatedVipGroups(Player p)
    {
        for (String g : hook.getGroups(p))
            for (String list : getConfig().getStringList("vip_groups"))
                if (g.equalsIgnoreCase(list))
                {
                    hook.removeGroup(p, g);
                    break;
                }
    }


    protected void DarVip(Player p, int dias, String grupo)
    {
        boolean temvip = false;
        for (String list : getConfig().getStringList("vip_groups"))
        {
            if (hook.getGroup(p).equalsIgnoreCase(list))
            {
                temvip = true;
                break;
            }
        }

        if (!temvip)
        {
            removeRelatedVipGroups(p);
            hook.setGroup(p, grupo);
            if (flatfile)
            {
                getConfig().set("vips." + getRealName(p.getName()) + ".usando", grupo);
                saveConfig();
            }
            else
            {
                TaskVZ t = new TaskVZ(this, "darvip", p, grupo);
                AsyncManager.getInstance().addQueue(t);
            }
        }
        DarItensVip(p, dias, grupo);
    }


    protected void TirarVip(final Player p, final String grupo, String fGrupo)
    {
        String gFinal;
        if (fGrupo == null)
            gFinal = getConfig().getString("default_group").trim();
        else
            gFinal = fGrupo;

        if (flatfile)
        {
            getConfig().set("vips." + getRealName(p.getName()) + "." + grupo, null);
            if (fGrupo == null)
                getConfig().set("vips." + getRealName(p.getName()), null);
            else
                getConfig().set("vips." + getRealName(p.getName()) + ".usando", fGrupo);
            saveConfig();
        }
        else
        {
            TaskVZ t = new TaskVZ(this, "tirarvip", p, grupo, fGrupo);
            AsyncManager.getInstance().addQueue(t);
        }

        hook.setGroup(p, gFinal);
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                p.sendMessage(getMessage("expired").replace("%group%", grupo.toUpperCase()) + ".");
            }
        }, 80L);
    }


    protected void TirarVip2(final Player p, final String grupo)
    {
        if (flatfile)
        {
            getConfig().set("vips." + getRealName(p.getName()) + "." + grupo, null);
            saveConfig();
        }
        else
        {
            TaskVZ t = new TaskVZ(this, grupo, "tirarvip2", p);
            AsyncManager.getInstance().addQueue(t);
        }

        getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
        {
            @Override
            public void run()
            {
                p.sendMessage(getMessage("expired").replace("%group%", grupo.toUpperCase()) + ".");
            }
        }, 80L);
    }


    protected void DarItensVip(Player p, int dias, String group)
    {
        Items i = new Items(this, p, dias, group);
        Thread th = new Thread(i);
        th.start();
    }


    @EventHandler
    protected void onLogin(PlayerJoinEvent e)
    {
        AtualizarVIP(e.getPlayer());
    }


    protected String getMessage(String t)
    {
        return language.getString(t).trim();
    }


    protected String getLanguage()
    {
        return getConfig().getString("language").trim();
    }


    public void AtualizarVIP(Player p)
    {
        if (flatfile)
        {
            if (getConfig().contains("vips." + getRealName(p.getName())))
            {
                Calendar now = Calendar.getInstance();
                Calendar vip = Calendar.getInstance();
                Calendar vip_fixo = Calendar.getInstance();
                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

                String data = getConfig().getString("vips." + getRealName(p.getName()) + ".inicio").trim();
                String usando = getConfig().getString("vips." + getRealName(p.getName()) + ".usando").trim();
                int dias = getConfig().getInt("vips." + getRealName(p.getName()) + "." + usando);

                try
                {
                    vip.setTime(fmt.parse(data));
                    vip_fixo.setTime(fmt.parse(data));
                }
                catch (ParseException e)
                {
                    Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, e);
                }

                if (!fmt.format(vip.getTime()).equals(fmt.format(now.getTime())))
                {
                    vip.add(Calendar.DATE, dias);
                    if (now.after(vip))
                    {
                        Calendar vip2 = Calendar.getInstance();
                        vip2.setTime(vip.getTime());
                        Calendar temp = Calendar.getInstance();
                        temp.setTime(vip.getTime());
                        String fim = null;
                        for (String n : getConfig().getStringList("vip_groups"))
                        {
                            n = n.trim();
                            if (!n.equalsIgnoreCase(usando))
                            {
                                if (getConfig().contains("vips." + getRealName(p.getName()) + "." + n))
                                {
                                    vip2.add(Calendar.DATE, getConfig().getInt("vips." + getRealName(p.getName()) + "." + n));
                                    if (now.after(vip2))
                                    {
                                        TirarVip2(p, n);
                                        temp.setTime(vip2.getTime());
                                    }
                                    else
                                    {
                                        fim = n;
                                        getConfig().set("vips." + getRealName(p.getName()) + ".inicio", fmt.format(temp.getTime()));
                                        saveConfig();
                                        break;
                                    }
                                }
                            }
                        }
                        TirarVip(p, usando, fim);
                    }
                    else
                    {
                        int total = 0;
                        while (!fmt.format(now.getTime()).equals(fmt.format(vip_fixo.getTime())))
                        {
                            vip_fixo.add(Calendar.DATE, 1);
                            total += 1;
                        }
                        getConfig().set("vips." + getRealName(p.getName()) + "." + usando, (dias - total));
                        getConfig().set("vips." + getRealName(p.getName()) + ".inicio", fmt.format(now.getTime()));
                        saveConfig();
                    }
                }
            }
            else if (getConfig().getBoolean("rvip_unlisted"))
            {
                List<String> l = hook.getGroups(p);
                for (String n : getConfig().getStringList("vip_groups"))
                    if (l.contains(n.trim()))
                        hook.setGroup(p, getConfig().getString("default_group").trim());
            }
        }
        else
        {
            TaskVZ t = new TaskVZ(this, "atualizar", p);
            AsyncManager.getInstance().addQueue(t);
        }
    }


    protected String getRealName(String name)
    {
        if (name == null)
            return null;

        if (getConfig().getBoolean("case_sensitive"))
            return name;

        if (!getConfig().contains("vips"))
            return name;

        for (String s : getConfig().getConfigurationSection("vips").getKeys(false))
            if (s.toLowerCase().equals(name.toLowerCase()))
                return s;

        return name;
    }


    public String getVControleKey(String key)
    {
        try
        {
            Scanner url = new Scanner((new URL("https://mine.vcontrole.com/api/usekey/" + getConfig().getString("Secret") + "/" + key)).openStream());
            String response = url.nextLine();
            url.close();
            return response;
        }
        catch (IOException ex)
        {
            Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    public String getVControleResponse(String response)
    {
        String mensagem;
        switch (response)
        {
            case "2;":
                mensagem = getMessage("vcontrole.2").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "3;":
                mensagem = getMessage("vcontrole.1").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "4;":
                mensagem = getMessage("vcontrole.3").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "5;":
                mensagem = getMessage("vcontrole.4").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "6;":
                mensagem = getMessage("vcontrole.5").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "7;":
                mensagem = getMessage("vcontrole.6").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            case "8;":
                mensagem = getMessage("vcontrole.7").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
            default:
                mensagem = getMessage("vcontrole.0").replace("%prefix%", getMessage("prefix")).replace('&', '§');
                break;
        }
        return mensagem;
    }


    protected boolean foundGroup(String grupo)
    {
        for (String gs : getConfig().getStringList("vip_groups"))
            if (gs.equalsIgnoreCase(grupo))
                return true;
        return false;
    }
}
