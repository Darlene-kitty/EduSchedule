"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { ChevronLeft, ChevronRight, CalendarIcon, Clock, MapPin, User } from "lucide-react"

interface CalendarEvent {
  id: string
  title: string
  date: string
  startTime: string
  endTime: string
  room: string
  professor: string
  group: string
  type: "Cours" | "Examen" | "Réunion" | "Événement"
  color: string
}

const events: CalendarEvent[] = [
  {
    id: "1",
    title: "Mathématiques",
    date: "2025-10-20",
    startTime: "08:00",
    endTime: "10:00",
    room: "A101",
    professor: "Dr. Martin",
    group: "L1-G1",
    type: "Cours",
    color: "blue",
  },
  {
    id: "2",
    title: "Physique",
    date: "2025-10-20",
    startTime: "10:00",
    endTime: "12:00",
    room: "B203",
    professor: "Dr. Laurent",
    group: "L1-G1",
    type: "Cours",
    color: "green",
  },
  {
    id: "3",
    title: "Examen Algèbre",
    date: "2025-10-21",
    startTime: "09:00",
    endTime: "11:00",
    room: "C301",
    professor: "Prof. Dubois",
    group: "L2-G2",
    type: "Examen",
    color: "red",
  },
  {
    id: "4",
    title: "Réunion pédagogique",
    date: "2025-10-22",
    startTime: "14:00",
    endTime: "16:00",
    room: "Salle réunion",
    professor: "Admin Système",
    group: "Tous",
    type: "Réunion",
    color: "purple",
  },
  {
    id: "5",
    title: "Conférence IA",
    date: "2025-10-23",
    startTime: "10:00",
    endTime: "12:00",
    room: "Amphi A",
    professor: "Prof. Richard",
    group: "M1",
    type: "Événement",
    color: "orange",
  },
]

export function CalendarView() {
  const [currentDate, setCurrentDate] = useState(new Date(2025, 9, 18)) // October 18, 2025
  const [selectedDate, setSelectedDate] = useState<Date | null>(null)

  const daysInMonth = new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 0).getDate()
  const firstDayOfMonth = new Date(currentDate.getFullYear(), currentDate.getMonth(), 1).getDay()
  const adjustedFirstDay = firstDayOfMonth === 0 ? 6 : firstDayOfMonth - 1

  const monthName = currentDate.toLocaleDateString("fr-FR", { month: "long", year: "numeric" })

  const getEventsForDate = (day: number) => {
    const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`
    return events.filter((event) => event.date === dateStr)
  }

  const getTypeColor = (type: string) => {
    switch (type) {
      case "Cours":
        return "bg-blue-100 text-blue-700"
      case "Examen":
        return "bg-red-100 text-red-700"
      case "Réunion":
        return "bg-purple-100 text-purple-700"
      case "Événement":
        return "bg-orange-100 text-orange-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const selectedDateEvents = selectedDate ? getEventsForDate(selectedDate.getDate()) : []

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="calendar" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Calendrier" subtitle="Visualisez tous les événements et cours planifiés" />

        <main className="flex-1 overflow-y-auto p-6">
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Calendar */}
            <div className="lg:col-span-2">
              <Card className="p-6">
                {/* Calendar Header */}
                <div className="flex items-center justify-between mb-6">
                  <h2 className="text-xl font-bold capitalize">{monthName}</h2>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1))}
                    >
                      <ChevronLeft className="w-4 h-4" />
                    </Button>
                    <Button variant="outline" size="sm">
                      Aujourd'hui
                    </Button>
                    <Button
                      variant="outline"
                      size="icon"
                      onClick={() => setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1))}
                    >
                      <ChevronRight className="w-4 h-4" />
                    </Button>
                  </div>
                </div>

                {/* Calendar Grid */}
                <div className="grid grid-cols-7 gap-2">
                  {["Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"].map((day) => (
                    <div key={day} className="text-center text-sm font-semibold text-muted-foreground py-2">
                      {day}
                    </div>
                  ))}

                  {Array.from({ length: adjustedFirstDay }).map((_, index) => (
                    <div key={`empty-${index}`} className="aspect-square" />
                  ))}

                  {Array.from({ length: daysInMonth }).map((_, index) => {
                    const day = index + 1
                    const dayEvents = getEventsForDate(day)
                    const isSelected =
                      selectedDate?.getDate() === day && selectedDate?.getMonth() === currentDate.getMonth()
                    const isToday = day === 18 && currentDate.getMonth() === 9

                    return (
                      <button
                        key={day}
                        onClick={() =>
                          setSelectedDate(new Date(currentDate.getFullYear(), currentDate.getMonth(), day))
                        }
                        className={`aspect-square p-2 rounded-lg border-2 transition-colors ${
                          isSelected
                            ? "border-[#15803D] bg-green-50"
                            : isToday
                              ? "border-blue-500 bg-blue-50"
                              : "border-gray-200 hover:border-gray-300"
                        }`}
                      >
                        <div className="text-sm font-semibold mb-1">{day}</div>
                        <div className="space-y-1">
                          {dayEvents.slice(0, 2).map((event) => (
                            <div key={event.id} className="w-full h-1 bg-[#15803D] rounded" />
                          ))}
                          {dayEvents.length > 2 && (
                            <div className="text-xs text-muted-foreground">+{dayEvents.length - 2}</div>
                          )}
                        </div>
                      </button>
                    )
                  })}
                </div>
              </Card>
            </div>

            {/* Events Sidebar */}
            <div className="space-y-4">
              <Card className="p-5">
                <div className="flex items-center gap-2 mb-4">
                  <CalendarIcon className="w-5 h-5 text-[#15803D]" />
                  <h3 className="font-semibold">
                    {selectedDate
                      ? selectedDate.toLocaleDateString("fr-FR", {
                          weekday: "long",
                          day: "numeric",
                          month: "long",
                        })
                      : "Sélectionnez une date"}
                  </h3>
                </div>

                {selectedDateEvents.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-8">Aucun événement ce jour</p>
                ) : (
                  <div className="space-y-3">
                    {selectedDateEvents.map((event) => (
                      <div key={event.id} className="p-3 bg-gray-50 rounded-lg">
                        <div className="flex items-start justify-between mb-2">
                          <h4 className="font-semibold text-sm">{event.title}</h4>
                          <Badge className={`${getTypeColor(event.type)} text-xs`}>{event.type}</Badge>
                        </div>
                        <div className="space-y-1 text-xs text-muted-foreground">
                          <div className="flex items-center gap-2">
                            <Clock className="w-3 h-3" />
                            <span>
                              {event.startTime} - {event.endTime}
                            </span>
                          </div>
                          <div className="flex items-center gap-2">
                            <MapPin className="w-3 h-3" />
                            <span>{event.room}</span>
                          </div>
                          <div className="flex items-center gap-2">
                            <User className="w-3 h-3" />
                            <span>{event.professor}</span>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </Card>

              <Card className="p-5">
                <h3 className="font-semibold mb-4">Légende</h3>
                <div className="space-y-2">
                  {["Cours", "Examen", "Réunion", "Événement"].map((type) => (
                    <div key={type} className="flex items-center gap-2">
                      <div className={`w-3 h-3 rounded ${getTypeColor(type).split(" ")[0]}`} />
                      <span className="text-sm">{type}</span>
                    </div>
                  ))}
                </div>
              </Card>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
