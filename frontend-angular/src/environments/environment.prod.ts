export const environment = {
  production: true,
  // Remplacer par ton vrai domaine Cloudflare
  apiUrl: 'https://api.tondomaine.com/api',
  apiUrlSsr: process.env['API_GATEWAY_URL'] ? `${process.env['API_GATEWAY_URL']}/api` : 'http://api-gateway:8080/api',
  wsUrl: 'wss://ws.tondomaine.com'
};
