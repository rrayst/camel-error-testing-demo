package de.predic8.demo.camelextensivedemo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.net.ConnectException;

@Component
public class SubRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        onException(ConnectException.class)
                .setBody(constant("error"))
                .handled(true);

        from("direct:additionalInfo")
                .routeId("subroute")
                .setBody(constant("subroute")).id("setbody");
    }
}
