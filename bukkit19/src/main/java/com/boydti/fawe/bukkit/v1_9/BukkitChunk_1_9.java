package com.boydti.fawe.bukkit.v1_9;

import com.boydti.fawe.FaweCache;
import com.boydti.fawe.example.CharFaweChunk;
import com.boydti.fawe.util.FaweQueue;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.DataBits;
import net.minecraft.server.v1_9_R1.DataPalette;
import net.minecraft.server.v1_9_R1.DataPaletteBlock;
import net.minecraft.server.v1_9_R1.DataPaletteGlobal;
import net.minecraft.server.v1_9_R1.IBlockData;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;

public class BukkitChunk_1_9 extends CharFaweChunk<Chunk> {

    public DataPaletteBlock[] sectionPalettes;

    /**
     * A FaweSections object represents a chunk and the blocks that you wish to change in it.
     *
     * @param parent
     * @param x
     * @param z
     */
    public BukkitChunk_1_9(FaweQueue parent, int x, int z) {
        super(parent, x, z);
    }

    @Override
    public Chunk getNewChunk() {
        return Bukkit.getWorld(getParent().world).getChunkAt(getX(), getZ());
    }

    @Override
    public CharFaweChunk<Chunk> copy(boolean shallow) {
        BukkitChunk_1_9 value = (BukkitChunk_1_9) super.copy(shallow);
        if (sectionPalettes != null) {
            value.sectionPalettes = new DataPaletteBlock[16];
            try {
                Field fieldBits = DataPaletteBlock.class.getDeclaredField("b");
                fieldBits.setAccessible(true);
                Field fieldPalette = DataPaletteBlock.class.getDeclaredField("c");
                fieldPalette.setAccessible(true);
                Field fieldSize = DataPaletteBlock.class.getDeclaredField("e");
                fieldSize.setAccessible(true);
                for (int i = 0; i < sectionPalettes.length; i++) {
                    DataPaletteBlock current = sectionPalettes[i];
                    if (current == null) {
                        continue;
                    }
                    DataPaletteBlock paletteBlock = new DataPaletteBlock();
                    // Clone palette
                    DataPalette currentPalette = (DataPalette) fieldPalette.get(paletteBlock);
                    if (!(currentPalette instanceof DataPaletteGlobal)) {
                        try {
                            Method resize = DataPaletteBlock.class.getDeclaredMethod("b", int.class);
                            resize.setAccessible(true);
                            resize.invoke(paletteBlock, 128);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                    currentPalette = (DataPalette) fieldPalette.get(paletteBlock);
                    fieldPalette.set(paletteBlock, currentPalette);
                    // Clone size
                    fieldSize.set(paletteBlock, fieldSize.get(current));
                    // Clone pallete
                    DataBits currentBits = (DataBits) fieldBits.get(current);
                    DataBits newBits = new DataBits(1, 0);
                    for (Field field : DataBits.class.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(newBits, field.get(currentBits));
                    }
                    fieldBits.set(paletteBlock, newBits);
                    value.sectionPalettes[i] = paletteBlock;
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public void optimize() {
        if (sectionPalettes != null) {
            return;
        }
        char[][] arrays = getIdArrays();
        IBlockData lastBlock = null;
        char lastChar = Character.MAX_VALUE;
        for (int layer = 0; layer < 16; layer++) {
            if (getCount(layer) > 0) {
                if (sectionPalettes == null) {
                    sectionPalettes = new DataPaletteBlock[16];
                }
                DataPaletteBlock palette = sectionPalettes[layer] = new DataPaletteBlock();
                char[] blocks = getIdArray(layer);
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; x++) {
                            char combinedId = blocks[FaweCache.CACHE_J[y][x][z]];
                            if (combinedId > 1) {
                                palette.setBlock(x, y, z, Block.getById(combinedId >> 4).fromLegacyData(combinedId & 0xF));
                            }
                        }
                    }
                }
            }
        }
    }
}