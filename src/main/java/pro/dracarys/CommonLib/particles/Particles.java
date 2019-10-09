package pro.dracarys.CommonLib.particles;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import pro.dracarys.CommonLib.CommonLib;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Particles {

    public static void spawnColoredParticleFromPercent(Location loc, Player p, int percent) {
        Color color = getColor(percent);
        spawnColoredParticle(loc, p, color.getRed(), color.getGreen(), color.getBlue());
    }

    public static void spawnColoredParticle(Location loc, Player p, int red, int green, int blue) {
        if (CommonLib.getServerVersion() > 8) {
            if (CommonLib.getServerVersion() >= 13) { // 1.13.x -> 1.14.x
                p.spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(org.bukkit.Color.fromRGB(red, green, blue), 1));
            } else { // 1.9.x -> 1.12.x
                p.spawnParticle(Particle.REDSTONE, loc, 1, red, green, blue, 1);
            }
        } else { // 1.8.x
            ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(red, green, blue), loc, p);
        }
    }

    private static Color getColor(int percent) {
        float f = percent;
        float f1 = 100;
        float f2 = Math.max(0.0F, Math.min(f, f1) / f1);
        int color = Color.HSBtoRGB(f2 / 3.0F, 1.0F, 1.0F) | 0xFF000000;
        return new Color(color);
    }

    public java.util.List<Location> getHollowCube(String world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        List<Location> result = new ArrayList<>();
        for (float x = minX; x <= maxX; x += .5) {
            for (float y = minY; y <= maxY; y += .5) {
                for (float z = minZ; z <= maxZ; z += .5) {
                    int components = 0;
                    if (x == minX || x == maxX) components++;
                    if (y == minY || y == maxY) components++;
                    if (z == minZ || z == maxZ) components++;
                    if (components >= 2) {
                        result.add(new Location(Bukkit.getWorld(world), x, y, z));
                    }
                }
            }
        }
        return result;
    }
}
