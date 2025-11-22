"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ResolveConflictModal } from "./resolve-conflict-modal"
import { AlertTriangle, Calendar, Clock, MapPin, Users, CheckCircle, XCircle, AlertCircle } from "lucide-react"

interface Conflict {
  id: string
  type: "Salle" | "Professeur" | "Groupe" | "Ressource"
  severity: "Critique" | "Élevée" | "Moyenne"
  description: string
  details: {
    course1: string
    course2: string
    time: string
    date: string
    location?: string
    professor?: string
    group?: string
  }
  status: "Non résolu" | "En cours" | "Résolu"
  detectedAt: string
}

const conflicts: Conflict[] = [
  {
    id: "1",
    type: "Salle",
    severity: "Critique",
    description: "Conflit de réservation de salle",
    details: {
      course1: "Mathématiques (L1-G1)",
      course2: "Algèbre (L2-G2)",
      time: "10:00-12:00",
      date: "2025-10-21",
      location: "A101",
    },
    status: "Non résolu",
    detectedAt: "Il y a 12 min",
  },
  {
    id: "2",
    type: "Professeur",
    severity: "Critique",
    description: "Professeur assigné à deux cours simultanés",
    details: {
      course1: "Physique (L1-G1)",
      course2: "TP Physique (L1-G2)",
      time: "14:00-16:00",
      date: "2025-10-20",
      professor: "Dr. Laurent",
    },
    status: "En cours",
    detectedAt: "Il y a 1h",
  },
  {
    id: "3",
    type: "Groupe",
    severity: "Élevée",
    description: "Groupe d'étudiants avec cours simultanés",
    details: {
      course1: "Analyse (L2-G1)",
      course2: "Statistiques (L2-G1)",
      time: "08:00-10:00",
      date: "2025-10-22",
      group: "L2-G1",
    },
    status: "Non résolu",
    detectedAt: "Il y a 2h",
  },
]

