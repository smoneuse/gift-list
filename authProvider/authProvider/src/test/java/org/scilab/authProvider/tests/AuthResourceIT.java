package org.scilab.authProvider.tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scilab.authProvider.resources.requests.UserRequest;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.ConfigurationProperty;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;


@RunWith(SeedITRunner.class)
@LaunchWithUndertow
@ConfigurationProperty(name = "security.users.authAdmin.password", value = "passwordForTest")
public class AuthResourceIT {

    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Test
    public void testNonAuthRequest(){
        Response response = RestAssured.given()
                .expect().statusCode(401)
                .when().get(baseUrl + "/api/auth?login=testLogin&password=testPasswd");

         response = RestAssured.given()
                 .auth().basic("authAdmin","notThePassword")
                .expect().statusCode(401)
                .when().get(baseUrl + "/api/auth?login=testLogin&password=testPasswd");
    }

    @Test
    public void testRegisterUser() throws JsonProcessingException {
        String userLogin="userLogin";
        String userPass="userPass";
        String userRole="USER";
        UserRequest addedUser= new UserRequest();
        addedUser.setUser(userLogin);
        addedUser.setPassword(userPass);
        addedUser.setRole(userRole);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(addedUser);
        try {
            Response response = RestAssured.given()
                    .auth().basic("authAdmin", "passwordForTest")
                    .header("Content-Type", "application/json")
                    .body(json)
                    .expect().statusCode(200)
                    .when().post(baseUrl + "/api/auth/register");
            Assert.assertEquals("The operation is not successful","SUCCESS",response.jsonPath().get("status"));

            response=RestAssured.given()
                    .auth().basic("authAdmin", "passwordForTest")
                    .queryParam("login","NoGoodLogin")
                    .queryParam("password", userPass)
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/auth/authenticate");
            Assert.assertEquals("The user should be unknown","UNKNOWN",response.jsonPath().get("status"));

            response=RestAssured.given()
                    .auth().basic("authAdmin", "passwordForTest")
                    .queryParam("login",userLogin)
                    .queryParam("password", "NotGoodPass")
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/auth/authenticate");
            Assert.assertEquals("The password should be refused","INVALID",response.jsonPath().get("status"));

            response=RestAssured.given()
                    .auth().basic("authAdmin", "passwordForTest")
                    .queryParam("login",userLogin)
                    .queryParam("password", userPass)
                    .expect().statusCode(200)
                    .when().get(baseUrl + "/api/auth/authenticate");
            Assert.assertEquals("The password should be refused","AUTHENTICATED",response.jsonPath().get("status"));

        }
        finally {
            Response response = RestAssured.given()
                    .auth().basic("authAdmin", "passwordForTest")
                    .queryParam("login", userLogin)
                    .expect().statusCode(200)
                    .when().delete(baseUrl + "/api/auth/delete");
        }
    }


}
