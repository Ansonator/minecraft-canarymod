/***
 * Excerpted from "Learn to Program with Minecraft Plugins, CanaryMod Edition",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/ahmine2 for more book information.
***/
package helloworld;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pragprog.ahmine.ez.EZPlugin;

import house.Builder;
import net.canarymod.BlockIterator;
import net.canarymod.Canary;
import net.canarymod.LineTracer;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.entity.living.EntityLiving;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.factory.EntityFactory;
import net.canarymod.api.factory.PotionFactory;
import net.canarymod.api.potion.PotionEffect;
import net.canarymod.api.potion.PotionEffectType;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.effects.Particle;
import net.canarymod.api.world.effects.SoundEffect;
import net.canarymod.api.world.position.Location;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.Command;

public class HelloWorldEZ extends EZPlugin {
	@Command(aliases = { "hello" }, description = "Displays the hello world message.", permissions = {
			"" }, toolTip = "/hello")
	public void helloCommand(MessageReceiver caller, String[] parameters) {
		String msg = String.format("HelloWorldEZ.helloCommand called with params \"%s\"",
				Stream.of(parameters).collect(Collectors.joining("\",\"")));
		Canary.getServer().broadcastMessage(msg);
		if (caller instanceof Player) {
			Player me = (Player) caller;
			Canary.getServer().broadcastMessage("at location " + me.getLocation());
		}
	}

	@Command(aliases = { "build" }, description = "Build a house or sphere", permissions = { "" }, toolTip = "/build")
	public void buildCommand(MessageReceiver caller, String[] parameters) {
		Builder builder = new Builder(logger, Canary.getServer());
		builder.build(caller, parameters);
	}

	@Command(aliases = { "sky" }, description = "Teleport all creatures into the air", permissions = {
			"" }, toolTip = "/sky")
	public void skyCommand(MessageReceiver caller, String[] params) {
		double height = Double.valueOf(params[1]);
		if (caller instanceof Player) {
			Player me = (Player) caller;
			for (EntityLiving target : me.getWorld().getEntityLivingList()) {
				if (!(target instanceof Player)) {
					Location loc = target.getLocation();
					double y = loc.getY();
					loc.setY(y + height);
					target.teleportTo(loc);
				}
			}
		}
	}

	@Command(aliases = {
			"lavavision" }, description = "Explode your target into a ball of flaming lava", permissions = {
					"" }, toolTip = "/lavavision")
	public void lavavisionCommand(MessageReceiver caller, String[] args) {
		if (caller instanceof Player) {
			Player me = (Player) caller;

			BlockIterator sightItr = new BlockIterator(new LineTracer(me));
			while (sightItr.hasNext()) {
				Block b = sightItr.next();
				spawnParticle(b.getLocation(), Particle.Type.LAVASPARK);
				if (b.getType() != BlockType.Air) {
					b.getWorld().setBlockAt(b.getLocation(), BlockType.Lava);
					playSound(b.getLocation(), SoundEffect.Type.EXPLODE);
					break;
				}
			}

		}
	}

	@Command(aliases = { "bat" }, description = "Explode your target into a ball of flaming lava", permissions = {
			"" }, toolTip = "/lavavision")
	public void batCommand(MessageReceiver caller, String[] args) {
		if (caller instanceof Player) {
			Player me = (Player) caller;
			Location loc = me.getLocation();
			loc.setY(loc.getY() + 5);

			EntityFactory factory = Canary.factory().getEntityFactory();
			EntityLiving bat = factory.newEntityLiving(EntityType.BAT, loc);
			EntityLiving creep = factory.newEntityLiving(EntityType.CREEPER, loc);
			bat.spawn(creep);

			PotionFactory potfact = Canary.factory().getPotionFactory();
			PotionEffect potion = potfact.newPotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1);
			bat.addPotionEffect(potion);

		}
	}
}