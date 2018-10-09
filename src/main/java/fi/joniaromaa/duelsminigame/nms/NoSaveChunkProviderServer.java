package fi.joniaromaa.duelsminigame.nms;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.ChunkProviderServer;
import net.minecraft.server.v1_8_R3.IChunkLoader;
import net.minecraft.server.v1_8_R3.IProgressUpdate;

public class NoSaveChunkProviderServer extends ChunkProviderServer
{
	private static Field chunkLoaderField;
	
	static
	{
		try
		{
			NoSaveChunkProviderServer.chunkLoaderField = ChunkProviderServer.class.getDeclaredField("chunkLoader");
			NoSaveChunkProviderServer.chunkLoaderField.setAccessible(true);
		}
		catch (NoSuchFieldException | SecurityException e)
		{
			e.printStackTrace();
		}
	}
	
	public NoSaveChunkProviderServer(ChunkProviderServer chunkProviderServer) throws IllegalArgumentException, IllegalAccessException
	{
		super(chunkProviderServer.world, (IChunkLoader)NoSaveChunkProviderServer.chunkLoaderField.get(chunkProviderServer), chunkProviderServer.chunkProvider);
		
		this.unloadQueue = chunkProviderServer.unloadQueue;
		this.emptyChunk = chunkProviderServer.emptyChunk;
		this.forceChunkLoad = chunkProviderServer.forceChunkLoad;
		this.chunks = chunkProviderServer.chunks;
	}
	
	@Override
    public void saveChunkNOP(Chunk chunk)
    {
    	//Literally do nothing
    }
	
	@Override
    public void saveChunk(Chunk chunk)
    {
		//Literally do nothing
    }

	@Override
    public boolean saveChunks(boolean flag, IProgressUpdate iprogressupdate)
    {
		//Literally do nothing
		
		return true; //Spoof it
    }

	@Override
	public void c()
	{
		//Literally do nothing
	}

	//This method has CraftBukkit event on it, skip on this one so plugins dont break
	/*@Override
    public boolean unloadChunks()
    {
        return this.chunkProvider.unloadChunks();
    }*/

	@Override
    public boolean canSave()
    {
    	return false;
    }

	@Override
    public String getName()
    {
        // CraftBukkit - this.chunks.count() -> .size()
        return "NoSaveServerChunkCache: " + this.chunks.size() + " Drop: " + this.unloadQueue.size();
    }
}
