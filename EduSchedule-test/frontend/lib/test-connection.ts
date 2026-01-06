/**
 * Script de test de connexion Backend/Frontend
 * Utilisez ce fichier pour vérifier que l'API Gateway est accessible
 */

import { API_CONFIG } from './api-config'

export async function testBackendConnection() {
  const results = {
    apiGatewayReachable: false,
    corsConfigured: false,
    authEndpointAvailable: false,
    errors: [] as string[]
  }

  try {
    // Test 1: Vérifier que l'API Gateway est accessible
    console.log('🔍 Test 1: Vérification de l\'accessibilité de l\'API Gateway...')
    console.log(`URL de base: ${API_CONFIG.baseURL}`)
    
    const healthResponse = await fetch(`${API_CONFIG.baseURL}/actuator/health`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    if (healthResponse.ok) {
      results.apiGatewayReachable = true
      console.log('✅ API Gateway accessible')
    } else {
      results.errors.push(`API Gateway non accessible: ${healthResponse.status}`)
      console.error('❌ API Gateway non accessible:', healthResponse.status)
    }
  } catch (error) {
    results.errors.push(`Erreur de connexion à l'API Gateway: ${error}`)
    console.error('❌ Erreur de connexion à l\'API Gateway:', error)
  }

  try {
    // Test 2: Vérifier la configuration CORS
    console.log('\n🔍 Test 2: Vérification de la configuration CORS...')
    
    const corsResponse = await fetch(`${API_CONFIG.baseURL}/api/auth/login`, {
      method: 'OPTIONS',
      headers: {
        'Origin': window.location.origin,
        'Access-Control-Request-Method': 'POST',
        'Access-Control-Request-Headers': 'Content-Type'
      }
    })
    
    const allowOrigin = corsResponse.headers.get('Access-Control-Allow-Origin')
    const allowMethods = corsResponse.headers.get('Access-Control-Allow-Methods')
    
    if (allowOrigin && allowMethods) {
      results.corsConfigured = true
      console.log('✅ CORS configuré correctement')
      console.log(`   - Allow-Origin: ${allowOrigin}`)
      console.log(`   - Allow-Methods: ${allowMethods}`)
    } else {
      results.errors.push('CORS non configuré correctement')
      console.error('❌ CORS non configuré correctement')
    }
  } catch (error) {
    results.errors.push(`Erreur lors du test CORS: ${error}`)
    console.error('❌ Erreur lors du test CORS:', error)
  }

  try {
    // Test 3: Vérifier que l'endpoint d'authentification est disponible
    console.log('\n🔍 Test 3: Vérification de l\'endpoint d\'authentification...')
    
    const authResponse = await fetch(`${API_CONFIG.baseURL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: 'test',
        password: 'test'
      })
    })
    
    // On s'attend à une erreur 401 ou 403, pas à une erreur de connexion
    if (authResponse.status === 401 || authResponse.status === 403 || authResponse.status === 400) {
      results.authEndpointAvailable = true
      console.log('✅ Endpoint d\'authentification disponible')
    } else if (authResponse.status === 404) {
      results.errors.push('Endpoint d\'authentification non trouvé (404)')
      console.error('❌ Endpoint d\'authentification non trouvé (404)')
    } else {
      results.errors.push(`Réponse inattendue de l'endpoint d'authentification: ${authResponse.status}`)
      console.error('⚠️ Réponse inattendue:', authResponse.status)
    }
  } catch (error) {
    results.errors.push(`Erreur lors du test de l'endpoint d'authentification: ${error}`)
    console.error('❌ Erreur lors du test de l\'endpoint d\'authentification:', error)
  }

  // Résumé
  console.log('\n📊 Résumé des tests:')
  console.log('='.repeat(50))
  console.log(`API Gateway accessible: ${results.apiGatewayReachable ? '✅' : '❌'}`)
  console.log(`CORS configuré: ${results.corsConfigured ? '✅' : '❌'}`)
  console.log(`Endpoint auth disponible: ${results.authEndpointAvailable ? '✅' : '❌'}`)
  
  if (results.errors.length > 0) {
    console.log('\n❌ Erreurs détectées:')
    results.errors.forEach((error, index) => {
      console.log(`   ${index + 1}. ${error}`)
    })
  } else {
    console.log('\n✅ Tous les tests sont passés avec succès!')
  }

  return results
}

// Fonction pour tester depuis la console du navigateur
if (typeof window !== 'undefined') {
  (window as any).testBackendConnection = testBackendConnection
}
