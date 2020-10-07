package com.itchannel;

import org.graylog2.plugin.Plugin;
import org.graylog2.plugin.PluginMetaData;
import org.graylog2.plugin.PluginModule;

import java.util.Collection;
import java.util.Collections;

/**
 * Implement the Plugin interface here.
 */
public class PushNotificationPlugin implements Plugin {
    @Override
    public PluginMetaData metadata() {
        return new PushNotificationMetaData();
    }

    @Override
    public Collection<PluginModule> modules () {
        return Collections.<PluginModule>singletonList(new PushNotificationModule());
    }
}
