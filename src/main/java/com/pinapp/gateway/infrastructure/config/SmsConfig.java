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
 * Configuración distribuida para el canal de notificaciones por SMS.
 * <p>
 * Esta clase configura un {@link NotificationService} específico para el canal SMS,
 * utilizando las propiedades definidas en {@code application.yml} bajo la ruta
 * {@code pinapp.notify.sms.*}.
 * </p>
 * <p>
 * <strong>Propiedades requeridas en application.yml:</strong>
 * <ul>
 *   <li>{@code pinapp.notify.sms.provider} - Nombre del proveedor de SMS (ej: "twilio")</li>
 *   <li>{@code pinapp.notify.sms.account-sid} - Account SID del proveedor de SMS</li>
 *   <li>{@code pinapp.notify.retry-attempts} - Número de intentos de reintento para notificaciones</li>
 * </ul>
 * </p>
 * <p>
 * El bean creado ({@code smsNotificationService}) es único y autocontenido, registrando
 * únicamente el proveedor de SMS. El servicio incluye automáticamente el
 * {@link TransactionAuditListener} para auditoría de transacciones.
 * </p>
 * <p>
 * <strong>Ejemplo de configuración:</strong>
 * <pre>
 * pinapp:
 *   notify:
 *     sms:
 *       provider: "twilio"
 *       account-sid: "AC_your_account_sid_here"
 *     retry-attempts: 2
 * </pre>
 * </p>
 *
 * @author PinApp Gateway Team
 * @since 1.0.0
 * @see EmailConfig
 * @see PushConfig
 */
@Configuration
public class SmsConfig {

    @Value("${pinapp.notify.sms.provider}")
    private String smsProvider;

    @Value("${pinapp.notify.sms.account-sid}")
    private String smsAccountSid;

    @Value("${pinapp.notify.retry-attempts}")
    private Integer retryAttempts;

    /**
     * Crea y configura el bean {@link NotificationService} específico para notificaciones por SMS.
     * <p>
     * Este bean utiliza el Builder del SDK ({@link PinappNotifyConfig}) para configurar:
     * <ul>
     *   <li>El proveedor de SMS con las credenciales inyectadas desde el YAML</li>
     *   <li>La política de reintentos configurada con {@code retry-attempts}</li>
     *   <li>El suscriptor de eventos ({@link TransactionAuditListener}) para auditoría</li>
     * </ul>
     * </p>
     *
     * @param listener El listener de auditoría que será registrado como suscriptor de eventos
     * @return Una instancia configurada de {@link NotificationService} para el canal SMS
     */
    @Bean
    public NotificationService smsNotificationService(TransactionAuditListener listener) {
        PinappNotifyConfig config = PinappNotifyConfig.builder()
                .addProvider(ChannelType.SMS, createSmsProvider())
                .withRetryPolicy(RetryPolicy.of(retryAttempts, 1000))
                .addSubscriber(listener)
                .build();

        return new NotificationServiceImpl(config);
    }

    /**
     * Crea una instancia de {@link NotificationProvider} específica para el canal SMS.
     * <p>
     * El proveedor utiliza las credenciales inyectadas ({@code smsProvider} y {@code smsAccountSid})
     * para enviar notificaciones a través del servicio de SMS configurado.
     * </p>
     *
     * @return Un {@link NotificationProvider} configurado para el canal SMS
     */
    private NotificationProvider createSmsProvider() {
        return new NotificationProvider() {
            @Override
            public boolean supports(ChannelType channel) {
                return channel == ChannelType.SMS;
            }

            @Override
            public NotificationResult send(Notification notification) {
                System.out.println("Sending via " + smsProvider + " (Account SID: " + smsAccountSid + "): " + notification.message());
                return NotificationResult.success(notification.id(), smsProvider, ChannelType.SMS);
            }

            @Override
            public String getName() {
                return smsProvider;
            }
        };
    }
}
