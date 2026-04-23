export const environment = {
  production: true,
  apiUrl: '/api',
  // En SSR (Node.js), les requêtes relatives ne fonctionnent pas — on utilise l'API Gateway interne Docker
  apiUrlSsr: process.env['API_GATEWAY_URL'] ? `${process.env['API_GATEWAY_URL']}/api` : 'http://api-gateway:8080/api',
  wsUrl: process.env['WS_URL'] || 'ws://api-gateway:8080/ws'
};
