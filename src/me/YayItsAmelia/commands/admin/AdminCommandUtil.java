package me.YayItsAmelia.commands.admin;

import me.YayItsAmelia.Main;

public final class AdminCommandUtil
{
	public static boolean areAdminsAnonymous()
	{
		return Main.instance.getConfig().getBoolean("adminCommandNotificationAnonymous", false);
	}
}
