'use client'

import React, { useState, useEffect } from 'react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'
import { Alert, AlertDescription } from '@/components/ui/alert'
import { Progress } from '@/components/ui/progress'
import { 
  AlertTriangle, 
  Clock, 
  Users, 
  MapPin, 
  CheckCircle, 
  XCircle,
  RefreshCw,
  Zap,
  TrendingUp,
  Calendar,
  Filter
} from 'lucide-react'

interface Conflict {
  id: string
  type: 'ROOM_DOUBLE_BOOKING' | 'TEACHER_OVERLAP' | 'RESOURCE_UNAVAILABLE' | 'SCHEDULE_CONFLICT'
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL'
  title: string
  description: string
  affectedSchedules: Schedule[]
  suggestedResolutions: Resolution[]
  autoResolvable: boolean
  createdAt: string
  estimatedImpact: number
  affectedUsers: number
}

interface Schedule {
  id: string
  title: string
  startTime: string
  endTime: string
  room: string
  teacher: string
  students: number
  subject: string
}

interface Resolution {
  id: string
  type: 'RESCHEDULE' | 'REASSIGN_ROOM' | 'SPLIT_CLASS' | 'CANCEL' | 'MERGE'
  title: string
  description: string
  impact: 'LOW' | 'MEDIUM' | 'HIGH'
  effort: number
  success_probability: number
  estimated_time: number
}

interface ConflictStats {
  total: number
  critical: number
  high: number
  medium: number
  low: number
  resolved_today: number
  auto_resolved: number
  avg_resolution_time: number
}

