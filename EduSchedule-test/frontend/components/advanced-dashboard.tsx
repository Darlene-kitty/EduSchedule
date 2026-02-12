"use client"

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar, Clock, Users, TrendingUp, TrendingDown, AlertTriangle, CheckCircle, BarChart3, PieChart, Activity } from 'lucide-react'
import { LineChart, Line, AreaChart, Area, BarChart, Bar, PieChart as RechartsPieChart, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'

interface RoomOccupancy {
  resourceId: number
  resourceName: string
  occupancyRate: number
  totalReservations: number
  averageCapacityUsage: number
  efficiencyScore: number
  status: 'excellent' | 'good' | 'average' | 'poor'
}

interface OptimizationSuggestion {
  type: string
  description: string
  resourceId: number
  resourceName: string
  currentEfficiency: number
  potentialImprovement: number
}

interface DashboardStats {
  totalRooms: number
  activeReservations: number
  occupancyRate: number
  efficiencyScore: number
  trends: {
    reservations: number
    occupancy: number
    efficiency: number
  }
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8']

export function AdvancedDashboard() {
  const [stats, setStats] = useState<DashboardStats>({
    totalRooms: 0,
    activeReservations: 0,
    occupancyRate: 0,
    efficiencyScore: 0,
    trends: { reservations: 0, occupancy: 0, efficiency: 0 }
  })
  
  const [roomOccupancy, setRoomOccupancy] = useState<RoomOccupancy[]>([])
  const [suggestions, setSuggestions] = useState<OptimizationSuggestion[]>([])
  const [selectedPeriod, setSelectedPeriod] = useState('week')
  const [loading, setLoading] = useState(true)

  // Données simulées pour la démonstration
  const mockOccupancyData = [
    { time: '08:00', amphitheater: 85, classroom: 60, lab: 40 },
    { time: '09:00', amphitheater: 95, classroom: 80, lab: 70 },
    { time: '10:00', amphitheater: 90, classroom: 85, lab: 85 },
    { time: '11:00', amphitheater: 75, classroom: 90, lab: 80 },
    { time: '12:00', amphitheater: 30, classroom: 40, lab: 20 },
    { time: '13:00', amphitheater: 20, classroom: 30, lab: 15 },
    { time: '14:00', amphitheater: 80, classroom: 75, lab: 90 },
    { time: '15:00', amphitheater: 85, classroom: 80, lab: 95 },
    { time: '16:00', amphitheater: 70, classroom: 70, lab: 85 },
    { time: '17:00', amphitheater: 50, classroom: 60, lab: 70 },
  ]

  const mockWeeklyData = [
    { day: 'Lun', reservations: 45, occupancy: 78 },
    { day: 'Mar', reservations: 52, occupancy: 82 },
    { day: 'Mer', reservations: 48, occupancy: 75 },
    { day: 'Jeu', reservations: 55, occupancy: 85 },
    { day: 'Ven', reservations: 42, occupancy: 70 },
    { day: 'Sam', reservations: 15, occupancy: 25 },
    { day: 'Dim', reservations: 8, occupancy: 15 },
  ]

  const mockRoomTypeData = [
    { name: 'Amphithéâtres', value: 35, count: 8 },
    { name: 'Salles de classe', value: 45, count: 12 },
    { name: 'Laboratoires', value: 20, count: 6 },
  ]

  useEffect(() => {
    loadDashboardData()
  }, [selectedPeriod])

  const loadDashboardData = async () => {
    setLoading(true)
    try {
      // Simulation du chargement des données
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Données simulées
      setStats({
        totalRooms: 26,
        activeReservations: 142,
        occupancyRate: 76,
        efficiencyScore: 82,
        trends: { reservations: 12, occupancy: 5, efficiency: 8 }
      })

      setRoomOccupancy([
        {
          resourceId: 1,
          resourceName: "Amphithéâtre A",
          occupancyRate: 85,
          totalReservations: 24,
          averageCapacityUsage: 78,
          efficiencyScore: 88,
          status: 'excellent'
        },
        {
          resourceId: 2,
          resourceName: "Salle TP Info",
          occupancyRate: 92,
          totalReservations: 31,
          averageCapacityUsage: 85,
          efficiencyScore: 91,
          status: 'excellent'
        },
        {
          resourceId: 3,
          resourceName: "Salle 101",
          occupancyRate: 45,
          totalReservations: 12,
          averageCapacityUsage: 35,
          efficiencyScore: 42,
          status: 'poor'
        }
      ])

      setSuggestions([
        {
          type: 'UNDERUTILIZED',
          description: 'Salle sous-utilisée - considérer une réaffectation',
          resourceId: 3,
          resourceName: 'Salle 101',
          currentEfficiency: 42,
          potentialImprovement: 25
        },
        {
          type: 'OVERUTILIZED',
          description: 'Salle sur-utilisée - considérer des alternatives',
          resourceId: 2,
          resourceName: 'Salle TP Info',
          currentEfficiency: 91,
          potentialImprovement: -15
        }
      ])

    } catch (error) {
      console.error('Erreur chargement dashboard:', error)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'excellent': return 'bg-green-500'
      case 'good': return 'bg-blue-500'
      case 'average': return 'bg-yellow-500'
      case 'poor': return 'bg-red-500'
      default: return 'bg-gray-500'
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'excellent': return <CheckCircle className="h-4 w-4 text-green-500" />
      case 'good': return <TrendingUp className="h-4 w-4 text-blue-500" />
      case 'average': return <Activity className="h-4 w-4 text-yellow-500" />
      case 'poor': return <AlertTriangle className="h-4 w-4 text-red-500" />
      default: return <Activity className="h-4 w-4 text-gray-500" />
    }
  }

  const getSuggestionIcon = (type: string) => {
    switch (type) {
      case 'UNDERUTILIZED': return <TrendingDown className="h-4 w-4 text-orange-500" />
      case 'OVERUTILIZED': return <TrendingUp className="h-4 w-4 text-red-500" />
      default: return <Activity className="h-4 w-4 text-blue-500" />
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* En-tête avec sélecteur de période */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">Tableau de Bord Avancé</h1>
          <p className="text-gray-600">Analyse et optimisation de l'occupation des salles</p>
        </div>
        <Select value={selectedPeriod} onValueChange={setSelectedPeriod}>
          <SelectTrigger className="w-48">
            <SelectValue placeholder="Sélectionner la période" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="day">Aujourd'hui</SelectItem>
            <SelectItem value="week">Cette semaine</SelectItem>
            <SelectItem value="month">Ce mois</SelectItem>
            <SelectItem value="quarter">Ce trimestre</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Statistiques principales */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Total Salles</CardTitle>
            <BarChart3 className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.totalRooms}</div>
            <p className="text-xs text-muted-foreground">
              Toutes catégories confondues
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Réservations Actives</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.activeReservations}</div>
            <p className="text-xs text-muted-foreground">
              <span className="text-green-600">+{stats.trends.reservations}%</span> vs période précédente
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Taux d'Occupation</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.occupancyRate}%</div>
            <Progress value={stats.occupancyRate} className="mt-2" />
            <p className="text-xs text-muted-foreground mt-1">
              <span className="text-green-600">+{stats.trends.occupancy}%</span> vs période précédente
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Score d'Efficacité</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats.efficiencyScore}%</div>
            <Progress value={stats.efficiencyScore} className="mt-2" />
            <p className="text-xs text-muted-foreground mt-1">
              <span className="text-green-600">+{stats.trends.efficiency}%</span> vs période précédente
            </p>
          </CardContent>
        </Card>
      </div>

      {/* Onglets principaux */}
      <Tabs defaultValue="overview" className="space-y-4">
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="overview">Vue d'ensemble</TabsTrigger>
          <TabsTrigger value="occupancy">Occupation</TabsTrigger>
          <TabsTrigger value="optimization">Optimisation</TabsTrigger>
          <TabsTrigger value="analytics">Analyses</TabsTrigger>
        </TabsList>

        {/* Vue d'ensemble */}
        <TabsContent value="overview" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Graphique d'occupation par heure */}
            <Card>
              <CardHeader>
                <CardTitle>Occupation par Heure</CardTitle>
                <CardDescription>Taux d'occupation moyen par créneau horaire</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <AreaChart data={mockOccupancyData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="time" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Area type="monotone" dataKey="amphitheater" stackId="1" stroke="#8884d8" fill="#8884d8" name="Amphithéâtres" />
                    <Area type="monotone" dataKey="classroom" stackId="1" stroke="#82ca9d" fill="#82ca9d" name="Salles de classe" />
                    <Area type="monotone" dataKey="lab" stackId="1" stroke="#ffc658" fill="#ffc658" name="Laboratoires" />
                  </AreaChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            {/* Répartition par type de salle */}
            <Card>
              <CardHeader>
                <CardTitle>Répartition par Type</CardTitle>
                <CardDescription>Distribution des réservations par type de salle</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <RechartsPieChart>
                    <Pie
                      data={mockRoomTypeData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                      outerRadius={80}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {mockRoomTypeData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </RechartsPieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>

          {/* Évolution hebdomadaire */}
          <Card>
            <CardHeader>
              <CardTitle>Évolution Hebdomadaire</CardTitle>
              <CardDescription>Réservations et taux d'occupation par jour</CardDescription>
            </CardHeader>
            <CardContent>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={mockWeeklyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="day" />
                  <YAxis yAxisId="left" />
                  <YAxis yAxisId="right" orientation="right" />
                  <Tooltip />
                  <Legend />
                  <Bar yAxisId="left" dataKey="reservations" fill="#8884d8" name="Réservations" />
                  <Line yAxisId="right" type="monotone" dataKey="occupancy" stroke="#82ca9d" name="Occupation %" />
                </BarChart>
              </ResponsiveContainer>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Occupation détaillée */}
        <TabsContent value="occupancy" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Occupation par Salle</CardTitle>
              <CardDescription>Détail de l'occupation et de l'efficacité par salle</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {roomOccupancy.map((room) => (
                  <div key={room.resourceId} className="flex items-center justify-between p-4 border rounded-lg">
                    <div className="flex items-center space-x-4">
                      {getStatusIcon(room.status)}
                      <div>
                        <h3 className="font-medium">{room.resourceName}</h3>
                        <p className="text-sm text-gray-600">{room.totalReservations} réservations</p>
                      </div>
                    </div>
                    <div className="flex items-center space-x-6">
                      <div className="text-center">
                        <div className="text-sm font-medium">{room.occupancyRate}%</div>
                        <div className="text-xs text-gray-500">Occupation</div>
                      </div>
                      <div className="text-center">
                        <div className="text-sm font-medium">{room.averageCapacityUsage}%</div>
                        <div className="text-xs text-gray-500">Capacité</div>
                      </div>
                      <div className="text-center">
                        <div className="text-sm font-medium">{room.efficiencyScore}%</div>
                        <div className="text-xs text-gray-500">Efficacité</div>
                      </div>
                      <Badge className={getStatusColor(room.status)}>
                        {room.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Optimisation */}
        <TabsContent value="optimization" className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Suggestions d'Optimisation</CardTitle>
              <CardDescription>Recommandations pour améliorer l'utilisation des salles</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {suggestions.map((suggestion, index) => (
                  <div key={index} className="flex items-start space-x-4 p-4 border rounded-lg">
                    {getSuggestionIcon(suggestion.type)}
                    <div className="flex-1">
                      <h3 className="font-medium">{suggestion.resourceName}</h3>
                      <p className="text-sm text-gray-600 mt-1">{suggestion.description}</p>
                      <div className="flex items-center space-x-4 mt-2">
                        <div className="text-xs">
                          <span className="text-gray-500">Efficacité actuelle:</span>
                          <span className="font-medium ml-1">{suggestion.currentEfficiency}%</span>
                        </div>
                        {suggestion.potentialImprovement !== 0 && (
                          <div className="text-xs">
                            <span className="text-gray-500">Amélioration potentielle:</span>
                            <span className={`font-medium ml-1 ${suggestion.potentialImprovement > 0 ? 'text-green-600' : 'text-red-600'}`}>
                              {suggestion.potentialImprovement > 0 ? '+' : ''}{suggestion.potentialImprovement}%
                            </span>
                          </div>
                        )}
                      </div>
                    </div>
                    <Button variant="outline" size="sm">
                      Appliquer
                    </Button>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Analyses */}
        <TabsContent value="analytics" className="space-y-4">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Tendances d'Utilisation</CardTitle>
                <CardDescription>Évolution de l'utilisation sur les 30 derniers jours</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={mockWeeklyData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="day" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="occupancy" stroke="#8884d8" strokeWidth={2} name="Taux d'occupation" />
                  </LineChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Métriques de Performance</CardTitle>
                <CardDescription>Indicateurs clés de performance</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm">Taux d'occupation moyen</span>
                  <span className="font-medium">76%</span>
                </div>
                <Progress value={76} />
                
                <div className="flex justify-between items-center">
                  <span className="text-sm">Efficacité énergétique</span>
                  <span className="font-medium">82%</span>
                </div>
                <Progress value={82} />
                
                <div className="flex justify-between items-center">
                  <span className="text-sm">Satisfaction utilisateurs</span>
                  <span className="font-medium">89%</span>
                </div>
                <Progress value={89} />
                
                <div className="flex justify-between items-center">
                  <span className="text-sm">Optimisation des coûts</span>
                  <span className="font-medium">71%</span>
                </div>
                <Progress value={71} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}