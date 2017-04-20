package com.naghtrion.vcontrole;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;

public class PermissionsManager
{

    public String getGroup(final Player base)
    {
        return VControle.perms.getPrimaryGroup(base);
    }


    public boolean setGroup(final Player base, final String group)
    {
        try
        {
            for (String gr : VControle.perms.getPlayerGroups(base))
                VControle.perms.playerRemove(base, gr);
            VControle.perms.playerAdd(base, group);
            return true;
        }
        catch (Exception e)
        {
            Logger.getLogger(PermissionsManager.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }


    public boolean removeGroup(final Player base, final String group)
    {
        try
        {
            VControle.perms.playerRemove(base, group);
            return true;
        }
        catch (Exception e)
        {
            Logger.getLogger(PermissionsManager.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }


    public List<String> getGroups(final Player base)
    {
        return Arrays.asList(VControle.perms.getPlayerGroups(base));
    }
}
