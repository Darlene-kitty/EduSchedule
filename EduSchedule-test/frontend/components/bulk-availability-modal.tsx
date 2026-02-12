"use client"

import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, Star, AlertCircle, CheckCircle2, Loader2 } from "lucide-react"
import { TimeSlotPicker } from "./time-slot-picker"
import { 
  CreateAvailabilityRequest, 
  DayOfWeek, 
  AvailabilityType,
  availabilityUtils 
} from "@/lib/api/teacher-availability"
import { useToast } from "@/hooks/use-toast"

interface BulkAvailabilityModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (requests: CreateAvailabilityRequest[]) => Promise<void>
  teacherId?: number
}

export function BulkAvailabilityModal({ 
  open, 
  onOpenChange, 
  onSubmit, 
  teacherId = 1 
}: BulkAvailabilityModalProps) {
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  
  const [selectedDays, setSelectedDays] = useState<DayOfWeek[]>([])
  const [selectedTimeSlots, setSelectedTimeSlots] = useState<string[]>([])
  const [availabilityType, setAvailabilityType] = useState<AvailabilityType>(AvailabilityType.AVAILABLE)
  const [recurring, setRecurring] = useState(true)
  const [priority, setPriority] = useState(2)
  const [notes, setNotes] = useState("")

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validation
    if (selectedDays.length === 0) {
      toast({
        title: "Erreur",
        description: "Veuillez sélectionner au moins un jour",
        variant: "destructive",
      })
      return
    }

    if (selectedTimeSlots.length === 0) {
      toast({
        title: "Erreur",
        description: "Veuillez sélectionner au moins un créneau horaire",
        variant: "destructive",
      })
      return
    }

    // Créer les requêtes pour chaque combinaison jour/créneau
    const requests: CreateAvailabilityRequest[] = []
    
    for (const day of selectedDays) {
      for (const startTime of selectedTimeSlots) {
        // Calculer l'heure de fin (1h après)
        const startMinutes = timeToMinutes(startTime)
        const endMinutes = startMinutes + 60
        const endHours = Math.floor(endMinutes / 60)
        const endMins = endMinutes % 60
        const endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
        
        requests.push({
          teacherId,
          dayOfWeek: day,
          startTime,
          endTime,
          availabilityType,
          recurring,
          priority,
          notes: notes || `Créneau ${availabilityUtils.getDayOfWeekLabel(day)} ${startTime}-${endTime}`
        })
      }
    }

    setLoading(true)
    try {
      await onSubmit(requests)
      onOpenChange(false)
      resetForm()
      
      toast({
        title: "Succès",
        description: `${requests.length} disponibilités créées avec succès`,
      })
    } catch (error) {
      // L'erreur est gérée par le composant parent
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setSelectedDays([])
    setSelectedTimeSlots([])
    setAvailabilityType(AvailabilityType.AVAILABLE)
    setRecurring(true)
    setPriority(2)
    setNotes("")
  }

  const timeToMinutes = (time: string): number => {
    const [hours, minutes] = time.split(':').map(Number)
    return hours * 60 + minutes
  }

  const toggleDay = (day: DayOfWeek) => {
    setSelectedDays(prev => 
      prev.includes(day) 
        ? prev.filter(d => d !== day)
        : [...prev, day]
    )
  }

  const selectWeekdays = () => {
    setSelectedDays([
      DayOfWeek.MONDAY,
      DayOfWeek.TUESDAY,
      DayOfWeek.WEDNESDAY,
      DayOfWeek.THURSDAY,
      DayOfWeek.FRIDAY
    ])
  }

  const selectAllDays = () => {
    setSelectedDays(Object.values(DayOfWeek))
  }

  const clearDays = () => {
    setSelectedDays([])
  }

  const getTypeIcon = (type: AvailabilityType) => {
    switch (type) {
      case AvailabilityType.AVAILABLE:
        return <CheckCircle2 className="w-4 h-4" />
      case AvailabilityType.PREFERRED:
        return <Star className="w-4 h-4" />
      case AvailabilityType.UNAVAILABLE:
        return <AlertCircle className="w-4 h-4" />
      default:
        return <Clock className="w-4 h-4" />
    }
  }

  const totalSlots = selectedDays.length * selectedTimeSlots.length

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Calendar className="w-5 h-5" />
            Ajouter des disponibilités en masse
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Sélection des jours */}
          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <Label>Jours de la semaine</Label>
              <div className="flex gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={selectWeekdays}
                >
                  Lun-Ven
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={selectAllDays}
                >
                  Tous
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={clearDays}
                >
                  Effacer
                </Button>
              </div>
            </div>
            
            <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-7 gap-2">
              {Object.values(DayOfWeek).map((day) => {
                const isSelected = selectedDays.includes(day)
                return (
                  <Button
                    key={day}
                    type="button"
                    variant={isSelected ? "default" : "outline"}
                    size="sm"
                    onClick={() => toggleDay(day)}
                    className={`
                      h-12 flex flex-col items-center justify-center
                      ${isSelected ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
                    `}
                  >
                    <span className="text-xs font-medium">
                      {availabilityUtils.getDayOfWeekLabel(day).slice(0, 3)}
                    </span>
                  </Button>
                )
              })}
            </div>
          </div>

          {/* Sélection des créneaux horaires */}
          <div className="space-y-3">
            <Label>Créneaux horaires (1h chaque)</Label>
            <TimeSlotPicker
              selectedSlots={selectedTimeSlots}
              onSlotsChange={setSelectedTimeSlots}
              maxSlots={10}
            />
          </div>

          {/* Configuration */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Type de disponibilité */}
            <div className="space-y-2">
              <Label htmlFor="availabilityType">Type de disponibilité</Label>
              <Select
                value={availabilityType}
                onValueChange={(value) => setAvailabilityType(value as AvailabilityType)}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.values(AvailabilityType).map((type) => (
                    <SelectItem key={type} value={type}>
                      <div className="flex items-center gap-2">
                        {getTypeIcon(type)}
                        {availabilityUtils.getAvailabilityTypeLabel(type)}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Priorité */}
            <div className="space-y-2">
              <Label htmlFor="priority">Priorité</Label>
              <Select
                value={priority.toString()}
                onValueChange={(value) => setPriority(parseInt(value))}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="1">
                    <div className="flex items-center gap-2">
                      <Star className="w-4 h-4 text-yellow-500" />
                      Priorité élevée
                    </div>
                  </SelectItem>
                  <SelectItem value="2">
                    <div className="flex items-center gap-2">
                      <CheckCircle2 className="w-4 h-4 text-green-500" />
                      Priorité normale
                    </div>
                  </SelectItem>
                  <SelectItem value="3">
                    <div className="flex items-center gap-2">
                      <AlertCircle className="w-4 h-4 text-orange-500" />
                      Priorité faible
                    </div>
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Options avancées */}
          <Card className="p-4 space-y-4">
            <h4 className="font-medium">Options</h4>
            
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <Label htmlFor="recurring">Récurrent</Label>
                <p className="text-sm text-muted-foreground">
                  Ces disponibilités se répètent chaque semaine
                </p>
              </div>
              <Switch
                id="recurring"
                checked={recurring}
                onCheckedChange={setRecurring}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="notes">Notes (optionnel)</Label>
              <Textarea
                id="notes"
                placeholder="Commentaires généraux pour toutes les disponibilités..."
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={2}
              />
            </div>
          </Card>

          {/* Aperçu */}
          {totalSlots > 0 && (
            <Card className="p-4 bg-muted/50">
              <h4 className="font-medium mb-3">Aperçu</h4>
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <Badge className={availabilityUtils.getAvailabilityTypeColor(availabilityType)}>
                    {getTypeIcon(availabilityType)}
                    <span className="ml-1">{availabilityUtils.getAvailabilityTypeLabel(availabilityType)}</span>
                  </Badge>
                  <Badge variant="outline">
                    {recurring ? "Récurrent" : "Ponctuel"}
                  </Badge>
                </div>
                <p className="text-sm text-muted-foreground">
                  <strong>{totalSlots} disponibilités</strong> seront créées :
                </p>
                <p className="text-sm text-muted-foreground">
                  • {selectedDays.length} jour{selectedDays.length > 1 ? 's' : ''} sélectionné{selectedDays.length > 1 ? 's' : ''}
                </p>
                <p className="text-sm text-muted-foreground">
                  • {selectedTimeSlots.length} créneau{selectedTimeSlots.length > 1 ? 'x' : ''} horaire{selectedTimeSlots.length > 1 ? 's' : ''} par jour
                </p>
              </div>
            </Card>
          )}

          {/* Actions */}
          <div className="flex justify-end gap-3">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={loading}
            >
              Annuler
            </Button>
            <Button 
              type="submit" 
              disabled={loading || totalSlots === 0} 
              className="bg-[#15803D] hover:bg-[#15803D]/90"
            >
              {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              Créer {totalSlots} disponibilité{totalSlots > 1 ? 's' : ''}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}