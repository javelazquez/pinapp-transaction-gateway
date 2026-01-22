package com.pinapp.gateway.infrastructure.config;

import com.pinapp.gateway.infrastructure.notification.TransactionAuditListener;
import com.pinapp.notify.config.PinappNotifyConfig;
import com.pinapp.notify.core.NotificationServiceImpl;
import com.pinapp.notify.domain.Notification;
import com.pinapp.notify.domain.NotificationResult;
import com.pinapp.notify.domain.RetryPolicy;
import com.pinapp.notify.domain.vo.ChannelType;
import com.pinapp.notify.ports.in.NotificationService;
import com.pinapp.notify.ports.out.NotificationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotifyConfig {

    @Bean
    public NotificationService notificationService(TransactionAuditListener listener) {
        PinappNotifyConfig config = PinappNotifyConfig.builder()
                .addProvider(ChannelType.EMAIL, createProvider("EmailProvider", ChannelType.EMAIL))
                .addProvider(ChannelType.SMS, createProvider("SmsProvider", ChannelType.SMS))
                .addProvider(ChannelType.PUSH, createProvider("PushProvider", ChannelType.PUSH))
                .withRetryPolicy(RetryPolicy.of(2, 1000))
                .addSubscriber(listener)
                .build();

        return new NotificationServiceImpl(config);
    }

    private NotificationProvider createProvider(String name, ChannelType type) {
        return new NotificationProvider() {
            @Override
            public boolean supports(ChannelType channel) {
                return channel == type;
            }

            @Override
            public NotificationResult send(Notification notification) {
                System.out.println("Sending " + name + ": " + notification.message());
                return NotificationResult.success(notification.id(), name, type);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }
}
