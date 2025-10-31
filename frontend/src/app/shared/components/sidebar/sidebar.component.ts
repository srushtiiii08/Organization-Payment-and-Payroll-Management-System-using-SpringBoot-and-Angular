import { Component, inject, HostListener, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarService } from '../../../core/services/sidebar.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  private authService = inject(AuthService);
  private elementRef = inject(ElementRef);
  private sidebarService = inject(SidebarService);

  currentUser$ = this.authService.currentUser$;
  
  // Now we get the open state from the service
  isOpen$ = this.sidebarService.sidebarOpen$;

  // Listen for clicks anywhere on the page
  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    const clickedElement = event.target as HTMLElement;
    const sidebarElement = this.elementRef.nativeElement.querySelector('.sidebar');
    const menuButton = document.querySelector('.menu-btn');
    
    // Check if sidebar is open
    this.sidebarService.sidebarOpen$.subscribe(isOpen => {
      if (isOpen) {
        // Close if clicked outside sidebar AND not on the menu button
        const clickedInsideSidebar = sidebarElement?.contains(clickedElement);
        const clickedOnMenuButton = menuButton?.contains(clickedElement);
        
        if (!clickedInsideSidebar && !clickedOnMenuButton) {
          this.closeSidebar();
        }
      }
    }).unsubscribe(); // Immediately unsubscribe to avoid memory leaks
  }

  //Close sidebar when clicking a nav link (good for mobile)
  closeSidebar(): void {
    this.sidebarService.closeSidebar();
  }

  logout(): void {
    if (confirm('Are you sure you want to logout?')) {
      this.authService.logout();
    }
  }

   getRoleName(role: string): string {
    switch(role) {
      case 'BANK_ADMIN': return 'Bank Admin';
      case 'ORGANIZATION': return 'Organization';
      case 'EMPLOYEE': return 'Employee';
      default: return 'User';
    }
  }
}