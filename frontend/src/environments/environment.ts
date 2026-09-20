export const environment = {
  production: true,
  // Relative path: a production deployment is expected to reverse-proxy
  // /api to the backend, same as the dev-server proxy does locally
  // (proxy.conf.json). Not yet decided/implemented - deployment is out of
  // scope for this frontend increment.
  apiBaseUrl: '/api'
};
