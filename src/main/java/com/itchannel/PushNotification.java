package com.itchannel;

import org.graylog.events.notifications.EventNotification;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.graylog.events.notifications.EventNotification;
import org.graylog.events.notifications.EventNotificationContext;
import org.graylog2.notifications.NotificationService;
import org.graylog.events.notifications.EventNotificationModelData;
import org.graylog.events.notifications.EventNotificationService;
import org.graylog.events.notifications.PermanentEventNotificationException;
import org.graylog.events.notifications.TemporaryEventNotificationException;
import org.graylog2.notifications.Notification;
import org.graylog2.plugin.MessageSummary;
import org.apache.commons.lang3.StringUtils;
import com.google.common.collect.ImmutableList;
import org.graylog2.notifications.NotificationService;
import org.graylog2.streams.StreamService;
import org.graylog2.plugin.system.NodeId;
import com.floreysoft.jmte.Engine;
import javax.inject.Inject;
import com.itchannel.template.PushHTMLEncoder;
import com.itchannel.template.RawNoopRenderer;
import org.graylog.events.notifications.*;
import org.graylog.events.processor.EventDefinitionDto;
import java.util.Optional;
import org.graylog2.plugin.streams.Stream;
//Classes specific to pushover
import java.net.URL;
import java.io.OutputStream;
import com.itchannel.models.MessageModelData;
import com.itchannel.models.StreamModelData;
import com.google.common.collect.ImmutableList;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import static java.util.Objects.requireNonNull;
import org.graylog.scheduler.JobTriggerDto;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import org.graylog2.jackson.TypeReferences;
import org.graylog.events.processor.aggregation.AggregationEventProcessorConfig;
/**
 * This is the plugin. Your class should implement one of the existing plugin
 * interfaces. (i.e. AlarmCallback, MessageInput, MessageOutput)
 */
public class PushNotification implements EventNotification {
    public interface Factory extends EventNotification.Factory {
        @Override
        PushNotification create();
    }

    private static final String UNKNOWN = "<unknown>";
    private final EventNotificationService notificationCallbackService;
    private final StreamService streamService;
    private final NotificationService notificationService;
    private final NodeId nodeId;
    private final ObjectMapper objectMapper;
    private final Engine templateEngine;

    @Inject
    public PushNotification(EventNotificationService notificationCallbackService,
                            StreamService streamService,
                            NotificationService notificationService,
                            NodeId nodeId,
                            ObjectMapper objectMapper) {
        this.notificationCallbackService = notificationCallbackService;
        this.streamService = streamService;
        this.notificationService = requireNonNull(notificationService, "notificationService");
        this.nodeId = requireNonNull(nodeId, "nodeId");
        this.objectMapper = requireNonNull(objectMapper, "objectMapper");
        this.templateEngine = new Engine();
        templateEngine.registerNamedRenderer(new RawNoopRenderer());
        templateEngine.setEncoder(new PushHTMLEncoder());
    }

    @Override
    public void execute(EventNotificationContext ctx) throws TemporaryEventNotificationException, PermanentEventNotificationException {
        final PushNotificationConfig config = (PushNotificationConfig) ctx.notificationConfig();
        ImmutableList<MessageSummary> backlog = notificationCallbackService.getBacklogForEvent(ctx);
        final Map<String, Object> model = getModel(ctx, backlog, config);
        


        try {
            //Current pushover API Endpoint
            URL obj = new URL("https://api.pushover.net/1/messages.json");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();
            String message = buildMessage(config.messageField(), model);
            String priority = "";
            String retry = "";
            String expire = "";
            String sound = "";
            if (message == "") {
                message = "This is a test";
            }
            if (config.priorityToken() == "")
            {
                priority = "0";
            }else
            {
                priority = config.priorityToken();
            }
            if (config.retryToken() == "")
            {
                retry = "60";
            } else {
                retry = config.retryToken();
            }
            if (config.expireToken() == "")
            {
                expire = "1200";
            } else {
                expire = config.expireToken();
            }
            if (config.soundToken() == "")
            {
                sound = "pushover";
            } else {
                sound = config.soundToken();
            }
            String POST_PARAMS = "token=" + config.apiToken() + "&user=" + config.userToken() + "&priority=" + priority + "&retry=" + retry + "&expire=" + expire + "&sound=" + sound + "&html=1&title=" + model.get("event_definition_title") + "&message=" + URLEncoder.encode(message, "UTF-8");
            os.write(POST_PARAMS.getBytes(Charset.forName("UTF-8")));
            os.flush();
            os.close();
            int responseCode = con.getResponseCode();
            System.out.println("Post Response Code :: " + responseCode);

        } catch (Exception exception) {
            errorNotification(exception.getMessage());

        }


    }

