package de.predic8.demo.camelextensivedemo;

import org.apache.camel.CamelContext;
import org.apache.camel.Configuration;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.AdviceWith;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.NotifyBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.net.ConnectException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.camel.Exchange.SPLIT_INDEX;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.apache.camel.builder.Builder.body;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Import(OnlyStepTwoTest.MyConfig.class)
class OnlyStepTwoTest {

    @Autowired
    CamelContext cc;

    @Test
    void contextLoads() throws InterruptedException {
        MockEndpoint mockTest = cc.getEndpoint("mock:test", MockEndpoint.class);

        mockTest.expectedMessageCount(1);

        mockTest.expectedMessagesMatches(
                body().convertToString().isEqualTo("demo1subroute,demo2subroute"));

        final NotifyBuilder notifyBuilder = new NotifyBuilder(cc)
                .whenDone(1)
                .create();

        cc.createProducerTemplate().send("file:in", exchange -> {
            exchange.getIn().setBody("demo1\ndemo2");
        });

        var done = notifyBuilder.matches(5, SECONDS);
        assertTrue(done, "NotifyBuilder does NOT match!");

        mockTest.assertIsSatisfied();
    }

    @TestConfiguration
    public static class MyConfig {
        @Bean
        public CamelContextConfiguration foo() {
            return new CamelContextConfiguration() {
                @Override
                public void beforeApplicationStart(CamelContext camelContext) {
                    try {
                        camelContext.setTracing(true);
                        adviceWith(
                                ((ModelCamelContext)camelContext).getRouteDefinition("mainRoute"),
                                camelContext,
                                new AdviceWithRouteBuilder() {
                                    @Override
                                    public void configure() throws Exception {
                                        interceptSendToEndpoint("file:out")
                                                .skipSendToOriginalEndpoint()
                                                .convertBodyTo(String.class)
                                                .to("mock:test");
                                    }
                                });

                        adviceWith(
                                ((ModelCamelContext)camelContext).getRouteDefinition("subroute"),
                                camelContext,
                                new AdviceWithRouteBuilder() {
                                    @Override
                                    public void configure() throws Exception {
                                        weaveById("setbody")
                                                .before()
                                                .filter(exchangeProperty(SPLIT_INDEX).isEqualTo(0))
                                                    .throwException(new ConnectException());

                                    }
                                });
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void afterApplicationStart(CamelContext camelContext) {

                }
            };
        }
    }

}
