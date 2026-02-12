'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { 
  Plus, 
  Settings, 
  Trash2, 
  RefreshCw, 
  Calendar as CalendarIcon,
  CheckCircle,
  XCircle,
  Clock,
  AlertTriangle
} from 'lucide-react';
import { calendarApi, CalendarIntegration } from '@/lib/api/calendar';
import { format } from 'date-fns';
import { fr } from 'date-fns/locale';

interface CalendarIntegrationsProps {
  userId: string;
  className?: string;
}

const PROVIDER_ICONS = {
  GOOGLE: '🔗',
  OUTLOOK: '📧',
  APPLE: '🍎',
  ICAL: '📅'
};

const PROVIDER_NAMES = {
  GOOGLE: 'Google Calendar',
  OUTLOOK: 'Outlook Calendar',
  APPLE: 'Apple Calendar',
  ICAL: 'iCal'
};

const SYNC_DIRECTION_LABELS = {
  IMPORT_ONLY: 'Import uniquement',
  EXPORT_ONLY: 'Export uniquement',
  BIDIRECTIONAL: 'Bidirectionnel'
};

const STATUS_COLORS = {
  PENDING: 'bg-yellow-100 text-yellow-800',
  SYNCING: 'bg-blue-100 text-blue-800',
  SUCCESS: 'bg-green-100 text-green-800',
  ERROR: 'bg-red-100 text-red-800',
  DISABLED: 'bg-gray-100 text-gray-800'
};

const STATUS_ICONS = {
  PENDING: Clock,
  SYNCING: RefreshCw,
  SUCCESS: CheckCircle,
  ERROR: XCircle,
  DISABLED: AlertTriangle
};

