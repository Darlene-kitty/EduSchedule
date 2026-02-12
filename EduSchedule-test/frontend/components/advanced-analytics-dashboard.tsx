'use client'

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Progress } from '@/components/ui/progress'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { 
  TrendingUp, 
  TrendingDown, 
  Activity, 
  AlertTriangle, 
  CheckCircle, 
  Brain,
  Zap,
  Target,
  BarChart3,
  PieChart,
  LineChart,
  RefreshCw
} from 'lucide-react'
import { 
  ResponsiveContainer, 
  LineChart as RechartsLineChart, 
  Line, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  PieChart as RechartsPieChart, 
  Cell, 
  Pie,
  BarChart as RechartsBarChart,
  Bar,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar
} from 'recharts'

interface AdvancedAnalyticsDashboardProps {
  className?: string
}

interface AdvancedMetrics {
  systemEfficiency: number
  userSatisfactionScore: number
  resourceOptimizationRate: number
  predictabilityIndex: number
  systemResilienceScore: number
}

interface Alert {
  id: string
  type: string
  severity: 'HIGH' | 'MEDIUM' | 'LOW'
  title: string
  message: string
  actionRequired: boolean
  suggestedActions: string[]
}

interface Recommendation {
  category: string
  priority: 'HIGH' | 'MEDIUM' | 'LOW'
  title: string
  description: string
  expectedImpact: string
  implementationEffort: 'HIGH' | 'MEDIUM' | 'LOW'
  timeframe: string
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8']

export function AdvancedAnalyticsDashboard({ className }: AdvancedAnalyticsDashboardProps) {
  const [metrics, setMetrics] = useState<AdvancedMetrics | null>(null)
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])
  const [loading, setLoading] = useState(false)
  const [activeTab, setActiveTab] = useState('overview')
  const [performanceScore, setPerformanceScore] = useState(0)

  // Données simulées pour les graphiques
  const [trendData] = useState([
    { name: 'Lun', efficiency: 82, satisfaction: 85, optimization: 78 },
    { name: 'Mar', efficiency: 85, satisfaction: 87, optimization: 82 },
    { name: 'Mer', efficiency: 88, satisfaction: 89, optimization: 85 },
    { name: 'Jeu', efficiency: 86, satisfaction: 88, optimization: 87 },
    { name: 'Ven', efficiency: 89, satisfaction: 91, optimization: 89 },
    { name: 'Sam', efficiency: 75, satisfaction: 80, optimization: 72 },
    { name: 'Dim', efficiency: 70, satisfaction: 75, optimization: 68 }
  ])

  const [distributionData] = useState([
    { name: 'Amphithéâtres', value: 35, count: 8 },
    { name: 'Salles de classe', value: 45, count: 12 },
    { name: 'Laboratoires', value: 20, count: 6 }
  ])

  const [radarData] = useState([
    { subject: 'Efficacité', A: 85, fullMark: 100 },
    { subject: 'Satisfaction', A: 88, fullMark: 100 },
    { subject: 'Optimisation', A: 82, fullMark: 100 },
    { subject: 'Prédictibilité', A: 79, fullMark: 100 },
    { subject: 'Résilience', A: 86, fullMark: 100 }
  ])

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    setLoading(true)
    try {
      // Simuler le chargement des données
      await new Promise(resolve => setTimeout(resolve, 1500))
      
      setMetrics({
        systemEfficiency: 85.2,
        userSatisfactionScore: 88.7,
        resourceOptimizationRate: 82.1,
        predictabilityIndex: 79.3,
        systemResilienceScore: 86.5
      })

      setAlerts([
        {
          id: 'ALERT_001',
          type: 'RESOURCE_OPTIMIZATION',
          severity: 'MEDIUM',
          title: 'Salle sous-utilisée détectée',
          message: 'Salle 205 utilisée à seulement 35% cette semaine',
          actionRequired: false,
          suggestedActions: [
            'Réaffecter certaines réservations',
            'Proposer la salle pour événements'
          ]
        },
        {
          id: 'ALERT_002',
          type: 'MAINTENANCE_PREDICTION',
          severity: 'LOW',
          title: 'Maintenance préventive recommandée',
          message: 'Projecteur Amphithéâtre A nécessite maintenance dans 2 semaines',
          actionRequired: true,
          suggestedActions: [
            'Planifier intervention technique',
            'Préparer salle alternative'
          ]
        }
      ])

      setRecommendations([
        {
          category: 'SCHEDULING',
          priority: 'HIGH',
          title: 'Optimiser créneaux de pointe',
          description: 'Redistribuer 15% des réservations des heures de pointe vers les heures creuses',
          expectedImpact: '15% amélioration efficacité',
          implementationEffort: 'MEDIUM',
          timeframe: '2 semaines'
        },
        {
          category: 'RESOURCES',
          priority: 'MEDIUM',
          title: 'Redistribuer équipements',
          description: 'Optimiser la répartition des équipements selon la demande',
          expectedImpact: '8% réduction conflits',
          implementationEffort: 'LOW',
          timeframe: '1 semaine'
        }
      ])

      setPerformanceScore(84.6)
    } catch (error) {
      console.error('Erreur lors du chargement:', error)
    } finally {
      setLoading(false)
    }
  }

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'HIGH': return 'text-red-600'
      case 'MEDIUM': return 'text-yellow-600'
      case 'LOW': return 'text-blue-600'
      default: return 'text-gray-600'
    }
  }

  const getSeverityBadge = (severity: string) => {
    switch (severity) {
      case 'HIGH': return 'destructive'
      case 'MEDIUM': return 'secondary'
      case 'LOW': return 'outline'
      default: return 'outline'
    }
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'text-red-600'
      case 'MEDIUM': return 'text-yellow-600'
      case 'LOW': return 'text-green-600'
      default: return 'text-gray-600'
    }
  }

  return (
    <div className={`space-y-6 ${className}`}>
      {/* En-tête */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight flex items-center gap-2">
            <Brain className="h-8 w-8 text-purple-600" />
            Analytics Avancés
          </h1>
          <p className="text-muted-foreground">
            Tableau de bord intelligent avec métriques prédictives et recommandations IA
          </p>
        </div>
        <Button onClick={loadDashboardData} disabled={loading} className="gap-2">
          <RefreshCw className={`h-4 w-4 ${loading ? 'animate-spin' : ''}`} />
          {loading ? 'Actualisation...' : 'Actualiser'}
        </Button>
      </div>

      {/* Score de performance global */}
      <Card className="bg-gradient-to-r from-purple-500 to-blue-600 text-white">
        <CardContent className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-2xl font-bold">Score de Performance Globale</h2>
              <p className="text-purple-100">Évaluation composite du système</p>
            </div>
            <div className="text-right">
              <div className="text-4xl font-bold">{performanceScore.toFixed(1)}</div>
              <div className="text-sm text-purple-100">/ 100</div>
            </div>
          </div>
          <div className="mt-4">
            <Progress value={performanceScore} className="h-2 bg-purple-200" />
          </div>
        </CardContent>
      </Card>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="grid w-full grid-cols-5">
          <TabsTrigger value="overview">Vue d'ensemble</TabsTrigger>
          <TabsTrigger value="metrics">Métriques</TabsTrigger>
          <TabsTrigger value="predictions">Prédictions</TabsTrigger>
          <TabsTrigger value="alerts">Alertes</TabsTrigger>
          <TabsTrigger value="recommendations">Recommandations</TabsTrigger>
        </TabsList>

        <TabsContent value="overview" className="space-y-4">
          {/* Métriques principales */}
          {metrics && (
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-5">
              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Efficacité Système</CardTitle>
                  <Activity className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{metrics.systemEfficiency.toFixed(1)}%</div>
                  <p className="text-xs text-muted-foreground">
                    <TrendingUp className="inline h-3 w-3 text-green-600" /> +3.2%
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Satisfaction</CardTitle>
                  <Target className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{metrics.userSatisfactionScore.toFixed(1)}%</div>
                  <p className="text-xs text-muted-foreground">
                    <TrendingUp className="inline h-3 w-3 text-green-600" /> +1.8%
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Optimisation</CardTitle>
                  <Zap className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{metrics.resourceOptimizationRate.toFixed(1)}%</div>
                  <p className="text-xs text-muted-foreground">
                    <TrendingUp className="inline h-3 w-3 text-green-600" /> +2.5%
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Prédictibilité</CardTitle>
                  <BarChart3 className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{metrics.predictabilityIndex.toFixed(1)}%</div>
                  <p className="text-xs text-muted-foreground">
                    <TrendingDown className="inline h-3 w-3 text-red-600" /> -0.8%
                  </p>
                </CardContent>
              </Card>

              <Card>
                <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                  <CardTitle className="text-sm font-medium">Résilience</CardTitle>
                  <CheckCircle className="h-4 w-4 text-muted-foreground" />
                </CardHeader>
                <CardContent>
                  <div className="text-2xl font-bold">{metrics.systemResilienceScore.toFixed(1)}%</div>
                  <p className="text-xs text-muted-foreground">
                    <TrendingUp className="inline h-3 w-3 text-green-600" /> +4.1%
                  </p>
                </CardContent>
              </Card>
            </div>
          )}

          {/* Graphiques de tendances */}
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <LineChart className="h-5 w-5" />
                  Tendances Hebdomadaires
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <RechartsLineChart data={trendData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="efficiency" stroke="#8884d8" strokeWidth={2} />
                    <Line type="monotone" dataKey="satisfaction" stroke="#82ca9d" strokeWidth={2} />
                    <Line type="monotone" dataKey="optimization" stroke="#ffc658" strokeWidth={2} />
                  </RechartsLineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <PieChart className="h-5 w-5" />
                  Répartition des Ressources
                </CardTitle>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <RechartsPieChart>
                    <Pie
                      data={distributionData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {distributionData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </RechartsPieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="metrics" className="space-y-4">
          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardHeader>
                <CardTitle>Performance Radar</CardTitle>
                <CardDescription>Vue d'ensemble des métriques clés</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={400}>
                  <RadarChart data={radarData}>
                    <PolarGrid />
                    <PolarAngleAxis dataKey="subject" />
                    <PolarRadiusAxis angle={90} domain={[0, 100]} />
                    <Radar name="Performance" dataKey="A" stroke="#8884d8" fill="#8884d8" fillOpacity={0.6} />
                  </RadarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Métriques Détaillées</CardTitle>
                <CardDescription>Analyse approfondie des performances</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                {metrics && Object.entries(metrics).map(([key, value]) => (
                  <div key={key} className="space-y-2">
                    <div className="flex items-center justify-between">
                      <span className="text-sm font-medium">
                        {key === 'systemEfficiency' ? 'Efficacité Système' :
                         key === 'userSatisfactionScore' ? 'Satisfaction Utilisateur' :
                         key === 'resourceOptimizationRate' ? 'Optimisation Ressources' :
                         key === 'predictabilityIndex' ? 'Index Prédictibilité' :
                         key === 'systemResilienceScore' ? 'Score Résilience' : key}
                      </span>
                      <span className="text-sm font-bold">{value.toFixed(1)}%</span>
                    </div>
                    <Progress value={value} className="h-2" />
                  </div>
                ))}
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="predictions" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Brain className="h-5 w-5 text-purple-600" />
                Prédictions IA
              </CardTitle>
              <CardDescription>
                Insights prédictifs basés sur l'analyse des données historiques
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid gap-4 md:grid-cols-3">
                <div className="text-center p-4 border rounded-lg">
                  <div className="text-2xl font-bold text-blue-600">82.3%</div>
                  <p className="text-sm text-muted-foreground">Occupation prévue semaine prochaine</p>
                </div>
                <div className="text-center p-4 border rounded-lg">
                  <div className="text-2xl font-bold text-green-600">3</div>
                  <p className="text-sm text-muted-foreground">Maintenances recommandées</p>
                </div>
                <div className="text-center p-4 border rounded-lg">
                  <div className="text-2xl font-bold text-yellow-600">FAIBLE</div>
                  <p className="text-sm text-muted-foreground">Risque de conflits</p>
                </div>
              </div>

              <Alert>
                <Brain className="h-4 w-4" />
                <AlertTitle>Prédiction Avancée</AlertTitle>
                <AlertDescription>
                  L'algorithme prévoit un pic d'utilisation mardi 14h-16h avec 94% de probabilité.
                  Recommandation: Préparer des salles alternatives et informer les utilisateurs.
                </AlertDescription>
              </Alert>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="alerts" className="space-y-4">
          <div className="space-y-4">
            {alerts.map((alert) => (
              <Card key={alert.id}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <AlertTriangle className={`h-5 w-5 ${getSeverityColor(alert.severity)}`} />
                      {alert.title}
                    </CardTitle>
                    <Badge variant={getSeverityBadge(alert.severity) as any}>
                      {alert.severity}
                    </Badge>
                  </div>
                </CardHeader>
                <CardContent className="space-y-4">
                  <p className="text-sm text-muted-foreground">{alert.message}</p>
                  
                  {alert.suggestedActions.length > 0 && (
                    <div>
                      <h4 className="text-sm font-medium mb-2">Actions suggérées:</h4>
                      <ul className="space-y-1">
                        {alert.suggestedActions.map((action, index) => (
                          <li key={index} className="flex items-start gap-2">
                            <CheckCircle className="h-4 w-4 text-green-600 mt-0.5 flex-shrink-0" />
                            <span className="text-sm">{action}</span>
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}

                  {alert.actionRequired && (
                    <Button size="sm" className="gap-2">
                      <Zap className="h-4 w-4" />
                      Traiter l'alerte
                    </Button>
                  )}
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        <TabsContent value="recommendations" className="space-y-4">
          <div className="space-y-4">
            {recommendations.map((rec, index) => (
              <Card key={index}>
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle className="flex items-center gap-2">
                      <Target className={`h-5 w-5 ${getPriorityColor(rec.priority)}`} />
                      {rec.title}
                    </CardTitle>
                    <div className="flex gap-2">
                      <Badge variant={rec.priority === 'HIGH' ? 'destructive' : rec.priority === 'MEDIUM' ? 'secondary' : 'outline'}>
                        {rec.priority}
                      </Badge>
                      <Badge variant="outline">{rec.category}</Badge>
                    </div>
                  </div>
                  <CardDescription>{rec.description}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="grid gap-4 md:grid-cols-3">
                    <div>
                      <h4 className="text-sm font-medium text-green-600">Impact Attendu</h4>
                      <p className="text-sm">{rec.expectedImpact}</p>
                    </div>
                    <div>
                      <h4 className="text-sm font-medium text-blue-600">Effort</h4>
                      <p className="text-sm">{rec.implementationEffort}</p>
                    </div>
                    <div>
                      <h4 className="text-sm font-medium text-purple-600">Délai</h4>
                      <p className="text-sm">{rec.timeframe}</p>
                    </div>
                  </div>

                  <Button size="sm" className="gap-2">
                    <CheckCircle className="h-4 w-4" />
                    Implémenter
                  </Button>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}