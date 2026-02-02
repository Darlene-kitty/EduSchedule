"use client"

import { useState, useEffect } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"
import { Switch } from "@/components/ui/switch"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Calendar, Clock, Star, AlertCircle, CheckCircle2, Loader2 } from "lucide-react"
import { 
  CreateAvailabilityRequest, 
  DayOfWeek, 
  AvailabilityType,
  availabilityUtils 
} from "@/lib/api/teacher-availability"
import { useToast } from "@/hooks/use-toast"

interface AddAvailabilityModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (data: CreateAvailabilityRequest) => Promise<void>
  editingData?: CreateAvailabilityRequest | null
  teacherId?: number
}

export function AddAvailabilityModal({ 
  open, 
  onOpenChange, 
  onSubmit, 
  editingData,
  teacherId = 1 
}: AddAvailabilityModalProps) {
  const { toast } = useToast()
  const [loading, setLoading] = useState(false)
  
  const [formData, setFormData] = useState<CreateAvailabilityRequest>({
    teacherId: teacherId,
    dayOfWeek: DayOfWeek.MONDAY,
    startTime: "08:00",
    endTime: "09:00", // Créneau d'1 heure par défaut
    availabilityType: AvailabilityType.AVAILABLE,
    recurring: true,
    priority: 2
  })

  // Mettre à jour le formulaire quand on édite
  useEffect(() => {
    if (editingData) {
      setFormData(editingData)
    }
  }, [editingData])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    // Validation spécifique pour les créneaux d'1 heure
    if (!formData.startTime || !formData.endTime) {
      toast({
        title: "Erreur",
        description: "Veuillez renseigner les heures de début et de fin",
        variant: "destructive",
      })
      return
    }

    // Calculer la durée en minutes
    const startMinutes = timeToMinutes(formData.startTime)
    const endMinutes = timeToMinutes(formData.endTime)
    const duration = endMinutes - startMinutes

    if (duration <= 0) {
      toast({
        title: "Erreur",
        description: "L'heure de fin doit être après l'heure de début",
        variant: "destructive",
      })
      return
    }

    if (duration !== 60) {
      toast({
        title: "Erreur",
        description: "Les créneaux doivent durer exactement 1 heure",
        variant: "destructive",
      })
      return
    }

    setLoading(true)
    try {
      await onSubmit(formData)
      onOpenChange(false)
      resetForm()
    } catch (error) {
      // L'erreur est gérée par le composant parent
    } finally {
      setLoading(false)
    }
  }

  const resetForm = () => {
    setFormData({
      teacherId: teacherId,
      dayOfWeek: DayOfWeek.MONDAY,
      startTime: "08:00",
      endTime: "09:00", // Créneau d'1 heure par défaut
      availabilityType: AvailabilityType.AVAILABLE,
      recurring: true,
      priority: 2
    })
  }

  // Fonction utilitaire pour convertir l'heure en minutes
  const timeToMinutes = (time: string): number => {
    const [hours, minutes] = time.split(':').map(Number)
    return hours * 60 + minutes
  }

  // Fonction pour ajuster automatiquement l'heure de fin (1h après le début)
  const handleStartTimeChange = (startTime: string) => {
    const startMinutes = timeToMinutes(startTime)
    const endMinutes = startMinutes + 60 // Ajouter 1 heure
    const endHours = Math.floor(endMinutes / 60)
    const endMins = endMinutes % 60
    const endTime = `${endHours.toString().padStart(2, '0')}:${endMins.toString().padStart(2, '0')}`
    
    setFormData(prev => ({
      ...prev,
      startTime: startTime,
      endTime: endTime
    }))
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

  const quickTimeSlots = [
    { label: "08h-09h", start: "08:00", end: "09:00" },
    { label: "09h-10h", start: "09:00", end: "10:00" },
    { label: "10h-11h", start: "10:00", end: "11:00" },
    { label: "11h-12h", start: "11:00", end: "12:00" },
    { label: "14h-15h", start: "14:00", end: "15:00" },
    { label: "15h-16h", start: "15:00", end: "16:00" },
    { label: "16h-17h", start: "16:00", end: "17:00" },
    { label: "17h-18h", start: "17:00", end: "18:00" },
  ]

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Calendar className="w-5 h-5" />
            {editingData ? "Modifier la disponibilité" : "Ajouter une disponibilité"}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          {/* Sélection rapide de créneaux */}
          <div className="space-y-3">
            <Label>Créneaux rapides</Label>
            <div className="grid grid-cols-2 gap-2">
              {quickTimeSlots.map((slot) => (
                <Button
                  key={slot.label}
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => {
                    setFormData(prev => ({
                      ...prev,
                      startTime: slot.start,
                      endTime: slot.end
                    }))
                  }}
                  className="justify-start"
                >
                  <Clock className="w-4 h-4 mr-2" />
                  {slot.label}
                </Button>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Jour de la semaine */}
            <div className="space-y-2">
              <Label htmlFor="dayOfWeek">Jour de la semaine</Label>
              <Select
                value={formData.dayOfWeek}
                onValueChange={(value) => setFormData(prev => ({ ...prev, dayOfWeek: value as DayOfWeek }))}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {Object.values(DayOfWeek).map((day) => (
                    <SelectItem key={day} value={day}>
                      {availabilityUtils.getDayOfWeekLabel(day)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            {/* Type de disponibilité */}
            <div className="space-y-2">
              <Label htmlFor="availabilityType">Type de disponibilité</Label>
              <Select
                value={formData.availabilityType}
                onValueChange={(value) => setFormData(prev => ({ ...prev, availabilityType: value as AvailabilityType }))}
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
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {/* Heure de début */}
            <div className="space-y-2">
              <Label htmlFor="startTime">Heure de début</Label>
              <Input
                id="startTime"
                type="time"
                value={formData.startTime}
                onChange={(e) => handleStartTimeChange(e.target.value)}
                required
              />
            </div>

            {/* Heure de fin (calculée automatiquement) */}
            <div className="space-y-2">
              <Label htmlFor="endTime">Heure de fin (automatique)</Label>
              <Input
                id="endTime"
                type="time"
                value={formData.endTime}
                readOnly
                className="bg-muted"
                title="L'heure de fin est calculée automatiquement (1h après le début)"
              />
            </div>
          </div>

          {/* Priorité */}
          <div className="space-y-2">
            <Label htmlFor="priority">Priorité</Label>
            <Select
              value={formData.priority?.toString()}
              onValueChange={(value) => setFormData(prev => ({ ...prev, priority: parseInt(value) }))}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="1">
                  <div className="flex items-center gap-2">
                    <Star className="w-4 h-4 text-yellow-500" />
                    Priorité élevée (Préféré)
                  </div>
                </SelectItem>
                <SelectItem value="2">
                  <div className="flex items-center gap-2">
                    <CheckCircle2 className="w-4 h-4 text-green-500" />
                    Priorité normale (Acceptable)
                  </div>
                </SelectItem>
                <SelectItem value="3">
                  <div className="flex items-center gap-2">
                    <AlertCircle className="w-4 h-4 text-orange-500" />
                    Priorité faible (Si nécessaire)
                  </div>
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          {/* Options avancées */}
          <Card className="p-4 space-y-4">
            <h4 className="font-medium">Options avancées</h4>
            
            <div className="flex items-center justify-between">
              <div className="space-y-1">
                <Label htmlFor="recurring">Récurrent</Label>
                <p className="text-sm text-muted-foreground">
                  Cette disponibilité se répète chaque semaine
                </p>
              </div>
              <Switch
                id="recurring"
                checked={formData.recurring}
                onCheckedChange={(checked) => setFormData(prev => ({ ...prev, recurring: checked }))}
              />
            </div>

            {!formData.recurring && (
              <div className="space-y-2">
                <Label htmlFor="specificDate">Date spécifique</Label>
                <Input
                  id="specificDate"
                  type="datetime-local"
                  value={formData.specificDate}
                  onChange={(e) => setFormData(prev => ({ ...prev, specificDate: e.target.value }))}
                />
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="notes">Notes</Label>
              <Textarea
                id="notes"
                placeholder="Commentaires ou précisions..."
                value={formData.notes || ""}
                onChange={(e) => setFormData(prev => ({ ...prev, notes: e.target.value }))}
                rows={3}
              />
            </div>
          </Card>

          {/* Aperçu */}
          <Card className="p-4 bg-muted/50">
            <h4 className="font-medium mb-3">Aperçu</h4>
            <div className="flex items-center gap-2 mb-2">
              <Badge className={availabilityUtils.getAvailabilityTypeColor(formData.availabilityType)}>
                {getTypeIcon(formData.availabilityType)}
                <span className="ml-1">{availabilityUtils.getAvailabilityTypeLabel(formData.availabilityType)}</span>
              </Badge>
            </div>
            <p className="text-sm text-muted-foreground">
              {availabilityUtils.getDayOfWeekLabel(formData.dayOfWeek)} de {formData.startTime} à {formData.endTime}
              {formData.recurring ? " (chaque semaine)" : " (ponctuel)"}
            </p>
          </Card>

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
            <Button type="submit" disabled={loading} className="bg-[#15803D] hover:bg-[#15803D]/90">
              {loading && <Loader2 className="w-4 h-4 mr-2 animate-spin" />}
              {editingData ? "Modifier" : "Ajouter"}
            </Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  )
}