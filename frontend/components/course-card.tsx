"use client"

import { useDraggable } from "@dnd-kit/core"
import { CSS } from "@dnd-kit/utilities"
import type { Course } from "@/types/schedule"
import { Clock, GripVertical, Edit, Trash2 } from "lucide-react"
import { useState } from "react"

interface CourseCardProps {
  course: Course
  isDragging?: boolean
}

const colorClasses = {
  blue: "bg-blue-500",
  green: "bg-green-600",
  cyan: "bg-cyan-500",
  purple: "bg-purple-500",
  orange: "bg-orange-500",
  red: "bg-red-500",
}

export function CourseCard({ course, isDragging = false }: CourseCardProps) {
  const [isHovered, setIsHovered] = useState(false)
  const { attributes, listeners, setNodeRef, transform } = useDraggable({
    id: course.id,
  })

  const style = {
    transform: CSS.Translate.toString(transform),
  }

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      className={`${colorClasses[course.color]} text-white rounded-lg p-4 cursor-move shadow-md hover:shadow-lg transition-all relative ${
        isDragging ? "opacity-50" : ""
      }`}
    >
      <div className="absolute top-2 right-2 flex gap-1">
        <GripVertical className="w-4 h-4 opacity-70" />
      </div>

      {isHovered && (
        <div className="absolute top-2 left-2 flex gap-1">
          <button
            onClick={(e) => {
              e.stopPropagation()
              console.log("Edit course:", course.id)
            }}
            className="bg-white/20 hover:bg-white/30 rounded p-1 transition-colors"
          >
            <Edit className="w-3 h-3" />
          </button>
          <button
            onClick={(e) => {
              e.stopPropagation()
              console.log("Delete course:", course.id)
            }}
            className="bg-white/20 hover:bg-white/30 rounded p-1 transition-colors"
          >
            <Trash2 className="w-3 h-3" />
          </button>
        </div>
      )}

      <div className="flex items-center gap-2 mb-2 mt-4">
        <Clock className="w-4 h-4" />
        <span className="text-sm font-medium">
          {course.startTime}-{course.endTime}
        </span>
      </div>

      <h4 className="font-bold text-lg mb-2">{course.title}</h4>

      <div className="space-y-1">
        <div className="bg-white/20 rounded px-2 py-1 text-xs font-semibold inline-block">{course.room}</div>
        <div className="text-sm">{course.group}</div>
        <div className="text-sm opacity-90">{course.professor}</div>
      </div>
    </div>
  )
}
