"use client"

import { useState } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Plus, Search, BookOpen, Users, Clock, Edit, Trash2, GraduationCap } from "lucide-react"
import { AddCourseModal } from "./add-course-modal"

interface Course {
  id: string
  name: string
  code: string
  professor: string
  level: string
  semester: string
  hours: number
  students: number
  color: string
}

interface Group {
  id: string
  name: string
  level: string
  students: number
  courses: number
  supervisor: string
}

const courses: Course[] = [
  {
    id: "1",
    name: "Mathématiques",
    code: "MATH101",
    professor: "Dr. Martin",
    level: "L1",
    semester: "S1",
    hours: 48,
    students: 45,
    color: "blue",
  },
  {
    id: "2",
    name: "Physique",
    code: "PHYS101",
    professor: "Dr. Laurent",
    level: "L1",
    semester: "S1",
    hours: 42,
    students: 45,
    color: "green",
  },
  {
    id: "3",
    name: "Algèbre",
    code: "MATH201",
    professor: "Prof. Dubois",
    level: "L2",
    semester: "S1",
    hours: 36,
    students: 38,
    color: "purple",
  },
  {
    id: "4",
    name: "Analyse",
    code: "MATH202",
    professor: "Prof. Bernard",
    level: "L2",
    semester: "S1",
    hours: 40,
    students: 38,
    color: "cyan",
  },
  {
    id: "5",
    name: "Statistiques",
    code: "STAT301",
    professor: "Dr. Sophie",
    level: "L3",
    semester: "S1",
    hours: 32,
    students: 30,
    color: "red",
  },
  {
    id: "6",
    name: "Probabilités",
    code: "STAT302",
    professor: "Dr. Sophie",
    level: "L3",
    semester: "S1",
    hours: 32,
    students: 30,
    color: "red",
  },
]

const groups: Group[] = [
  {
    id: "1",
    name: "L1-G1",
    level: "Licence 1",
    students: 45,
    courses: 8,
    supervisor: "Dr. Martin",
  },
  {
    id: "2",
    name: "L1-G2",
    level: "Licence 1",
    students: 42,
    courses: 8,
    supervisor: "Dr. Laurent",
  },
  {
    id: "3",
    name: "L2-G1",
    level: "Licence 2",
    students: 38,
    courses: 7,
    supervisor: "Prof. Bernard",
  },
  {
    id: "4",
    name: "L2-G2",
    level: "Licence 2",
    students: 35,
    courses: 7,
    supervisor: "Prof. Dubois",
  },
  {
    id: "5",
    name: "L3-G1",
    level: "Licence 3",
    students: 30,
    courses: 6,
    supervisor: "Dr. Sophie",
  },
  {
    id: "6",
    name: "L3-G2",
    level: "Licence 3",
    students: 28,
    courses: 6,
    supervisor: "Dr. Sophie",
  },
  {
    id: "7",
    name: "M1",
    level: "Master 1",
    students: 25,
    courses: 5,
    supervisor: "Prof. Richard",
  },
]

