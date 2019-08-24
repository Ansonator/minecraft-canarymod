package house;

import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.position.Location;
import net.canarymod.api.world.position.Position;
import net.canarymod.api.world.position.Vector3D;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.logger.Logman;

public class Builder {

	public void build(MessageReceiver caller, String[] parameters) {
		if (caller instanceof Player) {
			String command = parameters[1];
			Player me = (Player) caller;
			Location origin = me.getLocation();
			Item itemInHand = me.getInventory().getItemInHand();
			switch (command) {
			case "house": {
				int width = Integer.valueOf(parameters[2]);
				int height = Integer.valueOf(parameters[3]);
				buildHouse(origin, width, height);
				break;
			}
			case "sphere": {
				double radius = Double.valueOf(parameters[2]);
				BlockType type = BlockType.fromString(parameters[3]);
				if (type == null)
					type = BlockType.Glass;
				if (parameters.length > 4)
					buildSphere(origin, radius, type, Double.valueOf(parameters[3]), Double.valueOf(parameters[4]));
				else
					buildSphere(origin, radius, type, 0, 0);
				break;
			}
			case "stairs": {
				double innerRadius = Double.valueOf(parameters[2]);
				double outerRadius = Double.valueOf(parameters[3]);
				double height = Double.valueOf(parameters[4]);
				double thickness = Double.valueOf(parameters[5]);
				buildStairs(origin, height, innerRadius, outerRadius, thickness);
				break;
			}
			case "ray": { // draw a ray pointing in direction user is facing
				double length = Double.valueOf(parameters[2]);
				buildRay(origin, length, BlockType.Tnt);
				break;
			}
			case "circle": { // create a circle in plane perpendicular to direction user is facting
				double radius = Double.valueOf(parameters[2]);
				double gap = Double.valueOf(parameters[3]);
				buildCircle(origin, radius, gap, randomBlockType());
				break;
			}
			case "cylinder": { // create a hollow cylinder pointing in direction user is facing
				double length = Double.valueOf(parameters[2]);
				double radius = Double.valueOf(parameters[3]);
				double gap = Double.valueOf(parameters[4]);
				buildCylinder(origin, length, radius, gap,BlockType.BlueGlass);
				break;
			}
			case "tunnel": { // create a cylinder of air pointing in direction user is facing
				double length = Double.valueOf(parameters[2]);
				double radius = Double.valueOf(parameters[3]);
				buildCylinder(origin, length, radius, 0, BlockType.Air);
				break;
			}
			case "tree": {
				double branchLength = Double.valueOf(parameters[2]);
				double branchLengthMultipler = Double.valueOf(parameters[3]);
				double rollIncrement = Double.valueOf(parameters[4]);
				double azimuthIncrement = Double.valueOf(parameters[5]);
				buildTree(origin.getWorld(), origin, branchLength, branchLengthMultipler, 0, rollIncrement, 90,
						azimuthIncrement, BlockType.AcaciaWood);
				break;
			}
			default:
				server.broadcastMessage("Cannot build a " + command);
			}
		}
	}

	/**
	 * Build circular stairs into the sky
	 * 
	 * @param origin      center point of the circle
	 * @param height      height of the stairs
	 * @param innerRadius distance from center of circle to the inner edge of each
	 *                    stair
	 * @param outerRadius distance from center of circle to the outer edge of each
	 *                    stair
	 * @param thickness   thickness of each stair at the outer edge
	 */
	public void buildStairs(Location origin, double height, double innerRadius, double outerRadius, double thickness) {
		double angle = 0;
		double angleIncrement = Math.asin(1.0 / outerRadius) * 180.0 / Math.PI;
		BlockType type = randomBlockType();
		for (int y = 0; y < height; y++) {
			position.setY(origin.getY() + y);
			for (int t = 0; t < thickness; t++) {
				for (double r = innerRadius; r < outerRadius; r++) {
					double x = r * Math.cos(angle * Math.PI / 180);
					double z = r * Math.sin(angle * Math.PI / 180);
					position.setX(origin.getX() + x);
					position.setZ(origin.getZ() + z);
					origin.getWorld().setBlockAt(position, type);
				}
				angle += angleIncrement;
			}
		}
	}

