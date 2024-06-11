package com.vietle.mychatapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }

//    @Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
//        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
//            @Override
//            public WebSocketHandler decorate(WebSocketHandler handler) {
//                return new WebSocketHandlerDecorator(handler) {
//                    @Override
//                    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//                        super.afterConnectionEstablished(session);
//                        session.getHandshakeHeaders().add("Access-Control-Allow-Origin", "*");
//                    }
//                };
//            }
//        });
//    }

//    @Override
//    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
//        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
//        resolver.setDefaultMimeType(APPLICATION_JSON);
//        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
//        converter.setObjectMapper(new ObjectMapper());
//        converter.setContentTypeResolver(resolver);
//        messageConverters.add(new StringMessageConverter());
//        messageConverters.add(new ByteArrayMessageConverter());
//        messageConverters.add(converter);
//
//        return false;
//    }
}
