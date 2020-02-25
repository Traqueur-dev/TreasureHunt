package fr.traqueur.treasurehunt.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.google.common.collect.Lists;

public class Cuboid {
    private String worldName;
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private int z1;
    private int z2; 
    private transient List<Chunk> chunks;

    public Cuboid() {
    	this.chunks = Lists.newArrayList();
    }

    public Cuboid(Location location1, Location location2) {
        this.worldName = location1.getWorld().getName();
        this.x1 = Math.min(location1.getBlockX(), location2.getBlockX());
        this.y1 = Math.min(location1.getBlockY(), location2.getBlockY());
        this.z1 = Math.min(location1.getBlockZ(), location2.getBlockZ());
        this.x2 = Math.max(location1.getBlockX(), location2.getBlockX());
        this.y2 = Math.max(location1.getBlockY(), location2.getBlockY());
        this.z2 = Math.max(location1.getBlockZ(), location2.getBlockZ());
    	this.chunks = Lists.newArrayList();
    }

    public Cuboid(Location location) {
        this(location, location);
    }

    public Cuboid(Cuboid cuboid) {
        this(cuboid.getWorld().getName(), cuboid.x1, cuboid.y1, cuboid.z1, cuboid.x2, cuboid.y2, cuboid.z2);
    }

    public Cuboid(String string, int n, int n2, int n3, int n4, int n5, int n6) {
        this.worldName = string;
        this.x1 = Math.min(n, n4);
        this.x2 = Math.max(n, n4);
        this.y1 = Math.min(n2, n5);
        this.y2 = Math.max(n2, n5);
        this.z1 = Math.min(n3, n6);
        this.z2 = Math.max(n3, n6);
    }

    public Location getLowerLocation() {
        return new Location(this.getWorld(), (double)this.x1, (double)this.y1, (double)this.z1);
    }

    public Location getUpperLocation() {
        return new Location(this.getWorld(), (double)this.x2, (double)this.y2, (double)this.z2);
    }

    public Location getCenter() {
        int i = this.getUpperX() + 1;
        int j = this.getUpperY() + 1;
        int k = this.getUpperZ() + 1;
        return new Location(this.getWorld(), (double)this.getLowerX() + (double)(i - this.getLowerX()) / 2.0, (double)this.getLowerY() + (double)(j - this.getLowerY()) / 2.0, (double)this.getLowerZ() + (double)(k - this.getLowerZ()) / 2.0);
    }

    public World getWorld() {
        World world = Bukkit.getWorld((String)this.worldName);
        if (world == null) {
            Logger.getLogger("Le monde " + this.worldName + "n'est pas charg\ufffd !");
        }
        return world;
    }

    public int getSizeX() {
        return this.x2 - this.x1 + 1;
    }

    public int getSizeY() {
        return this.y2 - this.y1 + 1;
    }

    public int getSizeZ() {
        return this.z2 - this.z1 + 1;
    }

    public int getLowerX() {
        return this.x1;
    }

    public int getLowerY() {
        return this.y1;
    }

    public int getLowerZ() {
        return this.z1;
    }

    public int getUpperX() {
        return this.x2;
    }

    public int getUpperY() {
        return this.y2;
    }

    public int getUpperZ() {
        return this.z2;
    }

    public List<Player> getPlayersInside() {
        ArrayList<Player> list = new ArrayList<Player>();
        @SuppressWarnings("deprecation")
		Player[] arrplayer = Bukkit.getOnlinePlayers();
        int n = arrplayer.length;
        int n2 = 0;
        while (n2 < n) {
            Player player = arrplayer[n2];
            if (this.contains(player.getLocation())) {
                list.add(player);
            }
            ++n2;
        }
        return list;
    }

    public List<LivingEntity> getLivingEntityInside() {
        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        for (LivingEntity entity : this.getWorld().getLivingEntities()) {
            if (!this.contains(entity.getLocation())) continue;
            list.add(entity);
        }
        return list;
    }

    public List<LivingEntity> getLivingEntityInside(EntityType type) {
        ArrayList<LivingEntity> list = new ArrayList<LivingEntity>();
        for (LivingEntity entity : this.getWorld().getLivingEntities()) {
            if (!this.contains(entity.getLocation()) || entity.getType() != type) continue;
            list.add(entity);
        }
        return list;
    }

