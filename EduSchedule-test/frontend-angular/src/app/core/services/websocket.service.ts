import { Injectable, OnDestroy } from '@angular/core';
import { Subject, Observable, BehaviorSubject } from 'rxjs';
import { filter } from 'rxjs/operators';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { environment } from '../../../environments/environment';

export interface WsMessage {
  type: string;
  title: string;
  message: string;
  data?: any;
  timestamp: string;
}

export interface ReservationEvent {
  event: string;
  reservationId: number;
  title: string;
  status: string;
  timestamp: string;
}

export interface ScheduleChangeEvent {
  eventType: string;
  scheduleId: number;
  courseName: string;
  room: string;
  changeDescription: string;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class WebSocketService implements OnDestroy {

  private client: Client | null = null;
  private subscriptions: StompSubscription[] = [];

  private connected$ = new BehaviorSubject<boolean>(false);
  private notifications$ = new Subject<WsMessage>();
  private reservations$  = new Subject<ReservationEvent>();
  private schedule$      = new Subject<ScheduleChangeEvent>();

  // WebSocket natif STOMP — brokerURL pointe vers le endpoint /ws/websocket du notification-service
  private wsUrl = `${((environment as any).wsUrl || 'ws://localhost:8087')}/ws/websocket`;

  get isConnected$(): Observable<boolean> { return this.connected$.asObservable(); }
  get allNotifications$(): Observable<WsMessage> { return this.notifications$.asObservable(); }
  get reservationEvents$(): Observable<ReservationEvent> { return this.reservations$.asObservable(); }
  get scheduleChanges$(): Observable<ScheduleChangeEvent> { return this.schedule$.asObservable(); }

  notificationsOfType$(type: string): Observable<WsMessage> {
    return this.notifications$.pipe(filter(n => n.type === type));
  }

  userNotifications$(userId: number): Observable<WsMessage> {
    return new Observable(observer => {
      if (!this.client?.connected) { observer.complete(); return; }
      const sub = this.client.subscribe(`/topic/notifications/${userId}`, (msg: IMessage) => {
        try { observer.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      });
      return () => sub.unsubscribe();
    });
  }

  connect(): void {
    if (this.client?.connected) return;

    this.client = new Client({
      brokerURL: this.wsUrl,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        this.connected$.next(true);
        this.subscribeToTopics();
      },
      onDisconnect: () => {
        this.connected$.next(false);
        this.subscriptions = [];
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame.headers['message']);
        this.connected$.next(false);
      }
    });

    this.client.activate();
  }

  disconnect(): void {
    this.subscriptions.forEach(s => { try { s.unsubscribe(); } catch { /* ignore */ } });
    this.subscriptions = [];
    this.client?.deactivate();
    this.connected$.next(false);
  }

  private subscribeToTopics(): void {
    if (!this.client) return;
    this.subscriptions.push(
      this.client.subscribe('/topic/notifications', (msg: IMessage) => {
        try { this.notifications$.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      })
    );
    this.subscriptions.push(
      this.client.subscribe('/topic/reservations', (msg: IMessage) => {
        try { this.reservations$.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      })
    );
    this.subscriptions.push(
      this.client.subscribe('/topic/schedule', (msg: IMessage) => {
        try { this.schedule$.next(JSON.parse(msg.body)); } catch { /* ignore */ }
      })
    );
  }

  ngOnDestroy(): void { this.disconnect(); }
}
