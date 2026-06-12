import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AdminPlayersComponent } from './admin-players.component';
import { AdminService } from '../../../services/admin.service';

describe('AdminPlayersComponent', () => {
  let component: AdminPlayersComponent;
  let fixture: ComponentFixture<AdminPlayersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AdminPlayersComponent],
      providers: [
        {
          provide: AdminService,
          useValue: { getAllPlayers: () => of([]) }
        }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AdminPlayersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