export default function ConflictResolution() {
  const [conflicts, setConflicts] = useState<Conflict[]>([])
  const [stats, setStats] = useState<ConflictStats | null>(null)
  const [selectedConflict, setSelectedConflict] = useState<Conflict | null>(null)
  const [loading, setLoading] = useState(true)
  const [resolving, setResolving] = useState<string | null>(null)
  const [filter, setFilter] = useState<string>('ALL')
  const [autoResolveEnabled, setAutoResolveEnabled] = useState(true)

  useEffect(() => {
    loadConflicts()
    loadStats()
    
    // Actualisation automatique toutes les 30 secondes
    const interval = setInterval(() => {
      loadConflicts()
      loadStats()
    }, 30000)

    return () => clearInterval(interval)
  }, [])

  const loadConflicts = async () => {
    try {
      const response = await fetch('/api/conflicts/detect')
      const data = await response.json()
      setConflicts(data.conflicts || [])
    } catch (error) {
      console.error('Erreur lors du chargement des conflits:', error)
    } finally {
      setLoading(false)
    }
  }

  const loadStats = async () => {
    try {
      const response = await fetch('/api/conflicts/statistics')
      const data = await response.json()
      setStats(data)
    } catch (error) {
      console.error('Erreur lors du chargement des statistiques:', error)
    }
  }

  const resolveConflict = async (conflictId: string, resolutionId: string) => {
    setResolving(conflictId)
    try {
      const response = await fetch(`/api/conflicts/resolve/${conflictId}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ resolutionId })
      })

      if (response.ok) {
        await loadConflicts()
        await loadStats()
        setSelectedConflict(null)
      }
    } catch (error) {
      console.error('Erreur lors de la résolution:', error)
    } finally {
      setResolving(null)
    }
  }

  const autoResolveConflicts = async () => {
    try {
      const response = await fetch('/api/conflicts/auto-resolve', {
        method: 'PUT'
      })

      if (response.ok) {
        await loadConflicts()
        await loadStats()
      }
    } catch (error) {
      console.error('Erreur lors de la résolution automatique:', error)
    }
  }

  const getSeverityColor = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return 'bg-red-500'
      case 'HIGH': return 'bg-orange-500'
      case 'MEDIUM': return 'bg-yellow-500'
      case 'LOW': return 'bg-blue-500'
      default: return 'bg-gray-500'
    }
  }

  const getSeverityIcon = (severity: string) => {
    switch (severity) {
      case 'CRITICAL': return <XCircle className="h-4 w-4" />
      case 'HIGH': return <AlertTriangle className="h-4 w-4" />
      case 'MEDIUM': return <Clock className="h-4 w-4" />
      case 'LOW': return <CheckCircle className="h-4 w-4" />
      default: return <AlertTriangle className="h-4 w-4" />
    }
  }

  const getTypeLabel = (type: string) => {
    switch (type) {
      case 'ROOM_DOUBLE_BOOKING': return 'Double réservation'
      case 'TEACHER_OVERLAP': return 'Conflit enseignant'
      case 'RESOURCE_UNAVAILABLE': return 'Ressource indisponible'
      case 'SCHEDULE_CONFLICT': return 'Conflit d\'horaire'
      default: return type
    }
  }

  const filteredConflicts = conflicts.filter(conflict => {
    if (filter === 'ALL') return true
    return conflict.severity === filter
  })

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <RefreshCw className="h-8 w-8 animate-spin" />
        <span className="ml-2">Chargement des conflits...</span>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* En-tête avec statistiques */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Résolution de Conflits</h1>
          <p className="text-gray-600">Gestion intelligente des conflits de planification</p>
        </div>
        <div className="flex gap-2">
          <Button 
            onClick={autoResolveConflicts}
            disabled={!autoResolveEnabled}
            className="bg-green-600 hover:bg-green-700"
          >
            <Zap className="h-4 w-4 mr-2" />
            Résolution Auto
          </Button>
          <Button onClick={loadConflicts} variant="outline">
            <RefreshCw className="h-4 w-4 mr-2" />
            Actualiser
          </Button>
        </div>
      </div>

      {/* Statistiques */}
      {stats && (
        <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-8 gap-4">
          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Total</p>
                  <p className="text-2xl font-bold">{stats.total}</p>
                </div>
                <AlertTriangle className="h-8 w-8 text-gray-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Critiques</p>
                  <p className="text-2xl font-bold text-red-600">{stats.critical}</p>
                </div>
                <XCircle className="h-8 w-8 text-red-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Élevés</p>
                  <p className="text-2xl font-bold text-orange-600">{stats.high}</p>
                </div>
                <AlertTriangle className="h-8 w-8 text-orange-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Moyens</p>
                  <p className="text-2xl font-bold text-yellow-600">{stats.medium}</p>
                </div>
                <Clock className="h-8 w-8 text-yellow-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Faibles</p>
                  <p className="text-2xl font-bold text-blue-600">{stats.low}</p>
                </div>
                <CheckCircle className="h-8 w-8 text-blue-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Résolus</p>
                  <p className="text-2xl font-bold text-green-600">{stats.resolved_today}</p>
                </div>
                <CheckCircle className="h-8 w-8 text-green-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Auto</p>
                  <p className="text-2xl font-bold text-purple-600">{stats.auto_resolved}</p>
                </div>
                <Zap className="h-8 w-8 text-purple-400" />
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardContent className="p-4">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm text-gray-600">Temps Moy.</p>
                  <p className="text-2xl font-bold">{stats.avg_resolution_time}min</p>
                </div>
                <TrendingUp className="h-8 w-8 text-gray-400" />
              </div>
            </CardContent>
          </Card>
        </div>
      )}

      <Tabs defaultValue="conflicts" className="space-y-4">
        <TabsList>
          <TabsTrigger value="conflicts">Conflits Actifs</TabsTrigger>
          <TabsTrigger value="timeline">Timeline</TabsTrigger>
          <TabsTrigger value="analytics">Analyses</TabsTrigger>
        </TabsList>

        <TabsContent value="conflicts" className="space-y-4">
          {/* Filtres */}
          <div className="flex gap-2 items-center">
            <Filter className="h-4 w-4" />
            <span className="text-sm font-medium">Filtrer par sévérité:</span>
            {['ALL', 'CRITICAL', 'HIGH', 'MEDIUM', 'LOW'].map(severity => (
              <Button
                key={severity}
                variant={filter === severity ? 'default' : 'outline'}
                size="sm"
                onClick={() => setFilter(severity)}
              >
                {severity === 'ALL' ? 'Tous' : severity}
              </Button>
            ))}
          </div>

          {/* Liste des conflits */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Liste des conflits */}
            <div className="space-y-4">
              <h2 className="text-xl font-semibold">
                Conflits Détectés ({filteredConflicts.length})
              </h2>
              
              {filteredConflicts.length === 0 ? (
                <Card>
                  <CardContent className="p-8 text-center">
                    <CheckCircle className="h-12 w-12 text-green-500 mx-auto mb-4" />
                    <h3 className="text-lg font-medium mb-2">Aucun conflit détecté</h3>
                    <p className="text-gray-600">Tous les emplois du temps sont cohérents</p>
                  </CardContent>
                </Card>
              ) : (
                filteredConflicts.map(conflict => (
                  <Card 
                    key={conflict.id}
                    className={`cursor-pointer transition-all hover:shadow-md ${
                      selectedConflict?.id === conflict.id ? 'ring-2 ring-blue-500' : ''
                    }`}
                    onClick={() => setSelectedConflict(conflict)}
                  >
                    <CardContent className="p-4">
                      <div className="flex items-start justify-between mb-3">
                        <div className="flex items-center gap-2">
                          {getSeverityIcon(conflict.severity)}
                          <Badge className={getSeverityColor(conflict.severity)}>
                            {conflict.severity}
                          </Badge>
                          <Badge variant="outline">
                            {getTypeLabel(conflict.type)}
                          </Badge>
                        </div>
                        {conflict.autoResolvable && (
                          <Badge className="bg-green-100 text-green-800">
                            Auto-résolvable
                          </Badge>
                        )}
                      </div>

                      <h3 className="font-semibold mb-2">{conflict.title}</h3>
                      <p className="text-sm text-gray-600 mb-3">{conflict.description}</p>

                      <div className="flex items-center gap-4 text-sm text-gray-500">
                        <div className="flex items-center gap-1">
                          <Users className="h-4 w-4" />
                          {conflict.affectedUsers} utilisateurs
                        </div>
                        <div className="flex items-center gap-1">
                          <Calendar className="h-4 w-4" />
                          {conflict.affectedSchedules.length} cours
                        </div>
                        <div className="flex items-center gap-1">
                          <TrendingUp className="h-4 w-4" />
                          Impact: {conflict.estimatedImpact}%
                        </div>
                      </div>

                      <div className="mt-3">
                        <div className="flex justify-between text-xs text-gray-500 mb-1">
                          <span>Probabilité de résolution</span>
                          <span>{Math.max(...conflict.suggestedResolutions.map(r => r.success_probability))}%</span>
                        </div>
                        <Progress 
                          value={Math.max(...conflict.suggestedResolutions.map(r => r.success_probability))} 
                          className="h-2"
                        />
                      </div>
                    </CardContent>
                  </Card>
                ))
              )}
            </div>

            {/* Détails du conflit sélectionné */}
            <div className="space-y-4">
              {selectedConflict ? (
                <>
                  <h2 className="text-xl font-semibold">Détails du Conflit</h2>
                  
                  <Card>
                    <CardHeader>
                      <CardTitle className="flex items-center gap-2">
                        {getSeverityIcon(selectedConflict.severity)}
                        {selectedConflict.title}
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="space-y-4">
                      <div>
                        <h4 className="font-medium mb-2">Cours Affectés</h4>
                        <div className="space-y-2">
                          {selectedConflict.affectedSchedules.map(schedule => (
                            <div key={schedule.id} className="p-3 bg-gray-50 rounded-lg">
                              <div className="flex justify-between items-start mb-2">
                                <h5 className="font-medium">{schedule.title}</h5>
                                <Badge variant="outline">{schedule.subject}</Badge>
                              </div>
                              <div className="grid grid-cols-2 gap-2 text-sm text-gray-600">
                                <div className="flex items-center gap-1">
                                  <Clock className="h-3 w-3" />
                                  {new Date(schedule.startTime).toLocaleTimeString()} - 
                                  {new Date(schedule.endTime).toLocaleTimeString()}
                                </div>
                                <div className="flex items-center gap-1">
                                  <MapPin className="h-3 w-3" />
                                  {schedule.room}
                                </div>
                                <div className="flex items-center gap-1">
                                  <Users className="h-3 w-3" />
                                  {schedule.teacher}
                                </div>
                                <div className="flex items-center gap-1">
                                  <Users className="h-3 w-3" />
                                  {schedule.students} étudiants
                                </div>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>

                      <div>
                        <h4 className="font-medium mb-2">Solutions Suggérées</h4>
                        <div className="space-y-2">
                          {selectedConflict.suggestedResolutions.map(resolution => (
                            <div key={resolution.id} className="p-3 border rounded-lg">
                              <div className="flex justify-between items-start mb-2">
                                <h5 className="font-medium">{resolution.title}</h5>
                                <div className="flex gap-2">
                                  <Badge 
                                    variant="outline"
                                    className={
                                      resolution.impact === 'LOW' ? 'text-green-600' :
                                      resolution.impact === 'MEDIUM' ? 'text-yellow-600' :
                                      'text-red-600'
                                    }
                                  >
                                    Impact: {resolution.impact}
                                  </Badge>
                                  <Badge variant="outline">
                                    {resolution.success_probability}% succès
                                  </Badge>
                                </div>
                              </div>
                              <p className="text-sm text-gray-600 mb-3">{resolution.description}</p>
                              
                              <div className="flex justify-between items-center">
                                <div className="text-xs text-gray-500">
                                  Effort: {resolution.effort}h • Temps: {resolution.estimated_time}min
                                </div>
                                <Button
                                  size="sm"
                                  onClick={() => resolveConflict(selectedConflict.id, resolution.id)}
                                  disabled={resolving === selectedConflict.id}
                                >
                                  {resolving === selectedConflict.id ? (
                                    <RefreshCw className="h-4 w-4 animate-spin mr-2" />
                                  ) : null}
                                  Appliquer
                                </Button>
                              </div>
                            </div>
                          ))}
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </>
              ) : (
                <Card>
                  <CardContent className="p-8 text-center">
                    <AlertTriangle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium mb-2">Sélectionnez un conflit</h3>
                    <p className="text-gray-600">Cliquez sur un conflit pour voir les détails et solutions</p>
                  </CardContent>
                </Card>
              )}
            </div>
          </div>
        </TabsContent>

        <TabsContent value="timeline">
          <Card>
            <CardHeader>
              <CardTitle>Timeline des Conflits</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {conflicts.slice(0, 10).map(conflict => (
                  <div key={conflict.id} className="flex items-center gap-4 p-3 border-l-4 border-blue-500 bg-blue-50">
                    <div className="flex-shrink-0">
                      {getSeverityIcon(conflict.severity)}
                    </div>
                    <div className="flex-1">
                      <h4 className="font-medium">{conflict.title}</h4>
                      <p className="text-sm text-gray-600">{conflict.description}</p>
                    </div>
                    <div className="text-xs text-gray-500">
                      {new Date(conflict.createdAt).toLocaleString()}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value="analytics">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Répartition par Type</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  {Object.entries(
                    conflicts.reduce((acc, conflict) => {
                      acc[conflict.type] = (acc[conflict.type] || 0) + 1
                      return acc
                    }, {} as Record<string, number>)
                  ).map(([type, count]) => (
                    <div key={type} className="flex justify-between items-center">
                      <span className="text-sm">{getTypeLabel(type)}</span>
                      <div className="flex items-center gap-2">
                        <div className="w-20 bg-gray-200 rounded-full h-2">
                          <div 
                            className="bg-blue-500 h-2 rounded-full" 
                            style={{ width: `${(count / conflicts.length) * 100}%` }}
                          />
                        </div>
                        <span className="text-sm font-medium">{count}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Tendances de Résolution</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div className="flex justify-between items-center">
                    <span>Taux de résolution automatique</span>
                    <span className="font-bold text-green-600">
                      {stats ? Math.round((stats.auto_resolved / stats.resolved_today) * 100) : 0}%
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span>Temps moyen de résolution</span>
                    <span className="font-bold">
                      {stats?.avg_resolution_time || 0} minutes
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span>Conflits résolus aujourd'hui</span>
                    <span className="font-bold text-blue-600">
                      {stats?.resolved_today || 0}
                    </span>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}