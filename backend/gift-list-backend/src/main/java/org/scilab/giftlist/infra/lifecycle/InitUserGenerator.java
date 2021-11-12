package org.scilab.giftlist.infra.lifecycle;

import org.scilab.giftlist.infra.exceptions.security.AuthException;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.coffig.Config;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.LifecycleListener;
import org.seedstack.seed.Logging;
import org.slf4j.Logger;

import javax.inject.Inject;

/**
 * Called on startup, initiates if not present the first user
 */
public class InitUserGenerator implements LifecycleListener {

    @Logging
    private Logger logger;

    @Inject
    private AuthUserService authUserService;

    @Configuration("application.init-user.login")
    private String initUserLogin;
    @Configuration("application.init-user.password")
    private String initUserPassword;

    @Override
    public void started() {
        logger.info("Initializing first user");
        try {
            if (!authUserService.checkUserKnown(initUserLogin)) {
                logger.info("Registering first user");
                authUserService.register(initUserLogin, initUserPassword, GiftListRoles.ADMIN);
            }
            else{
                logger.info("First user already registered - No need for initialization");
            }
            logger.info("End of first user init");
        }
        catch(AuthException ae){
            logger.error("AuthException during first user initialization :"+ae.getMessage());
        }
    }
}
