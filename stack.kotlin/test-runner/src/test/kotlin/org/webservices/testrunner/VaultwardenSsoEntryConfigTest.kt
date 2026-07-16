package org.webservices.testrunner

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VaultwardenSsoEntryConfigTest {

    @Test
    fun `vaultwarden portal entry preselects internal sso organization`() {
        val caddyfile = TestSourceFiles.moduleText("vaultwarden", "stack.config/caddy/Caddyfile")
        val vaultwarden = serviceContract("vaultwarden")
        val portal = vaultwarden.getValue("portal").jsonObject

        assertTrue(caddyfile.contains("@vw_sso_login path /sso-login /sso-login/"))
        assertTrue(caddyfile.contains("import keycloak_auth vaultwarden"))
        assertTrue(caddyfile.contains("redir * \"/#/sso?identifier={\$VAULTWARDEN_ORG_ID}&email={http.request.header.Remote-Email}\" 302"))
        assertTrue(portal.getValue("visible").jsonPrimitive.boolean)
        assertEquals("vaultwarden", portal.getValue("hrefHost").jsonPrimitive.content)
        assertEquals("/sso-login", portal.getValue("path").jsonPrimitive.content)
    }

    @Test
    fun `vaultwarden sso derives email from keycloak verified email claim`() {
        val runtime = TestSourceFiles.moduleText("vaultwarden", "stack.runtime.yaml")
        val keycloakConfigure = TestSourceFiles.moduleText("keycloak", "stack.config/keycloak/configure-runtime.sh")

        assertTrue(runtime.contains("SSO_SCOPES: openid email profile"))
        assertTrue(runtime.contains("SSO_SIGNUPS_MATCH_EMAIL: true"))
        assertTrue(runtime.contains("SSO_ALLOW_UNKNOWN_EMAIL_VERIFICATION: false"))
        assertFalse(keycloakConfigure.contains("vaultwarden-email-verified"))
        assertFalse(keycloakConfigure.contains("\"email_verified\" \"true\""))
    }

    @Test
    fun `rootless vaultwarden reaches rootful smtp using the certificate hostname`() {
        val runtime = TestSourceFiles.moduleText("vaultwarden", "stack.runtime.yaml")

        assertTrue(runtime.contains("SMTP_HOST: \"mail.\${DOMAIN}\""))
        assertTrue(runtime.contains("- \"mail.\${DOMAIN}:host-gateway\""))
    }

    @Test
    fun `embedding service is not exposed in portal visible config`() {
        val inference = serviceContract("inference")
        val portal = inference.getValue("portal").jsonObject

        assertFalse(portal.getValue("visible").jsonPrimitive.boolean)
        assertFalse("hrefHost" in portal)
    }

    @Test
    fun `portal exposes restored keycloak backed sogo web ui`() {
        val sogo = serviceContract("sogo")
        val portal = sogo.getValue("portal").jsonObject

        assertEquals("SOGo", sogo.getValue("name").jsonPrimitive.content)
        assertTrue(portal.getValue("visible").jsonPrimitive.boolean)
        assertEquals("sogo", portal.getValue("hrefHost").jsonPrimitive.content)
        assertEquals(
            "Mail, calendar, and contacts with deterministic evidence views.",
            portal.getValue("description").jsonPrimitive.content,
        )
    }

    private fun serviceContract(id: String) = Json.parseToJsonElement(
        TestSourceFiles.moduleText("stack-foundation", "stack.config/service-contracts.json"),
    ).jsonObject.getValue("components").jsonObject.getValue(id).jsonObject
}