	/**
	 * -180 < rotation <= 180
	 * 
	 * -90 < pitch <= 90
	 * 
	 * -x = rotation 90, pitch 0 = west
	 * 
	 * +z = rotation 0, pitch 0 = south
	 * 
	 * +y = rotation any, pitch -90 = up
	 * 
	 * rotation 90 = -x = west
	 * 
	 * rotation 0 = +z = south
	 * 
	 * rotation -90 = +x = east
	 * 
	 * @param location
	 * @param length
	 * @param radius
	 * @param gap
	 * @param type
	 */
	public void buildCylinder(Location location, double length, double radius, double gap, BlockType type) {
		double cosPitch = Math.cos(location.getPitch() * Math.PI / 180);
		double sinPitch = Math.sin(location.getPitch() * Math.PI / 180);
		double cosRot = Math.cos(location.getRotation() * Math.PI / 180);
		double sinRot = Math.sin(location.getRotation() * Math.PI / 180);
		Location tmpLocation = location.clone();
		for (double cZ = 1; cZ <= length; cZ += 1) {
			double z = cZ * cosPitch * cosRot;
			double x = -cZ * cosPitch * sinRot;
			double y = -cZ * sinPitch;
			tmpLocation.setX(Math.round(location.getX() + x));
			tmpLocation.setY(Math.round(location.getY() + y));
			tmpLocation.setZ(Math.round(location.getZ() + z));
			buildCircle(tmpLocation, radius, gap, type);
		}
	}

	/**
	 * Draw a circle in the plane perpendicular to the location vector.
	 * 
	 * UNFINISHED -- doesn't draw in perpendicular plane
	 * 
	 * @param location
	 * @param radius
	 * @param gap
	 * @param type
	 */
	public void buildCircle(Location location, double radius, double gap, BlockType type) {
		double angleIncrement = Math.asin((gap + 1.0) / radius) * 180.0 / Math.PI;
		for (double angle = 0; angle < 180; angle += angleIncrement) {
			double cosA = Math.cos(angle * Math.PI / 180);
			double sinA = Math.sin(angle * Math.PI / 180);
			for (double r = -radius; r <= radius; r += 1) {
				Vector3D in = new Vector3D(r * cosA, -r * sinA, 1);
				Vector3D out = rotate(in, location.getPitch(), location.getRotation());
				position.setX(Math.round(location.getX() + out.getX()));
				position.setY(Math.round(location.getY() + out.getY()));
				position.setZ(Math.round(location.getZ() + out.getZ()));
				if (gap < .5 && r >= -radius + 1 && r <= radius - 1)
					location.getWorld().setBlockAt(position, BlockType.Air);
				else
					location.getWorld().setBlockAt(position, type);
			}
		}
	}

	public void buildRay(Location location, double length, BlockType type) {
		double cosPitch = Math.cos(location.getPitch() * Math.PI / 180);
		double sinPitch = Math.sin(location.getPitch() * Math.PI / 180);
		double cosRot = Math.cos(location.getRotation() * Math.PI / 180);
		double sinRot = Math.sin(location.getRotation() * Math.PI / 180);
		for (double cZ = 1; cZ <= length; cZ += 1) {
			double z = cZ * cosPitch * cosRot;
			double x = -cZ * cosPitch * sinRot;
			double y = -cZ * sinPitch;
			position.setX(location.getX() + x);
			position.setY(location.getY() + y);
			position.setZ(location.getZ() + z);
			location.getWorld().setBlockAt(position, type);
		}
	}

	/**
	 * @param radius    width of the tree
	 * @param density   branching factor
	 * @param height    height of the tree
	 * @param thickness thickness of the tree's trunk
	 */
	public void buildTree(World world, Position startPosition, double branchLength, double branchLengthMultipler,
			double roll, double rollIncrement, double azimuth, double azimuthIncrement, BlockType type) {
		if (branchLength >= 2 && azimuth < 225 && azimuth > -45) {
			Position myPosition = new Position();
			for (int radius = 0; radius < branchLength; radius++) {
				double y = radius * Math.sin(azimuth * Math.PI / 180);
				double horizontalRadius = radius * Math.cos(azimuth * Math.PI / 180);
				double x = horizontalRadius * Math.cos(roll * Math.PI / 180);
				double z = horizontalRadius * Math.sin(roll * Math.PI / 180);
				myPosition.setX(startPosition.getX() + x);
				myPosition.setY(startPosition.getY() + y);
				myPosition.setZ(startPosition.getZ() + z);
				world.setBlockAt(myPosition, type);
			}
			type = randomBlockType();
			for (double angle = roll; angle < 360 + roll; angle += rollIncrement) {
				buildTree(world, myPosition, branchLengthMultipler * branchLength, branchLengthMultipler, angle,
						rollIncrement, azimuth + azimuthIncrement, azimuthIncrement, type);
			}
		}
	}