export function ConflictsView() {
  const [selectedStatus, setSelectedStatus] = useState<string>("all")
  const [isResolveModalOpen, setIsResolveModalOpen] = useState(false)
  const [selectedConflict, setSelectedConflict] = useState<any>(null)

  const filteredConflicts = conflicts.filter((conflict) => {
    if (selectedStatus === "all") return true
    return conflict.status === selectedStatus
  })

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case "Critique":
        return "bg-red-100 text-red-700 border-red-200"
      case "Élevée":
        return "bg-orange-100 text-orange-700 border-orange-200"
      case "Moyenne":
        return "bg-yellow-100 text-yellow-700 border-yellow-200"
      default:
        return "bg-gray-100 text-gray-700 border-gray-200"
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case "Salle":
        return "bg-blue-100 text-blue-700"
      case "Professeur":
        return "bg-purple-100 text-purple-700"
      case "Groupe":
        return "bg-green-100 text-green-700"
      case "Ressource":
        return "bg-cyan-100 text-cyan-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case "Résolu":
        return "bg-green-100 text-green-700"
      case "En cours":
        return "bg-yellow-100 text-yellow-700"
      case "Non résolu":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const stats = [
    {
      label: "Total conflits",
      value: conflicts.length,
      icon: AlertTriangle,
      color: "bg-red-500",
    },
    {
      label: "Non résolus",
      value: conflicts.filter((c) => c.status === "Non résolu").length,
      icon: XCircle,
      color: "bg-orange-500",
    },
    {
      label: "En cours",
      value: conflicts.filter((c) => c.status === "En cours").length,
      icon: AlertCircle,
      color: "bg-yellow-500",
    },
    {
      label: "Résolus",
      value: conflicts.filter((c) => c.status === "Résolu").length,
      icon: CheckCircle,
      color: "bg-green-500",
    },
  ]

  const handleResolveClick = (conflict: any) => {
    setSelectedConflict(conflict)
    setIsResolveModalOpen(true)
  }

  const handleResolve = (resolution: any) => {
    console.log("[v0] Resolving conflict:", resolution)
    // Handle conflict resolution logic here
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="conflicts" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Conflits" subtitle="Détectez et résolvez les conflits d'emploi du temps" />

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

          {/* Filter Tabs */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            {["all", "Non résolu", "En cours", "Résolu"].map((status) => (
              <Button
                key={status}
                variant={selectedStatus === status ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedStatus(status)}
                className={selectedStatus === status ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {status === "all" ? "Tous" : status}
              </Button>
            ))}
          </div>

          {/* Conflicts List */}
          <div className="space-y-4">
            {filteredConflicts.length === 0 ? (
              <Card className="p-12 text-center">
                <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
                <h3 className="text-xl font-semibold mb-2">Aucun conflit détecté</h3>
                <p className="text-muted-foreground">Tous les conflits ont été résolus ou aucun conflit n'existe.</p>
              </Card>
            ) : (
              filteredConflicts.map((conflict) => (
                <Card key={conflict.id} className={`p-6 border-l-4 ${getSeverityColor(conflict.severity)}`}>
                  <div className="flex items-start justify-between mb-4">
                    <div className="flex items-start gap-4 flex-1">
                      <div className="bg-red-100 p-3 rounded-lg">
                        <AlertTriangle className="w-6 h-6 text-red-600" />
                      </div>
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-2">
                          <h3 className="font-semibold text-lg">{conflict.description}</h3>
                          <Badge className={`${getTypeColor(conflict.type)} text-xs`}>{conflict.type}</Badge>
                          <Badge className={`${getSeverityColor(conflict.severity)} text-xs`}>
                            {conflict.severity}
                          </Badge>
                        </div>
                        <p className="text-sm text-muted-foreground mb-3">Détecté {conflict.detectedAt}</p>

                        <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                            <div>
                              <p className="text-xs text-muted-foreground mb-1">Cours 1</p>
                              <p className="text-sm font-medium">{conflict.details.course1}</p>
                            </div>
                            <div>
                              <p className="text-xs text-muted-foreground mb-1">Cours 2</p>
                              <p className="text-sm font-medium">{conflict.details.course2}</p>
                            </div>
                          </div>

                          <div className="flex flex-wrap gap-4 pt-2 text-sm">
                            <div className="flex items-center gap-2">
                              <Calendar className="w-4 h-4 text-muted-foreground" />
                              <span>
                                {new Date(conflict.details.date).toLocaleDateString("fr-FR", {
                                  day: "numeric",
                                  month: "long",
                                  year: "numeric",
                                })}
                              </span>
                            </div>
                            <div className="flex items-center gap-2">
                              <Clock className="w-4 h-4 text-muted-foreground" />
                              <span>{conflict.details.time}</span>
                            </div>
                            {conflict.details.location && (
                              <div className="flex items-center gap-2">
                                <MapPin className="w-4 h-4 text-muted-foreground" />
                                <span>{conflict.details.location}</span>
                              </div>
                            )}
                            {conflict.details.professor && (
                              <div className="flex items-center gap-2">
                                <Users className="w-4 h-4 text-muted-foreground" />
                                <span>{conflict.details.professor}</span>
                              </div>
                            )}
                            {conflict.details.group && (
                              <div className="flex items-center gap-2">
                                <Users className="w-4 h-4 text-muted-foreground" />
                                <span>{conflict.details.group}</span>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    </div>

                    <Badge className={`${getStatusColor(conflict.status)} text-xs ml-4`}>{conflict.status}</Badge>
                  </div>

                  <div className="flex gap-2 mt-4">
                    <Button
                      size="sm"
                      className="bg-[#15803D] hover:bg-[#15803D]/90"
                      onClick={() => handleResolveClick(conflict)}
                    >
                      Résoudre automatiquement
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="bg-transparent"
                      onClick={() => handleResolveClick(conflict)}
                    >
                      Modifier manuellement
                    </Button>
                    <Button variant="outline" size="sm" className="text-red-600 hover:text-red-700 bg-transparent">
                      Ignorer
                    </Button>
                  </div>
                </Card>
              ))
            )}
          </div>
        </main>
      </div>

      {/* Resolve Conflict Modal */}
      <ResolveConflictModal
        open={isResolveModalOpen}
        onOpenChange={setIsResolveModalOpen}
        conflict={selectedConflict}
        onResolve={handleResolve}
      />
    </div>
  )
}
