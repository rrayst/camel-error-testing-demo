package de.predic8.demo.camelextensivedemo;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.Exchange;
import org.apache.camel.builder.FlexibleAggregationStrategy;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    private AggregationStrategy listAggregationStrategy() {
        return new AggregationStrategy() {
            @Override
            public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                if (oldExchange == null) {
                    List body = new ArrayList();
                    body.add(newExchange.getIn().getBody());
                    newExchange.getIn().setBody(body);
                    return newExchange;
                }
                List body = oldExchange.getIn().getBody(List.class);
                body.add(newExchange.getIn().getBody());
                return oldExchange;
            }
        };
    }
}
