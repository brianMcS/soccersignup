import {
  Component,
  ElementRef,
  HostListener,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';
import {CommonModule} from '@angular/common';
import {A11yModule} from '@angular/cdk/a11y';
import {NavigationEnd, Router, RouterModule} from '@angular/router';
import {filter, Subscription} from 'rxjs';
import {AuthService} from '../../services/auth.service';
import {CurrentUser, UserService} from '../../services/user.service';
import {NotificationBellComponent} from '../notification-bell/notification-bell.component';
import {NotificationService} from '../../services/notification.service';

interface NavItem{
  label: string;
  route: string;
  icon: string;
}

@Component({
  selector: 'app-nav',
  imports: [CommonModule, RouterModule, NotificationBellComponent, A11yModule],
  templateUrl: './nav.component.html',
  styleUrl: './nav.component.css'
})
export class NavComponent implements OnInit, OnDestroy {
  @ViewChild('menuButton') menuButton?: ElementRef<HTMLButtonElement>;

  menuOpen = false;
  currentUser: CurrentUser | null = null;
  canManageGames = false;
  activeRoute = '';

  readonly navItems: NavItem[] = [
    { label: 'Home',            route: '/',       icon: '🏠' },
    { label: "This Week's Game", route: '/play',   icon: '⚽' },
    { label: 'History',         route: '/history', icon: '📋' },
  ];

  private subs = new Subscription();

  constructor(
    private authService: AuthService,
    private userService: UserService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.subs.add(
      this.userService.currentUser$.subscribe(u => {
        this.currentUser = u;
        this.canManageGames = this.userService.isOrganiser;

        if(u){
          this.notificationService.fetchUnreadCount();
        }
      })
    );

    this.subs.add(
      this.router.events.pipe(
        filter(e => e instanceof NavigationEnd)
      ).subscribe((e: any) => {
        this.activeRoute = e.urlAfterRedirects;
        this.closeMenu(false);
      })
    );
    this.activeRoute = this.router.url;
  }


  ngOnDestroy(): void {
    this.subs.unsubscribe();
  }

  toggleMenu(): void {
    if (this.menuOpen) {
      this.closeMenu();
    } else {
      this.menuOpen = true;
    }
  }

  closeMenu(restoreFocus = true): void {
    if (!this.menuOpen) return;
    this.menuOpen = false;
    if (restoreFocus) {
      setTimeout(() => this.menuButton?.nativeElement.focus());
    }
  }

  logout(): void {
    this.closeMenu();
    this.authService.logout();
  }

  isActive(route : string): boolean {
    if (route === '/') return this.activeRoute === '/';
    return this.activeRoute.startsWith(route);
  }

  getInitials(name: string): string {
    return name
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  // Close drawer if user clicks outside it
  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.closeMenu();
  }
}
