import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AlertService, Alert } from '../../../core/services/alert.service';

@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './alert.component.html',
  styleUrls: ['./alert.component.css']
})
export class AlertComponent implements OnInit {
  private alertService = inject(AlertService);
  
  alert: Alert | null = null;

  ngOnInit(): void {
    this.alertService.getAlert().subscribe(alert => {
      this.alert = alert;
      // Auto-hide after 5 seconds
      setTimeout(() => {
        this.alert = null;
      }, 5000);
    });
  }

  close(): void {
    this.alert = null;
  }
}