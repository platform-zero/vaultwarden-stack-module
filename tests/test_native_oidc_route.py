"""Source contract; effective YAML/IR refusal and hosts are generator-tested."""
import unittest
from pathlib import Path

class NativeOidcRoute(unittest.TestCase):
    def test_public_oidc_hostname_routes_to_native_host_without_tls_bypass(self):
        runtime=(Path(__file__).resolve().parents[1]/'stack.runtime.yaml').read_text()
        self.assertIn('- "keycloak.${DOMAIN}:host-gateway"',runtime)
        self.assertIn('SSO_AUTHORITY: "https://keycloak.${DOMAIN}/realms/webservices"',runtime)
        self.assertIn('SSL_CERT_FILE: "/ca/caddy-ca.crt"',runtime)
        self.assertIn('ROCKET_TLS_INSECURE_SKIP_VERIFICATION: "${VAULTWARDEN_TLS_INSECURE_SKIP_VERIFY:-false}"',runtime)
        self.assertIn('"caddy_ca:/ca:ro"',runtime)

if __name__=='__main__':unittest.main()
