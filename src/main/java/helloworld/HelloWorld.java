package helloworld;

import net.canarymod.Canary;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandDependencyException;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;

public class HelloWorld extends Plugin implements CommandListener {
	public static Logman logger;
	
	public HelloWorld() {
		logger = getLogman();
	}
	
	@Override
	public boolean enable() {
		logger.info("starting up");
		try {
			Canary.commands().registerCommands(this, this, false);
		} catch (CommandDependencyException e) {
			logger.error("duplicate command name");
		}
		return true;
	}
	
	@Override
	public void disable() {
	}
	
	@Command(aliases = { "hello" },
		description = "Displays the hello world message.",
		permissions = { "" },
		toolTip = "/hello")
	public void helloCommand(MessageReceiver caller, String[] parameters) {
		String msg = "That'sss a very niccce program";
		Canary.instance().getServer().broadcastMessage(msg);
	}
}
