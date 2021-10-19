package org.scilab.giftlist.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.JsonOutput;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.scilab.giftlist.resources.users.representation.SingleUserRepresentation;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;
import org.seedstack.seed.undertow.LaunchWithUndertow;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
@LaunchWithUndertow
public class UserResourcesIT {
    @Configuration("runtime.web.baseUrl")
    private String baseUrl;

    @Test
    public void testGetNonExistingUser() throws Exception {
        String unknownId="NotAnId";
        Response response = given()
                .expect().statusCode(404)
                .when().get(baseUrl + "/api/user/id/"+unknownId);
    }

    @Test
    public void testDeleteNonExistingUser() {
        String unknownId="NotAnId";
        sendRequestDelete(unknownId);
    }

    @Test
    public void testCreateDeleteUser(){
        SingleUserRepresentation user = new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation responseObj= sendRequestCreate(user);
        assertThat(responseObj).isNotNull();
        assertThat(responseObj.getName()).isEqualTo(user.getName());
        assertThat(responseObj.geteMail()).isEqualTo(user.geteMail());
        assertThat(responseObj.getId()).isNotBlank();
        sendRequestDelete(responseObj.getId());
    }

    @Test
    public void testSearchUser(){
        SingleUserRepresentation user = new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation createdUser1= sendRequestCreate(user);
        SingleUserRepresentation secondUser = new SingleUserRepresentation();
        secondUser.setName("Bruce Wayne");
        secondUser.seteMail("batman@mail.com");
        SingleUserRepresentation createdUser2= sendRequestCreate(secondUser);
        try {
            String jsonResponse =given()
                    .baseUri(baseUrl)
                    .basePath("/api/user/search")
                    .queryParam("name","uce")
                    .get()
                    .then().assertThat().statusCode(200)
                    .extract().body().asString();
            SingleUserRepresentation[] results =  parseJson(jsonResponse, SingleUserRepresentation[].class);
            assertThat(results).isNotNull();
            assertThat(results.length).isEqualTo(1);
            assertThat(results[0].getName()).isEqualTo(secondUser.getName());
            assertThat(results[0].geteMail()).isEqualTo(secondUser.geteMail());

            jsonResponse =given()
                    .baseUri(baseUrl)
                    .basePath("/api/user/search")
                    .queryParam("name","e")
                    .get()
                    .then().assertThat().statusCode(200)
                    .extract().body().asString();
            results =  parseJson(jsonResponse, SingleUserRepresentation[].class);
            assertThat(results).isNotNull();
            assertThat(results.length).isEqualTo(2);
        }
        finally {
            sendRequestDelete(createdUser1.getId());
            sendRequestDelete(createdUser2.getId());
        }
    }

