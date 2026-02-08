'use client'

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { 
  Brain, 
  Target, 
  TrendingUp, 
  CheckCircle, 
  AlertTriangle, 
  Lightbulb,
  BarChart3,
  Settings,
  Zap
} from 'lucide-react'

interface IntelligentAssignmentViewProps {
  className?: string
}

interface AssignmentResult {
  success: boolean
  recommendedRoom: {
    room: {
      id: number
      name: string
      type: string
      capacity: number
      location: string
    }
    totalScore: number
    detailedScores: Record<string, number>
    reasoningExplanation: string
  }
  alternatives: Array<{
    room: {
      id: number
      name: string
      type: string
      capacity: number
      location: string
    }
    totalScore: number
    reasoningExplanation: string
  }>
  confidenceScore: number
  reasoning: string
  optimizationTips: string[]
}

interface AssignmentStatistics {
  totalAssignments: number
  intelligentAssignments: number
  averageOptimizationScore: number
  userSatisfactionRate: number
  timesSaved: string
  conflictsAvoided: number
  topCriteria: Record<string, number>
}

export function IntelligentAssignmentView({ className }: IntelligentAssignmentViewProps) {
  const [assignmentResult, setAssignmentResult] = useState<AssignmentResult | null>(null)
  const [statistics, setStatistics] = useState<AssignmentStatistics | null>(null)
  const [loading, setLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('overview')

  // Données simulées pour la démonstration
  useEffect(() => {
    // Simuler le chargement des statistiques
    setStatistics({
      totalAssignments: 156,
      intelligentAssignments: 142,
      averageOptimizationScore: 84.2,
      userSatisfactionRate: 91.5,
      timesSaved: "2.3 heures",
      conflictsAvoided: 23,
      topCriteria: {
        capacity: 28.5,
        equipment: 24.1,
        location: 19.8,
        availability: 15.2,
        history: 12.4
      }
    })

    // Simuler un résultat d'assignation
    setAssignmentResult({
      success: true,
      recommendedRoom: {
        room: {
          id: 1,
          name: "Amphithéâtre A",
          type: "AMPHITHEATRE",
          capacity: 150,
          location: "Bâtiment A - RDC"
        },
        totalScore: 87.5,
        detailedScores: {
          capacity: 22.5,
          equipment: 19.2,
          location: 16.8,
          history: 13.1,
          availability: 8.7,
          preference: 7.2
        },
        reasoningExplanation: "Salle optimale avec excellente capacité et équipements adaptés"
      },
      alternatives: [
        {
          room: {
            id: 2,
            name: "Salle 201",
            type: "CLASSROOM",
            capacity: 80,
            location: "Bâtiment B - 2ème étage"
          },
          totalScore: 76.3,
          reasoningExplanation: "Alternative viable avec bonne localisation"
        },
        {
          room: {
            id: 3,
            name: "Laboratoire Info",
            type: "LABORATORY",
            capacity: 40,
            location: "Bâtiment C - 1er étage"
          },
          totalScore: 68.9,
          reasoningExplanation: "Option avec équipements spécialisés"
        }
      ],
      confidenceScore: 89.2,
      reasoning: "Assignation basée sur analyse multi-critères avec historique d'utilisation",
      optimizationTips: [
        "Réserver 15 minutes avant pour la préparation",
        "Vérifier la disponibilité des équipements",
        "Considérer les alternatives en cas de conflit"
      ]
    })
  }, [])

  const handleFindOptimalRoom = async () => {
    setLoading(true)
    try {
      // Simuler l'appel API
      await new Promise(resolve => setTimeout(resolve, 2000))
      // Les données sont déjà chargées dans useEffect
    } catch (error) {
      console.error('Erreur lors de la recherche:', error)
    } finally {
      setLoading(false)
    }
  }

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600'
    if (score >= 60) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getScoreBadgeVariant = (score: number) => {
    if (score >= 80) return 'default'
    if (score >= 60) return 'secondary'
    return 'destructive'
  }

  return (
    <div className={`space-y-6 ${className}`}>
      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Brain className="h-8 w-8 text-blue-600" />
            Assignation Intelligente
          </h1>
          <p className="text-muted-foreground">
            Optimisation automatique des assignations de salles avec IA
          </p>
        </div>
        <Button onClick={handleFindOptimalRoom} disabled={loading} className="gap-2">
          <Zap className="h-4 w-4" />
          {loading ? 'Analyse en cours...' : 'Nouvelle Assignation'}
        </Button>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="overview">Vue d'ensemble</TabsTrigger>
          <TabsTrigger value="results">Résultats</TabsTrigger>
          <TabsTrigger value="statistics">Statistiques</TabsTrigger>
          <TabsTrigger value="configuration">Configuration</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          {/* Métriques principales */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Assignations Totales</CardTitle>
                <Target className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{statistics?.totalAssignments}</div>
                <p className="text-xs text-muted-foreground">
                  +12% par rapport au mois dernier
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Score Moyen</CardTitle>
                <BarChart3 className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{statistics?.averageOptimizationScore}%</div>
                <p className="text-xs text-muted-foreground">
                  +5.2% d'amélioration
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Satisfaction</CardTitle>
                <TrendingUp className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{statistics?.userSatisfactionRate}%</div>
                <p className="text-xs text-muted-foreground">
                  Très satisfaisant
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Conflits Évités</CardTitle>
                <CheckCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{statistics?.conflictsAvoided}</div>
                <p className="text-xs text-muted-foreground">
                  Cette semaine
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Critères d'optimisation */}
          <Card>
            <CardHeader>
              <CardTitle>Critères d'Optimisation Principaux</CardTitle>
              <CardDescription>
                Répartition de l'importance des critères dans l'algorithme
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {statistics && Object.entries(statistics.topCriteria).map(([criterion, value]) => (
                <div key={criterion} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm font-medium capitalize">
                      {criterion === 'capacity' ? 'Capacité' :
                       criterion === 'equipment' ? 'Équipements' :
                       criterion === 'location' ? 'Localisation' :
                       criterion === 'availability' ? 'Disponibilité' :
                       criterion === 'history' ? 'Historique' : criterion}
                    </span>
                    <span className="text-sm text-muted-foreground">{value}%</span>
                  </div>
                  <Progress value={value} className="h-2" />
                </div>
              ))}
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="results" className="space-y-4">
          {assignmentResult && (
            <>
              {/* Résultat principal */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <CheckCircle className="h-5 w-5 text-green-600" />
                    Salle Recommandée
                  </CardTitle>
                  <CardDescription>
                    Résultat de l'analyse intelligente multi-critères
                  </CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-lg font-semibold">
                        {assignmentResult.recommendedRoom.room.name}
                      </h3>
                      <p className="text-sm text-muted-foreground">
                        {assignmentResult.recommendedRoom.room.location} • 
                        Capacité: {assignmentResult.recommendedRoom.room.capacity} places
                      </p>
                    </div>
                    <Badge variant={getScoreBadgeVariant(assignmentResult.recommendedRoom.totalScore)}>
                      Score: {assignmentResult.recommendedRoom.totalScore.toFixed(1)}/100
                    </Badge>
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div>
                      <h4 className="text-sm font-medium mb-2">Scores Détaillés</h4>
                      <div className="space-y-2">
                        {Object.entries(assignmentResult.recommendedRoom.detailedScores).map(([criterion, score]) => (
                          <div key={criterion} className="flex items-center justify-between">
                            <span className="text-sm capitalize">
                              {criterion === 'capacity' ? 'Capacité' :
                               criterion === 'equipment' ? 'Équipements' :
                               criterion === 'location' ? 'Localisation' :
                               criterion === 'availability' ? 'Disponibilité' :
                               criterion === 'history' ? 'Historique' :
                               criterion === 'preference' ? 'Préférences' : criterion}
                            </span>
                            <span className={`text-sm font-medium ${getScoreColor(score)}`}>
                              {score.toFixed(1)}
                            </span>
                          </div>
                        ))}
                      </div>
                    </div>

                    <div>
                      <h4 className="text-sm font-medium mb-2">Confiance</h4>
                      <div className="space-y-2">
                        <Progress value={assignmentResult.confidenceScore} className="h-2" />
                        <p className="text-sm text-muted-foreground">
                          {assignmentResult.confidenceScore.toFixed(1)}% de confiance
                        </p>
                      </div>
                    </div>
                  </div>

                  <Alert>
                    <Lightbulb className="h-4 w-4" />
                    <AlertTitle>Explication</AlertTitle>
                    <AlertDescription>
                      {assignmentResult.recommendedRoom.reasoningExplanation}
                    </AlertDescription>
                  </Alert>
                </CardContent>
              </Card>

              {/* Alternatives */}
              <Card>
                <CardHeader>
                  <CardTitle>Alternatives Disponibles</CardTitle>
                  <CardDescription>
                    Autres options recommandées par l'algorithme
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {assignmentResult.alternatives.map((alternative, index) => (
                      <div key={alternative.room.id} className="flex items-center justify-between p-4 border rounded-lg">
                        <div>
                          <h4 className="font-medium">{alternative.room.name}</h4>
                          <p className="text-sm text-muted-foreground">
                            {alternative.room.location} • {alternative.room.capacity} places
                          </p>
                          <p className="text-xs text-muted-foreground mt-1">
                            {alternative.reasoningExplanation}
                          </p>
                        </div>
                        <Badge variant={getScoreBadgeVariant(alternative.totalScore)}>
                          {alternative.totalScore.toFixed(1)}
                        </Badge>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Conseils d'optimisation */}
              <Card>
                <CardHeader>
                  <CardTitle className="flex items-center gap-2">
                    <Lightbulb className="h-5 w-5 text-yellow-600" />
                    Conseils d'Optimisation
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="space-y-2">
                    {assignmentResult.optimizationTips.map((tip, index) => (
                      <li key={index} className="flex items-start gap-2">
                        <CheckCircle className="h-4 w-4 text-green-600 mt-0.5 flex-shrink-0" />
                        <span className="text-sm">{tip}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            </>
          )}
        </TabsContent>

        <TabsContent value="statistics" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Performance Globale</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Assignations Intelligentes</span>
                    <span className="text-sm font-medium">
                      {statistics?.intelligentAssignments}/{statistics?.totalAssignments}
                    </span>
                  </div>
                  <Progress 
                    value={statistics ? (statistics.intelligentAssignments / statistics.totalAssignments) * 100 : 0} 
                    className="h-2" 
                  />
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Satisfaction Utilisateur</span>
                    <span className="text-sm font-medium">{statistics?.userSatisfactionRate}%</span>
                  </div>
                  <Progress value={statistics?.userSatisfactionRate} className="h-2" />
                </div>

                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm">Score Moyen d'Optimisation</span>
                    <span className="text-sm font-medium">{statistics?.averageOptimizationScore}%</span>
                  </div>
                  <Progress value={statistics?.averageOptimizationScore} className="h-2" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Bénéfices Mesurés</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="text-center">
                  <div className="text-2xl font-bold text-green-600">{statistics?.timesSaved}</div>
                  <p className="text-sm text-muted-foreground">Temps économisé par semaine</p>
                </div>

                <div className="text-center">
                  <div className="text-2xl font-bold text-blue-600">{statistics?.conflictsAvoided}</div>
                  <p className="text-sm text-muted-foreground">Conflits évités cette semaine</p>
                </div>

                <div className="text-center">
                  <div className="text-2xl font-bold text-purple-600">€2,340</div>
                  <p className="text-sm text-muted-foreground">Économies estimées ce mois</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="configuration" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Settings className="h-5 w-5" />
                Configuration de l'Algorithme
              </CardTitle>
              <CardDescription>
                Ajustez les paramètres de l'assignation intelligente
              </CardDescription>
            </CardHeader>
            <CardContent>
              <Alert>
                <AlertTriangle className="h-4 w-4" />
                <AlertTitle>Configuration Avancée</AlertTitle>
                <AlertDescription>
                  Les paramètres de configuration seront disponibles dans une prochaine version.
                  Contactez l'administrateur pour des ajustements spécifiques.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}