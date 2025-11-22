"use client"

import { useState } from "react"
import {
  DndContext,
  type DragEndEvent,
  DragOverlay,
  type DragStartEvent,
  closestCenter,
  PointerSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core"
import { Plus } from "lucide-react"
import { Sidebar } from "@/components/sidebar"
import { Header } from "@/components/header"
import { ScheduleGrid } from "@/components/schedule-grid"
import { CourseCard } from "@/components/course-card"
import { AddScheduleCourseModal } from "@/components/add-schedule-course-modal"
import { Button } from "@/components/ui/button"
import type { Course } from "@/types/schedule"

const initialCourses: Course[] = [
  {
    id: "1",
    title: "Mathématiques",
    room: "A101",
    group: "L1-G1",
    professor: "Dr. Martin",
    startTime: "08:00",
    endTime: "10:00",
    day: "Lundi",
    color: "blue",
  },
  {
    id: "2",
    title: "Algèbre",
    room: "A102",
    group: "L2-G2",
    professor: "Prof. Dubois",
    startTime: "14:00",
    endTime: "16:00",
    day: "Lundi",
    color: "purple",
  },
  {
    id: "3",
    title: "Physique",
    room: "B203",
    group: "L1-G1",
    professor: "Dr. Laurent",
    startTime: "10:00",
    endTime: "12:00",
    day: "Mardi",
    color: "green",
  },
  {
    id: "4",
    title: "TP Physique",
    room: "Lab B1",
    group: "L1-G2",
    professor: "Dr. Laurent",
    startTime: "14:00",
    endTime: "16:00",
    day: "Mardi",
    color: "green",
  },
  {
    id: "5",
    title: "Analyse",
    room: "A103",
    group: "L2-G1",
    professor: "Prof. Bernard",
    startTime: "08:00",
    endTime: "10:00",
    day: "Mercredi",
    color: "cyan",
  },
  {
    id: "6",
    title: "Géométrie",
    room: "A101",
    group: "L1-G1",
    professor: "Dr. Martin",
    startTime: "09:00",
    endTime: "11:00",
    day: "Jeudi",
    color: "blue",
  },
  {
    id: "7",
    title: "Séminaire",
    room: "Amphi A",
    group: "M1",
    professor: "Prof. Richard",
    startTime: "14:00",
    endTime: "17:00",
    day: "Jeudi",
    color: "orange",
  },
  {
    id: "8",
    title: "Statistiques",
    room: "A104",
    group: "L3-G1",
    professor: "Dr. Sophie",
    startTime: "08:00",
    endTime: "10:00",
    day: "Vendredi",
    color: "red",
  },
  {
    id: "9",
    title: "Probabilités",
    room: "A104",
    group: "L3-G2",
    professor: "Dr. Sophie",
    startTime: "10:00",
    endTime: "12:00",
    day: "Vendredi",
    color: "red",
  },
]

export function ScheduleView() {
  const [courses, setCourses] = useState<Course[]>(initialCourses)
  const [activeCourse, setActiveCourse] = useState<Course | null>(null)
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    }),
  )

  const handleDragStart = (event: DragStartEvent) => {
    const course = courses.find((c) => c.id === event.active.id)
    if (course) {
      setActiveCourse(course)
    }
  }

  const handleDragEnd = (event: DragEndEvent) => {
    const { active, over } = event

    if (over && active.id !== over.id) {
      const activeId = active.id as string
      const overId = over.id as string

      const [day, timeSlot] = overId.split("-")

      if (day && timeSlot) {
        setCourses(
          courses.map((course) => {
            if (course.id === activeId) {
              return {
                ...course,
                day: day.charAt(0).toUpperCase() + day.slice(1),
                startTime: timeSlot,
                endTime: calculateEndTime(timeSlot),
              }
            }
            return course
          }),
        )
      }
    }

    setActiveCourse(null)
  }

  const calculateEndTime = (startTime: string): string => {
    const [hours, minutes] = startTime.split(":").map(Number)
    const endHours = hours + 2
    return `${endHours.toString().padStart(2, "0")}:${minutes.toString().padStart(2, "0")}`
  }

  const handleAddCourse = (newCourse: Course) => {
    setCourses([...courses, newCourse])
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={closestCenter}
      onDragStart={handleDragStart}
      onDragEnd={handleDragEnd}
    >
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="schedule" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header
            title="Emplois du temps"
            subtitle="Gérez et organisez les cours par glisser-déposer"
            action={
              <Button onClick={() => setIsAddModalOpen(true)} className="bg-[#15803D] hover:bg-[#166534]">
                <Plus className="w-4 h-4 mr-2" />
                Ajouter un cours
              </Button>
            }
          />
          <main className="flex-1 overflow-auto p-6">
            <ScheduleGrid courses={courses} />
          </main>
        </div>
      </div>

      <DragOverlay>
        {activeCourse ? (
          <div className="opacity-80">
            <CourseCard course={activeCourse} isDragging />
          </div>
        ) : null}
      </DragOverlay>

      <AddScheduleCourseModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} onSubmit={handleAddCourse} />
    </DndContext>
  )
}
