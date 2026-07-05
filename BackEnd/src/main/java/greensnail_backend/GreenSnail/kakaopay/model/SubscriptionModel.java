package greensnail_backend.GreenSnail.kakaopay.model;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Component
@Getter
public class SubscriptionModel {

    private final String modelName = "Premium Pass";
    private final int price = 14900;
}