    @Test
    public void testUpdateUser(){
        SingleUserRepresentation user =new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation createdUser= sendRequestCreate(user);
        createdUser.setName("Foo bar");
        SingleUserRepresentation updatedUser = sendRequestUpdate(createdUser);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.geteMail()).isEqualTo(createdUser.geteMail());
        assertThat(updatedUser.getName()).isNotEqualTo(user.getName());
        assertThat(updatedUser.getName()).isEqualTo(createdUser.getName());
        sendRequestDelete(updatedUser.getId());
    }
    @Test
    public void testCreateSameName(){
        SingleUserRepresentation user =new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation createdUser= sendRequestCreate(user);
        try {
            SingleUserRepresentation secondUser =new SingleUserRepresentation();
            secondUser.setName("John Doe");
            secondUser.seteMail("anotherMail@mail.com");
            given().log().all()
                    .baseUri(baseUrl)
                    .basePath("/api/user")
                    .header("Content-Type", "application/json")
                    .body(JsonOutput.toJson(secondUser))
                    .post()
                    .then()
                    .assertThat()
                    .statusCode(400);
            secondUser.setName("Another User");
            secondUser.seteMail("jDoe@mail.com");
            given().log().all()
                    .baseUri(baseUrl)
                    .basePath("/api/user")
                    .header("Content-Type", "application/json")
                    .body(JsonOutput.toJson(secondUser))
                    .post()
                    .then()
                    .assertThat()
                    .statusCode(400);
        }
        finally {
            sendRequestDelete(createdUser.getId());
        }
    }
    @Test
    public void testNameAlreadyExistOnUpdate(){
        SingleUserRepresentation user =new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation createdUser= sendRequestCreate(user);
        SingleUserRepresentation secondUser =new SingleUserRepresentation();
        secondUser.setName("Jane Dae");
        secondUser.seteMail("second@mail.com");
        SingleUserRepresentation createdSecondUser= sendRequestCreate(secondUser);
        try {
            createdSecondUser.setName("John Doe");
            given().log().all()
                    .baseUri(baseUrl)
                    .basePath("/api/user")
                    .header("Content-Type", "application/json")
                    .body(JsonOutput.toJson(createdSecondUser))
                    .put()
                    .then()
                    .assertThat()
                    .statusCode(400);
        }
        finally {
            sendRequestDelete(createdUser.getId());
            sendRequestDelete(createdSecondUser.getId());
        }
    }
    @Test
    public void testGetAllUsers(){
        SingleUserRepresentation user =new SingleUserRepresentation();
        user.setName("John Doe");
        user.seteMail("jDoe@mail.com");
        SingleUserRepresentation createdUser= sendRequestCreate(user);
        SingleUserRepresentation secondUser =new SingleUserRepresentation();
        secondUser.setName("Jane Dae");
        secondUser.seteMail("second@mail.com");
        SingleUserRepresentation createdSecondUser= sendRequestCreate(secondUser);
        try{
            String response =given().log().all()
                    .baseUri(baseUrl).basePath("/api/user/all")
                    .get()
                    .then()
                    .assertThat()
                    .statusCode(200)
                    .extract()
                    .body().asString();
            assertThat(response).isNotBlank();
            SingleUserRepresentation[] allUsers= parseJson(response,SingleUserRepresentation[].class);
            assertThat(allUsers).isNotNull();
            assertThat(allUsers.length).isNotEqualTo(0);
            Arrays.stream(allUsers).forEach(singleUserRepresentation -> {
                Assert.assertTrue(singleUserRepresentation.getName().equals(user.getName()) || singleUserRepresentation.getName().equals(secondUser.getName()));
                Assert.assertTrue(singleUserRepresentation.geteMail().equals(user.geteMail()) || singleUserRepresentation.geteMail().equals(secondUser.geteMail()));
            });
        }
        finally {
            sendRequestDelete(createdUser.getId());
            sendRequestDelete(createdSecondUser.getId());
        }
    }

    private <T> T parseJson(String jsonString, Class<T> objClass)  {
        ObjectMapper mapper = new ObjectMapper();
        T result= null;
        try {
            result = mapper.readValue(jsonString, objClass);
        } catch (JsonProcessingException e) {
            Assert.fail("JsonProcessingException during test");
        }
        return result;
    }

    private SingleUserRepresentation sendRequestCreate(SingleUserRepresentation user){
        String response= given()
                .log()
                .all()
                .baseUri(baseUrl)
                .basePath("/api/user")
                .header("Content-Type", "application/json")
                .body(JsonOutput.toJson(user))
                .post()
                .then()
                .assertThat()
                .statusCode(200)
                .extract().body().asString();
        return parseJson(response, SingleUserRepresentation.class);
    }

    private void sendRequestDelete(String userId){
        given().log().all()
                .baseUri(baseUrl)
                .basePath("/api/user/id")
                .delete("/"+userId)
                .then()
                .assertThat()
                .statusCode(204);
    }

    private SingleUserRepresentation sendRequestUpdate(SingleUserRepresentation modifiedUser){
        String response= given()
                .log()
                .all()
                .baseUri(baseUrl)
                .basePath("/api/user")
                .header("Content-Type", "application/json")
                .body(JsonOutput.toJson(modifiedUser))
                .put()
                .then()
                .assertThat()
                .statusCode(200)
                .extract().body().asString();
        return parseJson(response, SingleUserRepresentation.class);
    }
}
