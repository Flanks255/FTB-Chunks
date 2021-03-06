package com.feed_the_beast.mods.ftbchunks.api;

import net.minecraft.entity.Entity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.Objects;

/**
 * @author LatvianModder
 */
public class ChunkDimPos implements Comparable<ChunkDimPos>
{
	public final RegistryKey<World> dimension;
	public final int x;
	public final int z;
	private ChunkPos chunkPos;
	private int hash;

	public ChunkDimPos(RegistryKey<World> dim, int _x, int _z)
	{
		dimension = dim;
		x = _x;
		z = _z;
	}

	public ChunkDimPos(RegistryKey<World> dim, ChunkPos pos)
	{
		this(dim, pos.x, pos.z);
	}

	public ChunkDimPos(World world, BlockPos pos)
	{
		this(world.getDimensionKey(), pos.getX() >> 4, pos.getZ() >> 4);
	}

	public ChunkDimPos(Entity entity)
	{
		this(entity.world, entity.getPosition());
	}

	public ChunkPos getChunkPos()
	{
		if (chunkPos == null)
		{
			chunkPos = new ChunkPos(x, z);
		}

		return chunkPos;
	}

	@Override
	public String toString()
	{
		return "[" + dimension.getLocation() + ":" + x + ":" + z + "]";
	}

	@Override
	public int hashCode()
	{
		if (hash == 0)
		{
			hash = Objects.hash(dimension.getLocation(), x, z);

			if (hash == 0)
			{
				hash = 1;
			}
		}

		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj instanceof ChunkDimPos)
		{
			ChunkDimPos p = (ChunkDimPos) obj;
			return dimension == p.dimension && x == p.x && z == p.z;
		}

		return false;
	}

	@Override
	public int compareTo(ChunkDimPos o)
	{
		int i = dimension.getLocation().compareTo(o.dimension.getLocation());
		return i == 0 ? Long.compare(getChunkPos().asLong(), o.getChunkPos().asLong()) : i;
	}

	public ChunkDimPos offset(int ox, int oz)
	{
		return new ChunkDimPos(dimension, x + ox, z + oz);
	}
}