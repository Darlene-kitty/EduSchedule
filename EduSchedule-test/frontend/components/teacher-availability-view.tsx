"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import {
  Plus,
  Search,
  Calendar,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Loader2,
  Star,
  Settings,
  Grid3X3,
  List,
  Edit,
  Trash2,
} from "lucide-react"
import { 
  teacherAvailabilityApi, 
  TeacherAvailability, 
  CreateAvailabilityRequest,
  DayOfWeek,
  AvailabilityStatus,
  TimeSlot,
  availabilityUtils
} from "@/lib/api/teacher-availability"
import { AddAvailabilityModal } from "./add-availability-modal"
import { BulkAvailabilityModal } from "./bulk-availability-modal"
import { AvailabilityCalendar } from "./availability-calendar"
import { useToast } from "@/hooks/use-toast"
import { useAuth } from "@/contexts/auth-context"

export function TeacherAvailabilityView() {
  const { toast } = useToast()
  
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedTeacher] = useState<number>(1) // Pour l'instant, enseignant fixe
  const [selectedDay, setSelectedDay] = useState<string>("all")
  const [availabilities, setAvailabilities] = useState<TeacherAvailability[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [isBulkModalOpen, setIsBulkModalOpen] = useState(false)
  const [editingAvailability, setEditingAvailability] = useState<TeacherAvailability | null>(null)
  const [viewMode, setViewMode] = useState<"calendar" | "list">("calendar")

  // Charger les disponibilités au montage
  useEffect(() => {
    if (selectedTeacher) {
      loadAvailabilities(selectedTeacher)
    }
  }, [selectedTeacher])

  const loadAvailabilities = async (teacherId: number) => {
    try {
      setLoading(true)
      setError(null)
      const data = await teacherAvailabilityApi.getTeacherAvailabilities(teacherId)
      setAvailabilities(data)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des disponibilités'
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

  const handleSubmit = async (formData: CreateAvailabilityRequest) => {
    try {
      if (editingAvailability && editingAvailability.id) {
        await teacherAvailabilityApi.updateAvailability(editingAvailability.id, formData)
        toast({
          title: "Succès",
          description: "Disponibilité modifiée avec succès",
        })
      } else {
        await teacherAvailabilityApi.createAvailability(formData)
        toast({
          title: "Succès",
          description: "Disponibilité ajoutée avec succès",
        })
      }
      
      setIsAddModalOpen(false)
      setEditingAvailability(null)
      
      if (selectedTeacher) {
        await loadAvailabilities(selectedTeacher)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la sauvegarde'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
      throw err
    }
  }

  const handleBulkSubmit = async (requests: CreateAvailabilityRequest[]) => {
    try {
      await Promise.all(
        requests.map(request => teacherAvailabilityApi.createAvailability(request))
      )
      
      setIsBulkModalOpen(false)
      
      if (selectedTeacher) {
        await loadAvailabilities(selectedTeacher)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création en masse'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
      throw err
    }
  }

  const handleEdit = (availability: TeacherAvailability) => {
    setEditingAvailability(availability)
    setIsAddModalOpen(true)
  }

  const handleDelete = async (id: number | undefined) => {
    if (!id) return
    
    if (!confirm("Êtes-vous sûr de vouloir supprimer cette disponibilité ?")) {
      return
    }

    try {
      await teacherAvailabilityApi.deleteAvailability(id)
      toast({
        title: "Succès",
        description: "Disponibilité supprimée avec succès",
      })
      
      if (selectedTeacher) {
        await loadAvailabilities(selectedTeacher)
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  // Fonction pour créer des disponibilités par défaut (créneaux d'1h de 8h à 18h sauf 12h-14h)
  const handleSetDefault = async () => {
    if (!selectedTeacher) return

    try {
      const today = new Date()
      const defaultAvailability: CreateAvailabilityRequest = {
        teacherId: selectedTeacher,
        effectiveDate: availabilityUtils.formatDateForApi(today),
        availableSlots: [
          // Lundi à Vendredi, 8h-12h et 14h-18h
          ...Array.from({ length: 5 }, (_, dayIndex) => {
            const dayOfWeek = [DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY][dayIndex]
            return availabilityUtils.generateDaySlots(dayOfWeek)
          }).flat()
        ],
        status: AvailabilityStatus.ACTIVE,
        maxHoursPerDay: 8,
        maxHoursPerWeek: 40,
        notes: "Disponibilités par défaut générées automatiquement"
      }

      await teacherAvailabilityApi.createAvailability(defaultAvailability)
      toast({
        title: "Succès",
        description: "Disponibilités par défaut créées",
      })
      
      await loadAvailabilities(selectedTeacher)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création des disponibilités par défaut'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleAddNew = () => {
    setEditingAvailability(null)
    setIsAddModalOpen(true)
  }

  // Fonction pour obtenir tous les créneaux de toutes les disponibilités
  const getAllTimeSlots = (): (TimeSlot & { availabilityId?: number })[] => {
    return availabilities.flatMap(availability => 
      availability.availableSlots.map(slot => ({
        ...slot,
        availabilityId: availability.id
      }))
    )
  }

  const filteredAvailabilities = availabilities.filter((availability) => {
    const matchesSearch = availability.notes?.toLowerCase().includes(searchQuery.toLowerCase()) || 
                         availability.teacherName?.toLowerCase().includes(searchQuery.toLowerCase())
    
    // Filtrer par jour si spécifié
    if (selectedDay !== "all") {
      const hasMatchingSlot = availability.availableSlots.some(slot => slot.dayOfWeek === selectedDay)
      return matchesSearch && hasMatchingSlot
    }
    
    return matchesSearch
  })

  const stats = [
    {
      label: "Total disponibilités",
      value: availabilities.length,
      color: "bg-blue-500",
      icon: Calendar,
    },
    {
      label: "Créneaux actifs",
      value: availabilities.filter(a => a.status === AvailabilityStatus.ACTIVE).length,
      color: "bg-green-500",
      icon: Star,
    },
    {
      label: "Total créneaux",
      value: getAllTimeSlots().length,
      color: "bg-purple-500",
      icon: Clock,
    },
    {
      label: "Heures/semaine",
      value: Math.round(getAllTimeSlots().reduce((total, slot) => {
        return total + availabilityUtils.calculateDuration(slot.startTime, slot.endTime) / 60
      }, 0)),
      color: "bg-orange-500",
      icon: CheckCircle2,
    },
  ]

  if (loading && availabilities.length === 0) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="teacher-availability" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Disponibilités Enseignants" subtitle="Gérez les créneaux de disponibilité des enseignants" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des disponibilités...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error && availabilities.length === 0) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="teacher-availability" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Disponibilités Enseignants" subtitle="Gérez les créneaux de disponibilité des enseignants" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={() => selectedTeacher && loadAvailabilities(selectedTeacher)}>
                Réessayer
              </Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="teacher-availability" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Disponibilités Enseignants" 
          subtitle="Gérez les créneaux de disponibilité des enseignants"
          action={
            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={handleSetDefault}
                className="gap-2"
              >
                <Settings className="w-4 h-4" />
                Défaut
              </Button>
              <Button 
                onClick={() => setIsBulkModalOpen(true)}
                variant="outline"
                className="gap-2"
              >
                <Calendar className="w-4 h-4" />
                En masse
              </Button>
              <Button 
                onClick={() => handleAddNew()}
                className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90"
              >
                <Plus className="w-4 h-4" />
                Ajouter
              </Button>
            </div>
          }
        />

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

          {/* Barre d'actions */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher une disponibilité..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            
            <div className="flex gap-2">
              <Select value={selectedDay} onValueChange={setSelectedDay}>
                <SelectTrigger className="w-40">
                  <SelectValue placeholder="Jour" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">Tous les jours</SelectItem>
                  {Object.values(DayOfWeek).map((day) => (
                    <SelectItem key={day} value={day}>
                      {availabilityUtils.getDayOfWeekLabel(day)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <div className="flex border rounded-lg">
                <Button
                  variant={viewMode === "calendar" ? "default" : "ghost"}
                  size="sm"
                  onClick={() => setViewMode("calendar")}
                  className={viewMode === "calendar" ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
                >
                  <Grid3X3 className="w-4 h-4" />
                </Button>
                <Button
                  variant={viewMode === "list" ? "default" : "ghost"}
                  size="sm"
                  onClick={() => setViewMode("list")}
                  className={viewMode === "list" ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
                >
                  <List className="w-4 h-4" />
                </Button>
              </div>
            </div>
          </div>

          {/* Contenu principal */}
          {viewMode === "calendar" ? (
            <AvailabilityCalendar
              availabilities={filteredAvailabilities}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onAddNew={handleAddNew}
            />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredAvailabilities.map((availability) => (
                <Card key={availability.id} className="p-5 hover:shadow-lg transition-shadow">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <Calendar className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold">
                          {availability.teacherName || `Enseignant ${availability.teacherId}`}
                        </h3>
                        <p className="text-sm text-muted-foreground">
                          {availability.effectiveDate}
                          {availability.endDate && ` - ${availability.endDate}`}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Statut</span>
                      <Badge className={`${availabilityUtils.getStatusColor(availability.status)} text-xs`}>
                        {availabilityUtils.getStatusLabel(availability.status)}
                      </Badge>
                    </div>
                    
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Créneaux</span>
                      <span className="text-sm font-medium">
                        {availability.availableSlots.length} créneaux
                      </span>
                    </div>

                    {availability.maxHoursPerWeek && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Max h/semaine</span>
                        <span className="text-sm font-medium">
                          {availability.maxHoursPerWeek}h
                        </span>
                      </div>
                    )}
                  </div>

                  {/* Affichage des créneaux */}
                  <div className="mb-4">
                    <p className="text-sm font-medium mb-2">Créneaux disponibles:</p>
                    <div className="space-y-1 max-h-32 overflow-y-auto">
                      {availability.availableSlots.slice(0, 3).map((slot, index) => (
                        <div key={index} className="text-xs bg-gray-50 p-2 rounded">
                          {availabilityUtils.getDayOfWeekLabel(slot.dayOfWeek)} : {slot.startTime} - {slot.endTime}
                        </div>
                      ))}
                      {availability.availableSlots.length > 3 && (
                        <div className="text-xs text-muted-foreground">
                          +{availability.availableSlots.length - 3} autres créneaux
                        </div>
                      )}
                    </div>
                  </div>

                  {availability.notes && (
                    <p className="text-sm text-muted-foreground mb-4 line-clamp-2">
                      {availability.notes}
                    </p>
                  )}

                  <div className="flex gap-2">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="flex-1 gap-2 bg-transparent"
                      onClick={() => handleEdit(availability)}
                    >
                      <Edit className="w-3 h-3" />
                      Modifier
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                      onClick={() => handleDelete(availability.id)}
                    >
                      <Trash2 className="w-3 h-3" />
                    </Button>
                  </div>
                </Card>
              ))}
            </div>
          )}

          {filteredAvailabilities.length === 0 && !loading && (
            <div className="text-center py-12">
              <Calendar className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucune disponibilité trouvée</h3>
              <p className="text-muted-foreground mb-4">
                {searchQuery || selectedDay !== "all"
                  ? "Aucune disponibilité ne correspond à vos critères de recherche."
                  : "Commencez par ajouter vos créneaux de disponibilité."}
              </p>
              <Button onClick={() => handleAddNew()} className="bg-[#15803D] hover:bg-[#15803D]/90">
                <Plus className="w-4 h-4 mr-2" />
                Ajouter une disponibilité
              </Button>
            </div>
          )}
        </main>
      </div>

      <AddAvailabilityModal
        open={isAddModalOpen}
        onOpenChange={setIsAddModalOpen}
        onSubmit={handleSubmit}
        editingData={editingAvailability ? {
          teacherId: editingAvailability.teacherId,
          effectiveDate: editingAvailability.effectiveDate,
          endDate: editingAvailability.endDate,
          availableSlots: editingAvailability.availableSlots,
          status: editingAvailability.status,
          notes: editingAvailability.notes,
          maxHoursPerDay: editingAvailability.maxHoursPerDay,
          maxHoursPerWeek: editingAvailability.maxHoursPerWeek
        } : null}
        teacherId={selectedTeacher}
      />

      <BulkAvailabilityModal
        open={isBulkModalOpen}
        onOpenChange={setIsBulkModalOpen}
        onSubmit={handleBulkSubmit}
        teacherId={selectedTeacher}
      />
    </div>
  )
}