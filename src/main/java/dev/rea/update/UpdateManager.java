package dev.rea.update;

import dev.rea.ReaPlugin;
import dev.rea.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Auto-update, the safe way: Rea never swaps its own running classes.
 * Instead, on boot (and every few hours) it asks GitHub for the latest
 * release of tq1r/rea. If a newer version exists, its jar is downloaded
 * into {@code plugins/update/} — Paper replaces the old jar with it on
 * the next server start.
 *
 * Release assets must be a plugin jar (e.g. rea-1.0.1.jar). If the latest
 * release has no jar asset, or the network is unreachable, Rea logs a
 * single warning and carries on.
 */
public final class UpdateManager {

    private static final String API_LATEST = "https://api.github.com/repos/tq1r/rea/releases/latest";
    private static final String RELEASES_PAGE = "https://github.com/tq1r/rea/releases";
    private static final Pattern TAG = Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern JAR_URL = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.jar)\"");

    public enum Status {
        UP_TO_DATE, DOWNLOADED, NO_RELEASES, NO_JAR_ASSET, ERROR, DISABLED
    }

    public record Result(Status status, String currentVersion, String latestVersion, String detail) {
    }

    private final ReaPlugin plugin;
    private final HttpClient http;
    private final AtomicBoolean checkRunning = new AtomicBoolean(false);
    private volatile boolean notifiedThisSession = false;

    public UpdateManager(ReaPlugin plugin) {
        this.plugin = plugin;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    /** Current plugin version, from the jar manifest (falls back to 1.0.0). */
    public String currentVersion() {
        String v = plugin.getClass().getPackage().getImplementationVersion();
        return v == null || v.isBlank() ? "1.0.0" : v;
    }

    /** Kick off the hourly check (called on enable). Does nothing when disabled in config. */
    public void start() {
        if (!plugin.getConfig().getBoolean("settings.auto-update", true)) {
            plugin.getLogger().info("Auto-update is disabled in config.yml.");
            return;
        }
        int hours = Math.max(1, plugin.getConfig().getInt("settings.auto-update-hours", 6));
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::check);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::check,
                hours * 20L * 60 * 60, hours * 20L * 60 * 60);
    }

    /** Run a check now (used by /rea update). Result is reported to the given sender. */
    public void checkNow(CommandSender sender) {
        Msg.send(sender, "&7Checking GitHub for a new Rea release...");
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Result result = check();
            // Player messages must go out on the main thread.
            plugin.getServer().getScheduler().runTask(plugin, () -> report(sender, result));
        });
    }

    private Result check() {
        if (!checkRunning.compareAndSet(false, true)) {
            return new Result(Status.ERROR, currentVersion(), null, "An update check is already running.");
        }
        try {
            Result result = doCheck();
            // Shout it once per session when a newer release exists.
            if (result.status() == Status.DOWNLOADED && !notifiedThisSession) {
                notifiedThisSession = true;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player.hasPermission("rea.admin")) {
                            Msg.send(player, "&d&lRea &7- a new version (&e" + result.latestVersion()
                                    + "&7) was downloaded to &eplugins/update&7. Restart to apply it.");
                        }
                    }
                });
            }
            return result;
        } finally {
            checkRunning.set(false);
        }
    }

    private Result doCheck() {
        String current = currentVersion();
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(API_LATEST))
                    .header("User-Agent", "Rea/" + current + " (+https://github.com/tq1r/rea)")
                    .header("Accept", "application/vnd.github+json")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 404) {
                plugin.getLogger().info("Auto-update: no releases published yet (" + RELEASES_PAGE + ").");
                return new Result(Status.NO_RELEASES, current, null, null);
            }
            if (response.statusCode() != 200) {
                plugin.getLogger().warning("Auto-update: GitHub answered HTTP " + response.statusCode() + ". Will retry later.");
                return new Result(Status.ERROR, current, null, "GitHub answered HTTP " + response.statusCode());
            }
            String body = response.body();
            Matcher tagMatcher = TAG.matcher(body);
            if (!tagMatcher.find()) {
                return new Result(Status.ERROR, current, null, "Could not read the release tag.");
            }
            String latest = normalize(tagMatcher.group(1));
            if (compareVersions(latest, current) <= 0) {
                plugin.getLogger().info("Auto-update: Rea is up to date (v" + current + ").");
                return new Result(Status.UP_TO_DATE, current, latest, null);
            }
            Matcher jarMatcher = JAR_URL.matcher(body);
            if (!jarMatcher.find()) {
                plugin.getLogger().warning("Auto-update: release " + latest + " has no .jar asset. Publish one at " + RELEASES_PAGE);
                return new Result(Status.NO_JAR_ASSET, current, latest, "Release v" + latest + " has no jar asset.");
            }
            String jarUrl = jarMatcher.group(1);
            String fileName = jarUrl.substring(jarUrl.lastIndexOf('/') + 1);

            HttpRequest download = HttpRequest.newBuilder(URI.create(jarUrl))
                    .header("User-Agent", "Rea/" + current + " (+https://github.com/tq1r/rea)")
                    .timeout(Duration.ofSeconds(60))
                    .GET()
                    .build();
            HttpResponse<byte[]> file = http.send(download, HttpResponse.BodyHandlers.ofByteArray());
            byte[] bytes = file.body();
            if (file.statusCode() != 200 || bytes.length < 10_000) {
                return new Result(Status.ERROR, current, latest, "Download failed (HTTP " + file.statusCode() + ", " + bytes.length + " bytes).");
            }
            if (bytes[0] != 'P' || bytes[1] != 'K') {
                return new Result(Status.ERROR, current, latest, "Downloaded file is not a jar.");
            }
            Path updateDir = plugin.getDataFolder().toPath().getParent().resolve("update");
            Files.createDirectories(updateDir);
            Files.write(updateDir.resolve(fileName), bytes);
            plugin.getLogger().info("Auto-update: downloaded " + fileName + " (" + bytes.length + " bytes) to plugins/update. Restart to apply.");
            return new Result(Status.DOWNLOADED, current, latest, fileName);
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            plugin.getLogger().warning("Auto-update: check failed (" + e.getMessage() + "). Will retry later.");
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return new Result(Status.ERROR, current, null, e.getMessage());
        }
    }

    private void report(CommandSender sender, Result result) {
        if (result == null) {
            Msg.send(sender, "&cUpdate check failed. Please try again later.");
            return;
        }
        switch (result.status()) {
            case UP_TO_DATE -> Msg.send(sender, "&aRea is up to date (v" + result.currentVersion() + ").");
            case DOWNLOADED -> Msg.send(sender, "&aDownloaded &d" + result.detail() + " &7(v" + result.latestVersion()
                    + "). Restart the server to apply it.");
            case NO_RELEASES -> Msg.send(sender, "&7No Rea releases are published yet.");
            case NO_JAR_ASSET -> Msg.send(sender, "&eThe latest release (&d" + result.latestVersion() + "&e) has no .jar asset attached.");
            case DISABLED -> Msg.send(sender, "&7Auto-update is disabled in config.yml.");
            default -> Msg.send(sender, "&cUpdate check failed: &7" + (result.detail() == null ? "unknown error" : result.detail()));
        }
    }

    /** Strip a leading v and any pre-release suffix; keep dotted numbers. */
    private String normalize(String tag) {
        String s = tag == null ? "" : tag.trim();
        if (s.startsWith("v") || s.startsWith("V")) {
            s = s.substring(1);
        }
        int dash = s.indexOf('-');
        if (dash >= 0) {
            s = s.substring(0, dash);
        }
        return s;
    }

    /** Compare two dotted versions: positive if a > b, 0 if equal, negative if a < b. */
    private int compareVersions(String a, String b) {
        List<Integer> pa = parts(a);
        List<Integer> pb = parts(b);
        int max = Math.max(pa.size(), pb.size());
        for (int i = 0; i < max; i++) {
            int x = i < pa.size() ? pa.get(i) : 0;
            int y = i < pb.size() ? pb.get(i) : 0;
            if (x != y) {
                return Integer.compare(x, y);
            }
        }
        return 0;
    }

    private List<Integer> parts(String version) {
        return Pattern.compile("\\.")
                .splitAsStream(version)
                .map(part -> part.replaceAll("[^0-9]", ""))
                .filter(part -> !part.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
