import { Injectable } from '@angular/core';

export interface ICalEvent {
  uid: string;
  summary: string;
  description?: string;
  location?: string;
  dtstart: string; // format: YYYYMMDDTHHMMSS
  dtend: string;
  rrule?: string;  // ex: FREQ=WEEKLY;BYDAY=MO
}

@Injectable({ providedIn: 'root' })
export class ICalExportService {

  /**
   * Génère un fichier .ics à partir d'une liste d'événements et le télécharge.
   */
  exportToIcal(events: ICalEvent[], filename = 'emploi-du-temps.ics'): void {
    const lines: string[] = [
      'BEGIN:VCALENDAR',
      'VERSION:2.0',
      'PRODID:-//IUSJC EduSchedule//FR',
      'CALSCALE:GREGORIAN',
      'METHOD:PUBLISH',
      'X-WR-CALNAME:Emploi du temps IUSJC',
      'X-WR-TIMEZONE:Africa/Douala',
    ];

    for (const event of events) {
      lines.push('BEGIN:VEVENT');
      lines.push(`UID:${event.uid}`);
      lines.push(`DTSTAMP:${this.nowUtc()}`);
      lines.push(`DTSTART;TZID=Africa/Douala:${event.dtstart}`);
      lines.push(`DTEND;TZID=Africa/Douala:${event.dtend}`);
      lines.push(`SUMMARY:${this.escape(event.summary)}`);
      if (event.description) lines.push(`DESCRIPTION:${this.escape(event.description)}`);
      if (event.location)    lines.push(`LOCATION:${this.escape(event.location)}`);
      if (event.rrule)       lines.push(`RRULE:${event.rrule}`);
      lines.push('END:VEVENT');
    }

    lines.push('END:VCALENDAR');

    const content = lines.join('\r\n');
    const blob = new Blob([content], { type: 'text/calendar;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    URL.revokeObjectURL(url);
  }

  /**
   * Convertit une séance (jour + heure) en format iCal YYYYMMDDTHHMMSS
   * en se basant sur le lundi de la semaine courante.
   */
  seanceToICalDate(jour: string, heure: string, semaineOffset = 0): string {
    const dayIndex: Record<string, number> = {
      Lundi: 0, Mardi: 1, Mercredi: 2, Jeudi: 3, Vendredi: 4, Samedi: 5
    };
    const today = new Date();
    const monday = new Date(today);
    monday.setDate(today.getDate() - ((today.getDay() + 6) % 7) + semaineOffset * 7);

    const targetDay = new Date(monday);
    targetDay.setDate(monday.getDate() + (dayIndex[jour] ?? 0));

    const [h, m] = heure.split(':').map(Number);
    targetDay.setHours(h, m, 0, 0);

    const pad = (n: number) => String(n).padStart(2, '0');
    return `${targetDay.getFullYear()}${pad(targetDay.getMonth() + 1)}${pad(targetDay.getDate())}` +
           `T${pad(targetDay.getHours())}${pad(targetDay.getMinutes())}00`;
  }

  private nowUtc(): string {
    const now = new Date();
    const pad = (n: number) => String(n).padStart(2, '0');
    return `${now.getUTCFullYear()}${pad(now.getUTCMonth() + 1)}${pad(now.getUTCDate())}` +
           `T${pad(now.getUTCHours())}${pad(now.getUTCMinutes())}${pad(now.getUTCSeconds())}Z`;
  }

  private escape(str: string): string {
    return str.replace(/\\/g, '\\\\').replace(/;/g, '\\;').replace(/,/g, '\\,').replace(/\n/g, '\\n');
  }
}
