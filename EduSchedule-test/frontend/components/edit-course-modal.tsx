"use client"

import type React from "react"

import { useState } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"

interface EditCourseModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  course: any
  onSubmit: (course: any) => void
}

export function EditCourseModal({ open, onOpenChange, course, onSubmit }: EditCourseModalProps) {
  const [formData, setFormData] = useState({
    time: course?.time || "",
    room: course?.room || "",
    group: course?.group || "",
    professor: course?.professor || "",
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit({ ...course, ...formData })
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>Modifier le cours</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="time">Horaire</Label>
              <Input
                id="time"
                value={formData.time}
                onChange={(e) => setFormData({ ...formData, time: e.target.value })}
                placeholder="08:00-10:00"
                required
              />
            </div>
            <div>
              <Label htmlFor="room">Salle</Label>
              <Input
                id="room"
                value={formData.room}
                onChange={(e) => setFormData({ ...formData, room: e.target.value })}
                placeholder="A101"
                required
              />
            </div>
            <div>
              <Label htmlFor="group">Groupe</Label>
              <Input
                id="group"
                value={formData.group}
                onChange={(e) => setFormData({ ...formData, group: e.target.value })}
                placeholder="L1-G1"
                required
              />
            </div>
            <div>
              <Label htmlFor="professor">Professeur</Label>
              <Input
                id="professor"
                value={formData.professor}
                onChange={(e) => setFormData({ ...formData, professor: e.target.value })}
                placeholder="Dr. Martin"
                required
              />
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              Annuler
            </Button>
            <Button type="submit" className="bg-[#15803D] hover:bg-[#166534]">
              Enregistrer
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
