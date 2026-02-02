'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { 
  ChevronLeft, 
  ChevronRight, 
  Calendar as CalendarIcon, 
  Clock, 
  MapPin, 
  Users,
  RefreshCw,
  Download
} from 'lucide-react';
import { calendarApi, WeeklySchedule, CalendarEvent } from '@/lib/api/calendar';
import { format, addWeeks, subWeeks, startOfWeek, addDays } from 'date-fns';
import { fr } from 'date-fns/locale';

interface WeeklyCalendarProps {
  userId: string;
  type?: 'user' | 'teacher' | 'school' | 'room';
  entityId?: string;
  className?: string;
}

const DAYS_OF_WEEK = [
  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY'
];

const DAY_NAMES = [
  'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'
];

export function WeeklyCalendar({ userId, type = 'user', entityId, className }: WeeklyCalendarProps) {
  const [currentWeek, setCurrentWeek] = useState<Date>(startOfWeek(new Date(), { weekStartsOn: 1 }));
  const [schedule, setSchedule] = useState<WeeklySchedule | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [syncing, setSyncing] = useState(false);

  const loadSchedule = async () => {
    setLoading(true);
    setError(null);

    try {
      const weekStart = format(currentWeek, 'yyyy-MM-dd');
      let response;

      switch (type) {
        case 'teacher':
          response = await calendarApi.getTeacherWeeklySchedule(entityId || userId, weekStart);
          break;
        case 'school':
          response = await calendarApi.getSchoolWeeklySchedule(entityId || userId, weekStart);
          break;
        case 'room':
          response = await calendarApi.getRoomWeeklySchedule(entityId || userId, weekStart);
          break;
        default:
          response = await calendarApi.getWeeklySchedule(userId, weekStart);
      }

      if (response.success) {
        setSchedule(response.data);
      } else {
        setError(response.error || 'Erreur lors du chargement de l\'emploi du temps');
      }
    } catch (err) {
      setError('Erreur de connexion');
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async () => {
    setSyncing(true);
    try {
      const response = await calendarApi.syncCalendars(userId);
      if (response.success) {
        await loadSchedule(); // Recharger après synchronisation
      } else {
        setError(response.error || 'Erreur lors de la synchronisation');
      }
    } catch (err) {
      setError('Erreur lors de la synchronisation');
    } finally {
      setSyncing(false);
    }
  };

  const navigateWeek = (direction: 'prev' | 'next') => {
    setCurrentWeek(prev => 
      direction === 'prev' ? subWeeks(prev, 1) : addWeeks(prev, 1)
    );
  };

  const goToCurrentWeek = () => {
    setCurrentWeek(startOfWeek(new Date(), { weekStartsOn: 1 }));
  };

  useEffect(() => {
    loadSchedule();
  }, [currentWeek, userId, type, entityId]);

  const getEventColor = (event: CalendarEvent) => {
    if (event.scheduleId) return 'bg-blue-100 text-blue-800 border-blue-200';
    if (event.reservationId) return 'bg-green-100 text-green-800 border-green-200';
    return 'bg-gray-100 text-gray-800 border-gray-200';
  };

  const formatTime = (dateTime: string) => {
    return format(new Date(dateTime), 'HH:mm');
  };

  const formatDuration = (start: string, end: string) => {
    const startTime = new Date(start);
    const endTime = new Date(end);
    const duration = (endTime.getTime() - startTime.getTime()) / (1000 * 60);
    const hours = Math.floor(duration / 60);
    const minutes = duration % 60;
    
    if (hours > 0 && minutes > 0) {
      return `${hours}h${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else {
      return `${minutes}m`;
    }
  };

  if (loading) {
    return (
      <Card className={className}>
        <CardHeader>
          <Skeleton className="h-8 w-64" />
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-7 gap-4">
            {Array.from({ length: 7 }).map((_, i) => (
              <div key={i} className="space-y-2">
                <Skeleton className="h-6 w-full" />
                <Skeleton className="h-20 w-full" />
                <Skeleton className="h-16 w-full" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card className={className}>
        <CardContent className="pt-6">
          <Alert variant="destructive">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
          <Button onClick={loadSchedule} className="mt-4">
            Réessayer
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <CalendarIcon className="h-5 w-5" />
            Emploi du temps - Semaine du {format(currentWeek, 'dd MMMM yyyy', { locale: fr })}
          </CardTitle>
          
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleSync}
              disabled={syncing}
            >
              <RefreshCw className={`h-4 w-4 ${syncing ? 'animate-spin' : ''}`} />
              {syncing ? 'Sync...' : 'Synchroniser'}
            </Button>
            
            <div className="flex items-center gap-1">
              <Button
                variant="outline"
                size="sm"
                onClick={() => navigateWeek('prev')}
              >
                <ChevronLeft className="h-4 w-4" />
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={goToCurrentWeek}
              >
                Aujourd'hui
              </Button>
              
              <Button
                variant="outline"
                size="sm"
                onClick={() => navigateWeek('next')}
              >
                <ChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        </div>

        {schedule && (
          <div className="flex items-center gap-4 text-sm text-muted-foreground">
            <span className="flex items-center gap-1">
              <CalendarIcon className="h-4 w-4" />
              {schedule.totalEvents} événements
            </span>
            <span className="flex items-center gap-1">
              <Clock className="h-4 w-4" />
              {schedule.totalHours}h programmées
            </span>
          </div>
        )}
      </CardHeader>

      <CardContent>
        {schedule && (
          <div className="grid grid-cols-7 gap-4">
            {DAYS_OF_WEEK.map((dayKey, index) => {
              const dayDate = addDays(currentWeek, index);
              const dayEvents = schedule.dailySchedules[dayKey] || [];
              const isToday = format(dayDate, 'yyyy-MM-dd') === format(new Date(), 'yyyy-MM-dd');

              return (
                <div key={dayKey} className="space-y-2">
                  <div className={`text-center p-2 rounded-lg ${
                    isToday ? 'bg-primary text-primary-foreground' : 'bg-muted'
                  }`}>
                    <div className="font-medium">{DAY_NAMES[index]}</div>
                    <div className="text-sm">
                      {format(dayDate, 'dd/MM')}
                    </div>
                  </div>

                  <div className="space-y-2 min-h-[200px]">
                    {dayEvents.length === 0 ? (
                      <div className="text-center text-muted-foreground text-sm py-8">
                        Aucun événement
                      </div>
                    ) : (
                      dayEvents.map((event) => (
                        <div
                          key={event.id}
                          className={`p-3 rounded-lg border ${getEventColor(event)} hover:shadow-md transition-shadow cursor-pointer`}
                        >
                          <div className="font-medium text-sm mb-1 line-clamp-2">
                            {event.title}
                          </div>
                          
                          <div className="flex items-center gap-1 text-xs mb-1">
                            <Clock className="h-3 w-3" />
                            {formatTime(event.startTime)} - {formatTime(event.endTime)}
                            <span className="text-muted-foreground">
                              ({formatDuration(event.startTime, event.endTime)})
                            </span>
                          </div>

                          {event.location && (
                            <div className="flex items-center gap-1 text-xs mb-1">
                              <MapPin className="h-3 w-3" />
                              <span className="truncate">{event.location}</span>
                            </div>
                          )}

                          {event.attendees && (
                            <div className="flex items-center gap-1 text-xs mb-2">
                              <Users className="h-3 w-3" />
                              <span className="truncate">Participants</span>
                            </div>
                          )}

                          <div className="flex items-center justify-between">
                            <Badge variant="secondary" className="text-xs">
                              {event.scheduleId ? 'Cours' : event.reservationId ? 'Réservation' : 'Événement'}
                            </Badge>
                            
                            {event.syncStatus && (
                              <Badge 
                                variant={event.syncStatus === 'SYNCED' ? 'default' : 'destructive'}
                                className="text-xs"
                              >
                                {event.syncStatus === 'SYNCED' ? 'Sync' : 'Erreur'}
                              </Badge>
                            )}
                          </div>

                          {event.description && (
                            <div className="text-xs text-muted-foreground mt-2 line-clamp-2">
                              {event.description}
                            </div>
                          )}
                        </div>
                      ))
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}

        {schedule && Object.keys(schedule.eventsByType).length > 0 && (
          <div className="mt-6 pt-4 border-t">
            <h4 className="font-medium mb-2">Statistiques de la semaine</h4>
            <div className="flex flex-wrap gap-2">
              {Object.entries(schedule.eventsByType).map(([type, count]) => (
                <Badge key={type} variant="outline">
                  {type}: {count}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}