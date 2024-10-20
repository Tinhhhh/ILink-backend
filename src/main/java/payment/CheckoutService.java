package payment;

import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.service.CustomerOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final PayOS payOS;
    private final CustomerOrderService customerOrderService;

    @Transactional
    public String checkout(HttpServletRequest request, OrderInfo orderInfo) throws Exception {
        final String baseUrl = getBaseUrl(request);
//        final String productName = "Mì tôm hảo hảo ly";
//        final String description = "Thanh toan don hang";
//        final String returnUrl = baseUrl + "/#/paymentSuccess";
        final String returnUrl = "http://souvi.s3-website-ap-northeast-1.amazonaws.com/#/paymentSuccess";
//        final String cancelUrl = baseUrl + "/#/paymentError";
        final String cancelUrl = "http://souvi.s3-website-ap-northeast-1.amazonaws.com/#/paymentError";
        final int price = orderInfo.getTotalPrice();

        List<ItemData> items = orderInfo.getProducts().stream().map(product -> ItemData.builder().name(product.getProductName())
            .quantity(product.getQuantity()).price(product.getUnitPrice()).build()).toList();

        // Gen order code
        String currentTimeString = String.valueOf(new Date().getTime());
        long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));

//        ItemData item = ItemData.builder().name(productName).quantity(1).price(price).build();

        PaymentData paymentData = PaymentData.builder().orderCode(orderCode).amount(price).description(orderInfo.getDescription())
            .returnUrl(returnUrl).cancelUrl(cancelUrl).items(items).build();

        CheckoutResponseData data = payOS.createPaymentLink(paymentData);

        customerOrderService.saveOrder(orderInfo, String.valueOf(orderCode));

        return data.getCheckoutUrl();

    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }
}
