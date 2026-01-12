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
  Monitor,
  Projector,
  Laptop,
  Printer,
  Wifi,
  Mic,
  Camera,
  Package,
  Edit,
  Trash2,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Loader2,
  Building,
  Users,
} from "lucide-react"
import { AddResourceModal } from "./add-resource-modal"
import { resourcesApi, Resource as ApiResource, CreateResourceRequest } from "@/lib/api/resources"
import { useToast } from "@/hooks/use-toast"

interface Resource {
  id: number
  nom: string
  code: string
  type: string
  capacite: number
  batiment?: string
  etage?: number
  disponible: boolean
  createdAt?: string
}

export function ResourcesView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedType, setSelectedType] = useState<string>("all")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)
  const [resources, setResources] = useState<Resource[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  // Charger les ressources au montage du composant
  useEffect(() => {
    loadResources()
  }, [])

  const loadResources = async () => {
    try {
      setLoading(true)
      setError(null)
      const resourcesData = await resourcesApi.getAllResources()
      setResources(resourcesData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des ressources'
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

  const filteredResources = resources.filter((resource) => {
    const matchesSearch =
      resource.nom.toLowerCase().includes(searchQuery.toLowerCase()) ||
      resource.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
      resource.code.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesType = selectedType === "all" || resource.type === selectedType
    return matchesSearch && matchesType
  })

  const getStatusColor = (disponible: boolean) => {
    return disponible ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"
  }

  const getStatusIcon = (disponible: boolean) => {
    return disponible ? CheckCircle2 : XCircle
  }

  const getStatusLabel = (disponible: boolean) => {
    return disponible ? "Disponible" : "Indisponible"
  }

  const getTypeIcon = (type: string) => {
    const typeUpper = type.toUpperCase()
    if (typeUpper.includes('AMPHI') || typeUpper.includes('AMPHITHEATRE')) return Monitor
    if (typeUpper.includes('CLASSROOM') || typeUpper.includes('SALLE')) return Package
    if (typeUpper.includes('LAB') || typeUpper.includes('LABORATORY')) return Laptop
    return Building
  }

  const getTypeColor = (type: string) => {
    const typeUpper = type.toUpperCase()
    if (typeUpper.includes('AMPHI') || typeUpper.includes('AMPHITHEATRE')) return "bg-purple-100 text-purple-700"
    if (typeUpper.includes('CLASSROOM') || typeUpper.includes('SALLE')) return "bg-blue-100 text-blue-700"
    if (typeUpper.includes('LAB') || typeUpper.includes('LABORATORY')) return "bg-cyan-100 text-cyan-700"
    return "bg-gray-100 text-gray-700"
  }

  const uniqueTypes = Array.from(new Set(resources.map(r => r.type)))

  const stats = [
    { label: "Total ressources", value: resources.length, color: "bg-blue-500" },
    {
      label: "Disponibles",
      value: resources.filter((r) => r.disponible).length,
      color: "bg-green-500",
    },
    {
      label: "Indisponibles",
      value: resources.filter((r) => !r.disponible).length,
      color: "bg-red-500",
    },
    {
      label: "Capacité totale",
      value: resources.reduce((sum, r) => sum + r.capacite, 0),
      color: "bg-orange-500",
    },
  ]

  const handleAddResource = async (resourceData: CreateResourceRequest) => {
    try {
      const newResource = await resourcesApi.createResource(resourceData)
      setResources(prev => [...prev, newResource])
      toast({
        title: "Succès",
        description: "Ressource créée avec succès",
      })
      setIsAddModalOpen(false)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la création de la ressource'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const handleDeleteResource = async (resourceId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer cette ressource ?")) {
      return
    }

    try {
      await resourcesApi.deleteResource(resourceId)
      setResources(prev => prev.filter(resource => resource.id !== resourceId))
      toast({
        title: "Succès",
        description: "Ressource supprimée avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression de la ressource'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="resources" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Ressources" subtitle="Gérez l'inventaire et la disponibilité des équipements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des ressources...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="resources" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Ressources" subtitle="Gérez l'inventaire et la disponibilité des équipements" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={loadResources}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="resources" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Ressources" subtitle="Gérez l'inventaire et la disponibilité des équipements" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            {stats.map((stat, index) => (
              <Card key={index} className="p-4">
                <div className="flex items-center gap-3">
                  <div className={`${stat.color} w-12 h-12 rounded-lg flex items-center justify-center`}>
                    <Package className="w-6 h-6 text-white" />
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
                placeholder="Rechercher une ressource..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
              <Plus className="w-4 h-4" />
              Ajouter une ressource
            </Button>
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
                {type}
              </Button>
            ))}
          </div>

          {/* Resources Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredResources.map((resource) => {
              const StatusIcon = getStatusIcon(resource.disponible)
              const TypeIcon = getTypeIcon(resource.type)
              return (
                <Card key={resource.id} className="p-5 hover:shadow-lg transition-shadow">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <TypeIcon className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold">{resource.nom}</h3>
                        <p className="text-sm text-muted-foreground">{resource.code}</p>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Type</span>
                      <Badge className={`${getTypeColor(resource.type)} text-xs`}>{resource.type}</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Capacité</span>
                      <div className="flex items-center gap-1">
                        <Users className="w-3 h-3" />
                        <span className="text-sm font-medium">{resource.capacite}</span>
                      </div>
                    </div>
                    {resource.batiment && (
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Bâtiment</span>
                        <div className="flex items-center gap-1">
                          <Building className="w-3 h-3" />
                          <span className="text-sm font-medium">
                            {resource.batiment}
                            {resource.etage && ` - Étage ${resource.etage}`}
                          </span>
                        </div>
                      </div>
                    )}
                  </div>

                  <div className="flex items-center gap-2 pt-4 border-t mb-4">
                    <StatusIcon className="w-4 h-4" />
                    <Badge className={`${getStatusColor(resource.disponible)} text-xs flex-1`}>
                      {getStatusLabel(resource.disponible)}
                    </Badge>
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
                      onClick={() => handleDeleteResource(resource.id)}
                    >
                      <Trash2 className="w-3 h-3" />
                    </Button>
                  </div>
                </Card>
              )
            })}
          </div>
        </main>
      </div>

      <AddResourceModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} onSubmit={handleAddResource} />
    </div>
  )
}
