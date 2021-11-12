package org.scilab.giftlist.security;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.seed.Configuration;
import org.seedstack.seed.testing.junit4.SeedITRunner;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class GLSecurityIT {
    @Inject
    private SecurityManager securityManager;

    @Configuration("application.init-user.login")
    private String legitLogin;
    @Configuration("application.init-user.password")
    private String legitPassword;

    @Test
    public void correctLoginTest(){
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(legitLogin, legitPassword);
        subject.login(token);
        assertThat(subject.isAuthenticated()).isTrue();
    }

    @Test(expected = AuthenticationException.class)
    public void unknownUserTest(){
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("jdoe", "dummy");
        subject.login(token);
    }

    @Test(expected = IncorrectCredentialsException.class)
    public void incorrectCredentialsTest(){
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(legitLogin, "dummy");
        subject.login(token);
    }

    @Test
    public void testUserRoleAdmin(){
        ThreadContext.bind(securityManager);
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(legitLogin, legitPassword);
        subject.login(token);
        securityManager.checkRole(subject.getPrincipals(), "userRole");
        securityManager.checkRole(subject.getPrincipals(), "adminRole");
        securityManager.checkPermission(subject.getPrincipals(),"standardOps");
        securityManager.checkPermission(subject.getPrincipals(),"adminOps");
    }
}
