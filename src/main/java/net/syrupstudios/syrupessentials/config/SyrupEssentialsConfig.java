package net.syrupstudios.syrupessentials.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import net.fabricmc.loader.api.FabricLoader;
import net.syrupstudios.syrupessentials.SyrupEssentials;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public final class SyrupEssentialsConfig {
    private static final String FILE_NAME = "syrup_essentials.json5";
    private static final Gson GSON = new GsonBuilder().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);

    private static volatile Values values = new Values();

    private SyrupEssentialsConfig() {
    }

    public static Values get() {
        return values;
    }

    public static Path getPath() {
        return CONFIG_PATH;
    }

    public static synchronized LoadResult load() {
        try {
            createDefaultFileIfMissing();

            Values loaded;
            try (Reader fileReader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8);
                 JsonReader jsonReader = new JsonReader(fileReader)) {
                jsonReader.setLenient(true);
                loaded = GSON.fromJson(jsonReader, Values.class);
            }

            if (loaded == null) {
                throw new JsonParseException("The config file is empty");
            }

            List<String> warnings = loaded.validate();
            values = loaded;
            SyrupEssentials.LOGGER.info("Loaded config from {}", CONFIG_PATH);
            warnings.forEach(warning -> SyrupEssentials.LOGGER.warn("Config: {}", warning));
            return new LoadResult(true, warnings, null);
        } catch (IOException | JsonParseException | IllegalStateException e) {
            SyrupEssentials.LOGGER.error("Could not load config from {}; keeping the previous configuration", CONFIG_PATH, e);
            return new LoadResult(false, List.of(), e.getMessage());
        }
    }

    private static void createDefaultFileIfMissing() throws IOException {
        if (Files.exists(CONFIG_PATH)) {
            return;
        }

        Files.createDirectories(CONFIG_PATH.getParent());
        Files.writeString(
                CONFIG_PATH,
                defaultConfig(),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.WRITE
        );
        SyrupEssentials.LOGGER.info("Created default config at {}", CONFIG_PATH);
    }

    private static String defaultConfig() {
        return """
                /*
                 * Syrup Essentials config file
                 * Run /syrupessentials reload to apply edits that are not marked as restart-only.
                 */
                {
                  /*
                   * Keeps short root commands such as /home available while command namespacing is active.
                   * Ignored when register_to_namespace is false.
                   * Requires a server restart.
                   * Default: false
                   */
                  register_alias_as_well_as_namespace: false,
                  /*
                   * Places this mod's commands below /syrupessentials to avoid collisions with other mods.
                   * When disabled, commands remain at the root, for example /home and /warp.
                   * Requires a server restart.
                   * Default: false
                   */
                  register_to_namespace: false,
                  // Settings for travel commands and saved destinations
                  teleportation: {
                    // Controls the location history used by /back
                    back: {
                      // Default: true
                      enabled: true,
                      /*
                       * Number of recent positions retained for each player.
                       * Set this to 0 to disable history storage.
                       * Default: 10 | Range: 0 ~ 1000
                       */
                      max: 10
                    },
                    // Controls player-owned destinations created with /sethome
                    home: {
                      // Default: true
                      enabled: true,
                      /*
                       * Number of homes each player may own. Use -1 to remove the limit.
                       * Default: -1 | Range: -1 ~ 10000
                       */
                      max: -1
                    },
                    // Controls the operator-only /jump command
                    jump: {
                      // Default: true
                      enabled: true,
                      /*
                       * Furthest block that /jump can target.
                       * Default: 128 | Range: 1 ~ 1024
                       */
                      max_distance: 128
                    },
                    // Controls travel to the primary world's spawn point
                    spawn: {
                      // Default: true
                      enabled: true
                    },
                    /*
                     * Controls consent-based teleport requests between online players.
                     * This covers /tpa, /tpahere, /tpaccept, and /tpdeny.
                     */
                    tpa: {
                      // Default: true
                      enabled: true,
                      /*
                       * Seconds before an unanswered request expires.
                       * Default: 30 | Range: 1 ~ 3600
                       */
                      request_timeout: 30
                    },
                    // Controls the operator command for visiting a player's latest recorded position
                    tpl: {
                      // Default: true
                      enabled: true
                    },
                    // Controls direct operator travel between dimensions
                    tpx: {
                      // Default: true
                      enabled: true
                    },
                    // Controls shared server destinations managed through the warp commands
                    warp: {
                      // Default: true
                      enabled: true
                    }
                  },
                  // Background storage settings for player and world data
                  persistence: {
                    /*
                     * Seconds between periodic writes of changed data.
                       * Default: 180 | Range: 10 ~ 86400
                       */
                    autosave_interval: 180
                  }
                }
                """;
    }

    public record LoadResult(boolean successful, List<String> warnings, String error) {
    }

    public static final class Values {
        private boolean register_alias_as_well_as_namespace = false;
        private boolean register_to_namespace = false;
        private Teleportation teleportation = new Teleportation();
        private Persistence persistence = new Persistence();

        public boolean registerAliasAsWellAsNamespace() {
            return register_alias_as_well_as_namespace;
        }

        public boolean registerToNamespace() {
            return register_to_namespace;
        }

        public Teleportation teleportation() {
            return teleportation;
        }

        public Persistence persistence() {
            return persistence;
        }

        private List<String> validate() {
            List<String> warnings = new ArrayList<>();
            if (teleportation == null) {
                warnings.add("teleportation section is missing; using defaults");
                teleportation = new Teleportation();
            }
            if (persistence == null) {
                warnings.add("persistence section is missing; using defaults");
                persistence = new Persistence();
            }

            teleportation.validate(warnings);
            persistence.validate(warnings);
            return List.copyOf(warnings);
        }
    }

    public static final class Teleportation {
        private Tpa tpa = new Tpa();
        private Home home = new Home();
        private Toggle warp = new Toggle();
        private Back back = new Back();
        private Toggle spawn = new Toggle();
        private Toggle tpl = new Toggle();
        private Toggle tpx = new Toggle();
        private Jump jump = new Jump();

        public Tpa tpa() {
            return tpa;
        }

        public Home home() {
            return home;
        }

        public Toggle warp() {
            return warp;
        }

        public Back back() {
            return back;
        }

        public Toggle spawn() {
            return spawn;
        }

        public Toggle teleportLast() {
            return tpl;
        }

        public Toggle tpx() {
            return tpx;
        }

        public Jump jump() {
            return jump;
        }

        private void validate(List<String> warnings) {
            if (tpa == null) {
                warnings.add("teleportation.tpa is missing; using defaults");
                tpa = new Tpa();
            }
            if (home == null) {
                warnings.add("teleportation.home is missing; using defaults");
                home = new Home();
            }
            if (warp == null) {
                warnings.add("teleportation.warp is missing; using defaults");
                warp = new Toggle();
            }
            if (back == null) {
                warnings.add("teleportation.back is missing; using defaults");
                back = new Back();
            }
            if (spawn == null) {
                warnings.add("teleportation.spawn is missing; using defaults");
                spawn = new Toggle();
            }
            if (tpl == null) {
                warnings.add("teleportation.tpl is missing; using defaults");
                tpl = new Toggle();
            }
            if (tpx == null) {
                warnings.add("teleportation.tpx is missing; using defaults");
                tpx = new Toggle();
            }
            if (jump == null) {
                warnings.add("teleportation.jump is missing; using defaults");
                jump = new Jump();
            }

            tpa.request_timeout = clamp(
                    "teleportation.tpa.request_timeout",
                    tpa.request_timeout,
                    1,
                    3600,
                    warnings
            );
            home.max = clamp("teleportation.home.max", home.max, -1, 10000, warnings);
            back.max = clamp("teleportation.back.max", back.max, 0, 1000, warnings);
            jump.max_distance = clamp("teleportation.jump.max_distance", jump.max_distance, 1, 1024, warnings);
        }
    }

    public static class Toggle {
        private boolean enabled = true;

        public boolean enabled() {
            return enabled;
        }
    }

    public static final class Tpa extends Toggle {
        private int request_timeout = 30;

        public int requestTimeoutSeconds() {
            return request_timeout;
        }
    }

    public static final class Home extends Toggle {
        private int max = -1;

        public int maxHomes() {
            return max;
        }
    }

    public static final class Back extends Toggle {
        private int max = 10;

        public int maxHistory() {
            return max;
        }
    }

    public static final class Jump extends Toggle {
        private int max_distance = 128;

        public int maxDistance() {
            return max_distance;
        }
    }

    public static final class Persistence {
        private int autosave_interval = 180;

        public int autosaveIntervalSeconds() {
            return autosave_interval;
        }

        public long autosaveIntervalTicks() {
            return autosave_interval * 20L;
        }

        private void validate(List<String> warnings) {
            autosave_interval = clamp(
                    "persistence.autosave_interval",
                    autosave_interval,
                    10,
                    86400,
                    warnings
            );
        }
    }

    private static int clamp(String path, int value, int min, int max, List<String> warnings) {
        int clamped = Math.max(min, Math.min(max, value));
        if (clamped != value) {
            warnings.add(path + " must be between " + min + " and " + max + "; using " + clamped);
        }
        return clamped;
    }
}
