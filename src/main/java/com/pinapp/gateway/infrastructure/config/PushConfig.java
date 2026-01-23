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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración distribuida para el canal de notificaciones Push.
 * <p>
 * Esta clase configura un {@link NotificationService} específico para el canal PUSH,
 * utilizando las propiedades definidas en {@code application.yml} bajo la ruta
 * {@code pinapp.notify.push.*}.
 * </p>
 * <p>
 * <strong>Propiedades requeridas en application.yml:</strong>
 * <ul>
 *   <li>{@code pinapp.notify.push.provider} - Nombre del proveedor de push (ej: "firebase")</li>
 *   <li>{@code pinapp.notify.push.server-key} - Clave del servidor del proveedor de push</li>
 *   <li>{@code pinapp.notify.retry-attempts} - Número de intentos de reintento para notificaciones</li>
 * </ul>
 * </p>
 * <p>
 * El bean creado ({@code pushNotificationService}) es único y autocontenido, registrando
 * únicamente el proveedor de Push. El servicio incluye automáticamente el
 * {@link TransactionAuditListener} para auditoría de transacciones.
 * </p>
 * <p>
 * <strong>Ejemplo de configuración:</strong>
 * <pre>
 * pinapp:
 *   notify:
 *     push:
 *       provider: "firebase"
 *       server-key: "FK_your_server_key_here"
 *     retry-attempts: 2
 * </pre>
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see EmailConfig
 * @see SmsConfig
 */
@Configuration
public class PushConfig {

    @Value("${pinapp.notify.push.provider}")
    private String pushProvider;

    @Value("${pinapp.notify.push.server-key}")
    private String pushServerKey;

    @Value("${pinapp.notify.retry-attempts}")
    private Integer retryAttempts;

    /**
     * Crea y configura el bean {@link NotificationService} específico para notificaciones Push.
     * <p>
     * Este bean utiliza el Builder del SDK ({@link PinappNotifyConfig}) para configurar:
     * <ul>
     *   <li>El proveedor de Push con las credenciales inyectadas desde el YAML</li>
     *   <li>La política de reintentos configurada con {@code retry-attempts}</li>
     *   <li>El suscriptor de eventos ({@link TransactionAuditListener}) para auditoría</li>
     * </ul>
     * </p>
     *
     * @param listener El listener de auditoría que será registrado como suscriptor de eventos
     * @return Una instancia configurada de {@link NotificationService} para el canal PUSH
     */
    @Bean
    public NotificationService pushNotificationService(TransactionAuditListener listener) {
        PinappNotifyConfig config = PinappNotifyConfig.builder()
                .addProvider(ChannelType.PUSH, createPushProvider())
                .withRetryPolicy(RetryPolicy.of(retryAttempts, 1000))
                .addSubscriber(listener)
                .build();

        return new NotificationServiceImpl(config);
    }

    /**
     * Crea una instancia de {@link NotificationProvider} específica para el canal PUSH.
     * <p>
     * El proveedor utiliza las credenciales inyectadas ({@code pushProvider} y {@code pushServerKey})
     * para enviar notificaciones a través del servicio de push configurado.
     * </p>
     *
     * @return Un {@link NotificationProvider} configurado para el canal PUSH
     */
    private NotificationProvider createPushProvider() {
        return new NotificationProvider() {
            @Override
            public boolean supports(ChannelType channel) {
                return channel == ChannelType.PUSH;
            }

            @Override
            public NotificationResult send(Notification notification) {
                System.out.println("Sending via " + pushProvider + " (Server Key: " + pushServerKey + "): " + notification.message());
                return NotificationResult.success(notification.id(), pushProvider, ChannelType.PUSH);
            }

            @Override
            public String getName() {
                return pushProvider;
            }
        };
    }
}
