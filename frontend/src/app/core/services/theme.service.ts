import { Injectable } from '@angular/core';

const THEME_KEY = 'ecutrans9000_theme';
type ThemeMode = 'dark' | 'light';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private mode: ThemeMode = 'dark';

  constructor() {
    const persisted = localStorage.getItem(THEME_KEY) as ThemeMode | null;
    this.mode = persisted === 'light' ? 'light' : 'dark';
    this.applyTheme();
  }

  isDark(): boolean {
    return this.mode === 'dark';
  }

  toggle(): void {
    this.mode = this.mode === 'dark' ? 'light' : 'dark';
    this.applyTheme();
  }

  private applyTheme(): void {
    const root = document.documentElement;
    if (this.mode === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    localStorage.setItem(THEME_KEY, this.mode);
  }
}
