"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Users, Calendar, BookOpen, AlertTriangle, TrendingUp, Clock, CheckCircle2, XCircle, Loader2, RefreshCw } from "lucide-react"
import { analyticsService, DashboardStats } from "@/lib/api/analytics"
import { useToast } from "@/hooks/use-toast"

export function DashboardView() {
  const { toast } = useToast()
  const [stats, setStats] = useState<DashboardStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [selectedPeriod, setSelectedPeriod] = useState('week')
  const [lastUpdated, setLastUpdated] = useState<Date>(new Date())

  // Données statiques pour les activités récentes (à remplacer par une API plus tard)
  const recentActivities = [
    { type: "success", message: "Cours de Mathématiques ajouté pour L1-G1", time: "Il y a 5 min", icon: CheckCircle2 },
    { type: "warning", message: "Conflit détecté: Salle A101 (Lundi 10h)", time: "Il y a 12 min", icon: AlertTriangle },
    { type: "success", message: "Nouvel utilisateur: Prof. Dubois", time: "Il y a 23 min", icon: Users },
    { type: "error", message: "Réservation annulée: Salle B203", time: "Il y a 1h", icon: XCircle },
    { type: "success", message: "Emploi du temps validé pour L2-G2", time: "Il y a 2h", icon: Calendar },
  ]

  const upcomingCourses = [
    {
      subject: "Mathématiques",
      time: "08:00-10:00",
      room: "A101",
      group: "L1-G1",
      professor: "Dr. Martin",
      color: "bg-blue-500",
    },
    {
      subject: "Physique",
      time: "10:00-12:00",
      room: "B203",
      group: "L1-G1",
      professor: "Dr. Laurent",
      color: "bg-green-500",
    },
    {
      subject: "Analyse",
      time: "08:00-10:00",
      room: "A103",
      group: "L2-G1",
      professor: "Prof. Bernard",
      color: "bg-cyan-500",
    },
    {
      subject: "Géométrie",
      time: "09:00-11:00",
      room: "A101",
      group: "L1-G1",
      professor: "Dr. Martin",
      color: "bg-indigo-500",
    },
  ]

  useEffect(() => {
    loadDashboardData()
  }, [selectedPeriod])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      const dashboardStats = await analyticsService.getDashboardStats(selectedPeriod)
      setStats(dashboardStats)
      setLastUpdated(new Date())
    } catch (error) {
      console.error('Erreur chargement dashboard:', error)
      toast({
        title: "Erreur",
        description: "Impossible de charger les données du tableau de bord",
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const handleRefresh = () => {
    loadDashboardData()
  }

  const formatTrend = (value: number) => {
    const sign = value >= 0 ? '+' : ''
    return `${sign}${value}%`
  }

  const getTrendColor = (value: number) => {
    return value >= 0 ? 'text-green-500' : 'text-red-500'
  }

  const getTrendIcon = (value: number) => {
    return value >= 0 ? 'up' : 'down'
  }

  if (loading && !stats) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="dashboard" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Tableau de bord" subtitle="Vue d'ensemble de votre système de gestion académique" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement du tableau de bord...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  const dynamicStats = stats ? [
    { 
      icon: Users, 
      label: "Utilisateurs actifs", 
      value: "248", // TODO: Ajouter à l'API
      change: formatTrend(12), 
      trend: getTrendIcon(12), 
      color: "bg-blue-500" 
    },
    { 
      icon: Calendar, 
      label: "Réservations actives", 
      value: stats.activeReservations.toString(), 
      change: formatTrend(stats.trends.reservations), 
      trend: getTrendIcon(stats.trends.reservations), 
      color: "bg-green-500" 
    },
    { 
      icon: BookOpen, 
      label: "Total salles", 
      value: stats.totalRooms.toString(), 
      change: "0%", // Les salles ne changent pas souvent
      trend: "up", 
      color: "bg-purple-500" 
    },
    { 
      icon: AlertTriangle, 
      label: "Taux d'occupation", 
      value: `${stats.occupancyRate}%`, 
      change: formatTrend(stats.trends.occupancy), 
      trend: getTrendIcon(stats.trends.occupancy), 
      color: "bg-orange-500" 
    },
  ] : []
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="dashboard" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Tableau de bord" 
          subtitle="Vue d'ensemble de votre système de gestion académique"
          action={
            <div className="flex gap-2">
              <Select value={selectedPeriod} onValueChange={setSelectedPeriod}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Période" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="day">Aujourd'hui</SelectItem>
                  <SelectItem value="week">Cette semaine</SelectItem>
                  <SelectItem value="month">Ce mois</SelectItem>
                  <SelectItem value="quarter">Ce trimestre</SelectItem>
                </SelectContent>
              </Select>
              <Button 
                variant="outline" 
                size="sm" 
                onClick={handleRefresh}
                disabled={loading}
                className="gap-2"
              >
                <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
                Actualiser
              </Button>
            </div>
          }
        />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Indicateur de dernière mise à jour */}
          <div className="mb-4 text-sm text-muted-foreground">
            Dernière mise à jour : {lastUpdated.toLocaleTimeString('fr-FR')}
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
            {dynamicStats.map((stat, index) => (
              <Card key={index} className="p-6">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <p className="text-sm text-muted-foreground mb-1">{stat.label}</p>
                    <p className="text-3xl font-bold mb-2">{stat.value}</p>
                    <div className="flex items-center gap-1">
                      <TrendingUp className={`w-4 h-4 ${stat.trend === "up" ? "text-green-500" : "text-red-500"}`} />
                      <span
                        className={`text-sm font-medium ${stat.trend === "up" ? "text-green-500" : "text-red-500"}`}
                      >
                        {stat.change}
                      </span>
                      <span className="text-sm text-muted-foreground">
                        {selectedPeriod === 'day' ? 'aujourd\'hui' : 
                         selectedPeriod === 'week' ? 'cette semaine' :
                         selectedPeriod === 'month' ? 'ce mois' : 'ce trimestre'}
                      </span>
                    </div>
                  </div>
                  <div className={`${stat.color} p-3 rounded-lg`}>
                    <stat.icon className="w-6 h-6 text-white" />
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {/* Score d'efficacité global */}
          {stats && (
            <div className="mb-6">
              <Card className="p-6">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <h3 className="text-lg font-semibold">Score d'Efficacité Global</h3>
                    <p className="text-sm text-muted-foreground">Optimisation de l'utilisation des ressources</p>
                  </div>
                  <div className="text-right">
                    <div className="text-3xl font-bold text-[#15803D]">{stats.efficiencyScore}%</div>
                    <div className="flex items-center gap-1 justify-end">
                      <TrendingUp className={`w-4 h-4 ${getTrendColor(stats.trends.efficiency)}`} />
                      <span className={`text-sm font-medium ${getTrendColor(stats.trends.efficiency)}`}>
                        {formatTrend(stats.trends.efficiency)}
                      </span>
                    </div>
                  </div>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div 
                    className="bg-[#15803D] h-2 rounded-full transition-all duration-500" 
                    style={{ width: `${stats.efficiencyScore}%` }}
                  ></div>
                </div>
              </Card>
            </div>
          )}

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Recent Activities */}
            <Card className="p-6">
              <h3 className="text-lg font-semibold mb-4">Activités récentes</h3>
              <div className="space-y-4">
                {recentActivities.map((activity, index) => (
                  <div key={index} className="flex items-start gap-3">
                    <div
                      className={`p-2 rounded-lg ${
                        activity.type === "success"
                          ? "bg-green-100"
                          : activity.type === "warning"
                            ? "bg-yellow-100"
                            : "bg-red-100"
                      }`}
                    >
                      <activity.icon
                        className={`w-4 h-4 ${
                          activity.type === "success"
                            ? "text-green-600"
                            : activity.type === "warning"
                              ? "text-yellow-600"
                              : "text-red-600"
                        }`}
                      />
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium">{activity.message}</p>
                      <p className="text-xs text-muted-foreground mt-1">{activity.time}</p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>

            {/* Upcoming Courses */}
            <Card className="p-6">
              <h3 className="text-lg font-semibold mb-4">Prochains cours aujourd'hui</h3>
              <div className="space-y-3">
                {upcomingCourses.map((course, index) => (
                  <div key={index} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                    <div className={`w-1 h-16 ${course.color} rounded-full`} />
                    <div className="flex-1">
                      <p className="font-semibold text-sm">{course.subject}</p>
                      <div className="flex items-center gap-2 mt-1">
                        <Clock className="w-3 h-3 text-muted-foreground" />
                        <p className="text-xs text-muted-foreground">{course.time}</p>
                      </div>
                      <p className="text-xs text-muted-foreground mt-1">
                        {course.room} • {course.group} • {course.professor}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </Card>
          </div>
        </main>
      </div>
    </div>
  )
}
