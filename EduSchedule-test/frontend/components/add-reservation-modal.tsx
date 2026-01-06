"use client"

import type React from "react"

import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"

interface AddReservationModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onSubmit: (reservation: any) => void
}

export function AddReservationModal({ open, onOpenChange, onSubmit }: AddReservationModalProps) {
  const [formData, setFormData] = useState({
    room: "",
    date: "",
    startTime: "",
    endTime: "",
    purpose: "",
    requester: "",
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit(formData)
    setFormData({ room: "", date: "", startTime: "", endTime: "", purpose: "", requester: "" })
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Nouvelle réservation</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="room">Salle</Label>
              <Select value={formData.room} onValueChange={(value) => setFormData({ ...formData, room: value })}>
                <SelectTrigger>
                  <SelectValue placeholder="Sélectionnez une salle" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="A101">Salle A101</SelectItem>
                  <SelectItem value="B203">Salle B203</SelectItem>
                  <SelectItem value="C305">Salle C305</SelectItem>
                  <SelectItem value="Lab1">Laboratoire 1</SelectItem>
                  <SelectItem value="Amphi">Amphithéâtre</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="date">Date</Label>
              <Input
                id="date"
                type="date"
                value={formData.date}
                onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                required
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="startTime">Heure de début</Label>
                <Input
                  id="startTime"
                  type="time"
                  value={formData.startTime}
                  onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                  required
                />
              </div>
              <div>
                <Label htmlFor="endTime">Heure de fin</Label>
                <Input
                  id="endTime"
                  type="time"
                  value={formData.endTime}
                  onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                  required
                />
              </div>
            </div>
            <div>
              <Label htmlFor="requester">Demandeur</Label>
              <Input
                id="requester"
                value={formData.requester}
                onChange={(e) => setFormData({ ...formData, requester: e.target.value })}
                placeholder="Nom du demandeur"
                required
              />
            </div>
            <div>
              <Label htmlFor="purpose">Objet</Label>
              <Textarea
                id="purpose"
                value={formData.purpose}
                onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
                placeholder="Objet de la réservation..."
                rows={3}
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Annuler
            </Button>
            <Button type="submit" className="bg-[#15803D] hover:bg-[#166534]">
              Réserver
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