export function CalendarIntegrations({ userId, className }: CalendarIntegrationsProps) {
  const [integrations, setIntegrations] = useState<CalendarIntegration[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showAddForm, setShowAddForm] = useState(false);

  const loadIntegrations = async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await calendarApi.getUserIntegrations(userId);
      if (response.success) {
        setIntegrations(response.data);
      } else {
        setError(response.error || 'Erreur lors du chargement des intégrations');
      }
    } catch (err) {
      setError('Erreur de connexion');
    } finally {
      setLoading(false);
    }
  };

  const handleSync = async (integrationId: number) => {
    try {
      const response = await calendarApi.syncCalendars(userId);
      if (response.success) {
        await loadIntegrations(); // Recharger les intégrations
      } else {
        setError(response.error || 'Erreur lors de la synchronisation');
      }
    } catch (err) {
      setError('Erreur lors de la synchronisation');
    }
  };

  const toggleIntegration = async (integration: CalendarIntegration) => {
    try {
      const updatedIntegration = {
        ...integration,
        enabled: !integration.enabled
      };

      const response = await calendarApi.createIntegration(updatedIntegration);
      if (response.success) {
        await loadIntegrations();
      } else {
        setError(response.error || 'Erreur lors de la mise à jour');
      }
    } catch (err) {
      setError('Erreur lors de la mise à jour');
    }
  };

  const toggleSync = async (integration: CalendarIntegration) => {
    try {
      const updatedIntegration = {
        ...integration,
        syncEnabled: !integration.syncEnabled
      };

      const response = await calendarApi.createIntegration(updatedIntegration);
      if (response.success) {
        await loadIntegrations();
      } else {
        setError(response.error || 'Erreur lors de la mise à jour');
      }
    } catch (err) {
      setError('Erreur lors de la mise à jour');
    }
  };

  useEffect(() => {
    loadIntegrations();
  }, [userId]);

  const getStatusIcon = (status: CalendarIntegration['syncStatus']) => {
    const Icon = STATUS_ICONS[status];
    return <Icon className={`h-4 w-4 ${status === 'SYNCING' ? 'animate-spin' : ''}`} />;
  };

  if (loading) {
    return (
      <Card className={className}>
        <CardHeader>
          <CardTitle>Intégrations Calendrier</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {Array.from({ length: 2 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between p-4 border rounded-lg">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 bg-gray-200 rounded-lg animate-pulse" />
                  <div className="space-y-2">
                    <div className="w-32 h-4 bg-gray-200 rounded animate-pulse" />
                    <div className="w-24 h-3 bg-gray-200 rounded animate-pulse" />
                  </div>
                </div>
                <div className="w-12 h-6 bg-gray-200 rounded animate-pulse" />
              </div>
            ))}
          </div>
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
            Intégrations Calendrier
          </CardTitle>
          <Button
            onClick={() => setShowAddForm(true)}
            size="sm"
          >
            <Plus className="h-4 w-4 mr-2" />
            Ajouter
          </Button>
        </div>
      </CardHeader>

      <CardContent>
        {error && (
          <Alert variant="destructive" className="mb-4">
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {integrations.length === 0 ? (
          <div className="text-center py-8">
            <CalendarIcon className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium mb-2">Aucune intégration</h3>
            <p className="text-muted-foreground mb-4">
              Connectez vos calendriers externes pour synchroniser automatiquement vos emplois du temps.
            </p>
            <Button onClick={() => setShowAddForm(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Ajouter une intégration
            </Button>
          </div>
        ) : (
          <div className="space-y-4">
            {integrations.map((integration) => (
              <div
                key={integration.id}
                className="flex items-center justify-between p-4 border rounded-lg hover:bg-muted/50 transition-colors"
              >
                <div className="flex items-center gap-4">
                  <div className="text-2xl">
                    {PROVIDER_ICONS[integration.provider]}
                  </div>
                  
                  <div className="space-y-1">
                    <div className="flex items-center gap-2">
                      <h4 className="font-medium">
                        {PROVIDER_NAMES[integration.provider]}
                      </h4>
                      <Badge
                        variant="outline"
                        className={STATUS_COLORS[integration.syncStatus]}
                      >
                        {getStatusIcon(integration.syncStatus)}
                        <span className="ml-1">{integration.syncStatus}</span>
                      </Badge>
                    </div>
                    
                    <div className="text-sm text-muted-foreground">
                      {SYNC_DIRECTION_LABELS[integration.syncDirection]}
                      {integration.lastSyncAt && (
                        <span className="ml-2">
                          • Dernière sync: {format(new Date(integration.lastSyncAt), 'dd/MM/yyyy HH:mm', { locale: fr })}
                        </span>
                      )}
                    </div>
                    
                    {integration.syncError && (
                      <div className="text-sm text-red-600">
                        Erreur: {integration.syncError}
                      </div>
                    )}
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="flex items-center gap-2">
                    <label className="text-sm">Sync</label>
                    <Switch
                      checked={integration.syncEnabled}
                      onCheckedChange={() => toggleSync(integration)}
                      disabled={!integration.enabled}
                    />
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <label className="text-sm">Actif</label>
                    <Switch
                      checked={integration.enabled}
                      onCheckedChange={() => toggleIntegration(integration)}
                    />
                  </div>

                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleSync(integration.id!)}
                    disabled={!integration.enabled || integration.syncStatus === 'SYNCING'}
                  >
                    <RefreshCw className={`h-4 w-4 ${integration.syncStatus === 'SYNCING' ? 'animate-spin' : ''}`} />
                  </Button>

                  <Button
                    variant="outline"
                    size="sm"
                  >
                    <Settings className="h-4 w-4" />
                  </Button>

                  <Button
                    variant="outline"
                    size="sm"
                    className="text-red-600 hover:text-red-700"
                  >
                    <Trash2 className="h-4 w-4" />
                  </Button>
                </div>
              </div>
            ))}
          </div>
        )}

        {showAddForm && (
          <div className="mt-6 p-4 border rounded-lg bg-muted/50">
            <h4 className="font-medium mb-4">Ajouter une intégration</h4>
            <div className="grid grid-cols-2 gap-4">
              {Object.entries(PROVIDER_NAMES).map(([provider, name]) => (
                <Button
                  key={provider}
                  variant="outline"
                  className="h-16 flex-col gap-2"
                  onClick={() => {
                    // Ici, on déclencherait le processus OAuth pour le provider
                    console.log(`Connecting to ${provider}`);
                    setShowAddForm(false);
                  }}
                >
                  <span className="text-2xl">{PROVIDER_ICONS[provider as keyof typeof PROVIDER_ICONS]}</span>
                  <span className="text-sm">{name}</span>
                </Button>
              ))}
            </div>
            <div className="flex justify-end gap-2 mt-4">
              <Button
                variant="outline"
                onClick={() => setShowAddForm(false)}
              >
                Annuler
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  );
}