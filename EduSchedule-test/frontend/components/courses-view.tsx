"use client"

import { useState, useEffect } from "react"
import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Plus,
  Search,
  BookOpen,
  Clock,
  User,
  Edit,
  Trash2,
  Loader2,
  AlertCircle,
  GraduationCap,
  Building,
} from "lucide-react"
import { coursesApi, Course, CreateCourseRequest, CourseFilters } from "@/lib/api/courses"
import { useToast } from "@/hooks/use-toast"

export function CoursesView() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedDepartment, setSelectedDepartment] = useState<string>("all")
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { toast } = useToast()

  // Charger les cours au montage du composant
  useEffect(() => {
    loadCourses()
  }, [])

  const loadCourses = async (filters?: CourseFilters) => {
    try {
      setLoading(true)
      setError(null)
      const coursesData = await coursesApi.getAllCourses(filters)
      setCourses(coursesData)
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors du chargement des cours'
      setError(errorMessage)
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    } finally {
      setLoading(false)
    }
  }

  const filteredCourses = courses.filter((course) => {
    const matchesSearch =
      course.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      course.code.toLowerCase().includes(searchQuery.toLowerCase()) ||
      course.type.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesDepartment = selectedDepartment === "all" || course.department === selectedDepartment
    return matchesSearch && matchesDepartment
  })

  const getTypeColor = (type: string) => {
    switch (type.toUpperCase()) {
      case "COURS":
      case "LECTURE":
        return "bg-blue-100 text-blue-700"
      case "TD":
      case "TUTORIAL":
        return "bg-green-100 text-green-700"
      case "TP":
      case "PRACTICAL":
        return "bg-purple-100 text-purple-700"
      case "PROJET":
      case "PROJECT":
        return "bg-orange-100 text-orange-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const getLevelColor = (level?: string) => {
    if (!level) return "bg-gray-100 text-gray-700"
    
    switch (level.toUpperCase()) {
      case "L1":
      case "LICENCE 1":
        return "bg-green-100 text-green-700"
      case "L2":
      case "LICENCE 2":
        return "bg-blue-100 text-blue-700"
      case "L3":
      case "LICENCE 3":
        return "bg-purple-100 text-purple-700"
      case "M1":
      case "MASTER 1":
        return "bg-orange-100 text-orange-700"
      case "M2":
      case "MASTER 2":
        return "bg-red-100 text-red-700"
      default:
        return "bg-gray-100 text-gray-700"
    }
  }

  const uniqueDepartments = Array.from(new Set(courses.map(c => c.department).filter(Boolean)))

  const handleDeleteCourse = async (courseId: number) => {
    if (!confirm("Êtes-vous sûr de vouloir supprimer ce cours ?")) {
      return
    }

    try {
      await coursesApi.deleteCourse(courseId)
      setCourses(prev => prev.filter(course => course.id !== courseId))
      toast({
        title: "Succès",
        description: "Cours supprimé avec succès",
      })
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Erreur lors de la suppression du cours'
      toast({
        title: "Erreur",
        description: errorMessage,
        variant: "destructive",
      })
    }
  }

  const stats = [
    { label: "Total cours", value: courses.length, color: "bg-blue-500", icon: BookOpen },
    {
      label: "Heures/semaine",
      value: courses.reduce((sum, c) => sum + c.hoursPerWeek, 0),
      color: "bg-green-500",
      icon: Clock,
    },
    {
      label: "Départements",
      value: uniqueDepartments.length,
      color: "bg-purple-500",
      icon: Building,
    },
    {
      label: "Avec enseignant",
      value: courses.filter((c) => c.teacherId).length,
      color: "bg-orange-500",
      icon: User,
    },
  ]

  if (loading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="courses" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Cours" subtitle="Gérez les cours et programmes d'enseignement" />
          <main className="flex-1 flex items-center justify-center">
            <div className="flex items-center gap-2">
              <Loader2 className="w-6 h-6 animate-spin" />
              <span>Chargement des cours...</span>
            </div>
          </main>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar activePage="courses" />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header title="Cours" subtitle="Gérez les cours et programmes d'enseignement" />
          <main className="flex-1 flex items-center justify-center">
            <div className="text-center">
              <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Erreur de chargement</h3>
              <p className="text-muted-foreground mb-4">{error}</p>
              <Button onClick={() => loadCourses()}>Réessayer</Button>
            </div>
          </main>
        </div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="courses" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Cours" subtitle="Gérez les cours et programmes d'enseignement" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Stats */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            {stats.map((stat, index) => (
              <Card key={index} className="p-4">
                <div className="flex items-center gap-3">
                  <div className={`${stat.color} w-12 h-12 rounded-lg flex items-center justify-center`}>
                    <stat.icon className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <p className="text-2xl font-bold">{stat.value}</p>
                    <p className="text-sm text-muted-foreground">{stat.label}</p>
                  </div>
                </div>
              </Card>
            ))}
          </div>

          {/* Actions Bar */}
          <div className="flex flex-col sm:flex-row gap-4 mb-6">
            <div className="relative flex-1">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Rechercher un cours..."
                className="pl-10"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90">
              <Plus className="w-4 h-4" />
              Ajouter un cours
            </Button>
          </div>

          {/* Department Filter */}
          <div className="flex gap-2 mb-6 overflow-x-auto">
            <Button
              variant={selectedDepartment === "all" ? "default" : "outline"}
              size="sm"
              onClick={() => setSelectedDepartment("all")}
              className={selectedDepartment === "all" ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
            >
              Tous
            </Button>
            {uniqueDepartments.map((department) => (
              <Button
                key={department}
                variant={selectedDepartment === department ? "default" : "outline"}
                size="sm"
                onClick={() => setSelectedDepartment(department)}
                className={selectedDepartment === department ? "bg-[#15803D] hover:bg-[#15803D]/90" : ""}
              >
                {department}
              </Button>
            ))}
          </div>

          {/* Courses Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {filteredCourses.map((course) => (
              <Card key={course.id} className="p-5 hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex items-center gap-3">
                    <div className="bg-[#15803D] p-3 rounded-lg">
                      <BookOpen className="w-6 h-6 text-white" />
                    </div>
                    <div>
                      <h3 className="font-semibold">{course.name}</h3>
                      <p className="text-sm text-muted-foreground">{course.code}</p>
                    </div>
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Type</span>
                    <Badge className={`${getTypeColor(course.type)} text-xs`}>{course.type}</Badge>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-muted-foreground">Heures/semaine</span>
                    <div className="flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      <span className="text-sm font-medium">{course.hoursPerWeek}h</span>
                    </div>
                  </div>
                  {course.level && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Niveau</span>
                      <Badge className={`${getLevelColor(course.level)} text-xs`}>{course.level}</Badge>
                    </div>
                  )}
                  {course.department && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Département</span>
                      <div className="flex items-center gap-1">
                        <Building className="w-3 h-3" />
                        <span className="text-sm font-medium">{course.department}</span>
                      </div>
                    </div>
                  )}
                  {course.teacherId && (
                    <div className="flex items-center justify-between">
                      <span className="text-sm text-muted-foreground">Enseignant</span>
                      <div className="flex items-center gap-1">
                        <User className="w-3 h-3" />
                        <span className="text-sm font-medium">ID: {course.teacherId}</span>
                      </div>
                    </div>
                  )}
                </div>

                <div className="flex items-center justify-between pt-4 border-t mb-4">
                  <span className="text-sm text-muted-foreground">
                    Créé le {new Date(course.createdAt).toLocaleDateString()}
                  </span>
                </div>

                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="flex-1 gap-2 bg-transparent">
                    <Edit className="w-3 h-3" />
                    Modifier
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    className="gap-2 text-red-600 hover:text-red-700 bg-transparent"
                    onClick={() => handleDeleteCourse(course.id)}
                  >
                    <Trash2 className="w-3 h-3" />
                  </Button>
                </div>
              </Card>
            ))}
          </div>

          {filteredCourses.length === 0 && (
            <div className="text-center py-12">
              <GraduationCap className="w-12 h-12 text-gray-400 mx-auto mb-4" />
              <h3 className="text-lg font-semibold mb-2">Aucun cours trouvé</h3>
              <p className="text-muted-foreground">
                {searchQuery || selectedDepartment !== "all"
                  ? "Aucun cours ne correspond à vos critères de recherche."
                  : "Commencez par ajouter votre premier cours."}
              </p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}