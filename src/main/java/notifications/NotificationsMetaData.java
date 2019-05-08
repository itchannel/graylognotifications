package notifications;

import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.ServerStatus;
import org.graylog2.plugin.Version;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

/**
 * Implement the PluginMetaData interface here.
 */
public class NotificationsMetaData implements PluginMetaData {
    private static final String PLUGIN_PROPERTIES = "itchannel.graylog-plugin-pushnotifications/graylog-plugin.properties";

    @Override
    public String getUniqueId() {
        return "notifications.NotificationsPlugin";
    }

    @Override
    public String getName() {
        return "Notifications";
    }

    @Override
    public String getAuthor() {
        return "Steve <steve@itchannel.me>";
    }

    @Override
    public URI getURL() {
        return URI.create("https://github.com/github.com/itchannel/graylognotifications");
    }

    @Override
    public Version getVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public String getDescription() {
        // TODO Insert correct plugin description
        return "Description of Notifications plugin";
    }

    @Override
    public Version getRequiredVersion() {
        return Version.fromPluginProperties(getClass(), PLUGIN_PROPERTIES, "graylog.version", Version.from(0, 0, 0, "unknown"));
    }

    @Override
    public Set<ServerStatus.Capability> getRequiredCapabilities() {
        return Collections.emptySet();
    }
}
