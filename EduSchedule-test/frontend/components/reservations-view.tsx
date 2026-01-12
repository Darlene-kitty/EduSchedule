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
  CheckCircle2,
  XCircle,
  AlertCircle,
  Loader2,
  Filter,
} from "lucide-react"
import { reservationsApi, Reservation, ReservationStatus, ReservationType } from "@/lib/api/reservations"
import { useToast } from "@/hooks/use-toast"

export function ReservationsView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedStatus, setSelectedStatus] = useState<string>("all")
  const [selectedType, setSelectedType] = useState<string>("all")
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  // Charger les réservations au montage du composant
  useEffect(() => {
    loadReservations()
  }, [])

  const loadReservations = async () => {
    try {
      setLoading(true)
      setError(null)
      // Pour l'instant, on charge toutes les réservations
      // Dans une vraie application, on pourrait implémenter une pagination
      const reservationsData = await reservationsApi.searchReservations({})
      setReservations(reservationsData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des réservations'
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

  const filteredReservations = reservations.filter((reservation) => {
    const matchesSearch =
      reservation.purpose.toLowerCase().includes(searchQuery.toLowerCase()) ||
      reservation.id.toString().includes(searchQuery)
    const matchesStatus = selectedStatus === "all" || reservation.status === selectedStatus
    const matchesType = selectedType === "all" || reservation.type === selectedType
    return matchesSearch && matchesStatus && matchesType
  })

  const getStatusColor = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.APPROVED:
        return "bg-green-100 text-green-700"
      case ReservationStatus.PENDING:
        return "bg-yellow-100 text-yellow-700"
      case ReservationStatus.CANCELLED:
        return "bg-gray-100 text-gray-700"
      case ReservationStatus.REJECTED:
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusIcon = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.APPROVED:
        return CheckCircle2
      case ReservationStatus.PENDING:
        return Clock
      case ReservationStatus.CANCELLED:
      case ReservationStatus.REJECTED:
        return XCircle
      default:
        return AlertCircle
    }
  }

  const getStatusLabel = (status: ReservationStatus) => {
    switch (status) {
      case ReservationStatus.APPROVED:
        return "Approuvée"
      case ReservationStatus.PENDING:
        return "En attente"
      case ReservationStatus.CANCELLED:
        return "Annulée"
      case ReservationStatus.REJECTED:
        return "Rejetée"
      default:
        return status
    }
  }

  const getTypeColor = (type: ReservationType) => {
    switch (type) {
      case ReservationType.COURSE:
        return "bg-blue-100 text-blue-700"
      case ReservationType.MEETING:
        return "bg-purple-100 text-purple-700"
      case ReservationType.EVENT:
        return "bg-orange-100 text-orange-700"
      case ReservationType.MAINTENANCE:
        return "bg-gray-100 text-gray-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getTypeLabel = (type: ReservationType) => {
    switch (type) {
      case ReservationType.COURSE:
        return "Cours"
      case ReservationType.MEETING:
        return "Réunion"
      case ReservationType.EVENT:
        return "Événement"
      case ReservationType.MAINTENANCE:
        return "Maintenance"
      default:
        return type
    }
  }

  const handleApproveReservation = async (reservationId: number) => {
    try {
      // Dans une vraie application, on récupérerait l'ID de l'utilisateur connecté
      const currentUserId = 1 // Placeholder
      await reservationsApi.approveReservation(reservationId, currentUserId)
      
      // Mettre à jour l'état local
      setReservations(prev => prev.map(res => 
        res.id === reservationId 
          ? { ...res, status: ReservationStatus.APPROVED, approvedBy: currentUserId, approvedAt: new Date().toISOString() }
          : res
      ))
      
      toast({
        title: "Succès",
        description: "Réservation approuvée avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de l\'approbation'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleCancelReservation = async (reservationId: number) => {
    const reason = prompt("Raison de l'annulation (optionnel):")
    
    try {
      // Dans une vraie application, on récupérerait l'ID de l'utilisateur connecté
      const currentUserId = 1 // Placeholder
      await reservationsApi.cancelReservation(reservationId, currentUserId, reason || undefined)
      
      // Mettre à jour l'état local
      setReservations(prev => prev.map(res => 
        res.id === reservationId 
          ? { 
              ...res, 
              status: ReservationStatus.CANCELLED, 
              cancelledBy: currentUserId, 
              cancelledAt: new Date().toISOString(),
              cancellationReason: reason || undefined
            }
          : res
      ))
      
      toast({
        title: "Succès",
        description: "Réservation annulée avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de l\'annulation'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const stats = [
    { label: "Total réservations", value: reservations.length, color: "bg-blue-500", icon: Calendar },
    {
      label: "Approuvées",
      value: reservations.filter((r) => r.status === ReservationStatus.APPROVED).length,
      color: "bg-green-500",
      icon: CheckCircle2,
    },
    {
      label: "En attente",
      value: reservations.filter((r) => r.status === ReservationStatus.PENDING).length,
      color: "bg-yellow-500",
      icon: Clock,
    },
    {
      label: "Annulées",
      value: reservations.filter((r) => r.status === ReservationStatus.CANCELLED).length,
      color: "bg-red-500",
      icon: XCircle,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="reservations" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Réservations" subtitle="Gérez les réservations de salles et équipements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des réservations...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="reservations" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Réservations" subtitle="Gérez les réservations de salles et équipements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={loadReservations}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="reservations" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Réservations" subtitle="Gérez les réservations de salles et équipements" />

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
                placeholder="Rechercher une réservation..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="flex gap-2">
              <Button variant="outline" className="gap-2 bg-transparent">
                <Filter className="w-4 h-4" />
                Filtrer
              </Button>
              <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90">
                <Plus className="w-4 h-4" />
                Nouvelle réservation
              </Button>
            </div>
          </div>

          {/* Filters */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            <Button
              variant={selectedStatus === "all" ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedStatus("all")}
              className={selectedStatus === "all" ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
            >
              Tous les statuts
            </Button>
            {Object.values(ReservationStatus).map((status) => (
              <Button
                key={status}
                variant={selectedStatus === status ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedStatus(status)}
                className={selectedStatus === status ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {getStatusLabel(status)}
              </Button>
            ))}
          </div>

          {/* Reservations Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredReservations.map((reservation) => {
              const StatusIcon = getStatusIcon(reservation.status)
              return (
                <Card key={reservation.id} className="p-5 hover:shadow-lg transition-shadow">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <Calendar className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold">#{reservation.id}</h3>
                        <p className="text-sm text-muted-foreground">{reservation.purpose}</p>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Type</span>
                      <Badge className={`${getTypeColor(reservation.type)} text-xs`}>
                        {getTypeLabel(reservation.type)}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Ressource</span>
                      <div className="flex items-center gap-1">
                        <MapPin className="w-3 h-3" />
                        <span className="text-sm font-medium">ID: {reservation.resourceId}</span>
                      </div>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Utilisateur</span>
                      <div className="flex items-center gap-1">
                        <User className="w-3 h-3" />
                        <span className="text-sm font-medium">ID: {reservation.userId}</span>
                      </div>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Début</span>
                      <span className="text-sm font-medium">{formatDateTime(reservation.startDateTime)}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Fin</span>
                      <span className="text-sm font-medium">{formatDateTime(reservation.endDateTime)}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-4 border-t mb-4">
                    <StatusIcon className="w-4 h-4" />
                    <Badge className={`${getStatusColor(reservation.status)} text-xs flex-1`}>
                      {getStatusLabel(reservation.status)}
                    </Badge>
                  </div>

                  <div className="flex gap-2">
                    {reservation.status === ReservationStatus.PENDING && (
                      <>
                        <Button 
                          variant="outline" 
                          size="sm" 
                          className="flex-1 gap-2 bg-transparent text-green-600 hover:text-green-700"
                          onClick={() => handleApproveReservation(reservation.id)}
                        >
                          <CheckCircle2 className="w-3 h-3" />
                          Approuver
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                          onClick={() => handleCancelReservation(reservation.id)}
                        >
                          <XCircle className="w-3 h-3" />
                        </Button>
                      </>
                    )}
                    {reservation.status === ReservationStatus.APPROVED && (
                      <>
                        <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                          <Edit className="w-3 h-3" />
                          Modifier
                        </Button>
                        <Button
                          variant="outline"
                          size="sm"
                          className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                          onClick={() => handleCancelReservation(reservation.id)}
                        >
                          <XCircle className="w-3 h-3" />
                        </Button>
                      </>
                    )}
                    {(reservation.status === ReservationStatus.CANCELLED || reservation.status === ReservationStatus.REJECTED) && (
                      <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent" disabled>
                        <Trash2 className="w-3 h-3" />
                        Archiver
                      </Button>
                    )}
                  </div>
                </Card>
              )
            })}
          </div>

          {filteredReservations.length === 0 && (
            <div className="text-center py-12">
              <Calendar className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucune réservation trouvée</h3>
              <p className="text-muted-foreground">
                {searchQuery || selectedStatus !== "all"
                  ? "Aucune réservation ne correspond à vos critères de recherche."
                  : "Commencez par créer votre première réservation."}
              </p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}