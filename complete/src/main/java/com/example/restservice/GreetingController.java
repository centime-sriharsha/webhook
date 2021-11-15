package com.example.restservice;

import com.adyen.model.marketpay.notification.AccountHolderPayoutNotification;
import com.adyen.model.marketpay.notification.GenericNotification;
import com.adyen.model.notification.NotificationRequest;
import com.adyen.model.notification.NotificationRequestItem;
import com.adyen.model.notification.NotificationRequestItemContainer;
import com.adyen.util.HMACValidator;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidKeyException;
import java.security.SignatureException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {

    private NotificationHandler notificationHandler = new NotificationHandler();
    private static final String hmacSignatureKey = "DCE34E6EAE1C25C2B707719C7C469F2B941F85F06924CB8D18514CFA6AD66CDD";
    private final AtomicLong counter = new AtomicLong();
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    private final HMACValidator hmacValidator = new HMACValidator();

    @GetMapping("/")
    public String test() {
        return "Hello i am up and running !";
    }

    @PostMapping("/1.0/adyen/notification")
    public ResponseEntity<String> captureNotifications(@RequestHeader Map<String, String> headers, @RequestBody String payload)
            throws InvalidKeyException, SignatureException {
        System.out.println("##################");
        headers.forEach((key, value) -> {
            System.out.println(String.format("Header '%s' = %s", key, value));
        });

        System.out.println("##################");


        try {
            NotificationRequest request = notificationHandler.handleNotificationJson(payload);
            System.out.println("************************************");
            System.out.println(request.toString());
            System.out.println("************************************");
            testNotificationHmac(request.getNotificationItems());
            request.getNotificationItems().stream().map(x -> x.getEventCode()).forEach(x -> System.out.println(x.toString() + " " + LocalDateTime.now()));
            return new ResponseEntity<>("[accepted]", HttpStatus.OK);

        } catch (Exception ex) {
            if(ex instanceof SignatureException)
                throw ex;
        }


//
        try {
            compareSigntures(payload,headers.get("hmacsignature"));

            GenericNotification genericNotification = notificationHandler.handleMarketpayNotificationJson(payload);
            System.out.println("*********************");
            System.out.println(payload);

            return new ResponseEntity<>("[accepted]", HttpStatus.OK);

        } catch (Exception e) {
            System.out.println(e);
        }
        return new ResponseEntity<>("[denied]", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public void testNotificationHmac(List<NotificationRequestItem> list) throws InvalidKeyException, SignatureException {
        if (list == null || list.isEmpty())
            return;
        for (NotificationRequestItem x : list) {
            try {
                hmacValidator.validateHMAC(x, hmacSignatureKey);
            } catch (SignatureException signatureException) {
                signatureException.printStackTrace();
                throw signatureException;
            }

        }

    }

    public void compareSigntures(String payload,String hmacReceivedSignature) throws SignatureException {
        if(hmacReceivedSignature==null|| hmacReceivedSignature.isEmpty())
        {
            throw new SignatureException();
        }
       String generatedSignature= hmacValidator.calculateHMAC(payload, hmacSignatureKey);
        System.out.println("genereated signture:"+generatedSignature);
        System.out.println("recieved signture:"+hmacReceivedSignature);
        if(!hmacReceivedSignature.equals(generatedSignature))
        {
            throw new SignatureException();

        }
    }
}
