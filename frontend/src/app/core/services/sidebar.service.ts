import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SidebarService {
  // BehaviorSubject to track if sidebar is open or closed
  // Starting with 'false' means sidebar is hidden by default
  private sidebarOpen = new BehaviorSubject<boolean>(false);
  
  // Observable that components can subscribe to
  sidebarOpen$ = this.sidebarOpen.asObservable();

  // Method to toggle sidebar open/closed
  toggleSidebar(): void {
    this.sidebarOpen.next(!this.sidebarOpen.value);
  }

  // Method to close sidebar (useful for mobile)
  closeSidebar(): void {
    this.sidebarOpen.next(false);
  }

  // Method to check current state
  isSidebarOpen(): boolean {
    return this.sidebarOpen.value;
  }
}