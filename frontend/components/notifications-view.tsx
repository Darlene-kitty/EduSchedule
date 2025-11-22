"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Bell, CheckCircle2, AlertTriangle, Info, Calendar, Users, BookOpen, Trash2, Check, X } from "lucide-react"

interface Notification {
  id: string
  type: "success" | "warning" | "info" | "error"
  title: string
  message: string
  time: string
  read: boolean
  category: "Cours" | "Réservation" | "Conflit" | "Système"
}

const notifications: Notification[] = [
  {
    id: "1",
    type: "warning",
    title: "Conflit détecté",
    message: "Conflit de réservation pour la salle A101 le lundi à 10h",
    time: "Il y a 5 min",
    read: false,
    category: "Conflit",
  },
  {
    id: "2",
    type: "success",
    title: "Cours ajouté",
    message: "Le cours de Mathématiques a été ajouté avec succès pour L1-G1",
    time: "Il y a 12 min",
    read: false,
    category: "Cours",
  },
  {
    id: "3",
    type: "info",
    title: "Nouvelle réservation",
    message: "Prof. Bernard a réservé l'Amphi A pour une conférence",
    time: "Il y a 23 min",
    read: true,
    category: "Réservation",
  },
  {
    id: "4",
    type: "success",
    title: "Utilisateur ajouté",
    message: "Prof. Dubois a été ajouté au système",
    time: "Il y a 1h",
    read: true,
    category: "Système",
  },
  {
    id: "5",
    type: "error",
    title: "Réservation annulée",
    message: "La réservation de la salle B203 a été annulée",
    time: "Il y a 2h",
    read: true,
    category: "Réservation",
  },
  {
    id: "6",
    type: "info",
    title: "Emploi du temps validé",
    message: "L'emploi du temps pour L2-G2 a été validé",
    time: "Il y a 3h",
    read: true,
    category: "Cours",
  },
  {
    id: "7",
    type: "warning",
    title: "Maintenance programmée",
    message: "Maintenance du système prévue dimanche de 2h à 4h",
    time: "Il y a 5h",
    read: true,
    category: "Système",
  },
]

export function NotificationsView() {
  const [filter, setFilter] = useState<string>("all")

  const filteredNotifications = notifications.filter((notif) => {
    if (filter === "all") return true
    if (filter === "unread") return !notif.read
    return notif.category === filter
  })

  const unreadCount = notifications.filter((n) => !n.read).length

  const getTypeIcon = (type: string) => {
    switch (type) {
      case "success":
        return CheckCircle2
      case "warning":
        return AlertTriangle
      case "error":
        return X
      case "info":
        return Info
      default:
        return Bell
    }
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case "success":
        return "bg-green-100 text-green-700"
      case "warning":
        return "bg-yellow-100 text-yellow-700"
      case "error":
        return "bg-red-100 text-red-700"
      case "info":
        return "bg-blue-100 text-blue-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getCategoryIcon = (category: string) => {
    switch (category) {
      case "Cours":
        return BookOpen
      case "Réservation":
        return Calendar
      case "Conflit":
        return AlertTriangle
      case "Système":
        return Users
      default:
        return Bell
    }
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="notifications" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Notifications" subtitle="Restez informé des événements et mises à jour importantes" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
            <Card className="p-5">
              <div className="flex items-center gap-3">
                <div className="bg-blue-500 p-3 rounded-lg">
                  <Bell className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="text-2xl font-bold">{notifications.length}</p>
                  <p className="text-sm text-muted-foreground">Total notifications</p>
                </div>
              </div>
            </Card>

            <Card className="p-5">
              <div className="flex items-center gap-3">
                <div className="bg-orange-500 p-3 rounded-lg">
                  <AlertTriangle className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="text-2xl font-bold">{unreadCount}</p>
                  <p className="text-sm text-muted-foreground">Non lues</p>
                </div>
              </div>
            </Card>

            <Card className="p-5">
              <div className="flex items-center gap-3">
                <div className="bg-green-500 p-3 rounded-lg">
                  <CheckCircle2 className="w-6 h-6 text-white" />
                </div>
                <div>
                  <p className="text-2xl font-bold">{notifications.length - unreadCount}</p>
                  <p className="text-sm text-muted-foreground">Lues</p>
                </div>
              </div>
            </Card>
          </div>

          {/* Actions */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="flex gap-2 flex-wrap flex-1">
              {["all", "unread", "Cours", "Réservation", "Conflit", "Système"].map((category) => (
                <Button
                  key={category}
                  variant={filter === category ? "default" : "outline"}
                  size="sm"
                  onClick={() => setFilter(category)}
                  className={filter === category ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
                >
                  {category === "all" ? "Toutes" : category === "unread" ? "Non lues" : category}
                </Button>
              ))}
            </div>
            <Button variant="outline" className="gap-2 bg-transparent">
              <Check className="w-4 h-4" />
              Tout marquer comme lu
            </Button>
          </div>

          {/* Notifications List */}
          <div className="space-y-3">
            {filteredNotifications.length === 0 ? (
              <Card className="p-12 text-center">
                <Bell className="w-16 h-16 text-gray-300 mx-auto mb-4" />
                <h3 className="text-xl font-semibold mb-2">Aucune notification</h3>
                <p className="text-muted-foreground">Vous êtes à jour avec toutes vos notifications.</p>
              </Card>
            ) : (
              filteredNotifications.map((notification) => {
                const TypeIcon = getTypeIcon(notification.type)
                const CategoryIcon = getCategoryIcon(notification.category)

                return (
                  <Card
                    key={notification.id}
                    className={`p-5 hover:shadow-md transition-shadow ${!notification.read ? "border-l-4 border-l-[#15803D]" : ""}`}
                  >
                    <div className="flex items-start gap-4">
                      <div className={`${getTypeColor(notification.type)} p-3 rounded-lg`}>
                        <TypeIcon className="w-5 h-5" />
                      </div>

                      <div className="flex-1">
                        <div className="flex items-start justify-between mb-2">
                          <div className="flex items-center gap-2">
                            <h3 className="font-semibold">{notification.title}</h3>
                            {!notification.read && <Badge className="bg-[#15803D] text-white text-xs">Nouveau</Badge>}
                          </div>
                          <Badge variant="outline" className="text-xs">
                            <CategoryIcon className="w-3 h-3 mr-1" />
                            {notification.category}
                          </Badge>
                        </div>

                        <p className="text-sm text-muted-foreground mb-2">{notification.message}</p>
                        <p className="text-xs text-muted-foreground">{notification.time}</p>
                      </div>

                      <div className="flex gap-2">
                        {!notification.read && (
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <Check className="w-4 h-4" />
                          </Button>
                        )}
                        <Button variant="ghost" size="icon" className="h-8 w-8 text-red-600 hover:text-red-700">
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </div>
                  </Card>
                )
              })
            )}
          </div>
        </main>
      </div>
    </div>
  )
}
