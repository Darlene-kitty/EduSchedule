"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Plus,
  Search,
  Calendar,
  Clock,
  User,
  MapPin,
  Edit,
  Trash2,
  BookOpen,
  Users,
  Loader2,
  AlertCircle,
  CalendarDays,
} from "lucide-react"
import { schedulesApi, Schedule } from "@/lib/api/schedules"
import { useToast } from "@/hooks/use-toast"

export function SchedulesView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedFilter, setSelectedFilter] = useState<string>("all")
  const [schedules, setSchedules] = useState<Schedule[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  // Charger les emplois du temps au montage du composant
  useEffect(() => {
    loadSchedules()
  }, [])

  const loadSchedules = async () => {
    try {
      setLoading(true)
      setError(null)
      const schedulesData = await schedulesApi.getAllSchedules()
      setSchedules(schedulesData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des emplois du temps'
      setError(errorMessage)
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const filteredSchedules = schedules.filter((schedule) => {
    const matchesSearch =
      schedule.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (schedule.teacher && schedule.teacher.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (schedule.course && schedule.course.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (schedule.room && schedule.room.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (schedule.groupName && schedule.groupName.toLowerCase().includes(searchQuery.toLowerCase()))
    
    let matchesFilter = true
    if (selectedFilter !== "all") {
      const today = new Date()
      const scheduleDate = new Date(schedule.startTime)
      
      switch (selectedFilter) {
        case "today":
          matchesFilter = scheduleDate.toDateString() === today.toDateString()
          break
        case "week":
          const weekStart = new Date(today)
          weekStart.setDate(today.getDate() - today.getDay())
          const weekEnd = new Date(weekStart)
          weekEnd.setDate(weekStart.getDate() + 6)
          matchesFilter = scheduleDate >= weekStart && scheduleDate <= weekEnd
          break
        case "month":
          matchesFilter = scheduleDate.getMonth() === today.getMonth() && 
                         scheduleDate.getFullYear() === today.getFullYear()
          break
        case "active":
          matchesFilter = schedule.status === "ACTIVE"
          break
      }
    }
    
    return matchesSearch && matchesFilter
  })

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case "ACTIVE":
        return "bg-green-100 text-green-700"
      case "CANCELLED":
        return "bg-red-100 text-red-700"
      case "COMPLETED":
        return "bg-blue-100 text-blue-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status.toUpperCase()) {
      case "ACTIVE":
        return "Actif"
      case "CANCELLED":
        return "Annulé"
      case "COMPLETED":
        return "Terminé"
      default:
        return status
    }
  }

  const handleDeleteSchedule = async (scheduleId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer cet emploi du temps ?")) {
      return
    }

    try {
      await schedulesApi.deleteSchedule(scheduleId)
      setSchedules(prev => prev.filter(schedule => schedule.id !== scheduleId))
      toast({
        title: "Succès",
        description: "Emploi du temps supprimé avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('fr-FR', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const formatDuration = (startTime: string, endTime: string) => {
    const start = new Date(startTime)
    const end = new Date(endTime)
    const durationMs = end.getTime() - start.getTime()
    const hours = Math.floor(durationMs / (1000 * 60 * 60))
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60))
    
    if (hours > 0) {
      return `${hours}h${minutes > 0 ? ` ${minutes}min` : ''}`
    }
    return `${minutes}min`
  }

  const uniqueTeachers = Array.from(new Set(schedules.map(s => s.teacher).filter(Boolean)))
  const uniqueRooms = Array.from(new Set(schedules.map(s => s.room).filter(Boolean)))

  const stats = [
    { label: "Total créneaux", value: schedules.length, color: "bg-blue-500", icon: Calendar },
    {
      label: "Actifs",
      value: schedules.filter((s) => s.status === "ACTIVE").length,
      color: "bg-green-500",
      icon: CalendarDays,
    },
    {
      label: "Enseignants",
      value: uniqueTeachers.length,
      color: "bg-purple-500",
      icon: User,
    },
    {
      label: "Salles utilisées",
      value: uniqueRooms.length,
      color: "bg-orange-500",
      icon: MapPin,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="schedules" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Emplois du temps" subtitle="Gérez la planification des cours et événements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des emplois du temps...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="schedules" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Emplois du temps" subtitle="Gérez la planification des cours et événements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={loadSchedules}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="schedules" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Emplois du temps" subtitle="Gérez la planification des cours et événements" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            {stats.map((stat, index) => (
              <Card key={index} className="p-4">
                <div className="flex items-center gap-3">
                  <div className={`${stat.color} w-12 h-12 rounded-lg flex items-center justify-center`}>
                    <stat.icon className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <p className="text-2xl font-bold">{stat.value}</p>
                    <p className="text-sm text-muted-foreground">{stat.label}</p>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {/* Actions Bar */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher un créneau..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90">
              <Plus className="w-4 h-4" />
              Nouveau créneau
            </Button>
          </div>

          {/* Filters */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            {[
              { key: "all", label: "Tous" },
              { key: "today", label: "Aujourd'hui" },
              { key: "week", label: "Cette semaine" },
              { key: "month", label: "Ce mois" },
              { key: "active", label: "Actifs" },
            ].map((filter) => (
              <Button
                key={filter.key}
                variant={selectedFilter === filter.key ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedFilter(filter.key)}
                className={selectedFilter === filter.key ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {filter.label}
              </Button>
            ))}
          </div>

          {/* Schedules Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredSchedules.map((schedule) => (
              <Card key={schedule.id} className="p-5 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="bg-[#15803D] p-3 rounded-lg">
                      <CalendarDays className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <h3 className="font-semibold">{schedule.title}</h3>
                      <p className="text-sm text-muted-foreground">
                        {schedule.description || "Aucune description"}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Début</span>
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      <span className="text-sm font-medium">{formatDateTime(schedule.startTime)}</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Fin</span>
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      <span className="text-sm font-medium">{formatDateTime(schedule.endTime)}</span>
                    </div>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Durée</span>
                    <span className="text-sm font-medium">{formatDuration(schedule.startTime, schedule.endTime)}</span>
                  </div>
                  {schedule.teacher && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Enseignant</span>
                      <div className="flex items-center gap-1">
                        <User className="w-3 h-3" />
                        <span className="text-sm font-medium">{schedule.teacher}</span>
                      </div>
                    </div>
                  )}
                  {schedule.room && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Salle</span>
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3" />
                        <span className="text-sm font-medium">{schedule.room}</span>
                      </div>
                    </div>
                  )}
                  {schedule.course && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Cours</span>
                      <div className="flex items-center gap-1">
                        <BookOpen className="w-3 h-3" />
                        <span className="text-sm font-medium">{schedule.course}</span>
                      </div>
                    </div>
                  )}
                  {schedule.groupName && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Groupe</span>
                      <div className="flex items-center gap-1">
                        <Users className="w-3 h-3" />
                        <span className="text-sm font-medium">{schedule.groupName}</span>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex items-center justify-between pt-4 border-t mb-4">
                  <Badge className={`${getStatusColor(schedule.status)} text-xs`}>
                    {getStatusLabel(schedule.status)}
                  </Badge>
                  <span className="text-sm text-muted-foreground">
                    #{schedule.id}
                  </span>
                </div>

                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                    <Edit className="w-3 h-3" />
                    Modifier
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                    onClick={() => handleDeleteSchedule(schedule.id)}
                  >
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>

          {filteredSchedules.length === 0 && (
            <div className="text-center py-12">
              <CalendarDays className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucun créneau trouvé</h3>
              <p className="text-muted-foreground">
                {searchQuery || selectedFilter !== "all"
                  ? "Aucun créneau ne correspond à vos critères de recherche."
                  : "Commencez par créer votre premier créneau."}
              </p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}