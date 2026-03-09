import { CommonModule } from '@angular/common';
import { Component, ElementRef, HostListener, Input, forwardRef, inject } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

interface CalendarDay {
  iso: string;
  label: number;
  outsideMonth: boolean;
  isToday: boolean;
}

@Component({
  selector: 'app-date-picker',
  standalone: true,
  imports: [CommonModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => DatePickerComponent),
      multi: true
    }
  ],
  template: `
    <div class="date-picker" [class.date-picker-open]="isOpen">
      <button
        class="date-picker-trigger"
        [ngClass]="inputClass"
        type="button"
        [disabled]="disabled"
        [attr.aria-expanded]="isOpen"
        [attr.aria-label]="ariaLabel || placeholder"
        (click)="toggleCalendar()"
        (blur)="handleBlur()"
      >
        <span class="truncate text-left" [class.text-gray-400]="!value" [class.dark:text-gray-500]="!value">
          {{ value ? formatDisplay(value) : placeholder }}
        </span>
        <span class="date-picker-trigger-actions">
          <button
            *ngIf="clearable && value && !disabled"
            class="date-picker-icon-btn"
            type="button"
            aria-label="Limpiar fecha"
            (click)="clear($event)"
          >
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4">
              <path d="m7 7 10 10M17 7 7 17" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" />
            </svg>
          </button>
          <span class="date-picker-icon" aria-hidden="true">
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4">
              <path d="M8 2v4M16 2v4M3 10h18M5 6h14a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2Z" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </span>
        </span>
      </button>

      <div class="date-picker-panel panel-card" *ngIf="isOpen">
        <div class="date-picker-header">
          <button class="date-picker-nav-btn" type="button" aria-label="Mes anterior" (click)="changeMonth(-1)">
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="m15 18-6-6 6-6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
          <div class="date-picker-title">
            {{ monthLabel() }}
          </div>
          <button class="date-picker-nav-btn" type="button" aria-label="Mes siguiente" (click)="changeMonth(1)">
            <svg viewBox="0 0 24 24" fill="none" class="h-4 w-4"><path d="m9 6 6 6-6 6" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>
          </button>
        </div>

        <div class="date-picker-weekdays">
          <span *ngFor="let weekday of weekdays">{{ weekday }}</span>
        </div>

        <div class="date-picker-grid">
          <button
            *ngFor="let day of calendarDays"
            class="date-picker-day"
            type="button"
            [class.date-picker-day-outside]="day.outsideMonth"
            [class.date-picker-day-selected]="day.iso === value"
            [class.date-picker-day-today]="day.isToday && day.iso !== value"
            (click)="selectDate(day.iso)"
          >
            {{ day.label }}
          </button>
        </div>

        <div class="date-picker-footer">
          <button class="btn-outline-neutral px-3 py-1.5" type="button" (click)="selectToday()">Hoy</button>
          <button class="btn-outline-neutral px-3 py-1.5" type="button" (click)="closeCalendar()">Cerrar</button>
        </div>
      </div>
    </div>
  `
})
export class DatePickerComponent implements ControlValueAccessor {
  private static readonly WEEKDAYS = ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sa', 'Do'];
  private static readonly MONTH_FORMATTER = new Intl.DateTimeFormat('es-EC', {
    month: 'long',
    year: 'numeric'
  });

  private readonly elementRef = inject(ElementRef<HTMLElement>);

  @Input() inputClass = 'form-control';
  @Input() placeholder = 'Selecciona una fecha';
  @Input() ariaLabel = '';
  @Input() clearable = true;

  protected readonly weekdays = DatePickerComponent.WEEKDAYS;
  protected value = '';
  protected isOpen = false;
  protected disabled = false;
  protected calendarDays: CalendarDay[] = [];

  private visibleMonth = this.todayParts();
  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor() {
    this.syncCalendar();
  }

  writeValue(value: string | null): void {
    this.value = value ?? '';
    this.visibleMonth = this.value ? this.parseIso(this.value) : this.todayParts();
    this.syncCalendar();
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(isDisabled: boolean): void {
    this.disabled = isDisabled;
    if (isDisabled) {
      this.isOpen = false;
    }
  }

  @HostListener('document:mousedown', ['$event'])
  protected onDocumentMouseDown(event: MouseEvent): void {
    if (!this.isOpen) {
      return;
    }
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.closeCalendar();
      this.onTouched();
    }
  }

  protected toggleCalendar(): void {
    if (this.disabled) {
      return;
    }
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.visibleMonth = this.value ? this.parseIso(this.value) : this.todayParts();
      this.syncCalendar();
    }
  }

  protected closeCalendar(): void {
    this.isOpen = false;
  }

  protected handleBlur(): void {
    this.onTouched();
  }

  protected changeMonth(offset: number): void {
    const current = new Date(this.visibleMonth.year, this.visibleMonth.month - 1 + offset, 1);
    this.visibleMonth = {
      year: current.getFullYear(),
      month: current.getMonth() + 1,
      day: 1
    };
    this.syncCalendar();
  }

  protected selectDate(iso: string): void {
    this.value = iso;
    this.onChange(this.value);
    this.onTouched();
    this.visibleMonth = this.parseIso(iso);
    this.syncCalendar();
    this.closeCalendar();
  }

  protected selectToday(): void {
    this.selectDate(this.toIso(this.todayParts()));
  }

  protected clear(event: MouseEvent): void {
    event.stopPropagation();
    this.value = '';
    this.onChange('');
    this.onTouched();
    this.visibleMonth = this.todayParts();
    this.syncCalendar();
  }

  protected monthLabel(): string {
    return DatePickerComponent.MONTH_FORMATTER.format(new Date(this.visibleMonth.year, this.visibleMonth.month - 1, 1));
  }

  protected formatDisplay(iso: string): string {
    const { year, month, day } = this.parseIso(iso);
    return `${this.pad(day)}/${this.pad(month)}/${year}`;
  }

  private syncCalendar(): void {
    const firstDay = new Date(this.visibleMonth.year, this.visibleMonth.month - 1, 1);
    const firstWeekday = (firstDay.getDay() + 6) % 7;
    const gridStart = new Date(this.visibleMonth.year, this.visibleMonth.month - 1, 1 - firstWeekday);
    const todayIso = this.toIso(this.todayParts());

    this.calendarDays = Array.from({ length: 42 }, (_, index) => {
      const current = new Date(gridStart);
      current.setDate(gridStart.getDate() + index);
      const iso = this.toIso({
        year: current.getFullYear(),
        month: current.getMonth() + 1,
        day: current.getDate()
      });
      return {
        iso,
        label: current.getDate(),
        outsideMonth: current.getMonth() !== this.visibleMonth.month - 1,
        isToday: iso === todayIso
      };
    });
  }

  private todayParts(): { year: number; month: number; day: number } {
    const now = new Date();
    return {
      year: now.getFullYear(),
      month: now.getMonth() + 1,
      day: now.getDate()
    };
  }

  private parseIso(value: string): { year: number; month: number; day: number } {
    const [year, month, day] = value.split('-').map((part) => Number(part));
    return {
      year: Number.isFinite(year) ? year : this.todayParts().year,
      month: Number.isFinite(month) ? month : 1,
      day: Number.isFinite(day) ? day : 1
    };
  }

  private toIso(parts: { year: number; month: number; day: number }): string {
    return `${parts.year}-${this.pad(parts.month)}-${this.pad(parts.day)}`;
  }

  private pad(value: number): string {
    return value.toString().padStart(2, '0');
  }
}
