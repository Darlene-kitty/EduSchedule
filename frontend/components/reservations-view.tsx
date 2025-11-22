"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { CalendarIcon, Clock, MapPin, User, Plus, ChevronLeft, ChevronRight } from "lucide-react"
import { AddReservationModal } from "./add-reservation-modal"

interface Reservation {
  id: string
  room: string
  date: string
  startTime: string
  endTime: string
  purpose: string
  requestedBy: string
  status: "Confirmée" | "En attente" | "Annulée"
  capacity: number
  equipment: string[]
}

const reservations: Reservation[] = [
  {
    id: "1",
    room: "A101",
    date: "2025-10-20",
    startTime: "08:00",
    endTime: "10:00",
    purpose: "Cours de Mathématiques",
    requestedBy: "Dr. Martin",
    status: "Confirmée",
    capacity: 40,
    equipment: ["Projecteur", "Tableau blanc"],
  },
  {
    id: "2",
    room: "B203",
    date: "2025-10-20",
    startTime: "10:00",
    endTime: "12:00",
    purpose: "TP Physique",
    requestedBy: "Dr. Laurent",
    status: "Confirmée",
    capacity: 30,
    equipment: ["Matériel de laboratoire", "Projecteur"],
  },
  {
    id: "3",
    room: "Amphi A",
    date: "2025-10-21",
    startTime: "14:00",
    endTime: "16:00",
    purpose: "Conférence",
    requestedBy: "Prof. Bernard",
    status: "En attente",
    capacity: 200,
    equipment: ["Projecteur", "Micro", "Sonorisation"],
  },
  {
    id: "4",
    room: "Lab B1",
    date: "2025-10-21",
    startTime: "09:00",
    endTime: "11:00",
    purpose: "Travaux pratiques",
    requestedBy: "Dr. Sophie",
    status: "Confirmée",
    capacity: 25,
    equipment: ["Ordinateurs", "Projecteur"],
  },
  {
    id: "5",
    room: "A103",
    date: "2025-10-22",
    startTime: "13:00",
    endTime: "15:00",
    purpose: "Réunion pédagogique",
    requestedBy: "Admin Système",
    status: "En attente",
    capacity: 20,
    equipment: ["Tableau blanc", "Écran"],
  },
  {
    id: "6",
    room: "C301",
    date: "2025-10-18",
    startTime: "10:00",
    endTime: "12:00",
    purpose: "Examen",
    requestedBy: "Prof. Dubois",
    status: "Annulée",
    capacity: 50,
    equipment: ["Tables individuelles"],
  },
]

const rooms = [
  { name: "A101", capacity: 40, type: "Salle de cours", available: true },
  { name: "A102", capacity: 40, type: "Salle de cours", available: true },
  { name: "A103", capacity: 35, type: "Salle de cours", available: false },
  { name: "B203", capacity: 30, type: "Laboratoire", available: true },
  { name: "Lab B1", capacity: 25, type: "Laboratoire", available: false },
  { name: "Amphi A", capacity: 200, type: "Amphithéâtre", available: true },
  { name: "C301", capacity: 50, type: "Salle d'examen", available: true },
  { name: "C302", capacity: 50, type: "Salle d'examen", available: true },
]

