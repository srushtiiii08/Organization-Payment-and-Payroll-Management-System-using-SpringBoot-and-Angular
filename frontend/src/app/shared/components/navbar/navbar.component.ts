import { Component, inject } from '@angular/core';
import { CommonModule, Location } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarService } from '../../../core/services/sidebar.service'; 

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css']
})
export class NavbarComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  private sidebarService = inject(SidebarService); // Inject the sidebar service
  private location = inject(Location);

  currentUser$ = this.authService.currentUser$;

  //Toggle sidebar when menu button is clicked
  toggleSidebar(): void {
    this.sidebarService.toggleSidebar();
  }

  // Go back to previous page
  goBack(): void {
    this.location.back();
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