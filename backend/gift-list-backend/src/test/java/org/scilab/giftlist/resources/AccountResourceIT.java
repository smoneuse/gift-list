package org.scilab.giftlist.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scilab.giftlist.infra.security.GiftListRoles;
import org.scilab.giftlist.resources.security.models.request.CreateAccountModel;
import org.scilab.giftlist.resources.security.models.request.UpdateAccountModel;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class AccountResourceIT {

    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Configuration("application.init-user.login")
    private String legitLogin;
    @Configuration("application.init-user.password")
    private String legitPassword;

    @Test
    public void testNonAuthRequest(){
        Response response = RestAssured.given()
                .expect().statusCode(401)
                .when().delete(baseUrl + "/api/account/notAUser");

        response = RestAssured.given()
                .auth().basic(legitLogin,"notThePassword")
                .expect().statusCode(401)
                .when().delete(baseUrl + "/api/account/notAUser");

        response = RestAssured.given()
                .auth().basic(legitLogin,legitPassword)
                .expect().statusCode(204)
                .when().delete(baseUrl + "/api/account/notAUser");
    }

    @Test
    public void testCreateAccount() throws JsonProcessingException {
        CreateAccountModel newUser = new CreateAccountModel();
        newUser.setLogin("glTestUser");
        newUser.setPassword("glTestPassword");
        newUser.setRole(GiftListRoles.USER.toString());
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonBody = ow.writeValueAsString(newUser);
        try{
            Response response = RestAssured.given()
                    .auth().basic(legitLogin, legitPassword)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .expect().statusCode(200)
                    .when().post(baseUrl + "/api/account");
            Assert.assertEquals("The operation is not successful","SUCCESS",response.jsonPath().get("status"));

            //Try recreate same user, should get a ALREADY_PRESENT
            response = RestAssured.given()
                    .auth().basic(legitLogin, legitPassword)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .expect().statusCode(200)
                    .when().post(baseUrl + "/api/account");
            Assert.assertEquals("User have been created twice","ALREADY_PRESENT",response.jsonPath().get("status"));

            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(),newUser.getPassword())
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/account/echo/TestEchoing");
            Assert.assertEquals("Echo request failed","SUCCESS",response.jsonPath().get("status"));

            //Try forbidden action with non admin user
            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(),newUser.getPassword())
                    .expect().statusCode(403)
                    .when().delete(baseUrl + "/api/account/ThisIsNotAValidAccount");
        }
        finally {
            Response delResponse = RestAssured.given()
                    .auth().basic(legitLogin,legitPassword)
                    .expect().statusCode(204)
                    .when().delete(baseUrl + "/api/account/"+newUser.getLogin());
        }
    }

    @Test
    public void testForceUpdateUser() throws JsonProcessingException {
        CreateAccountModel newUser = new CreateAccountModel();
        newUser.setLogin("glTestUser");
        newUser.setPassword("glTestPassword");
        newUser.setRole(GiftListRoles.USER.toString());
        String updatedPassword="ThisIsTheNewPassword";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonBody = ow.writeValueAsString(newUser);

        try{
            Response response = RestAssured.given()
                    .auth().basic(legitLogin, legitPassword)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .expect().statusCode(200)
                    .when().post(baseUrl + "/api/account");
            Assert.assertEquals("Could not create user in testForceUpdateUser","SUCCESS",response.jsonPath().get("status"));

            UpdateAccountModel updatedUser= new UpdateAccountModel();
            updatedUser.setLogin(newUser.getLogin());
            updatedUser.setNewPassword(updatedPassword);
            updatedUser.setNewRole(GiftListRoles.ADMIN.toString());

            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(),newUser.getPassword())
                    .expect().statusCode(403)
                    .when().get(baseUrl + "/api/account/echoAdmin/TestEchoing");

            String jsonUpdate = ow.writeValueAsString(updatedUser);
            response = RestAssured.given()
                    .auth().basic(legitLogin, legitPassword)
                    .header("Content-Type", "application/json")
                    .body(jsonUpdate)
                    .expect().statusCode(200)
                    .when().put(baseUrl + "/api/account/force-update");
            Assert.assertEquals("Update request is not successful","SUCCESS",response.jsonPath().get("status"));

            //The admin echoing with the new password should be ok
            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(),updatedPassword)
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/account/echoAdmin/TestEchoing");
            Assert.assertEquals("Echo admin request failed","SUCCESS",response.jsonPath().get("status"));
        }
        finally {
            Response delResponse = RestAssured.given()
                    .auth().basic(legitLogin,legitPassword)
                    .expect().statusCode(204)
                    .when().delete(baseUrl + "/api/account/"+newUser.getLogin());
        }
    }

    @Test
    public void testUserAutoUpdate() throws JsonProcessingException {
        CreateAccountModel newUser = new CreateAccountModel();
        newUser.setLogin("glTestUser");
        newUser.setPassword("glTestPassword");
        newUser.setRole(GiftListRoles.USER.toString());
        String updatedPassword="ThisIsTheNewPassword";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonBody = ow.writeValueAsString(newUser);

        try{
            Response response = RestAssured.given()
                    .auth().basic(legitLogin, legitPassword)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .expect().statusCode(200)
                    .when().post(baseUrl + "/api/account");
            Assert.assertEquals("Could not create user in testUserAutoUpdate","SUCCESS",response.jsonPath().get("status"));

            UpdateAccountModel updatedUser= new UpdateAccountModel();
            updatedUser.setLogin(newUser.getLogin());
            updatedUser.setCurrentPassword(newUser.getPassword());
            updatedUser.setNewPassword(updatedPassword);

            String jsonUpdate = ow.writeValueAsString(updatedUser);
            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(), newUser.getPassword())
                    .header("Content-Type", "application/json")
                    .body(jsonUpdate)
                    .expect().statusCode(200)
                    .when().put(baseUrl + "/api/account");
            Assert.assertEquals("Update request is not successful","SUCCESS",response.jsonPath().get("status"));

            //Echoing request with new password should be successful
            response = RestAssured.given()
                    .auth().basic(newUser.getLogin(),updatedPassword)
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/account/echo/TestEchoing");
            Assert.assertEquals("Echo request failed","SUCCESS",response.jsonPath().get("status"));

        }
        finally {
            Response delResponse = RestAssured.given()
                    .auth().basic(legitLogin,legitPassword)
                    .expect().statusCode(204)
                    .when().delete(baseUrl + "/api/account/"+newUser.getLogin());
        }
    }
}
