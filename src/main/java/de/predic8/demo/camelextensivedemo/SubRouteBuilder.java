package de.predic8.demo.camelextensivedemo;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class SubRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:additionalInfo")
                .setBody(constant("subroute"));
    }
}
