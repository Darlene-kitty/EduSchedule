"use client"

import type React from "react"

import { useState, useEffect } from "react"
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Textarea } from "@/components/ui/textarea"

interface EditResourceModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  resource: any
  onSubmit: (resource: any) => void
}

export function EditResourceModal({ open, onOpenChange, resource, onSubmit }: EditResourceModalProps) {
  const [formData, setFormData] = useState({
    name: "",
    category: "",
    quantity: "",
    available: "",
    location: "",
    status: "",
    description: "",
  })

  useEffect(() => {
    if (resource) {
      setFormData({
        name: resource.name || "",
        category: resource.category || "",
        quantity: resource.quantity?.toString() || "",
        available: resource.available?.toString() || "",
        location: resource.location || "",
        status: resource.status || "",
        description: resource.description || "",
      })
    }
  }, [resource])

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    onSubmit({
      ...resource,
      ...formData,
      quantity: Number.parseInt(formData.quantity),
      available: Number.parseInt(formData.available),
    })
    onOpenChange(false)
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Modifier la ressource</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit}>
          <div className="space-y-4 py-4 max-h-[60vh] overflow-y-auto">
            <div className="space-y-2">
              <Label htmlFor="edit-res-name">Nom de la ressource</Label>
              <Input
                id="edit-res-name"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-res-category">Catégorie</Label>
              <Select
                value={formData.category}
                onValueChange={(value) => setFormData({ ...formData, category: value })}
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Matériel">Matériel</SelectItem>
                  <SelectItem value="Équipement">Équipement</SelectItem>
                  <SelectItem value="Logiciel">Logiciel</SelectItem>
                  <SelectItem value="Fourniture">Fourniture</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="edit-res-quantity">Quantité totale</Label>
                <Input
                  id="edit-res-quantity"
                  type="number"
                  value={formData.quantity}
                  onChange={(e) => setFormData({ ...formData, quantity: e.target.value })}
                  required
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="edit-res-available">Disponible</Label>
                <Input
                  id="edit-res-available"
                  type="number"
                  value={formData.available}
                  onChange={(e) => setFormData({ ...formData, available: e.target.value })}
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-res-location">Localisation</Label>
              <Input
                id="edit-res-location"
                value={formData.location}
                onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-res-status">Statut</Label>
              <Select value={formData.status} onValueChange={(value) => setFormData({ ...formData, status: value })}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="Disponible">Disponible</SelectItem>
                  <SelectItem value="En utilisation">En utilisation</SelectItem>
                  <SelectItem value="Maintenance">Maintenance</SelectItem>
                  <SelectItem value="Indisponible">Indisponible</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="edit-res-description">Description</Label>
              <Textarea
                id="edit-res-description"
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                rows={3}
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
