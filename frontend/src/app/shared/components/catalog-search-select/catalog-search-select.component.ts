import { CommonModule } from '@angular/common';
import { Component, ElementRef, forwardRef, HostListener, Input } from '@angular/core';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR } from '@angular/forms';

/**
 * Opcion reutilizable para catalogos con busqueda local.
 */
export interface CatalogSearchOption {
  value: string;
  label: string;
  secondaryLabel?: string | null;
  searchText?: string | null;
}

@Component({
  selector: 'app-catalog-search-select',
  standalone: true,
  imports: [CommonModule, FormsModule],
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CatalogSearchSelectComponent),
      multi: true
    }
  ],
  template: `
    <div class="catalog-select" [class.catalog-select-disabled]="disabled">
      <button
        class="catalog-select-trigger"
        type="button"
        [disabled]="disabled"
        [class.catalog-select-trigger-open]="open"
        (click)="toggle()">
        <span class="catalog-select-trigger-text">
          {{ selectedOption?.label || placeholder }}
        </span>
        <svg aria-hidden="true" class="h-4 w-4 shrink-0" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
          <path stroke-linecap="round" stroke-linejoin="round" d="m6 8 4 4 4-4" />
        </svg>
      </button>

      <div *ngIf="open" class="catalog-select-panel panel-card" (click)="$event.stopPropagation()">
        <label class="catalog-select-search">
          <span class="catalog-select-search-icon">
            <svg aria-hidden="true" class="h-4 w-4" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="1.8">
              <path stroke-linecap="round" stroke-linejoin="round" d="m17.5 17.5-3.6-3.6m1.6-4.4a6 6 0 1 1-12 0 6 6 0 0 1 12 0Z" />
            </svg>
          </span>
          <input
            class="catalog-select-search-input"
            type="search"
            [placeholder]="searchPlaceholder"
            [(ngModel)]="searchTerm"
            [ngModelOptions]="{ standalone: true }" />
        </label>

        <div class="catalog-select-options custom-scrollbar">
          <button
            *ngFor="let option of filteredOptions"
            class="catalog-select-option"
            type="button"
            [class.catalog-select-option-active]="option.value === value"
            (click)="selectOption(option.value)">
            <span class="catalog-select-option-label">{{ option.label }}</span>
            <span *ngIf="option.secondaryLabel" class="catalog-select-option-secondary">{{ option.secondaryLabel }}</span>
          </button>

          <p *ngIf="filteredOptions.length === 0" class="catalog-select-empty">{{ noResultsText }}</p>
        </div>
      </div>
    </div>
  `
})
export class CatalogSearchSelectComponent implements ControlValueAccessor {
  @Input() options: CatalogSearchOption[] = [];
  @Input() placeholder = 'Selecciona una opcion';
  @Input() searchPlaceholder = 'Buscar por nombre o documento';
  @Input() noResultsText = 'No hay coincidencias para esta busqueda.';

  protected open = false;
  protected disabled = false;
  protected searchTerm = '';
  protected value = '';

  private onChange: (value: string) => void = () => undefined;
  private onTouched: () => void = () => undefined;

  constructor(private readonly elementRef: ElementRef<HTMLElement>) {}

  protected get filteredOptions(): CatalogSearchOption[] {
    const normalizedTerm = this.normalize(this.searchTerm);
    if (!normalizedTerm) {
      return this.options;
    }
    return this.options.filter((option) => this.buildSearchIndex(option).includes(normalizedTerm));
  }

  protected get selectedOption(): CatalogSearchOption | undefined {
    return this.options.find((option) => option.value === this.value);
  }

  writeValue(value: string | null): void {
    this.value = value ?? '';
  }

  registerOnChange(fn: (value: string) => void): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: () => void): void {
    this.onTouched = fn;
  }

  setDisabledState(disabled: boolean): void {
    this.disabled = disabled;
  }

  protected toggle(): void {
    if (this.disabled) {
      return;
    }
    this.open = !this.open;
    if (!this.open) {
      this.onTouched();
    }
  }

  protected selectOption(value: string): void {
    this.value = value;
    this.onChange(value);
    this.onTouched();
    this.open = false;
  }

  @HostListener('document:click', ['$event'])
  protected onDocumentClick(event: MouseEvent): void {
    if (!this.open) {
      return;
    }
    if (!this.elementRef.nativeElement.contains(event.target as Node)) {
      this.open = false;
      this.onTouched();
    }
  }

  private buildSearchIndex(option: CatalogSearchOption): string {
    return this.normalize(`${option.label} ${option.secondaryLabel ?? ''} ${option.searchText ?? ''}`);
  }

  private normalize(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