export function ReservationsView() {
  const [selectedDate, setSelectedDate] = useState(new Date())
  const [filterStatus, setFilterStatus] = useState<string>("all")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const filteredReservations = reservations.filter((reservation) => {
    if (filterStatus === "all") return true
    return reservation.status === filterStatus
  })

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Confirmée":
        return "bg-green-100 text-green-700"
      case "En attente":
        return "bg-yellow-100 text-yellow-700"
      case "Annulée":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getRoomTypeColor = (type: string) => {
    switch (type) {
      case "Salle de cours":
        return "bg-blue-100 text-blue-700"
      case "Laboratoire":
        return "bg-purple-100 text-purple-700"
      case "Amphithéâtre":
        return "bg-orange-100 text-orange-700"
      case "Salle d'examen":
        return "bg-cyan-100 text-cyan-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const handleAddReservation = (reservationData: any) => {
    console.log("[v0] New reservation added:", reservationData)
    // Here you would typically add the reservation to your state/database
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="reservations" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Réservation de salles" subtitle="Gérez les réservations et la disponibilité des salles" />

        <main className="flex-1 overflow-y-auto p-6">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left Column - Reservations List */}
            <div className="lg:col-span-2 space-y-4">
              {/* Actions Bar */}
              <div className="flex flex-col sm:flex-row gap-4">
                <div className="flex gap-2 flex-1">
                  {["all", "Confirmée", "En attente", "Annulée"].map((status) => (
                    <Button
                      key={status}
                      variant={filterStatus === status ? "default" : "outline"}
                      size="sm"
                      onClick={() => setFilterStatus(status)}
                      className={filterStatus === status ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
                    >
                      {status === "all" ? "Toutes" : status}
                    </Button>
                  ))}
                </div>
                <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
                  <Plus className="w-4 h-4" />
                  Nouvelle réservation
                </Button>
              </div>

              {/* Date Navigator */}
              <Card className="p-4">
                <div className="flex items-center justify-between">
                  <Button variant="ghost" size="icon">
                    <ChevronLeft className="w-5 h-5" />
                  </Button>
                  <div className="flex items-center gap-2">
                    <CalendarIcon className="w-5 h-5 text-muted-foreground" />
                    <span className="font-semibold">
                      {selectedDate.toLocaleDateString("fr-FR", {
                        weekday: "long",
                        year: "numeric",
                        month: "long",
                        day: "numeric",
                      })}
                    </span>
                  </div>
                  <Button variant="ghost" size="icon">
                    <ChevronRight className="w-5 h-5" />
                  </Button>
                </div>
              </Card>

              {/* Reservations List */}
              <div className="space-y-3">
                {filteredReservations.map((reservation) => (
                  <Card key={reservation.id} className="p-5 hover:shadow-md transition-shadow">
                    <div className="flex items-start justify-between mb-3">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-lg">{reservation.purpose}</h3>
                          <Badge className={`${getStatusColor(reservation.status)} text-xs`}>
                            {reservation.status}
                          </Badge>
                        </div>
                        <div className="grid grid-cols-2 gap-2 text-sm text-muted-foreground">
                          <div className="flex items-center gap-2">
                            <MapPin className="w-4 h-4" />
                            <span>
                              {reservation.room} ({reservation.capacity} places)
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <User className="w-4 h-4" />
                            <span>{reservation.requestedBy}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <CalendarIcon className="w-4 h-4" />
                            <span>
                              {new Date(reservation.date).toLocaleDateString("fr-FR", {
                                day: "numeric",
                                month: "short",
                                year: "numeric",
                              })}
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <Clock className="w-4 h-4" />
                            <span>
                              {reservation.startTime} - {reservation.endTime}
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <div className="flex flex-wrap gap-2 pt-3 border-t">
                      {reservation.equipment.map((item, index) => (
                        <Badge key={index} variant="outline" className="text-xs">
                          {item}
                        </Badge>
                      ))}
                    </div>
                    <div className="flex gap-2 mt-4">
                      <Button variant="outline" size="sm" className="flex-1 bg-transparent">
                        Modifier
                      </Button>
                      <Button variant="outline" size="sm" className="text-red-600 hover:text-red-700 bg-transparent">
                        Annuler
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </div>

            {/* Right Column - Available Rooms */}
            <div className="space-y-4">
              <Card className="p-5">
                <h3 className="font-semibold text-lg mb-4">Salles disponibles</h3>
                <div className="space-y-3">
                  {rooms.map((room) => (
                    <div
                      key={room.name}
                      className={`p-4 rounded-lg border-2 transition-colors ${
                        room.available
                          ? "border-green-200 bg-green-50 hover:border-green-300"
                          : "border-gray-200 bg-gray-50"
                      }`}
                    >
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h4 className="font-semibold">{room.name}</h4>
                          <p className="text-sm text-muted-foreground">{room.capacity} places</p>
                        </div>
                        <Badge className={`${getRoomTypeColor(room.type)} text-xs`}>{room.type}</Badge>
                      </div>
                      <div className="flex items-center gap-2 mt-3">
                        <div className={`w-2 h-2 rounded-full ${room.available ? "bg-green-500" : "bg-red-500"}`} />
                        <span className="text-xs font-medium">{room.available ? "Disponible" : "Occupée"}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </Card>

              <Card className="p-5">
                <h3 className="font-semibold text-lg mb-4">Statistiques</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-muted-foreground">Taux d'occupation</span>
                    <span className="font-semibold">68%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div className="bg-[#15803D] h-2 rounded-full" style={{ width: "68%" }} />
                  </div>
                  <div className="grid grid-cols-2 gap-3 pt-3">
                    <div className="text-center p-3 bg-green-50 rounded-lg">
                      <p className="text-2xl font-bold text-green-700">5</p>
                      <p className="text-xs text-muted-foreground">Disponibles</p>
                    </div>
                    <div className="text-center p-3 bg-red-50 rounded-lg">
                      <p className="text-2xl font-bold text-red-700">3</p>
                      <p className="text-xs text-muted-foreground">Occupées</p>
                    </div>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </main>
      </div>
      <AddReservationModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} onSubmit={handleAddReservation} />
    </div>
  )
}
