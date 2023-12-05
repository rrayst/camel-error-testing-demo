package de.predic8.demo.camelextensivedemo;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.FlexibleAggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DemoRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // @formatter:off
        from("file:in")
                .routeId("mainRoute")
                .split().tokenize("\n").aggregationStrategy(listAggregationStrategy())
                    .enrich("direct:additionalInfo", appendAggregationStrategy())
                    .end()
                .end()
                .to("file:out");
        // @formatter:on

        /*
- erst stopOnEx, dann exc aggregieren

- testen: wie interagieren exc policies mit exc handler
         */
    }

    private AggregationStrategy appendAggregationStrategy() {
        return (oldExchange, newExchange) -> {
            oldExchange.getIn().setBody(
                    oldExchange.getIn().getBody(String.class) +
                    newExchange.getIn().getBody(String.class)
            );
            return oldExchange;
        };
    }

    private FlexibleAggregationStrategy listAggregationStrategy() {
        return new FlexibleAggregationStrategy()
                .storeInBody()
                .pick(simple("${body}"))
                .accumulateInCollection(ArrayList.class);
    }
}
