import { CommonModule } from '@angular/common';
import { Component, OnDestroy, inject } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import { PopupService } from '../../../../core/services/popup.service';
import { AuthService } from '../../../auth/services/auth.service';
import {
  ClienteEquivalencia,
  ClienteEquivalenciaRequest,
  ClienteImportResult,
  ClienteResponse,
  ClientesService,
  TipoDocumentoCliente
} from '../../services/clientes.service';

type EquivalenciaDraft = {
  id?: string;
  destino: string;
  valorDestino: number | null;
  costoChofer: number | null;
};

@Component({
  selector: 'app-clientes-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './clientes-list.component.html'
})
export class ClientesListComponent implements OnDestroy {
  private readonly clientesService = inject(ClientesService);
  private readonly popupService = inject(PopupService);
  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);

  protected clientes: ClienteResponse[] = [];
  protected logoPreviewUrls: Record<string, string> = {};
  protected selectedCliente: ClienteResponse | null = null;
  protected selectedLogoUrl: string | null = null;
  protected mode: 'none' | 'create' | 'edit' = 'none';
  protected editingId: string | null = null;
  protected page = 0;
  protected readonly size = 10;
  protected totalPages = 0;

  protected showImportModal = false;
  protected importFile: File | null = null;
  protected importMode: 'INSERT_ONLY' | 'UPSERT' = 'INSERT_ONLY';
  protected partialOk = true;
  protected importPreview: ClienteImportResult | null = null;

  protected equivalenciasEditMode = false;
  protected equivalenciasDraft: EquivalenciaDraft[] = [];

  private logoFile: File | null = null;
  protected pendingEquivalenciasFile: File | null = null;
  protected selectedEquivalenciasFile: File | null = null;

  protected readonly filtersForm = this.fb.nonNullable.group({
    q: [''],
    includeDeleted: ['false']
  });

  protected readonly clienteForm = this.fb.group({
    tipoDocumento: ['CEDULA', [Validators.required]],
    documento: ['', [Validators.required]],
    nombre: ['', [Validators.required]],
    direccion: [''],
    descripcion: [''],
    aplicaTablaEquivalencia: [false, [Validators.required]],
    activo: [true, [Validators.required]]
  });

  constructor() {
    this.loadClientes(0);
  }

  ngOnDestroy(): void {
    this.revokeLogoPreviews();
    this.revokeSelectedLogoPreview();
  }

  protected canWrite(): boolean {
    const role = this.authService.getRole();
    return role === 'SUPERADMINISTRADOR' || role === 'REGISTRADOR';
  }

  protected canAdmin(): boolean {
    return this.authService.getRole() === 'SUPERADMINISTRADOR';
  }

  protected loadClientes(page: number): void {
    const safePage = page < 0 ? 0 : page;
    const filters = this.filtersForm.getRawValue();
    this.clientesService
      .list({
        page: safePage,
        size: this.size,
        q: filters.q || undefined,
        includeDeleted: this.canAdmin() && filters.includeDeleted === 'true'
      })
      .subscribe({
        next: (response) => {
          this.revokeLogoPreviews();
          this.clientes = response.content;
          this.page = response.page;
          this.totalPages = response.totalPages;
          this.loadLogoPreviews(response.content);
        },
        error: (error) => {
          void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
        }
      });
  }

  protected selectDetail(cliente: ClienteResponse): void {
    this.clientesService.getById(cliente.id).subscribe({
      next: (detail) => {
        this.selectedCliente = detail;
        this.selectedEquivalenciasFile = null;
        this.cancelEquivalenciasEdit();
        this.loadSelectedLogo(detail);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected startCreate(): void {
    this.mode = 'create';
    this.editingId = null;
    this.logoFile = null;
    this.pendingEquivalenciasFile = null;
    this.clienteForm.reset({
      tipoDocumento: 'CEDULA',
      documento: '',
      nombre: '',
      direccion: '',
      descripcion: '',
      aplicaTablaEquivalencia: false,
      activo: true
    });
  }

  protected startEdit(cliente: ClienteResponse): void {
    this.mode = 'edit';
    this.editingId = cliente.id;
    this.logoFile = null;
    this.pendingEquivalenciasFile = null;
    this.clienteForm.reset({
      tipoDocumento: cliente.tipoDocumento,
      documento: cliente.documento,
      nombre: cliente.nombre,
      direccion: cliente.direccion ?? '',
      descripcion: cliente.descripcion ?? '',
      aplicaTablaEquivalencia: cliente.aplicaTablaEquivalencia,
      activo: cliente.activo
    });
  }

  protected cancelForm(): void {
    this.mode = 'none';
    this.editingId = null;
    this.logoFile = null;
    this.pendingEquivalenciasFile = null;
  }

  protected onLogoFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.logoFile = target.files?.[0] ?? null;
  }

  protected onPendingEquivalenciasFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.pendingEquivalenciasFile = target.files?.[0] ?? null;
  }

  protected onSelectedEquivalenciasFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.selectedEquivalenciasFile = target.files?.[0] ?? null;
  }

  protected async submitCliente(): Promise<void> {
    this.clienteForm.markAllAsTouched();
    if (this.clienteForm.invalid) {
      return;
    }

    const value = this.clienteForm.getRawValue();
    const payload = {
      tipoDocumento: (value.tipoDocumento ?? 'CEDULA') as TipoDocumentoCliente,
      documento: value.documento ?? '',
      nombre: value.nombre ?? '',
      direccion: value.direccion ?? '',
      descripcion: value.descripcion ?? '',
      aplicaTablaEquivalencia: value.aplicaTablaEquivalencia ?? false,
      activo: value.activo ?? true
    };

    try {
      if (this.mode === 'create') {
        const confirmed = await this.popupService.confirm({
          title: 'Crear cliente',
          message: 'Vas a crear un cliente nuevo. ¿Deseas continuar?'
        });
        if (!confirmed) {
          return;
        }
        const saved = await firstValueFrom(this.clientesService.create(payload));
        await this.uploadPendingLogo(saved.id);
        if (payload.aplicaTablaEquivalencia) {
          await this.uploadPendingEquivalencias(saved.id);
        }
        await this.popupService.info({ title: 'Cliente creado', message: 'Cliente creado correctamente.' });
        this.cancelForm();
        this.loadClientes(this.page);
        return;
      }

      if (this.mode === 'edit' && this.editingId) {
        const confirmed = await this.popupService.confirm({
          title: 'Editar cliente',
          message: 'Vas a editar este cliente. ¿Deseas continuar?'
        });
        if (!confirmed) {
          return;
        }
        const saved = await firstValueFrom(this.clientesService.update(this.editingId, payload));
        await this.uploadPendingLogo(saved.id);
        if (payload.aplicaTablaEquivalencia) {
          await this.uploadPendingEquivalencias(saved.id);
        }
        if (this.selectedCliente?.id === saved.id) {
          this.selectDetail(saved);
        }
        await this.popupService.info({ title: 'Cliente editado', message: 'Cliente editado correctamente.' });
        this.cancelForm();
        this.loadClientes(this.page);
      }
    } catch (error) {
      void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
    }
  }

  protected startEquivalenciasEdit(): void {
    if (!this.selectedCliente) {
      return;
    }
    this.equivalenciasDraft = this.selectedCliente.equivalencias.map((item) => this.toDraft(item));
    this.equivalenciasEditMode = true;
  }

  protected cancelEquivalenciasEdit(): void {
    this.equivalenciasEditMode = false;
    this.equivalenciasDraft = [];
  }

  protected addEquivalenciaRow(): void {
    this.equivalenciasDraft = [...this.equivalenciasDraft, { destino: '', valorDestino: null, costoChofer: null }];
  }

  protected removeEquivalenciaRow(index: number): void {
    this.equivalenciasDraft = this.equivalenciasDraft.filter((_, currentIndex) => currentIndex !== index);
  }

  protected async saveEquivalencias(): Promise<void> {
    if (!this.selectedCliente) {
      return;
    }

    const payload = this.buildEquivalenciasPayload();
    if (!payload) {
      return;
    }

    const confirmed = await this.popupService.confirm({
      title: 'Guardar tabla de equivalencia',
      message: 'Vas a actualizar la tabla de equivalencia del cliente. ¿Deseas continuar?'
    });
    if (!confirmed) {
      return;
    }

    this.clientesService.updateEquivalencias(this.selectedCliente.id, payload).subscribe({
      next: (detail) => {
        this.applySelectedCliente(detail);
        this.cancelEquivalenciasEdit();
        void this.popupService.info({ title: 'Tabla actualizada', message: 'La tabla de equivalencia fue actualizada correctamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected async importSelectedEquivalencias(): Promise<void> {
    if (!this.selectedCliente || !this.selectedEquivalenciasFile) {
      return;
    }

    const confirmed = await this.popupService.confirm({
      title: 'Importar tabla de equivalencia',
      message: 'La carga Excel reemplazará la tabla actual del cliente. ¿Deseas continuar?'
    });
    if (!confirmed) {
      return;
    }

    this.clientesService.importEquivalencias(this.selectedCliente.id, this.selectedEquivalenciasFile).subscribe({
      next: (detail) => {
        this.selectedEquivalenciasFile = null;
        this.applySelectedCliente(detail);
        this.cancelEquivalenciasEdit();
        void this.popupService.info({ title: 'Tabla cargada', message: 'La tabla de equivalencia fue cargada correctamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected async toggleActivo(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: cliente.activo ? 'Inactivar cliente' : 'Activar cliente',
      message: `Vas a ${cliente.activo ? 'inactivar' : 'activar'} a "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.toggleActivo(cliente.id).subscribe({
      next: () => this.loadClientes(this.page),
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async softDelete(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminación lógica',
      message: `Vas a eliminar lógicamente al cliente "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.softDelete(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente eliminado', message: 'Cliente eliminado lógicamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async restore(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Restaurar cliente',
      message: `Vas a restaurar al cliente "${cliente.nombre}". ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.restore(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente restaurado', message: 'Cliente restaurado correctamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected async forceDelete(cliente: ClienteResponse): Promise<void> {
    const confirmed = await this.popupService.confirm({
      title: 'Eliminación física',
      message: `Vas a eliminar físicamente al cliente "${cliente.nombre}". Esta acción no se puede deshacer. ¿Deseas continuar?`
    });
    if (!confirmed) {
      return;
    }
    this.clientesService.forceDelete(cliente.id).subscribe({
      next: () => {
        void this.popupService.info({ title: 'Cliente eliminado', message: 'Cliente eliminado físicamente.' });
        this.loadClientes(this.page);
      },
      error: (error) => void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) })
    });
  }

  protected openImportModal(): void {
    this.showImportModal = true;
    this.importPreview = null;
  }

  protected closeImportModal(): void {
    this.showImportModal = false;
  }

  protected onImportFileChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.importFile = target.files?.[0] ?? null;
    this.importPreview = null;
  }

  protected downloadTemplate(): void {
    this.clientesService.downloadTemplate().subscribe((blob) => this.downloadBlob(blob, 'clientes_template.xlsx'));
  }

  protected downloadExampleTemplate(): void {
    this.clientesService.downloadExampleTemplate().subscribe((blob) => this.downloadBlob(blob, 'clientes_template_ejemplo.xlsx'));
  }

  protected downloadEquivalenciasTemplate(): void {
    this.clientesService.downloadEquivalenciasTemplate().subscribe((blob) => this.downloadBlob(blob, 'clientes_equivalencias_template.xlsx'));
  }

  protected downloadEquivalenciasTemplateExample(): void {
    this.clientesService.downloadEquivalenciasTemplateExample().subscribe((blob) => this.downloadBlob(blob, 'clientes_equivalencias_template_ejemplo.xlsx'));
  }

  protected previewImport(): void {
    if (!this.importFile) {
      return;
    }
    this.clientesService.previewImport(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected executeImport(): void {
    if (!this.importFile) {
      return;
    }
    this.clientesService.importExcel(this.importFile, this.importMode, this.partialOk).subscribe({
      next: (result) => {
        this.importPreview = result;
        void this.popupService.info({
          title: 'Importación finalizada',
          message: `Procesadas ${result.processed} filas. Insertadas: ${result.inserted}. Actualizadas: ${result.updated}.`
        });
        this.loadClientes(this.page);
      },
      error: (error) => {
        void this.popupService.info({ title: 'Error', message: this.getErrorMessage(error) });
      }
    });
  }

  protected showError(controlName: string, errorName: string): boolean {
    const control = this.clienteForm.get(controlName);
    if (!control) {
      return false;
    }
    return control.hasError(errorName) && (control.touched || control.dirty);
  }

  private async uploadPendingLogo(clienteId: string): Promise<void> {
    if (!this.logoFile) {
      return;
    }
    await firstValueFrom(this.clientesService.uploadLogo(clienteId, this.logoFile));
  }

  private async uploadPendingEquivalencias(clienteId: string): Promise<void> {
    if (!this.pendingEquivalenciasFile) {
      return;
    }
    await firstValueFrom(this.clientesService.importEquivalencias(clienteId, this.pendingEquivalenciasFile));
  }

  private loadLogoPreviews(items: ClienteResponse[]): void {
    items.filter((c) => !!c.logoPath).forEach((cliente) => {
      this.clientesService.getLogoBlob(cliente.id).subscribe({
        next: (blob) => {
          this.logoPreviewUrls[cliente.id] = URL.createObjectURL(blob);
        }
      });
    });
  }

  private revokeLogoPreviews(): void {
    Object.values(this.logoPreviewUrls).forEach((url) => URL.revokeObjectURL(url));
    this.logoPreviewUrls = {};
  }

  private loadSelectedLogo(cliente: ClienteResponse): void {
    this.revokeSelectedLogoPreview();
    if (!cliente.logoPath) {
      this.selectedLogoUrl = null;
      return;
    }
    this.clientesService.getLogoBlob(cliente.id).subscribe({
      next: (blob) => {
        this.selectedLogoUrl = URL.createObjectURL(blob);
      },
      error: () => {
        this.selectedLogoUrl = null;
      }
    });
  }

  private revokeSelectedLogoPreview(): void {
    if (this.selectedLogoUrl) {
      URL.revokeObjectURL(this.selectedLogoUrl);
      this.selectedLogoUrl = null;
    }
  }

  private buildEquivalenciasPayload(): ClienteEquivalenciaRequest[] | null {
    const payload: ClienteEquivalenciaRequest[] = [];
    for (const item of this.equivalenciasDraft) {
      if (!item.destino.trim()) {
        void this.popupService.info({ title: 'Validación', message: 'Destino es obligatorio en cada fila de equivalencia.' });
        return null;
      }
      if (item.valorDestino === null || Number.isNaN(item.valorDestino)) {
        void this.popupService.info({ title: 'Validación', message: 'Valor destino es obligatorio en cada fila de equivalencia.' });
        return null;
      }
      if (item.costoChofer === null || Number.isNaN(item.costoChofer)) {
        void this.popupService.info({ title: 'Validación', message: 'Costo chofer es obligatorio en cada fila de equivalencia.' });
        return null;
      }
      payload.push({
        id: item.id,
        destino: item.destino.trim(),
        valorDestino: Number(item.valorDestino),
        costoChofer: Number(item.costoChofer)
      });
    }
    return payload;
  }

  private applySelectedCliente(detail: ClienteResponse): void {
    this.selectedCliente = detail;
    this.loadSelectedLogo(detail);
  }

  private downloadBlob(blob: Blob, fileName: string): void {
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName;
    a.click();
    URL.revokeObjectURL(url);
  }

  private toDraft(item: ClienteEquivalencia): EquivalenciaDraft {
    return {
      id: item.id,
      destino: item.destino,
      valorDestino: item.valorDestino,
      costoChofer: item.costoChofer
    };
  }

  private getErrorMessage(error: unknown): string {
    const maybe = error as { error?: { message?: string } };
    return maybe?.error?.message ?? 'Ocurrio un error inesperado.';
  }
}
