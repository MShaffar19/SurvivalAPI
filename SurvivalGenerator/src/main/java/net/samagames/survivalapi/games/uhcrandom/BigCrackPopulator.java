package net.samagames.survivalapi.games.uhcrandom;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import java.util.Random;

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
public class BigCrackPopulator extends BlockPopulator
{
    private static final int CRACK_LENGTH = 4;
    private static final int DECAY_LENGTH = 5;
    private static final int DECAY_DEPTH = 15;

    @Override
    public void populate(World world, Random random, Chunk chunk)
    {
        for (int x = 0; x < 16; x++)
        {
            final int currentX = chunk.getX() * 16 + x;

            if (Math.abs(currentX) - CRACK_LENGTH - DECAY_LENGTH > 0)
                continue;

            if (Math.abs(currentX) - CRACK_LENGTH <= 0)
            {
                // Hole

                for (int z = 0; z < 16; z++)
                    for (int y = 0; y < 256; y++)
                        chunk.getBlock(x, y, z).setType(Material.AIR);

            }
            else
            {
                // Decay

                int decayLocation = CRACK_LENGTH - Math.abs(Math.abs(currentX) - CRACK_LENGTH);
                double decay = (double) decayLocation / (double) DECAY_LENGTH;

                for (int z = 0; z < 16; z++)
                {
                    int begin = 0;

                    for (int y = 255; y >= 0; y--)
                        if (chunk.getBlock(x, y, z).getType() != Material.AIR)
                        {
                            begin = y;
                            break;
                        }

                    for (int i = begin; i >= begin - ((DECAY_DEPTH  - random.nextInt(5)) * decay) && begin >= 0; i--)
                        chunk.getBlock(x, i, z).setType(Material.AIR);
                }
            }


        }
    }
}