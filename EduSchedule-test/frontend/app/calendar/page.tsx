'use client';

import React, { useState } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { WeeklyCalendar } from '@/components/calendar/weekly-calendar';
import { CalendarIntegrations } from '@/components/calendar/calendar-integrations';
import { Sidebar } from '@/components/sidebar';
import { Header } from '@/components/header';
import { AppLayout } from '@/components/app-layout';
import { 
  Calendar as CalendarIcon, 
  Users, 
  School, 
  MapPin,
  Settings
} from 'lucide-react';

export default function CalendarPage() {
  const [selectedView, setSelectedView] = useState<'user' | 'teacher' | 'school' | 'room'>('user');
  const [selectedEntity, setSelectedEntity] = useState<string>('');
  
  // Ces données devraient venir d'une API ou du contexte utilisateur
  const currentUser = {
    id: 'user123',
    email: 'user@example.com',
    role: 'TEACHER'
  };

  const mockTeachers = [
    { id: 'teacher1', name: 'Prof. Martin Dupont' },
    { id: 'teacher2', name: 'Prof. Sophie Bernard' },
    { id: 'teacher3', name: 'Prof. Jean Moreau' }
  ];

  const mockSchools = [
    { id: 'school1', name: 'École Primaire Centre' },
    { id: 'school2', name: 'Collège Victor Hugo' },
    { id: 'school3', name: 'Lycée Jean Jaurès' }
  ];

  const mockRooms = [
    { id: 'room1', name: 'Salle A101' },
    { id: 'room2', name: 'Salle B205' },
    { id: 'room3', name: 'Laboratoire C301' }
  ];

  const getViewTitle = () => {
    switch (selectedView) {
      case 'teacher':
        const teacher = mockTeachers.find(t => t.id === selectedEntity);
        return teacher ? `Emploi du temps - ${teacher.name}` : 'Emploi du temps - Enseignant';
      case 'school':
        const school = mockSchools.find(s => s.id === selectedEntity);
        return school ? `Emploi du temps - ${school.name}` : 'Emploi du temps - École';
      case 'room':
        const room = mockRooms.find(r => r.id === selectedEntity);
        return room ? `Emploi du temps - ${room.name}` : 'Emploi du temps - Salle';
      default:
        return 'Mon emploi du temps';
    }
  };

  const getViewIcon = () => {
    switch (selectedView) {
      case 'teacher':
        return <Users className="h-5 w-5" />;
      case 'school':
        return <School className="h-5 w-5" />;
      case 'room':
        return <MapPin className="h-5 w-5" />;
      default:
        return <CalendarIcon className="h-5 w-5" />;
    }
  };

  return (
    <AppLayout 
      activePage="calendar" 
      title="Calendrier Intégré" 
      subtitle="Gérez vos emplois du temps et synchronisez avec vos calendriers externes"
      action={
        <Button variant="outline">
          <Settings className="h-4 w-4 mr-2" />
          Paramètres
        </Button>
      }
    >
      <Tabs defaultValue="calendar" className="space-y-6">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="calendar">Calendrier</TabsTrigger>
          <TabsTrigger value="integrations">Intégrations</TabsTrigger>
        </TabsList>

        <TabsContent value="calendar" className="space-y-6">
          <Card>
            <CardHeader>
              <div className="flex items-center justify-between">
                <CardTitle className="flex items-center gap-2">
                  {getViewIcon()}
                  {getViewTitle()}
                </CardTitle>
                
                <div className="flex items-center gap-4">
                  <Select
                    value={selectedView}
                    onValueChange={(value: 'user' | 'teacher' | 'school' | 'room') => {
                      setSelectedView(value);
                      setSelectedEntity('');
                    }}
                  >
                    <SelectTrigger className="w-48">
                      <SelectValue placeholder="Type de vue" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="user">Mon emploi du temps</SelectItem>
                      <SelectItem value="teacher">Enseignant</SelectItem>
                      <SelectItem value="school">École</SelectItem>
                      <SelectItem value="room">Salle</SelectItem>
                    </SelectContent>
                  </Select>

                  {selectedView === 'teacher' && (
                    <Select
                      value={selectedEntity}
                      onValueChange={setSelectedEntity}
                    >
                      <SelectTrigger className="w-48">
                        <SelectValue placeholder="Sélectionner un enseignant" />
                      </SelectTrigger>
                      <SelectContent>
                        {mockTeachers.map((teacher) => (
                          <SelectItem key={teacher.id} value={teacher.id}>
                            {teacher.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}

                  {selectedView === 'school' && (
                    <Select
                      value={selectedEntity}
                      onValueChange={setSelectedEntity}
                    >
                      <SelectTrigger className="w-48">
                        <SelectValue placeholder="Sélectionner une école" />
                      </SelectTrigger>
                      <SelectContent>
                        {mockSchools.map((school) => (
                          <SelectItem key={school.id} value={school.id}>
                            {school.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}

                  {selectedView === 'room' && (
                    <Select
                      value={selectedEntity}
                      onValueChange={setSelectedEntity}
                    >
                      <SelectTrigger className="w-48">
                        <SelectValue placeholder="Sélectionner une salle" />
                      </SelectTrigger>
                      <SelectContent>
                        {mockRooms.map((room) => (
                          <SelectItem key={room.id} value={room.id}>
                            {room.name}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                </div>
              </div>
            </CardHeader>
          </Card>

          <WeeklyCalendar
            userId={currentUser.id}
            type={selectedView}
            entityId={selectedEntity || undefined}
          />
        </TabsContent>

        <TabsContent value="integrations" className="space-y-6">
          <CalendarIntegrations userId={currentUser.id} />
          
          <Card>
            <CardHeader>
              <CardTitle>Guide d'intégration</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <h4 className="font-medium mb-2">Google Calendar</h4>
                  <p className="text-sm text-muted-foreground mb-3">
                    Synchronisez avec votre compte Google pour un accès complet à vos événements.
                  </p>
                  <ul className="text-sm space-y-1">
                    <li>• Synchronisation bidirectionnelle</li>
                    <li>• Notifications en temps réel</li>
                    <li>• Partage de calendriers</li>
                  </ul>
                </div>
                
                <div>
                  <h4 className="font-medium mb-2">Outlook Calendar</h4>
                  <p className="text-sm text-muted-foreground mb-3">
                    Intégrez avec Microsoft Outlook pour une synchronisation professionnelle.
                  </p>
                  <ul className="text-sm space-y-1">
                    <li>• Intégration Office 365</li>
                    <li>• Gestion des salles de réunion</li>
                    <li>• Calendriers partagés</li>
                  </ul>
                </div>
              </div>
              
              <div className="pt-4 border-t">
                <h4 className="font-medium mb-2">Fonctionnalités de synchronisation</h4>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                  <div>
                    <strong>Import uniquement:</strong> Les événements de votre calendrier externe apparaissent dans EduSchedule
                  </div>
                  <div>
                    <strong>Export uniquement:</strong> Vos cours EduSchedule sont ajoutés à votre calendrier externe
                  </div>
                  <div>
                    <strong>Bidirectionnel:</strong> Synchronisation complète dans les deux sens
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </AppLayout>
  );
}