"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { testBackendConnection } from "@/lib/test-connection"
import { CheckCircle, XCircle, Loader2 } from "lucide-react"

export default function TestConnectionPage() {
  const [testing, setTesting] = useState(false)
  const [results, setResults] = useState<any>(null)

  const runTests = async () => {
    setTesting(true)
    setResults(null)
    
    try {
      const testResults = await testBackendConnection()
      setResults(testResults)
    } catch (error) {
      console.error("Erreur lors des tests:", error)
    } finally {
      setTesting(false)
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <Card>
          <CardHeader>
            <CardTitle>Test de Connexion Backend/Frontend</CardTitle>
            <CardDescription>
              Vérifiez que le frontend peut communiquer avec le backend
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <Button 
              onClick={runTests} 
              disabled={testing}
              className="w-full"
            >
              {testing ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Tests en cours...
                </>
              ) : (
                "Lancer les tests"
              )}
            </Button>

            {results && (
              <div className="space-y-4">
                <div className="border rounded-lg p-4">
                  <h3 className="font-semibold mb-4">Résultats des tests:</h3>
                  
                  <div className="space-y-3">
                    <TestResult
                      label="API Gateway accessible"
                      passed={results.apiGatewayReachable}
                    />
                    <TestResult
                      label="CORS configuré"
                      passed={results.corsConfigured}
                    />
                    <TestResult
                      label="Endpoint d'authentification disponible"
                      passed={results.authEndpointAvailable}
                    />
                  </div>
                </div>

                {results.errors.length > 0 && (
                  <div className="border border-red-200 rounded-lg p-4 bg-red-50">
                    <h3 className="font-semibold text-red-800 mb-2">Erreurs détectées:</h3>
                    <ul className="list-disc list-inside space-y-1 text-sm text-red-700">
                      {results.errors.map((error: string, index: number) => (
                        <li key={index}>{error}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {results.errors.length === 0 && (
                  <div className="border border-green-200 rounded-lg p-4 bg-green-50">
                    <p className="text-green-800 font-semibold">
                      ✅ Tous les tests sont passés avec succès!
                    </p>
                  </div>
                )}
              </div>
            )}

            <div className="border-t pt-4 mt-4">
              <h3 className="font-semibold mb-2">Instructions:</h3>
              <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600">
                <li>Assurez-vous que l'API Gateway est démarré sur le port 8080</li>
                <li>Assurez-vous que le user-service est démarré et enregistré dans Eureka</li>
                <li>Vérifiez que MySQL est accessible</li>
                <li>Cliquez sur "Lancer les tests" pour vérifier la connexion</li>
              </ol>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}

function TestResult({ label, passed }: { label: string; passed: boolean }) {
  return (
    <div className="flex items-center gap-2">
      {passed ? (
        <CheckCircle className="h-5 w-5 text-green-600" />
      ) : (
        <XCircle className="h-5 w-5 text-red-600" />
      )}
      <span className={passed ? "text-green-700" : "text-red-700"}>
        {label}
      </span>
    </div>
  )
}