export function CoursesView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [isAddModalOpen, setIsAddModalOpen] = useState(false)

  const filteredCourses = courses.filter(
    (course) =>
      course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      course.code.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  const filteredGroups = groups.filter(
    (group) =>
      group.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      group.level.toLowerCase().includes(searchQuery.toLowerCase()),
  )

  const getColorClass = (color: string) => {
    const colors: Record<string, string> = {
      blue: "bg-blue-500",
      green: "bg-green-500",
      purple: "bg-purple-500",
      cyan: "bg-cyan-500",
      red: "bg-red-500",
      orange: "bg-orange-500",
    }
    return colors[color] || "bg-gray-500"
  }

  const handleAddCourse = (courseData: any) => {
    console.log("[v0] New course added:", courseData)
    // Here you would typically add the course to your state/database
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="courses" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Cours & Groupes" subtitle="Gérez les cours, matières et groupes d'étudiants" />

        <main className="flex-1 overflow-y-auto p-6">
          <Tabs defaultValue="courses" className="space-y-6">
            <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
              <TabsList>
                <TabsTrigger value="courses">Cours</TabsTrigger>
                <TabsTrigger value="groups">Groupes</TabsTrigger>
              </TabsList>

              <div className="flex gap-2 w-full sm:w-auto">
                <div className="relative flex-1 sm:w-64">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                  <Input
                    placeholder="Rechercher..."
                    className="pl-10"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                </div>
                <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90" onClick={() => setIsAddModalOpen(true)}>
                  <Plus className="w-4 h-4" />
                  Ajouter
                </Button>
              </div>
            </div>

            <TabsContent value="courses" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredCourses.map((course) => (
                  <Card key={course.id} className="p-5 hover:shadow-lg transition-shadow">
                    <div className="flex items-start gap-3 mb-4">
                      <div className={`${getColorClass(course.color)} p-3 rounded-lg`}>
                        <BookOpen className="w-6 h-6 text-white" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg">{course.name}</h3>
                        <p className="text-sm text-muted-foreground">{course.code}</p>
                      </div>
                    </div>

                    <div className="space-y-2 mb-4">
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Professeur</span>
                        <span className="font-medium">{course.professor}</span>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Niveau</span>
                        <Badge variant="outline">{course.level}</Badge>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-muted-foreground">Semestre</span>
                        <span className="font-medium">{course.semester}</span>
                      </div>
                    </div>

                    <div className="flex items-center gap-4 pt-4 border-t text-sm">
                      <div className="flex items-center gap-1">
                        <Clock className="w-4 h-4 text-muted-foreground" />
                        <span>{course.hours}h</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Users className="w-4 h-4 text-muted-foreground" />
                        <span>{course.students} étudiants</span>
                      </div>
                    </div>

                    <div className="flex gap-2 mt-4">
                      <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                        <Edit className="w-3 h-3" />
                        Modifier
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                      >
                        <Trash2 className="w-3 h-3" />
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </TabsContent>

            <TabsContent value="groups" className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {filteredGroups.map((group) => (
                  <Card key={group.id} className="p-5 hover:shadow-lg transition-shadow">
                    <div className="flex items-start gap-3 mb-4">
                      <div className="bg-[#15803D] p-3 rounded-lg">
                        <GraduationCap className="w-6 h-6 text-white" />
                      </div>
                      <div className="flex-1">
                        <h3 className="font-semibold text-lg">{group.name}</h3>
                        <p className="text-sm text-muted-foreground">{group.level}</p>
                      </div>
                    </div>

                    <div className="space-y-3 mb-4">
                      <div className="flex items-center justify-between">
                        <span className="text-sm text-muted-foreground">Responsable</span>
                        <span className="text-sm font-medium">{group.supervisor}</span>
                      </div>
                      <div className="grid grid-cols-2 gap-3">
                        <div className="text-center p-3 bg-blue-50 rounded-lg">
                          <p className="text-2xl font-bold text-blue-700">{group.students}</p>
                          <p className="text-xs text-muted-foreground">Étudiants</p>
                        </div>
                        <div className="text-center p-3 bg-green-50 rounded-lg">
                          <p className="text-2xl font-bold text-green-700">{group.courses}</p>
                          <p className="text-xs text-muted-foreground">Cours</p>
                        </div>
                      </div>
                    </div>

                    <div className="flex gap-2 mt-4">
                      <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                        <Edit className="w-3 h-3" />
                        Modifier
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                      >
                        <Trash2 className="w-3 h-3" />
                      </Button>
                    </div>
                  </Card>
                ))}
              </div>
            </TabsContent>
          </Tabs>
        </main>
      </div>
      <AddCourseModal open={isAddModalOpen} onOpenChange={setIsAddModalOpen} onSubmit={handleAddCourse} />
    </div>
  )
}
