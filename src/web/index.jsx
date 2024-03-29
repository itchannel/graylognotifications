import { PluginManifest, PluginStore } from 'graylog-web-plugin/plugin';

import PushNotificationForm from './form/PushNotificationForm';
import PushNotificationSummary from './form/PushNotificationSummary';
import PushNotificationDetails from './form/PushNotificationDetails';
PluginStore.register(
    new PluginManifest({}, {
        eventNotificationTypes: [
            {
                type: 'push-notifications-v2',
                displayName: 'Pushover Notification',
                formComponent: PushNotificationForm,
                summaryComponent: PushNotificationSummary,
                detailsComponent: PushNotificationDetails,
                defaultConfig: PushNotificationForm.defaultConfig
            }

        ]
    }
)
);



