package com.github.vzakharchenko.radius.radius.handlers.protocols;

import com.github.vzakharchenko.radius.models.OtpHolder;
import com.github.vzakharchenko.radius.radius.handlers.otp.IOtpPasswordFactory;
import com.github.vzakharchenko.radius.test.AbstractRadiusTest;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialModel;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.tinyradius.packet.AccessRequest;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class PAPTest extends AbstractRadiusTest {
    private AccessRequest request;

    @Mock
    private IOtpPasswordFactory passwordFactory;

    @BeforeMethod
    public void beforeMethods() {
        reset(passwordFactory);
        request = new AccessRequest(realDictionary, 0, new byte[16]);
        request.addAttribute(REALM_RADIUS, REALM_RADIUS_NAME);
        HashMap<String, OtpHolder> hashMap = new HashMap<>();
        hashMap.put("otp", new OtpHolder("otp", new CredentialModel(), Collections.singletonList("123456")));
        when(userCredentialManager.isValid(eq(realmModel), eq(userModel), any(CredentialInput.class))).thenReturn(false);
        when(passwordFactory.getOTPs(session)).thenReturn(hashMap);
    }

    @Test
    public void testPapSuccess() {
        request.setUserPassword("test");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        assertEquals(papProtocol.getType(), ProtocolType.PAP);
        papProtocol.answer(null, null);
        assertTrue(papProtocol.verifyPassword("test"));
        assertFalse(papProtocol.verifyPassword(null));
        assertFalse(papProtocol.verifyPassword(""));
        assertFalse(papProtocol.verifyPassword("asdf"));


    }

    @Test
    public void testPapKerberosFalse() {
        request.setUserPassword("test");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        assertFalse(papProtocol.verifyPassword());
    }

    @Test
    public void testPapPasswordSuccess() {
        request.setUserPassword("test");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        when(userCredentialManager.isValid(eq(realmModel), eq(userModel),
                any(CredentialInput.class))).thenReturn(true);
        assertTrue(papProtocol.verifyPassword());
    }

    @Test
    public void testPapKerberosSuccess() {
        request.setUserPassword("test");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        when(userCredentialManager.isValid(eq(realmModel), eq(userModel),
                any(CredentialInput.class))).thenReturn(false, true);
        assertTrue(papProtocol.verifyPassword());
    }

    @Test
    public void testOtpPassword() {
        request.setUserPassword("123456");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        papProtocol.setOtpPasswordGetter(passwordFactory);
        assertTrue(papProtocol.verifyPassword());
    }

    @Test
    public void testPasswordOtpPassword() {
        request.setUserPassword("test123456");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        papProtocol.setOtpPasswordGetter(passwordFactory);
        assertTrue(papProtocol.verifyPassword("test123456"));
    }

    @Test
    public void testPasswordOtpPasswordFailed() {
        request.setUserPassword("test1234567");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        papProtocol.setOtpPasswordGetter(passwordFactory);
        assertFalse(papProtocol.verifyPassword("test1234565"));
    }

    @Test
    public void testNoOtpPassword() {
        request.setUserPassword("1234567");
        PAPProtocol papProtocol = new PAPProtocol(request, session);
        papProtocol.setOtpPasswordGetter(passwordFactory);
        assertFalse(papProtocol.verifyPassword());
    }
}
