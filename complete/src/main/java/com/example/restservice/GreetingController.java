package com.example.restservice;

import com.adyen.model.marketpay.notification.GenericNotification;
import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.util.HMACValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private NotificationHandler notificationHandler = new NotificationHandler();
    private static final String signatureKey = "DCE34E6EAE1C25C2B707719C7C469F2B941F85F06924CB8D18514CFA6AD66CDD";
    private final AtomicLong counter = new AtomicLong();
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");


    @GetMapping("/")
    public String test() {
        return "Hello i am up and running !";
    }

    @PostMapping("/1.0/adyen/notification")
    public ResponseEntity<ResponseWrapper> captureNotifications(@RequestHeader(value = "HmacSignature", required = false) String headerSignature, @RequestHeader(value = "Protocol", required = false) String protocol, @RequestBody String payload) throws InvalidKeyException {

        protocol = protocol == null || protocol.isEmpty() ? "HmacSHA256" : protocol;


        //    testNotificationHmac(payload, headerSignature, protocol);
        try {
            NotificationRequest request = notificationHandler.handleNotificationJson(payload);
            testNotificationHmac(request.getNotificationItems(),headerSignature,protocol);
            request.getNotificationItems().stream().forEach(x -> System.out.println(x.toString() + " " + LocalDateTime.now()));
            return new ResponseEntity<>(new ResponseWrapper("[accepted]"), HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
        }


        try {
            GenericNotification genericNotification = notificationHandler.handleMarketpayNotificationJson(payload);
            System.out.println(genericNotification.getEventType() + " " + LocalDateTime.now());
            return new ResponseEntity<>(new ResponseWrapper("[accepted]"), HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
        }
        return new ResponseEntity<>(new ResponseWrapper("[denied]"), HttpStatus.OK);
    }

    public void testNotificationHmac(List<NotificationRequestItem> list, String headerSignture, String protocol) throws InvalidKeyException {
        HMACValidator hmacValidator = new HMACValidator();
        list.stream().forEach(x -> {
            try {
                hmacValidator.validateHMAC(x, signatureKey);
            } catch (SignatureException signatureException) {
                signatureException.printStackTrace();
            }

        });

    }

}
