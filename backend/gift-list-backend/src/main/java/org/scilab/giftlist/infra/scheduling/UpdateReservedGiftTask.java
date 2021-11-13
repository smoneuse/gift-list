package org.scilab.giftlist.infra.scheduling;

import org.scilab.giftlist.internal.gift.GiftService;
import org.seedstack.scheduler.Scheduled;
import org.seedstack.scheduler.SchedulingContext;
import org.seedstack.scheduler.Task;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

import javax.inject.Inject;

@Scheduled("0 0 0 ? * * *")
public class UpdateReservedGiftTask implements Task {
    @Logging
    private Logger logger;

    @Inject
    private GiftService giftService;

    @Override
    public void execute(SchedulingContext schedulingContext) throws Exception {
        logger.info("Starting scan gift to set offered");
        giftService.scanAndSetOffered();
        logger.info("Ending scan gift to set offered");
    }
}
