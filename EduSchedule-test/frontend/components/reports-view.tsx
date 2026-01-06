"use client"

import { Sidebar } from "./sidebar"
import { Header } from "./header"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { BarChart3, TrendingUp, Download, Calendar, Users, BookOpen, Clock } from "lucide-react"
import {
  Bar,
  BarChart,
  Pie,
  PieChart,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts"

const weeklyData = [
  { day: "Lun", courses: 24, hours: 48 },
  { day: "Mar", courses: 22, hours: 44 },
  { day: "Mer", courses: 20, hours: 40 },
  { day: "Jeu", courses: 26, hours: 52 },
  { day: "Ven", courses: 18, hours: 36 },
]

const roomUsageData = [
  { name: "A101", usage: 85 },
  { name: "A102", usage: 72 },
  { name: "A103", usage: 68 },
  { name: "B203", usage: 90 },
  { name: "Lab B1", usage: 78 },
  { name: "Amphi A", usage: 45 },
]

const courseDistribution = [
  { name: "Mathématiques", value: 28, color: "#3B82F6" },
  { name: "Physique", value: 22, color: "#10B981" },
  { name: "Informatique", value: 18, color: "#8B5CF6" },
  { name: "Langues", value: 15, color: "#F59E0B" },
  { name: "Autres", value: 17, color: "#6B7280" },
]

const professorWorkload = [
  { name: "Dr. Martin", hours: 24 },
  { name: "Dr. Laurent", hours: 22 },
  { name: "Dr. Sophie", hours: 26 },
  { name: "Prof. Bernard", hours: 20 },
  { name: "Prof. Dubois", hours: 18 },
]

export function ReportsView() {
  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activePage="reports" />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header title="Rapports & Analyses" subtitle="Visualisez les statistiques et performances du système" />

        <main className="flex-1 overflow-y-auto p-6">
          {/* Action Bar */}
          <div className="flex justify-between items-center mb-6">
            <div className="flex gap-2">
              <Button variant="outline" className="gap-2 bg-transparent">
                <Calendar className="w-4 h-4" />
                Cette semaine
              </Button>
            </div>
            <Button className="gap-2 bg-[#15803D] hover:bg-[#15803D]/90">
              <Download className="w-4 h-4" />
              Exporter PDF
            </Button>
          </div>

          {/* Key Metrics */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
            <Card className="p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-blue-500 p-3 rounded-lg">
                  <BookOpen className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Total cours</p>
                  <p className="text-2xl font-bold">156</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-sm">
                <TrendingUp className="w-4 h-4 text-green-500" />
                <span className="text-green-500 font-medium">+8%</span>
                <span className="text-muted-foreground">vs semaine dernière</span>
              </div>
            </Card>

            <Card className="p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-green-500 p-3 rounded-lg">
                  <Users className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Étudiants actifs</p>
                  <p className="text-2xl font-bold">248</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-sm">
                <TrendingUp className="w-4 h-4 text-green-500" />
                <span className="text-green-500 font-medium">+12%</span>
                <span className="text-muted-foreground">vs semaine dernière</span>
              </div>
            </Card>

            <Card className="p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-purple-500 p-3 rounded-lg">
                  <Clock className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Heures totales</p>
                  <p className="text-2xl font-bold">220h</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-sm">
                <TrendingUp className="w-4 h-4 text-green-500" />
                <span className="text-green-500 font-medium">+5%</span>
                <span className="text-muted-foreground">vs semaine dernière</span>
              </div>
            </Card>

            <Card className="p-5">
              <div className="flex items-center gap-3 mb-3">
                <div className="bg-orange-500 p-3 rounded-lg">
                  <BarChart3 className="w-5 h-5 text-white" />
                </div>
                <div>
                  <p className="text-sm text-muted-foreground">Taux occupation</p>
                  <p className="text-2xl font-bold">73%</p>
                </div>
              </div>
              <div className="flex items-center gap-1 text-sm">
                <TrendingUp className="w-4 h-4 text-green-500" />
                <span className="text-green-500 font-medium">+3%</span>
                <span className="text-muted-foreground">vs semaine dernière</span>
              </div>
            </Card>
          </div>

          {/* Charts Grid */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mb-6">
            {/* Weekly Activity */}
            <Card className="p-6">
              <h3 className="font-semibold text-lg mb-4">Activité hebdomadaire</h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={weeklyData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="day" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="courses" fill="#15803D" name="Cours" />
                  <Bar dataKey="hours" fill="#3B82F6" name="Heures" />
                </BarChart>
              </ResponsiveContainer>
            </Card>

            {/* Room Usage */}
            <Card className="p-6">
              <h3 className="font-semibold text-lg mb-4">Utilisation des salles (%)</h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={roomUsageData} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" />
                  <YAxis dataKey="name" type="category" />
                  <Tooltip />
                  <Bar dataKey="usage" fill="#10B981" />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Course Distribution */}
            <Card className="p-6">
              <h3 className="font-semibold text-lg mb-4">Répartition des cours par matière</h3>
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie
                    data={courseDistribution}
                    cx="50%"
                    cy="50%"
                    labelLine={false}
                    label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                    outerRadius={100}
                    fill="#8884d8"
                    dataKey="value"
                  >
                    {courseDistribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={entry.color} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </Card>

            {/* Professor Workload */}
            <Card className="p-6">
              <h3 className="font-semibold text-lg mb-4">Charge de travail des professeurs (heures/semaine)</h3>
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={professorWorkload}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" angle={-45} textAnchor="end" height={80} />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="hours" fill="#8B5CF6" />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          </div>
        </main>
      </div>
    </div>
  )
}
