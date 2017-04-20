package com.naghtrion.vcontrole;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.naghtrion.vcontrole.async.AsyncManager;
import java.net.URL;
import java.util.Scanner;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor
{

    private final VControle plugin;


    public Commands(VControle plugin)
    {
        this.plugin = plugin;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        if (cmd.getName().equalsIgnoreCase("addvip"))
        {
            if (sender.hasPermission("vcontrole.addvip") || sender.hasPermission("vcontrole.admin") || sender.isOp())
            {
                if (args.length == 2)
                {
                    String grupo = args[0].trim();
                    if (plugin.foundGroup(grupo))
                    {
                        int dias = 0;
                        try
                        {
                            dias = Integer.parseInt(args[1]);
                        }
                        catch (NumberFormatException e)
                        {
                            sender.sendMessage(plugin.getMessage("error2").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }

                        if (dias > 0)
                        {
                            if (plugin.flatfile)
                            {
                                for (String n : plugin.getConfig().getConfigurationSection("vips").getKeys(false))
                                    if (plugin.getConfig().contains("vips." + n.trim() + "." + grupo))
                                    {
                                        int old = plugin.getConfig().getInt("vips." + n.trim() + "." + grupo);
                                        if (old != 0)
                                            plugin.getConfig().set("vips." + n.trim() + "." + grupo, old + dias);
                                    }

                                plugin.saveConfig();
                                sender.sendMessage(plugin.getMessage("addvip").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§').replace("%days%", Integer.toString(dias)).replace("%group%", grupo));
                                plugin.reloadConfig();
                            }
                            else
                            {
                                TaskVZ nk = new TaskVZ(plugin, "addvip", sender, grupo, dias);
                                AsyncManager.getInstance().addQueue(nk);
                            }
                        }
                        else
                            sender.sendMessage(plugin.getMessage("error1").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                    sender.sendMessage("/addvip <" + plugin.getMessage("group") + "> <" + plugin.getMessage("days") + ">");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("darvip") || cmd.getName().equalsIgnoreCase("givevip"))
        {
            if (sender.hasPermission("vcontrole.darvip") || sender.hasPermission("vcontrole.givevip") || sender.hasPermission("vcontrole.admin") || sender.isOp() || sender == plugin.getServer().getConsoleSender())
            {
                if (args.length == 3)
                {
                    Player p = plugin.getServer().getPlayer(args[0]);
                    if (p != null)
                    {
                        String grupo = args[1].trim();
                        if (plugin.foundGroup(grupo))
                        {
                            int dias = 0;
                            try
                            {
                                dias = Integer.parseInt(args[2].trim());
                            }
                            catch (NumberFormatException e)
                            {
                                sender.sendMessage(plugin.getMessage("error2").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                            }

                            if (dias > 0 && dias < 100000)
                            {
                                if (plugin.flatfile)
                                {
                                    if (plugin.usekey_global)
                                        plugin.getServer().broadcastMessage(plugin.getMessage("success3").replace("%name%", p.getName()).replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                    else
                                        p.sendMessage(plugin.getMessage("success2").replace("%group%", grupo.toUpperCase()).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

                                    if (!plugin.getConfig().contains("vips." + plugin.getRealName(p.getName())))
                                    {
                                        Calendar now = Calendar.getInstance();
                                        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                                        plugin.getConfig().set("vips." + p.getName() + ".inicio", fmt.format(now.getTime()));
                                        plugin.getConfig().set("vips." + p.getName() + ".usando", grupo);
                                        plugin.getConfig().set("vips." + p.getName() + "." + grupo, dias);
                                        plugin.saveConfig();
                                        plugin.DarVip(p, dias, grupo);
                                    }
                                    else
                                    {
                                        if (plugin.getConfig().contains("vips." + plugin.getRealName(p.getName()) + "." + grupo))
                                            plugin.getConfig().set("vips." + plugin.getRealName(p.getName()) + "." + grupo, (plugin.getConfig().getInt("vips." + plugin.getRealName(p.getName()) + "." + grupo) + dias));
                                        else
                                            plugin.getConfig().set("vips." + plugin.getRealName(p.getName()) + "." + grupo, dias);

                                        plugin.saveConfig();
                                        plugin.DarItensVip(p, dias, grupo);
                                    }
                                }
                                else
                                {
                                    TaskVZ t = new TaskVZ(plugin, "givevip", p, dias, grupo);
                                    AsyncManager.getInstance().addQueue(t);
                                }
                            }
                            else
                                sender.sendMessage(plugin.getMessage("error1").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        else
                            sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error7").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                    sender.sendMessage("/" + (plugin.getLanguage().equalsIgnoreCase("br") ? "darvip" : "givevip") + " <" + plugin.getMessage("name") + "> <" + plugin.getMessage("group") + "> <" + plugin.getMessage("days") + ">");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("mudardias") || cmd.getName().equalsIgnoreCase("changedays"))
        {
            if (sender.hasPermission("vcontrole.mudardias") || sender.hasPermission("vcontrole.changedays") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
            {
                if (args.length > 0 && args.length < 4)
                {
                    Player p = plugin.getServer().getPlayer(args[0]);
                    if (p != null)
                    {
                        if (args.length < 3)
                        {
                            if (plugin.flatfile)
                            {
                                if (plugin.getConfig().contains("vips." + plugin.getRealName(p.getName())))
                                {
                                    sender.sendMessage(p.getName() + " - " + plugin.getMessage("message2").replace('&', '§'));
                                    sender.sendMessage(ChatColor.AQUA + plugin.getMessage("initialdate") + ": " + ChatColor.WHITE + plugin.getConfig().getString("vips." + plugin.getRealName(p.getName()) + ".inicio"));
                                    for (String gname : plugin.getConfig().getStringList("vip_groups"))
                                        if (plugin.getConfig().contains("vips." + plugin.getRealName(p.getName()) + "." + gname.trim()))
                                            sender.sendMessage(ChatColor.AQUA + gname.toUpperCase() + ChatColor.WHITE + " - " + plugin.getMessage("daysleft") + ": " + plugin.getConfig().getInt("vips." + plugin.getRealName(p.getName()) + "." + gname) + " " + plugin.getMessage("days"));
                                }
                                else
                                    sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace('&', '§') + "!");
                            }
                            else
                            {
                                TaskVZ t = new TaskVZ(plugin, "mudardias1", sender, p);
                                AsyncManager.getInstance().addQueue(t);
                            }
                        }
                        else if (plugin.flatfile)
                        {
                            if (plugin.getConfig().contains("vips." + plugin.getRealName(p.getName())))
                            {
                                String grupo = args[1].trim();
                                if (plugin.foundGroup(grupo))
                                {
                                    int dias = 0;

                                    try
                                    {
                                        dias = Integer.parseInt(args[2].trim());
                                    }
                                    catch (NumberFormatException e)
                                    {
                                        sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                    }

                                    if (dias > 1 && dias < 100000)
                                    {
                                        plugin.getConfig().set("vips." + plugin.getRealName(p.getName()) + "." + grupo, dias);
                                        plugin.saveConfig();
                                        plugin.getServer().broadcastMessage(plugin.getMessage("cdays").replace("%admin%", sender.getName()).replace("%group%", grupo).replace("%name%", p.getName()).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§') + "!");
                                    }
                                    else
                                        sender.sendMessage(plugin.getMessage("error1").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                }
                                else
                                    sender.sendMessage(plugin.getMessage("error2").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                            }
                            else
                                sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace('&', '§'));
                        }
                        else
                        {
                            String grupo = args[1].trim();
                            if (plugin.foundGroup(grupo))
                            {
                                TaskVZ t = new TaskVZ(plugin, "mudardias2", p, args, sender, grupo);
                                AsyncManager.getInstance().addQueue(t);
                            }
                        }
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error7").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                    sender.sendMessage("/" + (plugin.getLanguage().equalsIgnoreCase("br") ? "mudardias" : "changedays") + " <" + plugin.getMessage("name") + "> <" + plugin.getMessage("group") + "> <" + plugin.getMessage("days") + ">");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("trocarvip") || cmd.getName().equalsIgnoreCase("changevip"))
        {
            if (sender.hasPermission("vcontrole.trocarvip") || sender.hasPermission("vcontrole.changevip") || sender.isOp() || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
            {
                if (args.length == 1)
                {
                    if (plugin.flatfile)
                    {
                        if (plugin.getConfig().contains("vips." + sender.getName()))
                        {
                            String grupo = args[0].trim();
                            if (plugin.foundGroup(grupo))
                            {
                                Calendar now = Calendar.getInstance();
                                SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");

                                boolean blockuso = false;

                                if (plugin.getConfig().getBoolean("one_vip_change"))
                                    if (plugin.trocou.containsKey(sender.getName()))
                                        if (fmt.format(now.getTime()).equals(plugin.trocou.get(sender.getName())))
                                            blockuso = true;

                                if (!blockuso)
                                {
                                    if (plugin.getConfig().contains("vips." + sender.getName() + "." + grupo))
                                    {
                                        if (plugin.getConfig().getInt("vips." + sender.getName() + "." + grupo) > 0)
                                        {
                                            plugin.hook.setGroup((Player) sender, grupo);
                                            if (plugin.getConfig().getBoolean("one_vip_change"))
                                            {
                                                if (plugin.trocou.containsKey(sender.getName()))
                                                    plugin.trocou.remove(sender.getName());

                                                plugin.trocou.put(sender.getName(), fmt.format(now.getTime()));
                                            }
                                            plugin.getConfig().set("vips." + sender.getName() + ".usando", grupo);
                                            plugin.saveConfig();
                                            sender.sendMessage(plugin.getMessage("success4").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                        }
                                        else
                                            sender.sendMessage(plugin.getMessage("error12").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                    }
                                    else
                                        sender.sendMessage(plugin.getMessage("error12").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                }
                                else
                                    sender.sendMessage(plugin.getMessage("error10").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                            }
                            else
                                sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        else
                            sender.sendMessage(plugin.getMessage("error6").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    else
                    {
                        String grupo = args[0].trim();
                        if (plugin.foundGroup(grupo))
                        {
                            TaskVZ t = new TaskVZ(plugin, "trocarvip", sender, grupo);
                            AsyncManager.getInstance().addQueue(t);
                        }
                        else
                            sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                }
                else
                    sender.sendMessage("/" + (plugin.getLanguage().equalsIgnoreCase("br") ? "trocarvip" : "changevip") + " <" + plugin.getMessage("group") + ">");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("usarkey") || cmd.getName().equalsIgnoreCase("usekey"))
        {
            if (sender.hasPermission("vcontrole.usarkey") || sender.hasPermission("vcontrole.usekey") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
            {
                if (args.length == 1)
                {
                    String key = args[0].toUpperCase();
                    if (!plugin.using_codes.containsKey(key))
                    {
                        String vcontrole = plugin.getVControleKey(key);
                        System.out.println("VControle Response: " + vcontrole);

                        if (vcontrole != null && vcontrole.split(";")[0].trim().equals("1"))
                        {
                            String grupo = vcontrole.split(";")[1].trim();
                            if (plugin.foundGroup(grupo))
                            {
                                plugin.using_codes.put(key, "");

                                int dias = Integer.parseInt(vcontrole.split(";")[2]);

                                if (plugin.flatfile)
                                {
                                    if (plugin.usekey_global)
                                        plugin.getServer().broadcastMessage(plugin.getMessage("success3").replace("%name%", sender.getName()).replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                                    else
                                        sender.sendMessage(plugin.getMessage("success2").replace("%group%", grupo.toUpperCase()).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

                                    if (!plugin.getConfig().contains("vips." + sender.getName()))
                                    {
                                        Calendar now = Calendar.getInstance();
                                        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                                        plugin.getConfig().set("vips." + sender.getName() + ".inicio", fmt.format(now.getTime()));
                                        plugin.getConfig().set("vips." + sender.getName() + ".usando", grupo);
                                        plugin.getConfig().set("vips." + sender.getName() + "." + grupo, dias);
                                        plugin.saveConfig();
                                        plugin.DarVip(((Player) sender), dias, grupo);
                                    }
                                    else
                                    {
                                        if (plugin.getConfig().contains("vips." + sender.getName() + "." + grupo))
                                            plugin.getConfig().set("vips." + sender.getName() + "." + grupo, (plugin.getConfig().getInt("vips." + sender.getName() + "." + grupo) + dias));
                                        else
                                            plugin.getConfig().set("vips." + sender.getName() + "." + grupo, dias);

                                        plugin.saveConfig();
                                        plugin.DarItensVip(((Player) sender), dias, grupo);
                                    }

                                    plugin.saveConfig();

                                    if (plugin.getConfig().getBoolean("logging.usekey"))
                                    {
                                        try
                                        {
                                            Calendar now = Calendar.getInstance();
                                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                                            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(plugin.getDataFolder() + File.separator + "log.txt", true)));
                                            out.println("usekey|" + sender.getName() + "|" + key + "|" + fmt.format(now.getTime()) + "|" + grupo + "|" + dias);
                                            out.close();
                                        }
                                        catch (IOException e)
                                        {
                                            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, e);
                                        }
                                    }

                                    plugin.using_codes.remove(key);
                                }
                                else
                                {
                                    TaskVZ t = new TaskVZ(plugin, "usekey", sender, grupo, dias);
                                    AsyncManager.getInstance().addQueue(t);
                                }
                            }
                            else
                                sender.sendMessage(plugin.getMessage("error8").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        else
                            sender.sendMessage(plugin.getVControleResponse(vcontrole));
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error13").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                    sender.sendMessage("/" + (plugin.getLanguage().equalsIgnoreCase("br") ? "usarkey" : "usekey") + " <key>");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("tempovip") || cmd.getName().equalsIgnoreCase("viptime"))
        {
            if (sender.hasPermission("vcontrole.tempovip") || sender.hasPermission("vcontrole.viptime") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
            {
                if (plugin.flatfile)
                {
                    if (plugin.getConfig().contains("vips." + sender.getName()))
                    {
                        sender.sendMessage(plugin.getMessage("message2").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        sender.sendMessage(plugin.getMessage("initialdate").replace('&', '§').replace("%date%", plugin.getConfig().getString("vips." + sender.getName() + ".inicio")));
                        for (String gname : plugin.getConfig().getStringList("vip_groups"))
                            if (plugin.getConfig().contains("vips." + sender.getName() + "." + gname.trim()))
                                sender.sendMessage(ChatColor.AQUA + gname.toUpperCase() + ChatColor.WHITE + " - " + plugin.getMessage("daysleft") + ": " + plugin.getConfig().getInt("vips." + sender.getName() + "." + gname) + " " + plugin.getMessage("days"));
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error6").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                {
                    TaskVZ t = new TaskVZ(plugin, "tempovip", sender);
                    AsyncManager.getInstance().addQueue(t);
                }
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("tirarvip") || cmd.getName().equalsIgnoreCase("rvip"))
        {
            if (sender.hasPermission("vcontrole.tirarvip") || sender.hasPermission("vcontrole.rvip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
            {
                if (args.length == 1)
                {
                    Player p = plugin.getServer().getPlayer(args[0]);
                    if (p != null)
                    {
                        if (plugin.flatfile)
                        {
                            if (plugin.getConfig().contains("vips." + plugin.getRealName(p.getName())))
                            {
                                plugin.getConfig().set("vips." + plugin.getRealName(p.getName()), null);
                                plugin.saveConfig();
                                plugin.removeRelatedVipGroups(p);
                                plugin.hook.setGroup(p, plugin.getConfig().getString("default_group").trim());
                                plugin.getServer().broadcastMessage(plugin.getMessage("rvip").replace("%admin%", sender.getName()).replace("%name%", p.getName()).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                            }
                            else
                                sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace('&', '§'));
                        }
                        else
                        {
                            TaskVZ t = new TaskVZ(plugin, "rvip", sender, p);
                            AsyncManager.getInstance().addQueue(t);
                        }
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error7").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                }
                else
                    sender.sendMessage("/" + (plugin.getLanguage().equalsIgnoreCase("br") ? "tirarvip" : "rvip") + " <" + plugin.getMessage("name") + ">");
            }
            else
                sender.sendMessage(plugin.getMessage("error11").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

            return true;
        }


        if (cmd.getName().equalsIgnoreCase("vcontrole"))
        {

            if (args.length == 1)
            {
                if (args[0].equalsIgnoreCase("reload"))
                {
                    if (sender.hasPermission("vcontrole.reload") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    {
                        plugin.reloadConfig();
                        File lFile = new File(plugin.getDataFolder(), "language_" + plugin.getConfig().getString("language").trim() + ".yml");
                        plugin.language = YamlConfiguration.loadConfiguration(lFile);

                        for (Player p : plugin.getServer().getOnlinePlayers())
                            plugin.AtualizarVIP(p);

                        sender.sendMessage(plugin.getMessage("reload").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        return true;
                    }
                }

                if (args[0].equalsIgnoreCase("myip"))
                {
                    if (sender.hasPermission("vcontrole.myip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    {
                        try
                        {
                            Scanner url = new Scanner((new URL("https://mine.vcontrole.com/api/myip")).openStream());
                            String response = url.nextLine();
                            url.close();
                            sender.sendMessage(plugin.getMessage("myip").replace("%ip%", response).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        catch (IOException ex)
                        {
                            Logger.getLogger(VControle.class.getName()).log(Level.SEVERE, null, ex);
                            sender.sendMessage(plugin.getMessage("myiperror'").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        return true;
                    }
                }
            }

            if (plugin.getLanguage().equalsIgnoreCase("br"))
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "Comandos do VControle:");

                if (sender.hasPermission("vcontrole.usarkey") || sender.hasPermission("vcontrole.usekey") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/usarkey " + ChatColor.WHITE + "- Utiliza uma key VIP.");

                if (sender.hasPermission("vcontrole.tempovip") || sender.hasPermission("vcontrole.viptime") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/tempovip " + ChatColor.WHITE + "- Mostra o ultimo dia de seu VIP.");

                if (sender.hasPermission("vcontrole.trocarvip") || sender.hasPermission("vcontrole.changevip") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/trocarvip " + ChatColor.WHITE + "- Muda o VIP que você está usando.");

                if (sender.hasPermission("vcontrole.tirarvip") || sender.hasPermission("vcontrole.rvip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/tirarvip " + ChatColor.WHITE + "- Tira o VIP de um jogador.");

                if (sender.hasPermission("vcontrole.mudardias") || sender.hasPermission("vcontrole.changedays") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/mudardias " + ChatColor.WHITE + "- Muda os dias de do grupo VIP.");

                if (sender.hasPermission("vcontrole.darvip") || sender.hasPermission("vcontrole.givevip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/darvip " + ChatColor.WHITE + "- Dá VIP sem o uso de uma key.");

                if (sender.hasPermission("vcontrole.addvip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/addvip " + ChatColor.WHITE + "- Dá dias VIPs a todos desse grupo (sem itens).");

                if (sender.hasPermission("vcontrole.reload") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/vcontrole reload " + ChatColor.WHITE + "- Recarrega o arquivo de configuração.");

                if (sender.hasPermission("vcontrole.myip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/vcontrole myip " + ChatColor.WHITE + "- Mostra o endereço IP deste servidor. Para liberar no Painel.");
            }
            else
            {
                sender.sendMessage(ChatColor.DARK_AQUA + "Commands of VControle:");

                if (sender.hasPermission("vcontrole.usarkey") || sender.hasPermission("vcontrole.usekey") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/usekey " + ChatColor.WHITE + "- Uses a key.");

                if (sender.hasPermission("vcontrole.tempovip") || sender.hasPermission("vcontrole.viptime") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/viptime " + ChatColor.WHITE + "- Show your last day with VIP.");

                if (sender.hasPermission("vcontrole.trocarvip") || sender.hasPermission("vcontrole.changevip") || sender.hasPermission("vcontrole.user") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/changevip " + ChatColor.WHITE + "- Changes the VIP that you are using.");

                if (sender.hasPermission("vcontrole.tirarvip") || sender.hasPermission("vcontrole.rvip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/rvip " + ChatColor.WHITE + "- Remove a VIP from player.");

                if (sender.hasPermission("vcontrole.mudardias") || sender.hasPermission("vcontrole.changedays") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/changedays " + ChatColor.WHITE + "- Change the days of a VIP.");

                if (sender.hasPermission("vcontrole.darvip") || sender.hasPermission("vcontrole.givevip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/givevip " + ChatColor.WHITE + "- Give VIP without generating a key.");

                if (sender.hasPermission("vcontrole.addvip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/addvip " + ChatColor.WHITE + "- Give VIP days to all from this group (no items).");

                if (sender.hasPermission("vcontrole.reload") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/vcontrole reload " + ChatColor.WHITE + "- Reload config file.");

                if (sender.hasPermission("vcontrole.myip") || sender.isOp() || sender.hasPermission("vcontrole.admin"))
                    sender.sendMessage(ChatColor.AQUA + "/vcontrole myip " + ChatColor.WHITE + "- Displays the IP address of this server. To release on the Panel.");
            }
            return true;
        }

        return false;
    }
}
