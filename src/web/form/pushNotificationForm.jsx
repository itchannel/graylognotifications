import React from 'react';
import PropTypes from 'prop-types';
import { ControlLabel, FormGroup, HelpBlock } from 'react-bootstrap';
import lodash from 'lodash';

import Input from './components/Input';
import FormsUtils from './utils/FormUtils';

const DEFAULT_MESSAGE_TEMPLATE = "[${stream.title}](${stream_url}): ${alert_condition.title}\n" +
    "\n" +
    "${foreach backlog message}\n" +
    "${message.message}\n\\n" +
    "${end}\n" +
    "";

class PushNotificationForm extends React.Component {
    static propTypes = {
        config: PropTypes.object.isRequired,
        validation: PropTypes.object.isRequired,
        onChange: PropTypes.func.isRequired,
    };

    static defaultConfig = {
        user_token: '',
        api_token: '',
        priority_token: '',
        message_field: DEFAULT_MESSAGE_TEMPLATE,
    };

    propagateChange = (key, value) => {
        const { config, onChange } = this.props;
        const nextConfig = lodash.cloneDeep(config);
        nextConfig[key] = value;
        onChange(nextConfig);
    };

    handleChange = event => {
        const { name } = event.target;
        this.propagateChange(name, FormsUtils.getValueFromInput(event.target));
    };

    handleChatsChange = selectedOptions => {
        this.propagateChange('chats', selectedOptions || []);
    };

    render() {
        const { config, validation } = this.props;

        return (
            <React.Fragment>
                <Input id="notification-user-token"
                       name="user_token"
                       label="User/Group Token"
                       type="text"
                       bsStyle={validation.errors.user_token ? 'error' : null}
                       help={lodash.get(validation, 'errors.user_token[0]', 'User/Group Token')}
                       value={config.user_token || ''}
                       onChange={this.handleChange}
                       required />

                <Input id="notification-api-token"
                       name="api_token"
                       label="Application API Token"
                       type="text"
                       bsStyle={validation.errors.api_token ? 'error' : null}
                       help={lodash.get(validation, 'errors.api_token[0]', 'Application Token for Pushover.')}
                       value={config.api_token || ''}
                       onChange={this.handleChange}
                       required />

                <Input id="notification-priority-token"
                       name="priority_token"
                       label="Priority Number"
                       type="text"
                       bsStyle={validation.errors.priority_token ? 'error' : null}
                       help={lodash.get(validation, 'errors.priority_token[0]', 'Priority value for notification')}
                       value={config.priority_token|| ''}
                       onChange={this.handleChange} />


                <Input id="notification-message-template"
                       name="message_field"
                       label="Message Template"
                       type="textarea"
                       rows={10}
                       bsStyle={validation.errors.message_field ? 'error' : null}
                       help={lodash.get(validation, 'errors.message_field[0]', <>This uses the same syntax as the EmailNotification Template. See <a href="https://docs.graylog.org/en/latest/pages/alerts.html#email-alert-notification" target="_blank" rel="noopener">Graylog documentation</a> for more details.</>)}
                       value={config.message_field || ''}
                       onChange={this.handleChange}
                       required />

            </React.Fragment>
        );
    }
}

export default PushNotificationForm;
