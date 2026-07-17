package fedor.dev.payment.api;



import fedor.dev.payment.domain.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;


    @PostMapping
    public CreatePaymentResponseDto createPayment(
            @RequestBody CreatePaymentRequestDto request
    ){
        log.info("Received request: paymentRequest={}", request);

        return paymentService.makePayment(request);

    }



}
