import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ConfirmDialogVariant = 'default' | 'danger';

export interface ConfirmDialogOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: ConfirmDialogVariant;
}

export interface ConfirmDialogState extends Required<ConfirmDialogOptions> {
  open: boolean;
}

const CLOSED_STATE: ConfirmDialogState = {
  open: false,
  title: '',
  message: '',
  confirmText: 'Confirm',
  cancelText: 'Cancel',
  variant: 'default'
};

@Injectable({ providedIn: 'root' })
export class ConfirmDialogService {
  private resolver: ((confirmed: boolean) => void) | null = null;
  private stateSubject = new BehaviorSubject<ConfirmDialogState>(CLOSED_STATE);

  readonly state$ = this.stateSubject.asObservable();

  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    if (this.resolver) {
      this.resolve(false);
    }

    this.stateSubject.next({
      open: true,
      title: options.title,
      message: options.message,
      confirmText: options.confirmText ?? 'Confirm',
      cancelText: options.cancelText ?? 'Cancel',
      variant: options.variant ?? 'default'
    });

    return new Promise<boolean>((resolve) => {
      this.resolver = resolve;
    });
  }

  confirmAction(): void {
    this.resolve(true);
  }

  cancel(): void {
    this.resolve(false);
  }

  private resolve(confirmed: boolean): void {
    this.resolver?.(confirmed);
    this.resolver = null;
    this.stateSubject.next(CLOSED_STATE);
  }
}
