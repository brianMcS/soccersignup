import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { A11yModule } from '@angular/cdk/a11y';
import { ConfirmDialogService } from '../../services/confirm-dialog.service';

@Component({
  selector: 'app-confirm-dialog',
  standalone: true,
  imports: [CommonModule, A11yModule],
  templateUrl: './confirm-dialog.component.html',
  styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
  readonly state$;

  constructor(private confirmDialog: ConfirmDialogService) {
    this.state$ = this.confirmDialog.state$;
  }

  confirm(): void {
    this.confirmDialog.confirmAction();
  }

  cancel(): void {
    this.confirmDialog.cancel();
  }
}
