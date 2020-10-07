import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import PushNotificationForm from 'form/pushNotificationForm';
import PushNotificationSummary from 'form/pushNotificationSummary';
PluginStore.register(
    new PluginManifest({}, {
        eventNotificationTypes: [
            {
                type: 'push-notifications-v2',
                displayName: 'Pushover Notifications',
                formComponent: 'PushNotificationForm',
                summaryComponent: 'PushNotificationSummary',
                defaultConfig: 'PushNotificationForm.defaultConfig'
            }

        ]
    }
)
);



