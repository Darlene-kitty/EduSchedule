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
  Building,
  Users,
  MapPin,
  Monitor,
  Wifi,
  Edit,
  Trash2,
  Eye,
  Loader2,
  AlertCircle,
  CheckCircle2,
  XCircle,
  Settings,
} from "lucide-react"
import { AddRoomModal } from "./add-room-modal"
import { roomsApi, Room, CreateRoomRequest, RoomFilters } from "@/lib/api/rooms"
import { useToast } from "@/hooks/use-toast"

export function RoomsView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<string>("all")
  const [rooms, setRooms] = useState<Room[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [editingRoom, setEditingRoom] = useState<Room | null>(null)
  const { toast } = useToast()

  // Charger les salles au montage du composant
  useEffect(() => {
    loadRooms()
  }, [])

  const loadRooms = async (filters?: RoomFilters) => {
    try {
      setLoading(true)
      setError(null)
      const roomsData = await roomsApi.getAllRooms(filters)
      setRooms(roomsData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des salles'
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

  const filteredRooms = rooms.filter((room) => {
    const matchesSearch =
      room.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      room.code?.toLowerCase().includes(searchQuery.toLowerCase()) ||
      room.building?.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesType = selectedType === "all" || room.type === selectedType
    return matchesSearch && matchesType
  })

  const getTypeColor = (type: string) => {
    switch (type.toUpperCase()) {
      case "AMPHITHEATRE":
      case "AMPHITHEATER":
        return "bg-purple-100 text-purple-700"
      case "CLASSROOM":
      case "SALLE_CLASSE":
        return "bg-blue-100 text-blue-700"
      case "LABORATORY":
      case "LABORATOIRE":
        return "bg-green-100 text-green-700"
      case "MEETING_ROOM":
      case "SALLE_REUNION":
        return "bg-orange-100 text-orange-700"
      case "COMPUTER_LAB":
      case "SALLE_INFORMATIQUE":
        return "bg-cyan-100 text-cyan-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusColor = (available: boolean) => {
    return available ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
  }

  const getStatusIcon = (available: boolean) => {
    return available ? CheckCircle2 : XCircle
  }

  const getStatusLabel = (available: boolean) => {
    return available ? "Disponible" : "Occupée"
  }

  const getTypeLabel = (type: string) => {
    switch (type.toUpperCase()) {
      case "AMPHITHEATRE":
      case "AMPHITHEATER":
        return "Amphithéâtre"
      case "CLASSROOM":
      case "SALLE_CLASSE":
        return "Salle de classe"
      case "LABORATORY":
      case "LABORATOIRE":
        return "Laboratoire"
      case "MEETING_ROOM":
      case "SALLE_REUNION":
        return "Salle de réunion"
      case "COMPUTER_LAB":
      case "SALLE_INFORMATIQUE":
        return "Salle informatique"
      default:
        return type
    }
  }

  const uniqueTypes = Array.from(new Set(rooms.map(r => r.type)))

  const handleAddRoom = async (roomData: CreateRoomRequest) => {
    try {
      const newRoom = await roomsApi.createRoom(roomData)
      setRooms(prev => [...prev, newRoom])
      toast({
        title: "Succès",
        description: "Salle créée avec succès",
      })
      setIsAddModalOpen(false)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création de la salle'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleViewRoom = async (roomId: number) => {
    try {
      const room = await roomsApi.getRoomById(roomId)
      // Ici vous pouvez ouvrir une modal de détails ou naviguer vers une page de détails
      toast({
        title: "Détails de la salle",
        description: `${room.name} - Capacité: ${room.capacity} personnes`,
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

  const handleEditRoom = (room: Room) => {
    setEditingRoom(room)
    setIsAddModalOpen(true)
  }

  const handleUpdateRoom = async (roomData: CreateRoomRequest) => {
    if (!editingRoom) return

    try {
      const updatedRoom = await roomsApi.updateRoom(editingRoom.id, roomData)
      setRooms(prev => prev.map(r => r.id === editingRoom.id ? updatedRoom : r))
      toast({
        title: "Succès",
        description: "Salle modifiée avec succès",
      })
      setIsAddModalOpen(false)
      setEditingRoom(null)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la modification de la salle'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleDeleteRoom = async (roomId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer cette salle ?")) {
      return
    }

    try {
      await roomsApi.deleteRoom(roomId)
      setRooms(prev => prev.filter(room => room.id !== roomId))
      toast({
        title: "Succès",
        description: "Salle supprimée avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression de la salle'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const stats = [
    { label: "Total salles", value: rooms.length, color: "bg-blue-500", icon: Building },
    {
      label: "Disponibles",
      value: rooms.filter((r) => r.available).length,
      color: "bg-green-500",
      icon: CheckCircle2,
    },
    {
      label: "Occupées",
      value: rooms.filter((r) => !r.available).length,
      color: "bg-red-500",
      icon: XCircle,
    },
    {
      label: "Capacité totale",
      value: rooms.reduce((sum, r) => sum + r.capacity, 0),
      color: "bg-purple-500",
      icon: Users,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="rooms" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Salles" subtitle="Gérez les salles de classe, amphithéâtres et laboratoires" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des salles...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="rooms" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Salles" subtitle="Gérez les salles de classe, amphithéâtres et laboratoires" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={() => loadRooms()}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="rooms" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header 
          title="Salles" 
          subtitle="Gérez les salles de classe, amphithéâtres et laboratoires"
          action={
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
              <Plus className="w-4 h-4" />
              Ajouter une salle
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
                placeholder="Rechercher une salle..."
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
              Toutes
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

          {/* Rooms Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredRooms.map((room) => {
              const StatusIcon = getStatusIcon(room.available)
              return (
                <Card key={room.id} className="p-5 hover:shadow-lg transition-shadow">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <Building className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold">{room.name}</h3>
                        {room.code && (
                          <p className="text-sm text-muted-foreground">{room.code}</p>
                        )}
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Type</span>
                      <Badge className={`${getTypeColor(room.type)} text-xs`}>
                        {getTypeLabel(room.type)}
                      </Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Capacité</span>
                      <div className="flex items-center gap-1">
                        <Users className="w-3 h-3" />
                        <span className="text-sm font-medium">{room.capacity} personnes</span>
                      </div>
                    </div>
                    {room.building && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Bâtiment</span>
                        <div className="flex items-center gap-1">
                          <MapPin className="w-3 h-3" />
                          <span className="text-sm font-medium">
                            {room.building}
                            {room.floor && ` - Étage ${room.floor}`}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  {room.equipment && room.equipment.length > 0 && (
                    <div className="mb-4">
                      <p className="text-sm font-medium mb-2">Équipements:</p>
                      <div className="flex flex-wrap gap-1">
                        {room.equipment.slice(0, 3).map((item, index) => (
                          <Badge key={index} variant="outline" className="text-xs">
                            {item}
                          </Badge>
                        ))}
                        {room.equipment.length > 3 && (
                          <Badge variant="outline" className="text-xs">
                            +{room.equipment.length - 3} autres
                          </Badge>
                        )}
                      </div>
                    </div>
                  )}

                  <div className="flex items-center gap-2 pt-4 border-t mb-4">
                    <StatusIcon className="w-4 h-4" />
                    <Badge className={`${getStatusColor(room.available)} text-xs flex-1`}>
                      {getStatusLabel(room.available)}
                    </Badge>
                  </div>

                  <div className="flex gap-2">
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="flex-1 gap-2 bg-transparent"
                      onClick={() => handleViewRoom(room.id)}
                    >
                      <Eye className="w-3 h-3" />
                      Voir
                    </Button>
                    <Button 
                      variant="outline" 
                      size="sm" 
                      className="flex-1 gap-2 bg-transparent"
                      onClick={() => handleEditRoom(room)}
                    >
                      <Edit className="w-3 h-3" />
                      Modifier
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                      onClick={() => handleDeleteRoom(room.id)}
                    >
                      <Trash2 className="w-3 h-3" />
                    </Button>
                  </div>
                </Card>
              )
            })}
          </div>

          {filteredRooms.length === 0 && !loading && (
            <div className="text-center py-12">
              <Building className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucune salle trouvée</h3>
              <p className="text-muted-foreground mb-4">
                {searchQuery || selectedType !== "all"
                  ? "Aucune salle ne correspond à vos critères de recherche."
                  : "Commencez par ajouter votre première salle."}
              </p>
              <Button onClick={() => setIsAddModalOpen(true)} className="bg-[#15803D] hover:bg-[#15803D]/90">
                <Plus className="w-4 h-4 mr-2" />
                Ajouter une salle
              </Button>
            </div>
          )}
        </main>
      </div>

      <AddRoomModal 
        open={isAddModalOpen} 
        onOpenChange={(open) => {
          setIsAddModalOpen(open)
          if (!open) setEditingRoom(null)
        }}
        onSubmit={editingRoom ? handleUpdateRoom : handleAddRoom}
        editingData={editingRoom ? {
          name: editingRoom.name,
          code: editingRoom.code,
          type: editingRoom.type,
          capacity: editingRoom.capacity,
          building: editingRoom.building,
          floor: editingRoom.floor,
          equipment: editingRoom.equipment,
          available: editingRoom.available
        } : null}
      />
    </div>
  )
}