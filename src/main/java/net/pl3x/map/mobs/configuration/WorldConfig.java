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
package net.pl3x.map.mobs.configuration;

import java.nio.file.Path;
import java.util.Map;
import libs.org.simpleyaml.configuration.ConfigurationSection;
import net.pl3x.map.core.configuration.AbstractConfig;
import net.pl3x.map.core.markers.Vector;
import net.pl3x.map.core.world.World;
import net.pl3x.map.mobs.Pl3xMapMobs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WorldConfig extends AbstractConfig {
    @Key("layer.label")
    @Comment("""
            Label for map layer""")
    public String LAYER_LABEL = "Mobs";
    @Key("layer.show-controls")
    @Comment("""
            Show controls for map layer""")
    public boolean LAYER_SHOW_CONTROLS = true;
    @Key("layer.default-hidden")
    @Comment("""
            Whether map layer is hidden by default""")
    public boolean LAYER_DEFAULT_HIDDEN = false;
    @Key("layer.update-interval")
    @Comment("""
            Update interval for map layer""")
    public int LAYER_UPDATE_INTERVAL = 1;
    @Key("layer.priority")
    @Comment("""
            Priority for map layer""")
    public int LAYER_PRIORITY = 99;
    @Key("layer.z-index")
    @Comment("""
            zIndex for map layer""")
    public int LAYER_ZINDEX = 600;
    @Key("layer.pane")
    @Comment("""
            Name of the pane for the mob's layer.
            (Used for custom CSS attributes)""")
    public String LAYER_PANE = "pl3xmap_mobs";
    @Key("layer.css")
    @Comment("""
            Custom CSS for the mob's layer.""")
    public String LAYER_CSS = """
            div.leaflet-pl3xmap_mobs-pane img {
              border: 1px solid #000000C0;
              border-radius: 5px;
              box-shadow: 1px 2px 5px #00000080
            }""";

    @Key("marker.icon.size")
    @Comment("""
            The size (in pixels) the icon should be.""")
    public Vector ICON_SIZE = new Vector(20, 20);

    @Key("marker.popup.content")
    @Comment("""
            Contents of the icon's popup.""")
    public String ICON_TOOLTIP_CONTENT = "<mob-id>";

    @Key("only-show-mobs-exposed-to-sky")
    @Comment("""
            Only show mobs that are exposed to the sky.""")
    public boolean ONLY_SHOW_MOBS_EXPOSED_TO_SKY = true;

    private final World world;

    public WorldConfig(@NotNull World world) {
        this.world = world;
        reload();
    }

    public @NotNull World getWorld() {
        return this.world;
    }

    public void reload() {
        Path mainDir = Pl3xMapMobs.getPlugin(Pl3xMapMobs.class).getDataFolder().toPath();
        reload(mainDir.resolve("config.yml"), WorldConfig.class);
    }

    @Override
    protected @NotNull Object getClassObject() {
        return this;
    }

    @Override
    protected @Nullable Object getValue(@NotNull String path, @Nullable Object def) {
        if (getConfig().get("world-settings.default." + path) == null) {
            set("world-settings.default." + path, def);
        }
        return get("world-settings." + this.world.getName() + "." + path,
                get("world-settings.default." + path, def));
    }

    @Override
    protected void setComment(@NotNull String path, @Nullable String comment) {
        getConfig().setComment("world-settings.default." + path, comment);
    }

    @Override
    protected @Nullable Object get(@NotNull String path) {
        Object value = getConfig().get(path);
        if (value == null) {
            return null;
        }
        if ("marker.icon.size".equals(path.substring(path.indexOf(".", path.indexOf(".") + 1) + 1))) {
            if (value instanceof ConfigurationSection section) {
                return Vector.of(section.getDouble("x"), section.getDouble("z"));
            } else if (value instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Double> map = (Map<String, Double>) value;
                return Vector.of(map.get("x"), map.get("z"));
            }
        }
        return super.get(path);
    }

    @Override
    protected void set(@NotNull String path, @Nullable Object value) {
        if (value instanceof Vector vector) {
            value = Map.of("x", vector.x(), "z", vector.z());
        }
        getConfig().set(path, value);
    }
}
