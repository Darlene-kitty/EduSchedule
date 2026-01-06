"use client"

import { useState } from "react"
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
} from "lucide-react"
import { AddResourceModal } from "./add-resource-modal"

interface Resource {
  id: string
  name: string
  type: string
  category: "Électronique" | "Mobilier" | "Audiovisuel" | "Informatique"
  location: string
  status: "Disponible" | "En utilisation" | "En maintenance" | "Hors service"
  quantity: number
  icon: any
}

const resources: Resource[] = [
  {
    id: "1",
    name: "Projecteur HD",
    type: "Projecteur",
    category: "Audiovisuel",
    location: "A101",
    status: "Disponible",
    quantity: 3,
    icon: Projector,
  },
  {
    id: "2",
    name: "Ordinateur portable",
    type: "Laptop Dell",
    category: "Informatique",
    location: "Lab B1",
    status: "En utilisation",
    quantity: 15,
    icon: Laptop,
  },
  {
    id: "3",
    name: "Écran interactif",
    type: "Smart Board",
    category: "Électronique",
    location: "A103",
    status: "Disponible",
    quantity: 2,
    icon: Monitor,
  },
  {
    id: "4",
    name: "Imprimante laser",
    type: "HP LaserJet",
    category: "Informatique",
    location: "Bureau admin",
    status: "Disponible",
    quantity: 4,
    icon: Printer,
  },
  {
    id: "5",
    name: "Microphone sans fil",
    type: "Micro HF",
    category: "Audiovisuel",
    location: "Amphi A",
    status: "En utilisation",
    quantity: 6,
    icon: Mic,
  },
  {
    id: "6",
    name: "Caméra de visioconférence",
    type: "Logitech 4K",
    category: "Audiovisuel",
    location: "Salle réunion",
    status: "Disponible",
    quantity: 2,
    icon: Camera,
  },
  {
    id: "7",
    name: "Routeur WiFi",
    type: "TP-Link AC1750",
    category: "Informatique",
    location: "Bâtiment A",
    status: "En utilisation",
    quantity: 8,
    icon: Wifi,
  },
  {
    id: "8",
    name: "Tableau blanc mobile",
    type: "Tableau 120x90",
    category: "Mobilier",
    location: "C301",
    status: "Disponible",
    quantity: 5,
    icon: Package,
  },
  {
    id: "9",
    name: "Projecteur 4K",
    type: "Epson Pro",
    category: "Audiovisuel",
    location: "Amphi A",
    status: "En maintenance",
    quantity: 1,
    icon: Projector,
  },
  {
    id: "10",
    name: "Ordinateur de bureau",
    type: 'iMac 27"',
    category: "Informatique",
    location: "Lab B1",
    status: "Hors service",
    quantity: 1,
    icon: Monitor,
  },
]

export function ResourcesView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedCategory, setSelectedCategory] = useState<string>("all")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const filteredResources = resources.filter((resource) => {
    const matchesSearch =
      resource.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      resource.type.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesCategory = selectedCategory === "all" || resource.category === selectedCategory
    return matchesSearch && matchesCategory
  })

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Disponible":
        return "bg-green-100 text-green-700"
      case "En utilisation":
        return "bg-blue-100 text-blue-700"
      case "En maintenance":
        return "bg-yellow-100 text-yellow-700"
      case "Hors service":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case "Disponible":
        return CheckCircle2
      case "En utilisation":
        return AlertCircle
      case "En maintenance":
        return AlertCircle
      case "Hors service":
        return XCircle
      default:
        return AlertCircle
    }
  }

  const getCategoryColor = (category: string) => {
    switch (category) {
      case "Électronique":
        return "bg-purple-100 text-purple-700"
      case "Mobilier":
        return "bg-orange-100 text-orange-700"
      case "Audiovisuel":
        return "bg-blue-100 text-blue-700"
      case "Informatique":
        return "bg-cyan-100 text-cyan-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const stats = [
    { label: "Total ressources", value: resources.length, color: "bg-blue-500" },
    {
      label: "Disponibles",
      value: resources.filter((r) => r.status === "Disponible").length,
      color: "bg-green-500",
    },
    {
      label: "En utilisation",
      value: resources.filter((r) => r.status === "En utilisation").length,
      color: "bg-yellow-500",
    },
    {
      label: "En maintenance",
      value: resources.filter((r) => r.status === "En maintenance").length,
      color: "bg-orange-500",
    },
  ]

  const handleAddResource = (resourceData: any) => {
    console.log("[v0] New resource added:", resourceData)
    // Here you would typically add the resource to your state/database
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

          {/* Category Filter */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            {["all", "Électronique", "Mobilier", "Audiovisuel", "Informatique"].map((category) => (
              <Button
                key={category}
                variant={selectedCategory === category ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedCategory(category)}
                className={selectedCategory === category ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {category === "all" ? "Toutes" : category}
              </Button>
            ))}
          </div>

          {/* Resources Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredResources.map((resource) => {
              const StatusIcon = getStatusIcon(resource.status)
              return (
                <Card key={resource.id} className="p-5 hover:shadow-lg transition-shadow">
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-center gap-3">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <resource.icon className="w-6 h-6 text-white" />
                      </div>
                      <div>
                        <h3 className="font-semibold">{resource.name}</h3>
                        <p className="text-sm text-muted-foreground">{resource.type}</p>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-2 mb-4">
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Catégorie</span>
                      <Badge className={`${getCategoryColor(resource.category)} text-xs`}>{resource.category}</Badge>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Localisation</span>
                      <span className="text-sm font-medium">{resource.location}</span>
                    </div>
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Quantité</span>
                      <span className="text-sm font-medium">{resource.quantity}</span>
                    </div>
                  </div>

                  <div className="flex items-center gap-2 pt-4 border-t mb-4">
                    <StatusIcon className="w-4 h-4" />
                    <Badge className={`${getStatusColor(resource.status)} text-xs flex-1`}>{resource.status}</Badge>
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