    private String buildMessage(String template, Map<String, Object> model) {
        return this.templateEngine.transform(template, model);
    }

    private Map<String, Object> getModel(EventNotificationContext ctx, ImmutableList<MessageSummary> backlog, PushNotificationConfig config) {
        final Optional<EventDefinitionDto> definitionDto = ctx.eventDefinition();
        final Optional<JobTriggerDto> jobTriggerDto = ctx.jobTrigger();


        List<StreamModelData> streams = streamService.loadByIds(ctx.event().sourceStreams())
                .stream()
                .map(stream -> buildStreamWithUrl(stream, ctx, "https://graylog.dcrs.tech"))
                .collect(Collectors.toList());

        final MessageModelData modelData = MessageModelData.builder()
                .eventDefinition(definitionDto)
                .eventDefinitionId(definitionDto.map(EventDefinitionDto::id).orElse(UNKNOWN))
                .eventDefinitionType(definitionDto.map(d -> d.config().type()).orElse(UNKNOWN))
                .eventDefinitionTitle(definitionDto.map(EventDefinitionDto::title).orElse(UNKNOWN))
                .eventDefinitionDescription(definitionDto.map(EventDefinitionDto::description).orElse(UNKNOWN))
                .jobDefinitionId(jobTriggerDto.map(JobTriggerDto::jobDefinitionId).orElse(UNKNOWN))
                .jobTriggerId(jobTriggerDto.map(JobTriggerDto::id).orElse(UNKNOWN))
                .event(ctx.event())
                .backlog(backlog)
                .backlogSize(backlog.size())
                .graylogUrl("https://graylog.dcrs.tech")
                .streams(streams)
                .build();

        return objectMapper.convertValue(modelData, TypeReferences.MAP_STRING_OBJECT);
    }


    private StreamModelData buildStreamWithUrl(Stream stream, EventNotificationContext ctx, String graylogURL) {
        String streamUrl = null;
        if (StringUtils.isNotBlank(graylogURL)) {
            streamUrl = StringUtils.appendIfMissing(graylogURL, "/") + "streams/" + stream.getId() + "/search";

            if (ctx.eventDefinition().isPresent()) {
                EventDefinitionDto eventDefinitionDto = ctx.eventDefinition().get();
                if (eventDefinitionDto.config() instanceof AggregationEventProcessorConfig) {
                    String query = ((AggregationEventProcessorConfig) eventDefinitionDto.config()).query();
                    try {
                        streamUrl += "?q=" + URLEncoder.encode(query, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        // url without query as fallback
                    }
                }
            }
        }
        return StreamModelData.builder()
                .id(stream.getId())
                .title(stream.getTitle())
                .description(stream.getDescription())
                .url(Optional.ofNullable(streamUrl).orElse(UNKNOWN))
                .build();
    }

        private void errorNotification (String error){
            //LOG.warn(error);

            final Notification systemNotification = notificationService.buildNow()
                    .addNode(nodeId.toString())
                    .addType(Notification.Type.GENERIC)
                    .addSeverity(Notification.Severity.NORMAL)
                    .addDetail("title", "Failed to send Pushover messages.")
                    .addDetail("description", error)
                    .addDetail("exception", error);
            notificationService.publishIfFirst(systemNotification);
        }

}
