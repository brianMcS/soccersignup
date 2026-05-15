import {Component, Inject, OnInit, PLATFORM_ID} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ActivatedRoute, Router} from '@angular/router';
import {AuthService} from '../../services/auth.service';

@Component({
  selector: 'app-oauth-redirect',
  imports: [CommonModule],
  templateUrl: './o-auth-redirect.component.html',
  styleUrl: './o-auth-redirect.component.css'
})
export class OAuthRedirectComponent implements OnInit {
  loading = true;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    @Inject(PLATFORM_ID) private platformId: Object,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      if (token) {
        this.authService.setToken(token); // use the service, not localStorage directly
        this.loading = false;
        this.router.navigate(['/play']);
      } else {
        this.loading = false;
        this.error = 'No token received from authentication server.';
      }
    });
  }
}
