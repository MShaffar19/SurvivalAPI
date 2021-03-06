package net.samagames.survivalapi;

import net.minecraft.server.v1_8_R3.*;
import net.samagames.survivalapi.games.AbstractGame;
import net.samagames.survivalapi.games.Game;
import net.samagames.survivalapi.gen.WorldLoader;
import net.samagames.survivalapi.utils.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * This file is part of SurvivalAPI.
 *
 * SurvivalAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SurvivalAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SurvivalAPI.  If not, see <http://www.gnu.org/licenses/>.
 */
public class SurvivalGenerator extends JavaPlugin
{
    private List<BiomeBase> biomesToRemove;
    private AbstractGame game;
    private BukkitTask startTimer;
    private boolean worldLoaded;
    private WorldLoader worldLoader;

    @Override
    public void onEnable()
    {
        this.saveDefaultConfig();
        this.biomesToRemove = new ArrayList<>();

        String gameRaw = this.getConfig().getString("game", "UHC");

        try
        {
            this.game = Game.valueOf(gameRaw).getGameClass().getConstructor(SurvivalGenerator.class).newInstance(this);

            this.patchBiomes();
            this.game.preInit();

            this.getServer().getPluginManager().registerEvents(this.game, this);

            this.startTimer = getServer().getScheduler().runTaskTimer(this, this::postInit, 20L, 20L);
        }
        catch (ReflectiveOperationException e)
        {
            e.printStackTrace();
        }
    }

    public void postInit()
    {
        this.startTimer.cancel();
        this.worldLoaded = true;

        this.worldLoader = new WorldLoader(this, this.getConfig().getInt("size", 1000), this.getConfig().getBoolean("strict", true));
        this.worldLoader.begin(this.getServer().getWorld("world"));
    }

    public void finishGeneration(World world, long time)
    {
        this.getLogger().info("Ready in " + time + "ms");
        Bukkit.shutdown();
    }

    public void addBiomeToRemove(BiomeBase biomeBase)
    {
        this.biomesToRemove.add(biomeBase);
    }

    public boolean isWorldLoaded()
    {
        return this.worldLoaded;
    }

    private void patchBiomes() throws ReflectiveOperationException
    {
        BiomeBase[] biomes = BiomeBase.getBiomes();
        Map<String, BiomeBase> biomesMap = BiomeBase.o;
        BiomeBase defaultBiome = BiomeBase.FOREST;

        Reflection.setFinalStatic(BiomeBase.class.getDeclaredField("ad"), defaultBiome);

        biomesMap.remove(BiomeBase.OCEAN.ah);
        biomesMap.remove(BiomeBase.DEEP_OCEAN.ah);
        biomesMap.remove(BiomeBase.FROZEN_OCEAN.ah);

        for (BiomeBase biomeBase : this.biomesToRemove)
            biomesMap.remove(biomeBase.ah);

        for (int i = 0; i < biomes.length; i++)
        {
            if (biomes[i] != null)
            {
                if (!biomesMap.containsKey(biomes[i].ah))
                    biomes[i] = defaultBiome;

                this.setReedsPerChunk(biomes[i], 64);
            }
        }

        Reflection.setFinalStatic(BiomeBase.class.getDeclaredField("biomes"), biomes);
    }

    public AbstractGame getGame()
    {
        return this.game;
    }

    public WorldLoader getWorldLoader()
    {
        return worldLoader;
    }

    private void setReedsPerChunk(BiomeBase biome, int value) throws NoSuchFieldException, IllegalAccessException
    {
        Reflection.setValue(biome.as, BiomeDecorator.class, true, "F", value);
    }
}