	/**
	 * Build a sphere.
	 * 
	 * @param origin        center point of the sphere
	 * @param radius
	 * @param horizontalGap amount of space to leave between vertical rings of the
	 *                      sphere
	 * @param verticalGap   amount of space to leave between blocks in the vertical
	 *                      rings of the sphere
	 */
	public void buildSphere(Location origin, double radius, BlockType type, double horizontalGap, double verticalGap) {
		// Base is i X j, k goes up for height
		double horizontalAngleIncrement = Math.asin((horizontalGap + 1.0) / radius) * 180.0 / Math.PI;
		double verticalAngleIncrement = Math.asin((verticalGap + 1.0) / radius) * 180.0 / Math.PI;
		for (double horizontalAngle = 0; horizontalAngle < 180; horizontalAngle += horizontalAngleIncrement) {
			for (double verticalAngle = 0; verticalAngle < 360; verticalAngle += verticalAngleIncrement) {
				double y = radius * Math.sin(verticalAngle * Math.PI / 180);
				double horizontalRadius = radius * Math.cos(verticalAngle * Math.PI / 180);
				double x = horizontalRadius * Math.cos(horizontalAngle * Math.PI / 180);
				double z = horizontalRadius * Math.sin(horizontalAngle * Math.PI / 180);
				position.setX(origin.getX() + x);
				position.setY(origin.getY() + y);
				position.setZ(origin.getZ() + z);
				origin.getWorld().setBlockAt(position, type);
			}
		}
	}

	/**
	 * Build a cube. Fill the inside with air.
	 * 
	 * @param world
	 * @param origin
	 * @param width
	 * @param height
	 * @param type
	 */
	private void makeCube(final World world, final Position origin, int width, int height, final BlockType type) {
		double x = origin.getX();
		double y = origin.getY();
		double z = origin.getZ();
		// Base is i X j, k goes up for height
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < width; j++) {
				for (int k = 0; k < height; k++) {
					position.setX(x + i);
					position.setY(y + k);
					position.setZ(z + j);
					if (i > 0 && j > 0 && k > 0 && i < width - 1 && j < width - 1 && k < height - 1)
						world.setBlockAt(position, BlockType.Air);
					else
						world.setBlockAt(position, type);
				}
			}
		}
	}

	/**
	 * Build a house
	 * 
	 * @param origin
	 * @param width
	 * @param height
	 */
	public void buildHouse(Location origin, int width, int height) {

		// Position the house so its door is in front of the player
		origin.setX(origin.getX() - (width / 2));
		origin.setZ(origin.getZ() + 1);
		origin.setY(origin.getY() - 1);

		makeCube(origin.getWorld(), origin, width, height, randomBlockType());

		// Pop a door in one wall
		Location door = new Location(origin.getWorld(), origin.getX() + (width / 2), origin.getY(), origin.getZ(), 0,
				0);

		// The door is two high, with a torch over the door
		// Magic values to establish top and bottom of door.
		// 64 is BlockType.WoodenDoor

		door.setY(door.getY() + 1);
		origin.getWorld().setBlockAt(door, (short) 64, (short) 0x4);// bottom

		door.setY(door.getY() + 1);
		origin.getWorld().setBlockAt(door, (short) 64, (short) 0x8);// top

		door.setY(door.getY() + 1);
		door.setZ(door.getZ() + 1);
		origin.getWorld().setBlockAt(door, BlockType.Torch);

		// Move over to make next house if called in a loop.
		origin.setX(origin.getX() + width);
	}

	public static Vector3D rotate(final Vector3D in, double pitch, double rot) {
		pitch = Math.PI * pitch / 180.0;
		rot = Math.PI * rot / 180.0;
		// @formatter:off
		double x = in.getX() * Math.cos(rot) - in.getY() * Math.sin(rot) * Math.sin(pitch)
				- in.getZ() * Math.sin(rot) * Math.cos(pitch);
		double y = +in.getY() * Math.cos(pitch) - in.getZ() * Math.sin(pitch);
		double z = in.getX() * Math.sin(rot) + in.getY() * Math.cos(rot) * Math.sin(pitch)
				+ in.getZ() * Math.cos(rot) * Math.cos(pitch);
		// @formatter:on
		return new Vector3D(x, y, z);
	}

	public static Vector3D round(final Vector3D in) {
		return new Vector3D(Math.round(in.getX()), Math.round(in.getY()), Math.round(in.getZ()));
	}

	@SuppressWarnings("unused")
	private final Logman logger;
	private final Server server;
	private Position position = new Position();
	private int nextBlockTypeIndex = 0;
	private static BlockType[] blockTypes = { BlockType.OakWood, BlockType.SpruceWood, BlockType.BirchWood,
			BlockType.RedGlass, BlockType.GreenGlass, BlockType.Glass, BlockType.Tnt, BlockType.AcaciaWood,
			BlockType.CoalOre, BlockType.DarkOakWood, BlockType.GoldBlock, BlockType.JungleLog };

	public Builder(Logman logger, Server server) {
		this.logger = logger;
		this.server = server;
	}

	private BlockType randomBlockType() {
		return blockTypes[(int) (blockTypes.length * Math.random())];
	}

	private BlockType nextBlockType() {
		BlockType result = blockTypes[nextBlockTypeIndex];
		nextBlockTypeIndex += 1;
		if (nextBlockTypeIndex >= blockTypes.length)
			nextBlockTypeIndex = 0;
		return result;
	}

}
