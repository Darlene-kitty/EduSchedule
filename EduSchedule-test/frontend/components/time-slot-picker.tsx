"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Clock, CheckCircle2 } from "lucide-react"

interface TimeSlot {
  start: string
  end: string
  label: string
}

interface TimeSlotPickerProps {
  selectedSlots: string[]
  onSlotsChange: (slots: string[]) => void
  disabled?: boolean
  maxSlots?: number
}

export function TimeSlotPicker({ 
  selectedSlots, 
  onSlotsChange, 
  disabled = false,
  maxSlots = 10 
}: TimeSlotPickerProps) {
  
  // Créneaux d'1 heure de 8h à 18h
  const timeSlots: TimeSlot[] = [
    { start: "08:00", end: "09:00", label: "08h-09h" },
    { start: "09:00", end: "10:00", label: "09h-10h" },
    { start: "10:00", end: "11:00", label: "10h-11h" },
    { start: "11:00", end: "12:00", label: "11h-12h" },
    { start: "12:00", end: "13:00", label: "12h-13h" },
    { start: "13:00", end: "14:00", label: "13h-14h" },
    { start: "14:00", end: "15:00", label: "14h-15h" },
    { start: "15:00", end: "16:00", label: "15h-16h" },
    { start: "16:00", end: "17:00", label: "16h-17h" },
    { start: "17:00", end: "18:00", label: "17h-18h" },
  ]

  const toggleSlot = (startTime: string) => {
    if (disabled) return

    const isSelected = selectedSlots.includes(startTime)
    
    if (isSelected) {
      // Désélectionner
      onSlotsChange(selectedSlots.filter(slot => slot !== startTime))
    } else {
      // Sélectionner (si pas de limite ou limite non atteinte)
      if (selectedSlots.length < maxSlots) {
        onSlotsChange([...selectedSlots, startTime])
      }
    }
  }

  const selectMorningSlots = () => {
    const morningSlots = timeSlots
      .filter(slot => slot.start < "12:00")
      .map(slot => slot.start)
    onSlotsChange(morningSlots)
  }

  const selectAfternoonSlots = () => {
    const afternoonSlots = timeSlots
      .filter(slot => slot.start >= "14:00")
      .map(slot => slot.start)
    onSlotsChange(afternoonSlots)
  }

  const selectAllSlots = () => {
    const allSlots = timeSlots.map(slot => slot.start)
    onSlotsChange(allSlots)
  }

  const clearAllSlots = () => {
    onSlotsChange([])
  }

  return (
    <Card className="p-4">
      <div className="space-y-4">
        {/* Actions rapides */}
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={selectMorningSlots}
            disabled={disabled}
          >
            Matinée (8h-12h)
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={selectAfternoonSlots}
            disabled={disabled}
          >
            Après-midi (14h-18h)
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={selectAllSlots}
            disabled={disabled}
          >
            Toute la journée
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={clearAllSlots}
            disabled={disabled}
          >
            Effacer tout
          </Button>
        </div>

        {/* Grille des créneaux */}
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
          {timeSlots.map((slot) => {
            const isSelected = selectedSlots.includes(slot.start)
            const isLunchTime = slot.start >= "12:00" && slot.start < "14:00"
            
            return (
              <Button
                key={slot.start}
                type="button"
                variant={isSelected ? "default" : "outline"}
                size="sm"
                onClick={() => toggleSlot(slot.start)}
                disabled={disabled || isLunchTime}
                className={`
                  relative h-12 flex flex-col items-center justify-center
                  ${isSelected ? "bg-[#15803D] hover:bg-[#15803D]/90 text-white" : ""}
                  ${isLunchTime ? "opacity-50 cursor-not-allowed" : ""}
                `}
              >
                {isSelected && (
                  <CheckCircle2 className="absolute top-1 right-1 w-3 h-3" />
                )}
                <Clock className="w-3 h-3 mb-1" />
                <span className="text-xs font-medium">{slot.label}</span>
                {isLunchTime && (
                  <span className="text-xs opacity-75">Pause</span>
                )}
              </Button>
            )
          })}
        </div>

        {/* Résumé */}
        <div className="flex items-center justify-between text-sm text-muted-foreground">
          <span>
            {selectedSlots.length} créneau{selectedSlots.length > 1 ? 'x' : ''} sélectionné{selectedSlots.length > 1 ? 's' : ''}
          </span>
          {maxSlots && (
            <span>
              Maximum : {maxSlots} créneaux
            </span>
          )}
        </div>

        {/* Créneaux sélectionnés */}
        {selectedSlots.length > 0 && (
          <div className="space-y-2">
            <h4 className="text-sm font-medium">Créneaux sélectionnés :</h4>
            <div className="flex flex-wrap gap-1">
              {selectedSlots
                .sort()
                .map((startTime) => {
                  const slot = timeSlots.find(s => s.start === startTime)
                  return (
                    <Badge
                      key={startTime}
                      variant="secondary"
                      className="text-xs"
                    >
                      {slot?.label}
                    </Badge>
                  )
                })}
            </div>
          </div>
        )}
      </div>
    </Card>
  )
}