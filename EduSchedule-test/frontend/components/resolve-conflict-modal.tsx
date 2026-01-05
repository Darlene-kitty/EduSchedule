"use client"

import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { AlertTriangle } from "lucide-react"
import { useState } from "react"

interface ResolveConflictModalProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  conflict: any
  onResolve: (resolution: any) => void
}

export function ResolveConflictModal({ open, onOpenChange, conflict, onResolve }: ResolveConflictModalProps) {
  const [resolutionType, setResolutionType] = useState<string>("auto")
  const [selectedOption, setSelectedOption] = useState<string>("")

  const handleResolve = () => {
    onResolve({
      conflictId: conflict?.id,
      type: resolutionType,
      option: selectedOption,
    })
    onOpenChange(false)
  }

  if (!conflict) return null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <AlertTriangle className="w-5 h-5 text-orange-500" />
            Résoudre le conflit
          </DialogTitle>
        </DialogHeader>

        <div className="space-y-6 py-4">
          {/* Conflict Details */}
          <div className="bg-orange-50 border border-orange-200 rounded-lg p-4">
            <h4 className="font-semibold mb-2">{conflict.description}</h4>
            <div className="text-sm space-y-1 text-muted-foreground">
              <p>
                <strong>Cours 1:</strong> {conflict.details.course1}
              </p>
              <p>
                <strong>Cours 2:</strong> {conflict.details.course2}
              </p>
              <p>
                <strong>Horaire:</strong> {conflict.details.time} - {conflict.details.date}
              </p>
              {conflict.details.location && (
                <p>
                  <strong>Lieu:</strong> {conflict.details.location}
                </p>
              )}
            </div>
          </div>

          {/* Resolution Options */}
          <div className="space-y-4">
            <Label>Type de résolution</Label>
            <RadioGroup value={resolutionType} onValueChange={setResolutionType}>
              <div className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50">
                <RadioGroupItem value="auto" id="auto" />
                <Label htmlFor="auto" className="flex-1 cursor-pointer">
                  <div>
                    <p className="font-medium">Résolution automatique</p>
                    <p className="text-sm text-muted-foreground">
                      Le système trouvera automatiquement un créneau disponible
                    </p>
                  </div>
                </Label>
              </div>

              <div className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50">
                <RadioGroupItem value="manual" id="manual" />
                <Label htmlFor="manual" className="flex-1 cursor-pointer">
                  <div>
                    <p className="font-medium">Résolution manuelle</p>
                    <p className="text-sm text-muted-foreground">Choisir manuellement une solution</p>
                  </div>
                </Label>
              </div>

              <div className="flex items-center space-x-2 p-3 border rounded-lg hover:bg-gray-50">
                <RadioGroupItem value="reschedule" id="reschedule" />
                <Label htmlFor="reschedule" className="flex-1 cursor-pointer">
                  <div>
                    <p className="font-medium">Reprogrammer un cours</p>
                    <p className="text-sm text-muted-foreground">Déplacer l'un des cours en conflit</p>
                  </div>
                </Label>
              </div>
            </RadioGroup>
          </div>

          {resolutionType === "reschedule" && (
            <div className="space-y-2">
              <Label>Cours à reprogrammer</Label>
              <Select value={selectedOption} onValueChange={setSelectedOption}>
                <SelectTrigger>
                  <SelectValue placeholder="Sélectionner un cours" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="course1">{conflict.details.course1}</SelectItem>
                  <SelectItem value="course2">{conflict.details.course2}</SelectItem>
                </SelectContent>
              </Select>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            Annuler
          </Button>
          <Button onClick={handleResolve} className="bg-[#15803D] hover:bg-[#166534]">
            Résoudre le conflit
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
