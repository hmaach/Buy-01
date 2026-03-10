import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { LucideAngularModule, AlertTriangle } from 'lucide-angular';

@Component({
  selector: 'app-delete-account-dialog',
  standalone: true,
  imports: [CommonModule, MatDialogModule, MatButtonModule, LucideAngularModule],
  template: `
    <div class="delete-dialog">
      <div class="dialog-icon">
        <lucide-angular [img]="AlertTriangle" [size]="48" class="warning-icon"></lucide-angular>
      </div>
      
      <h2 mat-dialog-title>Delete Account</h2>
      
      <mat-dialog-content>
        <p>Are you sure you want to delete your account?</p>
        <p class="warning-text">This action cannot be undone. All your data will be permanently removed.</p>
      </mat-dialog-content>
      
      <mat-dialog-actions align="center">
        <button mat-button (click)="onCancel()">Cancel</button>
        <button mat-flat-button color="warn" (click)="onDelete()">Delete Account</button>
      </mat-dialog-actions>
    </div>
  `,
  styles: [`
    .delete-dialog {
      padding: 20px;
      text-align: center;
    }
    
    .dialog-icon {
      margin-bottom: 16px;
    }
    
    .warning-icon {
      color: #f44336;
    }
    
    h2 {
      margin: 0 0 16px 0;
      color: #333;
    }
    
    mat-dialog-content p {
      color: #666;
      margin: 8px 0;
    }
    
    .warning-text {
      color: #f44336 !important;
      font-size: 14px;
    }
    
    mat-dialog-actions {
      margin-top: 24px;
      gap: 12px;
    }
    
    button[mat-flat-button] {
      background-color: #f44336;
      color: white;
    }
  `]
})
export class DeleteAccountDialogComponent {
  readonly AlertTriangle = AlertTriangle;

  constructor(private dialogRef: MatDialogRef<DeleteAccountDialogComponent>) {}

  onCancel(): void {
    this.dialogRef.close('cancel');
  }

  onDelete(): void {
    this.dialogRef.close('delete');
  }
}
