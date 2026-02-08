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
  MapPin,
  Users,
  Edit,
  Trash2,
  Eye,
  Loader2,
  AlertCircle,
  CalendarDays,
  Star,
} from "lucide-react"
import { AddEventModal } from "./add-event-modal"
import { eventsApi, Event, CreateEventRequest, EventFilters } from "@/lib/api/events"
import { useToast } from "@/hooks/use-toast"

export function EventsView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<string>("all")
  const [events, setEvents] = useState<Event[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [editingEvent, setEditingEvent] = useState<Event | null>(null)
  const { toast } = useToast()

  // Charger les événements au montage du composant
  useEffect(() => {
    loadEvents()
  }, [])

  const loadEvents = async (filters?: EventFilters) => {
    try {
      setLoading(true)
      setError(null)
      const eventsData = await eventsApi.getAllEvents(filters)
      setEvents(eventsData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des événements'
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

  const filteredEvents = events.filter((event) => {
    const matchesSearch =
      event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      event.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      event.location?.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesType = selectedType === "all" || event.type === selectedType
    return matchesSearch && matchesType
  })

  const getTypeColor = (type: string) => {
    switch (type.toUpperCase()) {
      case "CONFERENCE":
        return "bg-blue-100 text-blue-700"
      case "WORKSHOP":
      case "ATELIER":
        return "bg-green-100 text-green-700"
      case "SEMINAR":
      case "SEMINAIRE":
        return "bg-purple-100 text-purple-700"
      case "MEETING":
      case "REUNION":
        return "bg-orange-100 text-orange-700"
      case "EXAM":
      case "EXAMEN":
        return "bg-red-100 text-red-700"
      case "SOCIAL":
      case "SOCIAL_EVENT":
        return "bg-pink-100 text-pink-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusColor = (status: string) => {
    switch (status.toUpperCase()) {
      case "SCHEDULED":
      case "PLANIFIE":
        return "bg-blue-100 text-blue-700"
      case "ONGOING":
      case "EN_COURS":
        return "bg-green-100 text-green-700"
      case "COMPLETED":
      case "TERMINE":
        return "bg-gray-100 text-gray-700"
      case "CANCELLED":
      case "ANNULE":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status.toUpperCase()) {
      case "SCHEDULED":
      case "PLANIFIE":
        return "Planifié"
      case "ONGOING":
      case "EN_COURS":
        return "En cours"
      case "COMPLETED":
      case "TERMINE":
        return "Terminé"
      case "CANCELLED":
      case "ANNULE":
        return "Annulé"
      default:
        return status
    }
  }

  const getTypeLabel = (type: string) => {
    switch (type.toUpperCase()) {
      case "CONFERENCE":
        return "Conférence"
      case "WORKSHOP":
      case "ATELIER":
        return "Atelier"
      case "SEMINAR":
      case "SEMINAIRE":
        return "Séminaire"
      case "MEETING":
      case "REUNION":
        return "Réunion"
      case "EXAM":
      case "EXAMEN":
        return "Examen"
      case "SOCIAL":
      case "SOCIAL_EVENT":
        return "Événement social"
      default:
        return type
    }
  }

  const uniqueTypes = Array.from(new Set(events.map(e => e.type)))

  const handleAddEvent = async (eventData: CreateEventRequest) => {
    try {
      const newEvent = await eventsApi.createEvent(eventData)
      setEvents(prev => [...prev, newEvent])
      toast({
        title: "Succès",
        description: "Événement créé avec succès",
      })
      setIsAddModalOpen(false)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création de l\'événement'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleViewEvent = async (eventId: number) => {
    try {
      const event = await eventsApi.getEventById(eventId)
      // Ici vous pouvez ouvrir une modal de détails ou naviguer vers une page de détails
      toast({
        title: "Détails de l'événement",
        description: `${event.title} - ${new Date(event.startDate).toLocaleDateString()}`,
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des détails'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleEditEvent = (event: Event) => {
    setEditingEvent(event)
    setIsAddModalOpen(true)
  }

  const handleUpdateEvent = async (eventData: CreateEventRequest) => {
    if (!editingEvent) return

    try {
      const updatedEvent = await eventsApi.updateEvent(editingEvent.id, eventData)
      setEvents(prev => prev.map(e => e.id === editingEvent.id ? updatedEvent : e))
      toast({
        title: "Succès",
        description: "Événement modifié avec succès",
      })
      setIsAddModalOpen(false)
      setEditingEvent(null)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la modification de l\'événement'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleDeleteEvent = async (eventId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer cet événement ?")) {
      return
    }

    try {
      await eventsApi.deleteEvent(eventId)
      setEvents(prev => prev.filter(event => event.id !== eventId))
      toast({
        title: "Succès",
        description: "Événement supprimé avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression de l\'événement'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const stats = [
    { label: "Total événements", value: events.length, color: "bg-blue-500", icon: Calendar },
    {
      label: "À venir",
      value: events.filter((e) => new Date(e.startDate) > new Date()).length,
      color: "bg-green-500",
      icon: CalendarDays,
    },
    {
      label: "En cours",
      value: events.filter((e) => e.status === 'ONGOING' || e.status === 'EN_COURS').length,
      color: "bg-orange-500",
      icon: Star,
    },
    {
      label: "Participants total",
      value: events.reduce((sum, e) => sum + (e.maxParticipants || 0), 0),
      color: "bg-purple-500",
      icon: Users,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="events" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Événements" subtitle="Gérez les conférences, ateliers et événements académiques" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des événements...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="events" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Événements" subtitle="Gérez les conférences, ateliers et événements académiques" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={() => loadEvents()}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="events" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Événements" 
          subtitle="Gérez les conférences, ateliers et événements académiques"
          action={
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
              <Plus className="w-4 h-4" />
              Ajouter un événement
            </Button>
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

          {/* Actions Bar */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher un événement..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </div>

          {/* Type Filter */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            <Button
              variant={selectedType === "all" ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedType("all")}
              className={selectedType === "all" ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
            >
              Tous
            </Button>
            {uniqueTypes.map((type) => (
              <Button
                key={type}
                variant={selectedType === type ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedType(type)}
                className={selectedType === type ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {getTypeLabel(type)}
              </Button>
            ))}
          </div>

          {/* Events Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredEvents.map((event) => (
              <Card key={event.id} className="p-5 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="bg-[#15803D] p-3 rounded-lg">
                      <Calendar className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <h3 className="font-semibold">{event.title}</h3>
                      <Badge className={`${getTypeColor(event.type)} text-xs mt-1`}>
                        {getTypeLabel(event.type)}
                      </Badge>
                    </div>
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Clock className="w-4 h-4" />
                    <span>
                      {new Date(event.startDate).toLocaleDateString()} à{" "}
                      {new Date(event.startDate).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </span>
                  </div>
                  {event.location && (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <MapPin className="w-4 h-4" />
                      <span className="truncate">{event.location}</span>
                    </div>
                  )}
                  {event.maxParticipants && (
                    <div className="flex items-center gap-2 text-sm text-muted-foreground">
                      <Users className="w-4 h-4" />
                      <span>Max {event.maxParticipants} participants</span>
                    </div>
                  )}
                </div>

                {event.description && (
                  <p className="text-sm text-muted-foreground mb-4 line-clamp-2">
                    {event.description}
                  </p>
                )}

                <div className="flex items-center justify-between pt-4 border-t mb-4">
                  <Badge className={`${getStatusColor(event.status)} text-xs`}>
                    {getStatusLabel(event.status)}
                  </Badge>
                  <span className="text-sm text-muted-foreground">
                    ID: {event.id}
                  </span>
                </div>

                <div className="flex gap-2">
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="flex-1 gap-2 bg-transparent"
                    onClick={() => handleViewEvent(event.id)}
                  >
                    <Eye className="w-3 h-3" />
                    Voir
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="flex-1 gap-2 bg-transparent"
                    onClick={() => handleEditEvent(event)}
                  >
                    <Edit className="w-3 h-3" />
                    Modifier
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                    onClick={() => handleDeleteEvent(event.id)}
                  >
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>

          {filteredEvents.length === 0 && !loading && (
            <div className="text-center py-12">
              <Calendar className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucun événement trouvé</h3>
              <p className="text-muted-foreground mb-4">
                {searchQuery || selectedType !== "all"
                  ? "Aucun événement ne correspond à vos critères de recherche."
                  : "Commencez par ajouter votre premier événement."}
              </p>
              <Button onClick={() => setIsAddModalOpen(true)} className="bg-[#15803D] hover:bg-[#15803D]/90">
                <Plus className="w-4 h-4 mr-2" />
                Ajouter un événement
              </Button>
            </div>
          )}
        </main>
      </div>

      <AddEventModal 
        open={isAddModalOpen} 
        onOpenChange={(open) => {
          setIsAddModalOpen(open)
          if (!open) setEditingEvent(null)
        }}
        onSubmit={editingEvent ? handleUpdateEvent : handleAddEvent}
        editingData={editingEvent ? {
          title: editingEvent.title,
          description: editingEvent.description,
          type: editingEvent.type,
          startDate: editingEvent.startDate,
          endDate: editingEvent.endDate,
          location: editingEvent.location,
          maxParticipants: editingEvent.maxParticipants,
          status: editingEvent.status
        } : null}
      />
    </div>
  )
}