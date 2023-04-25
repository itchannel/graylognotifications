import React from 'react';
import PropTypes from 'prop-types';

import CommonNotificationSummary from './mNotificationSummary';

class PushNotificationSummary extends React.Component {
    static propTypes = {
        type: PropTypes.string.isRequired,
        notification: PropTypes.object,
        definitionNotification: PropTypes.object.isRequired,
    };

    static defaultProps = {
        notification: {},
    };

    render() {
        const { notification } = this.props;
        return (
            <CommonNotificationSummary {...this.props}>
                <React.Fragment>
                    <tr>
                        <td>User/Group Token</td>
                        <td>{notification.config.user_token}</td>
                    </tr>
                    <tr>
                        <td>Application Token</td>
                        <td>{notification.config.api_token}</td>
                    </tr>
                    <tr>
                        <td>Message Priority</td>
                        <td>{notification.config.priority_token}</td>
                    </tr>
                    <tr>
                        <td>Message</td>
                        <td>{notification.config.message_field}</td>
                    </tr>

                </React.Fragment>
            </CommonNotificationSummary>
        );
    }
}

export default PushNotificationSummary;