import { Component } from '@angular/core';

@Component({
  selector: 'app-login',
  standalone: true,
  template: `
    <section class="mx-auto flex min-h-[70vh] max-w-md items-center justify-center">
      <div class="w-full rounded-2xl border border-gray-200 bg-white p-6 shadow-theme-sm">
        <h1 class="mb-1 text-2xl font-semibold text-gray-900">Iniciar sesion</h1>
        <p class="mb-6 text-sm text-gray-500">Accede al panel de EcuTrans9000.</p>
        <form class="space-y-4">
          <div>
            <label class="mb-2 block text-sm font-medium text-gray-700">Correo</label>
            <input
              type="email"
              placeholder="admin@ecutrans.com"
              class="h-11 w-full rounded-lg border border-gray-300 px-3 text-sm outline-none focus:border-brand-500"
            />
          </div>
          <div>
            <label class="mb-2 block text-sm font-medium text-gray-700">Contrasena</label>
            <input
              type="password"
              placeholder="********"
              class="h-11 w-full rounded-lg border border-gray-300 px-3 text-sm outline-none focus:border-brand-500"
            />
          </div>
          <button type="button" class="h-11 w-full rounded-lg bg-brand-500 text-sm font-semibold text-white hover:bg-brand-600">
            Entrar
          </button>
        </form>
      </div>
    </section>
  `
})
export class LoginComponent {}
