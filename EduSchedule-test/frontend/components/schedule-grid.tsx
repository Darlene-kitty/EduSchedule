"use client"

import React from "react"
import { useDroppable } from "@dnd-kit/core"
import { CourseCard } from "@/components/course-card"
import type { Course } from "@/types/schedule"
import { Edit, Trash2, GripVertical } from "lucide-react"

interface ScheduleGridProps {
  courses: Course[]
}

const days = ["Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi"]
const timeSlots = ["08:00", "10:00", "12:00", "14:00", "16:00"]

export function ScheduleGrid({ courses }: ScheduleGridProps) {
  return (
    <div className="p-8">
      <div className="mb-6 flex items-center justify-between">
        <h2 className="text-2xl font-bold text-[#1E3A8A]">Emplois du temps</h2>
        <div className="text-right">
          <p className="text-sm text-gray-600">Samedi 18 Octobre 2025</p>
          <p className="text-xs text-gray-500">09:30</p>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="grid grid-cols-6 gap-4 p-6">
          <div className="col-span-1"></div>
          {days.map((day) => (
            <div key={day} className="text-center">
              <h3 className="font-semibold text-[#1E3A8A] text-lg">{day}</h3>
            </div>
          ))}

          {timeSlots.map((timeSlot) => (
            <React.Fragment key={`row-${timeSlot}`}>
              <div className="flex items-start justify-end pr-4 pt-2">
                <span className="text-sm text-gray-500 font-medium">{timeSlot}</span>
              </div>
              {days.map((day) => {
                const coursesInSlot = courses.filter((c) => c.day === day && c.startTime === timeSlot)
                return (
                  <DropZone
                    key={`${day}-${timeSlot}`}
                    id={`${day.toLowerCase()}-${timeSlot}`}
                    courses={coursesInSlot}
                  />
                )
              })}
            </React.Fragment>
          ))}
        </div>
      </div>

      <div className="mt-8 bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h3 className="font-semibold text-lg mb-4 text-[#1E3A8A]">Légende</h3>
        <div className="flex flex-wrap gap-6 text-sm text-gray-600">
          <div className="flex items-center gap-2">
            <GripVertical className="w-4 h-4" />
            <span>Cliquez et glissez pour déplacer</span>
          </div>
          <div className="flex items-center gap-2">
            <Edit className="w-4 h-4" />
            <span>Modifier (survolez le cours)</span>
          </div>
          <div className="flex items-center gap-2">
            <Trash2 className="w-4 h-4" />
            <span>Supprimer (survolez le cours)</span>
          </div>
        </div>
      </div>
    </div>
  )
}

interface DropZoneProps {
  id: string
  courses: Course[]
}

function DropZone({ id, courses }: DropZoneProps) {
  const { setNodeRef, isOver } = useDroppable({ id })

  return (
    <div
      ref={setNodeRef}
      className={`min-h-[140px] rounded-lg border-2 border-dashed transition-colors ${
        isOver ? "border-[#15803D] bg-green-50" : "border-gray-200 bg-gray-50"
      }`}
    >
      {courses.map((course) => (
        <CourseCard key={course.id} course={course} />
      ))}
    </div>
  )
}
