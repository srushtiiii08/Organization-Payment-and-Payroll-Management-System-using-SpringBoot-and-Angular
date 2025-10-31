import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface Alert {
  type: 'success' | 'error' | 'warning' | 'info';
  message: string;
}

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  private alertSubject = new Subject<Alert>();

  getAlert(): Observable<Alert> {
    return this.alertSubject.asObservable();
  }

  success(message: string): void {
    this.alertSubject.next({ type: 'success', message });
  }

  error(message: string): void {
    this.alertSubject.next({ type: 'error', message });
  }

  warning(message: string): void {
    this.alertSubject.next({ type: 'warning', message });
  }

  info(message: string): void {
    this.alertSubject.next({ type: 'info', message });
  }
}