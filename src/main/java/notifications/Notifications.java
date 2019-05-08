package notifications;

import java.nio.charset.Charset;
import java.util.*;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.net.URLEncoder;
import com.floreysoft.jmte.Engine;
import org.graylog2.plugin.Message;
import org.graylog2.plugin.MessageSummary;
import org.graylog2.plugin.alarms.AlertCondition;
import org.graylog2.plugin.alarms.callbacks.AlarmCallback;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackConfigurationException;
import org.graylog2.plugin.alarms.callbacks.AlarmCallbackException;
import org.graylog2.plugin.configuration.Configuration;
import org.graylog2.plugin.configuration.ConfigurationException;
import org.graylog2.plugin.configuration.ConfigurationRequest;
import org.graylog2.plugin.streams.Stream;
import org.graylog2.plugin.configuration.fields.ConfigurationField;
import org.graylog2.plugin.configuration.fields.TextField;
import org.graylog2.plugin.configuration.fields.TextField.Attribute;



public class Notifications implements AlarmCallback{

    private final Engine engine = new Engine();
    private Configuration config;

    @Override
    public void initialize(Configuration config) throws AlarmCallbackConfigurationException {
        this.config = config;

        try {
            checkConfiguration();
        } catch (ConfigurationException e) {
            throw new AlarmCallbackConfigurationException("Configuration error: " + e.getMessage());
        }
    }

    @Override
    public void call(Stream stream, AlertCondition.CheckResult result) throws AlarmCallbackException {


    try {
        //Current pushover API Endpoint
        URL obj = new URL("https://api.pushover.net/1/messages.json");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        String title = stream.getTitle();
        //Message must be URL encoded due to pushover using post parameters
        String message = URLEncoder.encode(buildMessage(stream,result));
        //When a test message is sent the message section can be blank this is a placeholder for a proper error function
        if (message == "")
        {
        message = "This is a test";
        }
        //Generate post string, current supports HTML and User/Group ID's
        String POST_PARAMS = "token="+ this.config.getString("token") + "&user=" + this.config.getString("groupid") + "&html=1&title=" + title + "&message=" + message;
        os.write(POST_PARAMS.getBytes(Charset.forName("UTF-8")));
        os.flush();
        os.close();
        int responseCode = con.getResponseCode();
        //Response code is printed to the console for debugging purporses 200 = OK 400 = Malformed POST
        System.out.println("POST Response Code :: " + responseCode);

        } catch(Exception exception) {

            System.out.println(exception.getMessage());

        }

    }

    private String buildMessage(Stream stream, AlertCondition.CheckResult result) {
        List<Message> backlog = getAlarmBacklog(result);

        Map<String, Object> model = getModel(stream, result, backlog);
        System.out.println(model);
        try {
            System.out.println(config.getString("message"));
            String template = config.getString("message");
            return this.engine.transform(template, model);

        } catch (Exception ex) {
            return ex.toString();
        }
    }

    private Map<String, Object> getModel(Stream stream, AlertCondition.CheckResult result, List<Message> backlog) {
        //Current Model map
        Map<String, Object> model = new HashMap<>();
        model.put("stream", stream);
        model.put("check_result", result);
        model.put("alert_condition", result.getTriggeredCondition());
        model.put("backlog", backlog);
        model.put("backlog_size", backlog.size());
        return model;
    }


    private List<Message> getAlarmBacklog(AlertCondition.CheckResult result) {
        final AlertCondition alertCondition = result.getTriggeredCondition();
        final List<MessageSummary> matchingMessages = result.getMatchingMessages();
        final int effectiveBacklogSize = Math.min(alertCondition.getBacklog(), matchingMessages.size());
        System.out.println(effectiveBacklogSize);

        if (effectiveBacklogSize == 0) return Collections.emptyList();
        final List<MessageSummary> backlogSummaries = matchingMessages.subList(0, effectiveBacklogSize);
        final List<Message> backlog = new ArrayList<>(effectiveBacklogSize);
        for (MessageSummary messageSummary : backlogSummaries) {
            backlog.add(messageSummary.getRawMessage());


        }

        return backlog;
    }

    @Override
    public ConfigurationRequest getRequestedConfiguration() {

	    final ConfigurationRequest configurationRequest = new ConfigurationRequest();

	    //Pushover API Token generated by creating an application
	    configurationRequest.addField(new TextField("token",
                "TOKEN",
                "",
                "Pushover API Token",
                ConfigurationField.Optional.NOT_OPTIONAL));

	    //Pushover USERID or GROUPID, both are supported here
	    configurationRequest.addField(new TextField("groupid",
                "GROUPID",
                "",
                "User or Group ID (Pushover)",
                ConfigurationField.Optional.NOT_OPTIONAL));

	    //Default message template, can be altered by the user for more specific conditions
	    configurationRequest.addField(new TextField(
               "message", "Message",
                "[${stream.title}](${stream_url}): ${alert_condition.title}\n" +
                        "\n" +
                        "${foreach backlog message}\n" +
                        "${message.message}\n\\n" +
                        "${end}\n" +
                        "",
                "See http://docs.graylog.org/en/latest/pages/streams/alerts.html#email-alert-notification for more details.",
                ConfigurationField.Optional.NOT_OPTIONAL,
                Attribute.TEXTAREA
        ));



        return configurationRequest;
    }

    @Override
    public String getName() {
        return "Push Notification";
    }

    @Override
    public Map<String, Object> getAttributes() {
        return config.getSource();
    }

    @Override
    public void checkConfiguration() throws ConfigurationException {


        //2 Mandatory fields that are required for pushover to work
        String[] mandatoryFields = {
                    "token",
                    "groupid",
        };

        for (String field : mandatoryFields) {
            System.out.println(field);
            if (!config.stringIsSet(field)) {
                throw new ConfigurationException(String.format("%s is mandatory and must not be empty.", field));
            }
        }

            //Future functionality to support a proxy, not currently implemented in this version
        if (config.stringIsSet(this.config.getString("proxy"))) {
            String proxy = this.config.getString("proxy");
            assert proxy != null;
            String[] proxyArr = proxy.split(":");
            if (proxyArr.length != 2 || Integer.parseInt(proxyArr[1]) == 0) {
                throw new ConfigurationException("Invalid Proxy format.");
            }
        }

    }


}
