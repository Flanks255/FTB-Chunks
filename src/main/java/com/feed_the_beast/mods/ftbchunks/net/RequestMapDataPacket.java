package com.feed_the_beast.mods.ftbchunks.net;

import com.feed_the_beast.mods.ftbchunks.FTBChunks;
import com.feed_the_beast.mods.ftbchunks.api.ChunkDimPos;
import com.feed_the_beast.mods.ftbchunks.api.ClaimedChunk;
import com.feed_the_beast.mods.ftbchunks.api.FTBChunksAPI;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * @author LatvianModder
 */
public class RequestMapDataPacket
{
	public RequestMapDataPacket()
	{
	}

	RequestMapDataPacket(PacketBuffer buf)
	{
	}

	void write(PacketBuffer buf)
	{
	}

	void handle(Supplier<NetworkEvent.Context> context)
	{
		context.get().enqueueWork(() -> {
			ServerPlayerEntity player = context.get().getSender();
			LinkedHashMap<NetClaimedChunkGroup, NetClaimedChunkGroup> groups = new LinkedHashMap<>();
			LinkedHashMap<ChunkDimPos, NetClaimedChunk> chunks = new LinkedHashMap<>();
			NetClaimedChunkData data = new NetClaimedChunkData();
			data.maxClaimed = 100;
			data.maxLoaded = 25;

			for (ClaimedChunk chunk : FTBChunksAPI.INSTANCE.getManager().getData(player).getClaimedChunks())
			{
				data.claimed++;

				if (chunk.isForceLoaded())
				{
					data.loaded++;
				}
			}

			DimensionType dim = player.dimension;
			int cx = player.chunkCoordX;
			int cz = player.chunkCoordZ;

			for (int z = -FTBChunks.TILE_OFFSET; z <= FTBChunks.TILE_OFFSET; z++)
			{
				for (int x = -FTBChunks.TILE_OFFSET; x <= FTBChunks.TILE_OFFSET; x++)
				{
					ClaimedChunk chunk = FTBChunksAPI.INSTANCE.getManager().getChunk(new ChunkDimPos(dim, x + cx, z + cz));

					if (chunk != null)
					{
						NetClaimedChunkGroup g = new NetClaimedChunkGroup();
						g.color = chunk.getColor();
						g.forceLoaded = chunk.isForceLoaded() && chunk.getPlayerData().isAlly(player);
						g.owner = chunk.getDisplayName().deepCopy();

						NetClaimedChunk c = new NetClaimedChunk();
						c.group = groups.computeIfAbsent(g, g1 -> {
							g1.id = groups.size();
							return g1;
						});

						c.x = x;
						c.z = z;
						c.borders = 0;
						chunks.put(chunk.getPos(), c);
					}
				}
			}

			NetClaimedChunk[] connectionChunks = new NetClaimedChunk[4];

			for (NetClaimedChunk c : chunks.values())
			{
				connectionChunks[0] = chunks.get(new ChunkDimPos(dim, c.x + cx, c.z + cz - 1));
				connectionChunks[1] = chunks.get(new ChunkDimPos(dim, c.x + cx, c.z + cz + 1));
				connectionChunks[2] = chunks.get(new ChunkDimPos(dim, c.x + cx - 1, c.z + cz));
				connectionChunks[3] = chunks.get(new ChunkDimPos(dim, c.x + cx + 1, c.z + cz));

				for (int i = 0; i < 4; i++)
				{
					if (connectionChunks[i] == null || !connectionChunks[i].group.connect(c.group))
					{
						c.borders |= 1 << i;
					}
				}
			}

			data.groups = new ArrayList<>(groups.values());
			data.chunks = new ArrayList<>(chunks.values());
			FTBChunksNet.MAIN.send(PacketDistributor.PLAYER.with(() -> player), new SendMapDataPacket(data));
		});


		context.get().setPacketHandled(true);
	}
}