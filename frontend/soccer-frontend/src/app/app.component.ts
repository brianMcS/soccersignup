import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterOutlet } from '@angular/router';
import {NavComponent} from './components/nav/nav.component';
import {NotificationService} from './services/notification.service';
import {AuthService} from './services/auth.service';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavComponent, FormsModule, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'soccer-frontend';

  constructor(
    private notificationService: NotificationService,
    private authService: AuthService) {}

  ngOnInit():void {
    if(this.authService.isLoggedIn()) {
      this.notificationService.fetchUnreadCount();
    }
  }
}
