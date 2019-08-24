package test;

import house.Builder;
import net.canarymod.api.world.position.Vector3D;

public class Main {
	public static void main(String[] args) {
		rotate(0,0,1,90,0); // down
		rotate(0,0,1,-90,0); // up
		rotate(0,0,1,0,90);  // -x (west)
		rotate(0,0,1,-90,90); // up
		rotate(0,0,1,0,45); // 
	}
	
	private static void rotate(int x, int y, int z, int pitch, int roll) {
		Vector3D in = new Vector3D(x, y, z);
		Vector3D out = Builder.rotate(in, pitch, roll);
		System.out.format("rotate %s -> %s\n", in, out);
	}
}