    public boolean isInside(Player player) {
        return this.contains(player.getLocation());
    }

    public Block[] corners() {
        Block[] arrayOfBlock = new Block[8];
        World localWorld = this.getWorld();
        arrayOfBlock[0] = localWorld.getBlockAt(this.x1, this.y1, this.z1);
        arrayOfBlock[1] = localWorld.getBlockAt(this.x1, this.y1, this.z2);
        arrayOfBlock[2] = localWorld.getBlockAt(this.x1, this.y2, this.z1);
        arrayOfBlock[3] = localWorld.getBlockAt(this.x1, this.y2, this.z2);
        arrayOfBlock[4] = localWorld.getBlockAt(this.x2, this.y1, this.z1);
        arrayOfBlock[5] = localWorld.getBlockAt(this.x2, this.y1, this.z2);
        arrayOfBlock[6] = localWorld.getBlockAt(this.x2, this.y2, this.z1);
        arrayOfBlock[7] = localWorld.getBlockAt(this.x2, this.y2, this.z2);
        return arrayOfBlock;
    }

    public boolean contains(int n, int n2, int n3) {
        if (n >= this.x1 && n <= this.x2 && n2 >= this.y1 && n2 <= this.y2 && n3 >= this.z1 && n3 <= this.z2) {
            return true;
        }
        return false;
    }

    public boolean contains(Block block) {
        return this.contains(block.getLocation());
    }

    public boolean contains(Location location) {
        if (!this.worldName.equals(location.getWorld().getName())) {
            return false;
        }
        return this.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public int getVolume() {
        return this.getSizeX() * this.getSizeY() * this.getSizeZ();
    }

    public Cuboid getBoundingCuboid(Cuboid paramCuboid) {
        if (paramCuboid == null) {
            return this;
        }
        int i = Math.min(this.getLowerX(), paramCuboid.getLowerX());
        int j = Math.min(this.getLowerY(), paramCuboid.getLowerY());
        int k = Math.min(this.getLowerZ(), paramCuboid.getLowerZ());
        int m = Math.max(this.getUpperX(), paramCuboid.getUpperX());
        int n = Math.max(this.getUpperY(), paramCuboid.getUpperY());
        int i1 = Math.max(this.getUpperZ(), paramCuboid.getUpperZ());
        return new Cuboid(this.worldName, i, j, k, m, n, i1);
    }

    public Block getRelativeBlock(int n, int n2, int n3) {
        return this.getWorld().getBlockAt(this.x1 + n, this.y1 + n2, this.z1 + n3);
    }

    public Block getRelativeBlock(World world, int n, int n2, int n3) {
        return world.getBlockAt(this.x1 + n, this.y1 + n2, this.z1 + n3);
    }

    public List<Chunk> getChunks() {
    	if (! this.chunks.isEmpty()) {
			return this.chunks;
		}
        ArrayList<Chunk> arrayList = new ArrayList<Chunk>();
        World world = this.getWorld();
        int i = this.getLowerX() & -16;
        int j = this.getUpperX() & -16;
        int k = this.getLowerZ() & -16;
        int m = this.getUpperZ() & -16;
        int n = i;
        while (n <= j) {
            int i1 = k;
            while (i1 <= m) {
                arrayList.add(world.getChunkAt(n >> 4, i1 >> 4));
                i1 += 16;
            }
            n += 16;
        }
    	this.chunks.addAll(arrayList);
        return arrayList;
    }

    public void loadChunks() {
    	List<Chunk> chunks = this.getChunks();
    	for (int i = 0; i < chunks.size(); i++) {
    		Chunk chunk = chunks.get(i);
    		chunk.load(true);
    	}
    }
    
    public Cuboid clone() {
        return new Cuboid(this);
    }

    public String toString() {
        return new String("Cuboid: " + this.worldName + "," + this.x1 + "," + this.y1 + "," + this.z1 + "=>" + this.x2 + "," + this.y2 + "," + this.z2);
    }
}

