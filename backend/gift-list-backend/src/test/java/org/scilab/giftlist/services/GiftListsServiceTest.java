package org.scilab.giftlist.services;

import com.google.common.base.Strings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scilab.giftlist.domain.models.list.GiftList;
import org.scilab.giftlist.domain.models.security.AuthUser;
import org.scilab.giftlist.infra.exceptions.GiftListException;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.internal.account.AccountService;
import org.scilab.giftlist.internal.list.GiftListsService;
import org.scilab.giftlist.internal.security.AuthUserService;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.Logging;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.transaction.Transactional;
import org.slf4j.Logger;


import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(SeedITRunner.class)
public class GiftListsServiceTest {
    @Logging
    private Logger logger;

    @Configuration("application.init-user.login")
    private String testUserLogin;

    @Inject
    private GiftListsService giftListsService;
    @Inject
    private AccountService accountService;

    @Inject
    private AuthUserService authUserService;

    @Test
    public void testCreateList() throws GiftListException {
        String titleTest="This is a test list";
        String description="testListDescription";
        GiftList createdList=null;
        try{
            Assert.assertTrue(accountService.ownedLists(testUserLogin).isEmpty());
            createdList =giftListsService.createList(testUserLogin,titleTest,description);
            Assert.assertNotNull("the created list is null",createdList);
            Assert.assertFalse("the created list identifier is blank", Strings.isNullOrEmpty(createdList.getId()));
            Assert.assertEquals(titleTest, createdList.getTitle());
            Assert.assertEquals(description, createdList.getDescription());
            Set<GiftList> userGiftLists=accountService.ownedLists(testUserLogin);
            Assert.assertFalse(userGiftLists.isEmpty());
            Assert.assertEquals(1, userGiftLists.size());
            GiftList[] userLists = (GiftList[]) userGiftLists.toArray();
            Assert.assertEquals(createdList.getId(), userLists[0].getId());
            Assert.assertEquals(testUserLogin, userLists[0].getOwner().getLogin());
        }
        catch (GiftListException gle){
            Assert.fail("GiftListException during test :"+gle.getMessage());
        }
        finally {
            if(createdList !=null){
                giftListsService.deleteList(createdList.getId());
            }
            Assert.assertTrue(accountService.ownedLists(testUserLogin).isEmpty());
        }
    }

    @Test
    public void testGrantRevokeView() throws GiftListException {
        String viewerLogin="Viewerlogin";
        List<String> createdListsId= new ArrayList<>();
        try{
            AuthUser viewerTest =createTestUser(viewerLogin,"ViewerPassword");
            createdListsId.add(createTestList(testUserLogin,"List 1","").getId());
            createdListsId.add(createTestList(testUserLogin,"List 2","").getId());
            Assert.assertTrue(accountService.authorizedLists(viewerTest.getLogin()).isEmpty());
            for(String listId : createdListsId){
                Assert.assertTrue(accountService.listViewers(listId).isEmpty());
            }
            accountService.addViewer(viewerLogin, createdListsId.get(0));
            accountService.addViewer(viewerLogin, createdListsId.get(1));
            Set<GiftList> listAuthorized= accountService.authorizedLists(viewerLogin);
            Assert.assertEquals(2, listAuthorized.size());
            for(String listId : createdListsId){
                Set<AuthUser> authViewers= accountService.listViewers(listId);
                Assert.assertEquals(1,authViewers.size());
                AuthUser[] viewers= (AuthUser[]) authViewers.stream().toArray();
                Assert.assertEquals(viewerLogin, viewers[0].getLogin());
            }
            accountService.revokeViewer(viewerLogin, createdListsId.get(0));
            listAuthorized= accountService.authorizedLists(viewerLogin);
            Assert.assertEquals(1, listAuthorized.size());
            //True delete without revoke
            giftListsService.deleteList(createdListsId.get(1));
        }
        catch (GiftListException gle){
            Assert.fail("GiftListException during testGrantRevokeView :"+gle.getMessage());
        }
        finally {
            authUserService.deleteUser(viewerLogin);
            for(String listId : createdListsId){
                try {
                    giftListsService.deleteList(listId);
                }
                catch (GiftListException gle){
                    logger.error("GiftListException while deleting test list");
                }
            }
            Assert.assertFalse(authUserService.findAccount(viewerLogin).isPresent());
            Assert.assertTrue(accountService.ownedLists(testUserLogin).isEmpty());
        }
    }

    private AuthUser createTestUser(String login, String password) throws GiftListException {
        return authUserService.register(login, password, GiftListRoles.USER);
    }
    private GiftList createTestList(String ownerId, String title, String desc) throws GiftListException{
        return giftListsService.createList(ownerId,title,desc);
    }
}
