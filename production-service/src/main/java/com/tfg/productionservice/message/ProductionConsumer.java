package com.tfg.productionservice.message;

import com.tfg.productionservice.event.ProductionEvent;
import com.tfg.productionservice.service.ProductionService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
public class ProductionConsumer {

    private final ProductionService productionService;

    public ProductionConsumer(ProductionService productionService) {

        this.productionService = productionService;
    }

    // This method receive two type of events.
    // If event is approved the production starts,
    // if the event is reject start the compensating transaction
    @RabbitListener(queues = ProductionPublish.PRODUCTION_QUEUE)
    public void consume(ProductionEvent event) {

        if ("production.approved".equals(event.getEvent())) {
            productionService.startProduction(event.getProductionId());
        }

        if ("production.rejected".equals(event.getEvent())) {
            productionService.compensateRejectedProduction(
                    event.getProductionId(),
                    event.getAmount()
            );
        }
    }
}
