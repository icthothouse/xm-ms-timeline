package com.icthh.xm.ms.timeline.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "xmtimeline")
@Getter
@Setter
@ToString(exclude = {"httpStatusCode","startDate","requestBody","responseBody","requestHeaders","responseHeaders","browser","opSystem"})
public class XmTimeline implements Serializable {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rid")
    private String rid;

    @Column(name = "login")
    private String login;

    @Column(name = "user_key")
    private String userKey;

    @Column(name = "tenant")
    private String tenant;

    @Column(name = "ms_name")
    private String msName;

    @Column(name = "operation_name")
    private String operationName;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "entity_key")
    private String entityKey;

    @Column(name = "entity_type_key")
    private String entityTypeKey;

    @Column(name = "operation_url")
    private String operationUrl;

    @Column(name = "http_method")
    private String httpMethod;

    @Column(name = "http_status_code")
    private Integer httpStatusCode;

    @Column(name = "start_date")
    private Instant startDate;

    @Lob
    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "request_length")
    private Long requestLength;

    @Lob
    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "response_length")
    private Long responseLength;

    @Column(name = "channel_type")
    private String channelType;

    @ElementCollection
    @MapKeyColumn(name="header_key")
    @Column(name="header_value")
    @CollectionTable(name="timeline_request_headers", joinColumns=@JoinColumn(name="timeline_id"))
    private Map<String, String> requestHeaders;

    @ElementCollection
    @MapKeyColumn(name="header_key")
    @Column(name="header_value")
    @CollectionTable(name="timeline_response_headers", joinColumns=@JoinColumn(name="timeline_id"))
    private Map<String, String> responseHeaders;

    @Column(name = "exec_time")
    private Long execTime;

    @Column(name = "browser")
    private String browser;

    @Column(name = "op_system")
    private String opSystem;
}
