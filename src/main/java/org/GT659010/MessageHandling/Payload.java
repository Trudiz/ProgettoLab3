package org.GT659010.MessageHandling;

import com.fasterxml.jackson.annotation.*;
import org.GT659010.MessageHandling.RequestMessages.*;

@JsonTypeInfo(
        use      = JsonTypeInfo.Id.NAME,
        include  = JsonTypeInfo.As.EXTERNAL_PROPERTY, // <-- notice
        property = "operation")                       // <-- points to sibling key
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegisterPayload.class,          name = "register"),
        @JsonSubTypes.Type(value = UpdateCredentialsPayload.class, name = "updateCredentials"),
        @JsonSubTypes.Type(value = LoginPayload.class,             name = "login"),
        @JsonSubTypes.Type(value = LogoutPayload.class,            name = "logout"),
        @JsonSubTypes.Type(value = LimitOrderPayload.class,        name = "insertLimitOrder"),
        @JsonSubTypes.Type(value = MarketOrderPayload.class,       name = "insertMarketOrder"),
        @JsonSubTypes.Type(value = StopOrderPayload.class,         name = "insertStopOrder"),
        @JsonSubTypes.Type(value = CancelOrderPayload.class,       name = "cancelOrder"),
        @JsonSubTypes.Type(value = PriceHistoryPayload.class,      name = "getPriceHistory")
})
public interface Payload {}      // <- empty marker interface
