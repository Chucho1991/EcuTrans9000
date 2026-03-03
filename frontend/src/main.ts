import { DATE_PIPE_DEFAULT_OPTIONS, registerLocaleData } from '@angular/common';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { LOCALE_ID } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideRouter } from '@angular/router';
import localeEsEc from '@angular/common/locales/es-EC';

import { AppComponent } from './app/app.component';
import { APP_ROUTES } from './app/app.routes';
import { authInterceptor } from './app/core/interceptors/auth.interceptor';

registerLocaleData(localeEsEc);

if ((localStorage.getItem('ecutrans9000_theme') ?? 'dark') === 'dark') {
  document.documentElement.classList.add('dark');
} else {
  document.documentElement.classList.remove('dark');
}

bootstrapApplication(AppComponent, {
  providers: [
    provideRouter(APP_ROUTES),
    provideHttpClient(withInterceptors([authInterceptor])),
    { provide: LOCALE_ID, useValue: 'es-EC' },
    { provide: DATE_PIPE_DEFAULT_OPTIONS, useValue: { timezone: '-0500', dateFormat: 'dd/MM/yyyy HH:mm:ss' } }
  ]
}).catch((err) => console.error(err));
