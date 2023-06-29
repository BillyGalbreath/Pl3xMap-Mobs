/*
 * MIT License
 *
 * Copyright (c) 2020-2023 William Blake Galbreath
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package net.pl3x.map.mobs.markers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.WorldLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.markers.option.Tooltip;
import net.pl3x.map.mobs.Pl3xMapMobs;
import net.pl3x.map.mobs.configuration.WorldConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class MobsLayer extends WorldLayer {
    public static final String KEY = "pl3xmap_mobs";

    private final Pl3xMapMobs plugin;
    private final WorldConfig config;
    private final List<Marker<?>> markers = new ArrayList<>();

    private final Object syncLock = new Object();

    private boolean running;

    public MobsLayer(@NotNull Pl3xMapMobs plugin, @NotNull WorldConfig config) {
        super(KEY, config.getWorld(), () -> config.LAYER_LABEL);
        this.plugin = plugin;
        this.config = config;

        setShowControls(config.LAYER_SHOW_CONTROLS);
        setDefaultHidden(config.LAYER_DEFAULT_HIDDEN);
        setUpdateInterval(config.LAYER_UPDATE_INTERVAL);
        setPriority(config.LAYER_PRIORITY);
        setZIndex(config.LAYER_ZINDEX);
    }

    @Override
    public @NotNull Collection<Marker<?>> getMarkers() {
        synchronized (this.syncLock) {
            if (!this.running) {
                this.running = true;
                Bukkit.getScheduler().runTask(this.plugin, this::syncUpdate);
            }
            return this.markers;
        }
    }

    public void syncUpdate() {
        synchronized (this.syncLock) {
            Collection<Marker<?>> markers = new HashSet<>();
            World bukkitWorld = Bukkit.getWorld(this.config.getWorld().getName());
            if (bukkitWorld == null) {
                return;
            }
            bukkitWorld.getEntitiesByClass(Mob.class).forEach(mob -> {
                if (config.ONLY_SHOW_MOBS_EXPOSED_TO_SKY && bukkitWorld.getHighestBlockYAt(mob.getLocation()) > mob.getLocation().getY()) {
                    return;
                }
                String key = String.format("%s_%s_%s", KEY, getWorld().getName(), mob.getUniqueId());
                markers.add(Marker.icon(key, point(mob.getLocation()), Icon.get(mob).getKey(), this.config.ICON_SIZE)
                        .setOptions(Options.builder()
                                .tooltipDirection(Tooltip.Direction.TOP)
                                .tooltipContent(config.ICON_TOOLTIP_CONTENT
                                        .replace("<mob-id>", mob(mob))
                                ).build()));
            });
            this.markers.clear();
            this.markers.addAll(markers);
            this.running = false;
        }
    }

    private @NotNull String mob(@NotNull Mob mob) {
        @SuppressWarnings("deprecation")
        String name = mob.getCustomName();
        return name == null ? mob.getName() : name;
    }

    private @NotNull Point point(@NotNull Location loc) {
        return Point.of(loc.getBlockX(), loc.getBlockZ());
    }
}
