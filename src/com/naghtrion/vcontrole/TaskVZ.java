package com.naghtrion.vcontrole;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.naghtrion.vcontrole.async.AsyncManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TaskVZ implements Runnable
{

    private final VControle plugin;
    private final String tipo;
    private CommandSender sender;
    private Player p;
    private int dias;
    private String grupo;
    private String key;
    private String fGrupo;
    private String[] args;

    private final List<Player> lista0 = new ArrayList<>();
    private final HashMap<Player, List<Integer>> lista1 = new HashMap<>();
    private final HashMap<Player, List<String>> lista2 = new HashMap<>();


    public TaskVZ(VControle plugin, String tipo, CommandSender sender, String grupo)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.sender = sender;
        this.grupo = grupo;
    }


    public TaskVZ(VControle plugin, String tipo, String key, CommandSender sender)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.sender = sender;
        this.key = key;
    }


    public TaskVZ(VControle plugin, String tipo, CommandSender sender)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.sender = sender;
    }


    public TaskVZ(VControle plugin, String tipo, Player p, String[] args, CommandSender sender, String grupo)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.p = p;
        this.args = args;
        this.sender = sender;
        this.grupo = grupo;
    }


    public TaskVZ(VControle plugin, String tipo, Player p, String grupo, String fGrupo)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.p = p;
        this.grupo = grupo;
        this.fGrupo = fGrupo;
    }


    public TaskVZ(VControle plugin, String tipo, Player p, int dias, String grupo)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.p = p;
        this.grupo = grupo;
        this.dias = dias;
    }


    public TaskVZ(VControle plugin, String tipo, CommandSender sender, Player p)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.sender = sender;
        this.p = p;
    }


    public TaskVZ(VControle plugin, String tipo, CommandSender sender, String grupo, int dias)
    {
        this.plugin = plugin;
        this.tipo = tipo;
        this.sender = sender;
        this.grupo = grupo;
        this.dias = dias;
    }


    @Override
    public void run()
    {
        switch (tipo)
        {
            case "addvip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("SELECT `nome`,`" + grupo + "` FROM `v_vips` WHERE `" + grupo + "`!=0;");
                    ResultSet rs = pst.executeQuery();
                    while (rs.next())
                    {
                        PreparedStatement pst2 = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=? WHERE `nome`=?;");
                        pst2.setInt(1, (rs.getInt(grupo) + dias));
                        pst2.setString(2, rs.getString("nome"));
                        pst2.executeUpdate();
                        pst2.close();
                    }
                    sender.sendMessage(plugin.getMessage("addvip").replace("%days%", Integer.toString(dias)).replace("%group%", grupo).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    pst.close();
                    rs.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "trocarvip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pstSV = con.prepareStatement("SELECT `" + grupo + "` FROM `v_vips` WHERE `nome`='" + sender.getName() + "';");
                    ResultSet rs = pstSV.executeQuery();
                    if (rs.next())
                    {
                        if (rs.getInt(grupo) != 0)
                        {
                            plugin.hook.setGroup((Player) sender, grupo);
                            PreparedStatement pst = con.prepareStatement("UPDATE `v_vips` SET `usando`=? WHERE `nome`=?;");
                            pst.setString(1, grupo);
                            pst.setString(2, sender.getName());
                            pst.executeUpdate();
                            pst.close();
                            pstSV.close();
                            rs.close();
                            sender.sendMessage(plugin.getMessage("success4").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        else
                        {
                            sender.sendMessage(plugin.getMessage("error12").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                    }
                    else
                    {
                        sender.sendMessage(plugin.getMessage("error6").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "usekey":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);

                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                    pst.setString(1, sender.getName());
                    ResultSet rs = pst.executeQuery();

                    if (plugin.usekey_global)
                        plugin.getServer().broadcastMessage(plugin.getMessage("success3").replace("%name%", sender.getName()).replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    else
                        sender.sendMessage(plugin.getMessage("success2").replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

                    if (rs.next())
                    {
                        PreparedStatement upp = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=? WHERE `nome`=?;");
                        upp.setInt(1, (rs.getInt(grupo) + dias));
                        upp.setString(2, sender.getName());
                        upp.executeUpdate();
                        upp.close();
                        plugin.DarItensVip(((Player) sender), dias, grupo);
                    }
                    else
                    {
                        Calendar now = Calendar.getInstance();
                        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                        PreparedStatement upp = con.prepareStatement("INSERT INTO `v_vips` (`nome`,`inicio`,`usando`,`" + grupo + "`) VALUES (?, ?, ?, ?);");
                        upp.setString(1, sender.getName().trim());
                        upp.setString(2, fmt.format(now.getTime()));
                        upp.setString(3, grupo);
                        upp.setInt(4, dias);
                        upp.executeUpdate();
                        upp.close();
                        plugin.DarVip(((Player) sender), dias, grupo);
                    }

                    if (plugin.getConfig().getBoolean("logging.usekey"))
                    {
                        Calendar now = Calendar.getInstance();
                        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                        PreparedStatement addlog = con.prepareStatement("INSERT INTO `v_logs` (`comando`, `nome`,`key`,`data`,`grupo`,`dias`) VALUES ('usekey', ?, ?, ?, ?, ?);");
                        addlog.setString(1, sender.getName());
                        addlog.setString(2, key);
                        addlog.setString(3, fmt.format(now.getTime()));
                        addlog.setString(4, grupo);
                        addlog.setInt(5, dias);
                        addlog.executeUpdate();
                        addlog.close();
                    }

                    rs.close();
                    pst.close();

                    con.close();
                    plugin.using_codes.remove(key);
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "tempovip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                    pst.setString(1, sender.getName());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next())
                    {
                        sender.sendMessage(plugin.getMessage("message2"));
                        for (String gname : plugin.getConfig().getStringList("vip_groups"))
                            if (rs.getInt(gname.trim()) != 0)
                                sender.sendMessage(ChatColor.AQUA + gname.toUpperCase() + ChatColor.WHITE + " - " + plugin.getMessage("daysleft") + ": " + rs.getInt(gname.trim()) + " " + plugin.getMessage("days"));
                    }
                    else
                        sender.sendMessage(plugin.getMessage("error6").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "rvip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("SELECT `nome` FROM `v_vips` WHERE `nome`=?;");
                    pst.setString(1, p.getName());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next())
                    {
                        PreparedStatement pst2 = con.prepareStatement("DELETE FROM `v_vips` WHERE `nome`=?;");
                        pst2.setString(1, p.getName());
                        pst2.execute();
                        pst2.close();
                        plugin.hook.setGroup(p, plugin.getConfig().getString("default_group").trim());
                        plugin.getServer().broadcastMessage(plugin.getMessage("rvip").replace("%admin%", sender.getName()).replace("%name%", p.getName()).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    else
                    {
                        sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    pst.close();
                    rs.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "mudardias1":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                    pst.setString(1, p.getName());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next())
                    {
                        sender.sendMessage(rs.getString("nome") + " - " + plugin.getMessage("message2") + ":");
                        for (String gname : plugin.getConfig().getStringList("vip_groups"))
                            if (rs.getInt(gname.trim()) != 0)
                                sender.sendMessage(ChatColor.AQUA + gname.toUpperCase() + ChatColor.WHITE + " - " + plugin.getMessage("daysleft") + ": " + rs.getInt(gname.trim()) + " " + plugin.getMessage("days"));
                    }
                    else
                    {
                        sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    pst.close();
                    rs.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "mudardias2":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                    pst.setString(1, p.getName());
                    ResultSet rs = pst.executeQuery();
                    if (rs.next())
                    {
                        int days = Integer.parseInt(args[2].trim());
                        if (days > 1 && days < 10000)
                        {
                            PreparedStatement pst2 = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=? WHERE `nome`=?;");
                            pst2.setInt(1, days);
                            pst2.setString(2, p.getName());
                            pst2.executeUpdate();
                            pst2.close();
                            plugin.getServer().broadcastMessage(plugin.getMessage("cdays").replace("%admin%", sender.getName()).replace("%group%", grupo).replace("%name%", p.getName()).replace("%days%", Integer.toString(days)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                        else
                        {
                            sender.sendMessage(plugin.getMessage("error1").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        }
                    }
                    else
                    {
                        sender.sendMessage(p.getName() + " " + plugin.getMessage("error9").replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                    }
                    pst.close();
                    rs.close();
                    con.close();
                }
                catch (NumberFormatException | SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "darvip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("UPDATE `v_vips` SET `usando`=? WHERE `nome`=?;");
                    pst.setString(1, grupo);
                    pst.setString(2, p.getName());
                    pst.executeUpdate();
                    pst.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "tirarvip":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=0 WHERE `nome`=?;");
                    pst.setString(1, p.getName());
                    pst.executeUpdate();
                    if (fGrupo == null)
                    {
                        PreparedStatement pst2 = con.prepareStatement("DELETE FROM `v_vips` WHERE `nome`=?;");
                        pst2.setString(1, p.getName());
                        pst2.execute();
                        pst2.close();
                    }
                    else
                    {
                        PreparedStatement pst2 = con.prepareStatement("UPDATE `v_vips` SET `usando`=? WHERE `nome`=?;");
                        pst2.setString(1, fGrupo);
                        pst2.setString(2, p.getName());
                        pst2.executeUpdate();
                        pst2.close();
                    }
                    pst.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "tirarvip2":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    PreparedStatement pst = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=0 WHERE `nome`=?;");
                    pst.setString(1, p.getName());
                    pst.executeUpdate();
                    pst.close();
                    con.close();
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "atualizar":
            {
                try
                {
                    Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                    Player p2 = null;
                    if (p.isOnline() && p.getName() != null && p != null)
                    {
                        p2 = p;
                    }
                    if (p2 != null)
                    {
                        PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                        pst.setString(1, p2.getName());
                        ResultSet rs = pst.executeQuery();
                        if (rs.next())
                        {
                            Calendar now = Calendar.getInstance();
                            Calendar vip = Calendar.getInstance();
                            Calendar vip_fixo = Calendar.getInstance();
                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                            String data = rs.getString("inicio");
                            String usando = rs.getString("usando");
                            int days = rs.getInt(usando);
                            try
                            {
                                vip.setTime(fmt.parse(data));
                                vip_fixo.setTime(fmt.parse(data));
                            }
                            catch (ParseException e)
                            {
                                Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                            }
                            if (!fmt.format(vip.getTime()).equals(fmt.format(now.getTime())))
                            {
                                vip.add(Calendar.DATE, days);
                                if (now.after(vip))
                                {
                                    Calendar vip2 = Calendar.getInstance();
                                    vip2.setTime(vip.getTime());
                                    Calendar temp = Calendar.getInstance();
                                    temp.setTime(vip.getTime());
                                    String fim = null;
                                    for (String n : plugin.getConfig().getStringList("vip_groups"))
                                    {
                                        if (!n.equalsIgnoreCase(usando))
                                        {
                                            if (rs.getInt(n.trim()) != 0)
                                            {
                                                vip2.add(Calendar.DATE, rs.getInt(n.trim()));
                                                if (now.after(vip2))
                                                {
                                                    plugin.TirarVip2(p2, n.trim());
                                                    temp.setTime(vip2.getTime());
                                                }
                                                else
                                                {
                                                    fim = n.trim();
                                                    PreparedStatement pst4 = con.prepareStatement("UPDATE `v_vips` SET `inicio`=? WHERE `nome`=?;");
                                                    pst4.setString(1, fmt.format(temp.getTime()));
                                                    pst4.setString(2, p2.getName());
                                                    pst4.executeUpdate();
                                                    pst4.close();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    plugin.TirarVip(p2, usando.trim(), fim);
                                }
                                else
                                {
                                    int total = 0;
                                    while (!fmt.format(now.getTime()).equals(fmt.format(vip_fixo.getTime())))
                                    {
                                        vip_fixo.add(Calendar.DATE, 1);
                                        total++;
                                    }
                                    PreparedStatement pst5 = con.prepareStatement("UPDATE `v_vips` SET `" + usando + "`=?, `inicio`=? WHERE `nome`=?;");
                                    pst5.setInt(1, (days - total));
                                    pst5.setString(2, fmt.format(now.getTime()));
                                    pst5.setString(3, p2.getName());
                                    pst5.executeUpdate();
                                    pst5.close();
                                }
                            }
                        }
                        else if (plugin.getConfig().getBoolean("rvip_unlisted"))
                        {
                            List<String> l = plugin.hook.getGroups(p2);
                            for (String n : plugin.getConfig().getStringList("vip_groups"))
                                if (l.contains(n.trim()))
                                    plugin.hook.setGroup(p2, plugin.getConfig().getString("default_group").trim());
                        }
                        pst.close();
                        rs.close();
                        con.close();
                    }
                }
                catch (SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }


            case "givevip":
            {
                try
                {
                    if (!lista0.contains(p))
                    {
                        lista0.add(p);
                        Connection con = DriverManager.getConnection(plugin.mysql_url, plugin.mysql_user, plugin.mysql_pass);
                        PreparedStatement pst = con.prepareStatement("SELECT * FROM `v_vips` WHERE `nome`=?;");
                        pst.setString(1, p.getName());
                        ResultSet rs = pst.executeQuery();

                        if (plugin.usekey_global)
                            plugin.getServer().broadcastMessage(plugin.getMessage("success3").replace("%name%", p.getName()).replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));
                        else
                            p.sendMessage(plugin.getMessage("success2").replace("%group%", grupo).replace("%days%", Integer.toString(dias)).replace("%prefix%", plugin.getMessage("prefix")).replace('&', '§'));

                        if (rs.next())
                        {
                            PreparedStatement upp = con.prepareStatement("UPDATE `v_vips` SET `" + grupo + "`=? WHERE `nome`=?;");
                            upp.setInt(1, (rs.getInt(grupo) + dias));
                            upp.setString(2, p.getName());
                            upp.executeUpdate();
                            upp.close();
                            plugin.DarItensVip(p, dias, grupo);
                        }
                        else
                        {
                            Calendar now = Calendar.getInstance();
                            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
                            PreparedStatement upp = con.prepareStatement("INSERT INTO `v_vips` (`nome`,`inicio`,`usando`,`" + grupo + "`) VALUES (?, ?, ?, ?);");
                            upp.setString(1, p.getName().trim());
                            upp.setString(2, fmt.format(now.getTime()));
                            upp.setString(3, grupo);
                            upp.setInt(4, dias);
                            upp.executeUpdate();
                            upp.close();
                            plugin.DarVip(p, dias, grupo.trim());
                        }

                        pst.close();
                        rs.close();
                        con.close();

                        if (lista1.containsKey(p))
                        {
                            Thread.sleep(1000);
                            int dias_h = lista1.get(p).get(0);
                            lista1.get(p).remove(0);
                            String grupo_h = lista2.get(p).get(0);
                            lista2.get(p).remove(0);
                            TaskVZ t = new TaskVZ(plugin, "givevip", p, dias_h, grupo_h.trim());
                            AsyncManager.getInstance().addQueue(t);
                        }
                        else
                        {
                            lista0.remove(p);
                        }
                    }
                    else
                    {
                        List<Integer> l1 = lista1.get(p);
                        List<String> l2 = lista2.get(p);
                        l1.add(dias);
                        l2.add(grupo);
                        lista1.remove(p);
                        lista2.remove(p);
                        lista1.put(p, l1);
                        lista2.put(p, l2);
                    }
                }
                catch (InterruptedException | SQLException e)
                {
                    Logger.getLogger(TaskVZ.class.getName()).log(Level.SEVERE, null, e);
                }
                break;
            }
        }
    }
}
