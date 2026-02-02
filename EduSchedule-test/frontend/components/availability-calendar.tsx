"use client"

import { useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { 
  Calendar, 
  Clock, 
  Star, 
  AlertCircle, 
  CheckCircle2, 
  Edit, 
  Trash2,
  Plus 
} from "lucide-react"
import { 
  TeacherAvailability, 
  DayOfWeek, 
  AvailabilityType,
  availabilityUtils 
} from "@/lib/api/teacher-availability"

interface AvailabilityCalendarProps {
  availabilities: TeacherAvailability[]
  onEdit: (availability: TeacherAvailability) => void
  onDelete: (id: number) => void
  onAddNew: (dayOfWeek: DayOfWeek) => void
}

export function AvailabilityCalendar({ 
  availabilities, 
  onEdit, 
  onDelete, 
  onAddNew 
}: AvailabilityCalendarProps) {
  
  const daysOfWeek = [
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY,
    DayOfWeek.SUNDAY,
  ]

  const timeSlots = [
    "08:00", "09:00", "10:00", "11:00", "12:00", 
    "13:00", "14:00", "15:00", "16:00", "17:00", "18:00"
  ]

  const getAvailabilitiesForDay = (day: DayOfWeek) => {
    return availabilities
      .filter(a => a.dayOfWeek === day)
      .sort((a, b) => a.startTime.localeCompare(b.startTime))
  }

  const getTypeIcon = (type: AvailabilityType) => {
    switch (type) {
      case AvailabilityType.AVAILABLE:
        return <CheckCircle2 className="w-3 h-3" />
      case AvailabilityType.PREFERRED:
        return <Star className="w-3 h-3" />
      case AvailabilityType.UNAVAILABLE:
        return <AlertCircle className="w-3 h-3" />
      default:
        return <Clock className="w-3 h-3" />
    }
  }

  const formatTimeRange = (startTime: string, endTime: string) => {
    return `${startTime.slice(0, 5)} - ${endTime.slice(0, 5)}`
  }

  return (
    <div className="space-y-4">
      {/* Vue calendrier hebdomadaire */}
      <div className="grid grid-cols-1 lg:grid-cols-7 gap-4">
        {daysOfWeek.map((day) => {
          const dayAvailabilities = getAvailabilitiesForDay(day)
          
          return (
            <Card key={day} className="p-4">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold text-sm">
                  {availabilityUtils.getDayOfWeekLabel(day)}
                </h3>
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => onAddNew(day)}
                  className="h-6 w-6 p-0"
                >
                  <Plus className="w-3 h-3" />
                </Button>
              </div>

              <div className="space-y-2">
                {dayAvailabilities.length === 0 ? (
                  <div className="text-center py-8 text-muted-foreground">
                    <Calendar className="w-8 h-8 mx-auto mb-2 opacity-50" />
                    <p className="text-xs">Aucune disponibilité</p>
                    <Button
                      size="sm"
                      variant="ghost"
                      onClick={() => onAddNew(day)}
                      className="mt-2 text-xs"
                    >
                      Ajouter
                    </Button>
                  </div>
                ) : (
                  dayAvailabilities.map((availability) => (
                    <div
                      key={availability.id}
                      className="group relative p-3 rounded-lg border hover:shadow-sm transition-shadow"
                    >
                      <div className="flex items-start justify-between mb-2">
                        <Badge 
                          className={`${availabilityUtils.getAvailabilityTypeColor(availability.availabilityType)} text-xs`}
                        >
                          {getTypeIcon(availability.availabilityType)}
                          <span className="ml-1">
                            {availabilityUtils.getAvailabilityTypeLabel(availability.availabilityType)}
                          </span>
                        </Badge>
                        
                        <div className="opacity-0 group-hover:opacity-100 transition-opacity flex gap-1">
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => onEdit(availability)}
                            className="h-6 w-6 p-0"
                          >
                            <Edit className="w-3 h-3" />
                          </Button>
                          <Button
                            size="sm"
                            variant="ghost"
                            onClick={() => onDelete(availability.id)}
                            className="h-6 w-6 p-0 text-red-600 hover:text-red-700"
                          >
                            <Trash2 className="w-3 h-3" />
                          </Button>
                        </div>
                      </div>

                      <div className="flex items-center gap-1 text-xs text-muted-foreground mb-1">
                        <Clock className="w-3 h-3" />
                        {formatTimeRange(availability.startTime, availability.endTime)}
                      </div>

                      {availability.priority && (
                        <div className="flex items-center gap-1 text-xs">
                          {availability.priority === 1 && (
                            <>
                              <Star className="w-3 h-3 text-yellow-500" />
                              <span className="text-yellow-600">Priorité élevée</span>
                            </>
                          )}
                          {availability.priority === 2 && (
                            <>
                              <CheckCircle2 className="w-3 h-3 text-green-500" />
                              <span className="text-green-600">Priorité normale</span>
                            </>
                          )}
                          {availability.priority === 3 && (
                            <>
                              <AlertCircle className="w-3 h-3 text-orange-500" />
                              <span className="text-orange-600">Priorité faible</span>
                            </>
                          )}
                        </div>
                      )}

                      {availability.notes && (
                        <p className="text-xs text-muted-foreground mt-2 truncate">
                          {availability.notes}
                        </p>
                      )}

                      {!availability.recurring && (
                        <Badge variant="outline" className="mt-2 text-xs">
                          Ponctuel
                        </Badge>
                      )}
                    </div>
                  ))
                )}
              </div>
            </Card>
          )
        })}
      </div>

      {/* Légende */}
      <Card className="p-4">
        <h4 className="font-medium mb-3">Légende</h4>
        <div className="flex flex-wrap gap-4">
          {Object.values(AvailabilityType).map((type) => (
            <div key={type} className="flex items-center gap-2">
              <Badge className={`${availabilityUtils.getAvailabilityTypeColor(type)} text-xs`}>
                {getTypeIcon(type)}
                <span className="ml-1">{availabilityUtils.getAvailabilityTypeLabel(type)}</span>
              </Badge>
            </div>
          ))}
        </div>
      </Card>
    </div>
  )
}