import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, NgZone, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { take } from 'rxjs/operators';
import {
  CatalogService,
  EquipmentCatalogItem,
  ExerciseCatalogItem,
  MuscleCatalogItem
} from '../../../services/catalog.service';

type CatalogMode = 'exercises' | 'muscles' | 'equipments';

@Component({
  selector: 'app-catalog-management',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './catalog-management.component.html',
  styleUrls: ['./catalog-management.component.css']
})
export class CatalogManagementComponent implements OnInit {
  mode: CatalogMode = 'exercises';
  pageTitle = 'Exercicios';
  pageDescription = 'Gerencie os exercicios cadastrados.';

  loading = true;
  saving = false;
  errorMessage = '';
  successMessage = '';

  equipments: EquipmentCatalogItem[] = [];
  muscles: MuscleCatalogItem[] = [];
  exercises: ExerciseCatalogItem[] = [];

  newDescription = '';
  editId: number | null = null;
  editDescription = '';

  newExercise = {
    description: '',
    type: 'STRENGTH' as 'STRENGTH' | 'CARDIO',
    equipmentId: null as number | null
  };
  editExercise = {
    id: null as number | null,
    description: '',
    type: 'STRENGTH' as 'STRENGTH' | 'CARDIO',
    equipmentId: null as number | null
  };

  constructor(
    private readonly route: ActivatedRoute,
    private readonly catalogService: CatalogService,
    private readonly cdr: ChangeDetectorRef,
    private readonly zone: NgZone
  ) {}

  ngOnInit(): void {
    this.mode = (this.route.snapshot.data['mode'] as CatalogMode) ?? 'exercises';
    this.applyPageMeta();
    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.cancelEdit();

    if (this.mode === 'equipments') {
      this.catalogService.listEquipments().pipe(take(1)).subscribe({
        next: (list) => {
          this.zone.run(() => {
            this.equipments = list ?? [];
            this.loading = false;
            this.cdr.detectChanges();
          });
        },
        error: (error) => {
          this.handleError(error, 'Nao foi possivel carregar ferramentas.');
        }
      });
      return;
    }

    if (this.mode === 'muscles') {
      this.catalogService.listMuscles().pipe(take(1)).subscribe({
        next: (list) => {
          this.zone.run(() => {
            this.muscles = list ?? [];
            this.loading = false;
            this.cdr.detectChanges();
          });
        },
        error: (error) => {
          this.handleError(error, 'Nao foi possivel carregar musculos.');
        }
      });
      return;
    }

    this.catalogService.listEquipments().pipe(take(1)).subscribe({
      next: (equipmentList) => {
        this.zone.run(() => {
          this.equipments = equipmentList ?? [];
          this.cdr.detectChanges();
        });
        this.catalogService.listExercises().pipe(take(1)).subscribe({
          next: (exerciseList) => {
            this.zone.run(() => {
              this.exercises = exerciseList ?? [];
              this.loading = false;
              this.cdr.detectChanges();
            });
          },
          error: (error) => this.handleError(error, 'Nao foi possivel carregar exercicios.')
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel carregar ferramentas.')
    });
  }

  startEditSimple(item: EquipmentCatalogItem | MuscleCatalogItem): void {
    this.editId = item.id;
    this.editDescription = item.description;
  }

  startEditExercise(item: ExerciseCatalogItem): void {
    this.editId = null;
    this.editDescription = '';
    this.editExercise = {
      id: item.id,
      description: item.description,
      type: item.type,
      equipmentId: item.equipmentId
    };
  }

  cancelEdit(): void {
    this.editId = null;
    this.editDescription = '';
    this.editExercise = {
      id: null,
      description: '',
      type: 'STRENGTH',
      equipmentId: null
    };
  }

  createSimple(): void {
    if (!this.newDescription.trim() || this.saving) {
      return;
    }

    this.saving = true;
    this.clearMessages();
    const description = this.newDescription.trim();
    const request$ = this.mode === 'equipments'
      ? this.catalogService.createEquipment(description)
      : this.catalogService.createMuscle(description);

    request$.pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.newDescription = '';
          this.successMessage = 'Cadastro criado com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel criar cadastro.')
    });
  }

  updateSimple(): void {
    if (this.editId === null || !this.editDescription.trim() || this.saving) {
      return;
    }

    this.saving = true;
    this.clearMessages();

    const description = this.editDescription.trim();
    const request$ = this.mode === 'equipments'
      ? this.catalogService.updateEquipment(this.editId, description)
      : this.catalogService.updateMuscle(this.editId, description);

    request$.pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.successMessage = 'Cadastro atualizado com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel atualizar cadastro.')
    });
  }

  deleteSimple(id: number): void {
    if (this.saving) {
      return;
    }

    const confirmed = window.confirm('Deseja realmente remover este cadastro?');
    if (!confirmed) {
      return;
    }

    this.saving = true;
    this.clearMessages();

    const request$ = this.mode === 'equipments'
      ? this.catalogService.deleteEquipment(id)
      : this.catalogService.deleteMuscle(id);

    request$.pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.successMessage = 'Cadastro removido com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel remover cadastro.')
    });
  }

  createExercise(): void {
    if (this.saving) {
      return;
    }

    if (!this.newExercise.description.trim() || this.newExercise.equipmentId === null) {
      this.errorMessage = 'Informe descricao, tipo e ferramenta do exercicio.';
      return;
    }

    this.saving = true;
    this.clearMessages();

    this.catalogService.createExercise({
      description: this.newExercise.description.trim(),
      type: this.newExercise.type,
      equipmentId: this.newExercise.equipmentId
    }).pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.newExercise = { description: '', type: 'STRENGTH', equipmentId: null };
          this.successMessage = 'Exercicio criado com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel criar exercicio.')
    });
  }

  updateExercise(): void {
    if (this.saving || this.editExercise.id === null) {
      return;
    }

    if (!this.editExercise.description.trim() || this.editExercise.equipmentId === null) {
      this.errorMessage = 'Informe descricao, tipo e ferramenta do exercicio.';
      return;
    }

    this.saving = true;
    this.clearMessages();

    this.catalogService.updateExercise(this.editExercise.id, {
      description: this.editExercise.description.trim(),
      type: this.editExercise.type,
      equipmentId: this.editExercise.equipmentId
    }).pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.successMessage = 'Exercicio atualizado com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel atualizar exercicio.')
    });
  }

  deleteExercise(id: number): void {
    if (this.saving) {
      return;
    }

    const confirmed = window.confirm('Deseja realmente remover este exercicio?');
    if (!confirmed) {
      return;
    }

    this.saving = true;
    this.clearMessages();

    this.catalogService.deleteExercise(id).pipe(take(1)).subscribe({
      next: () => {
        this.zone.run(() => {
          this.successMessage = 'Exercicio removido com sucesso.';
          this.saving = false;
          this.cdr.detectChanges();
          this.loadData();
        });
      },
      error: (error) => this.handleError(error, 'Nao foi possivel remover exercicio.')
    });
  }

  private applyPageMeta(): void {
    if (this.mode === 'equipments') {
      this.pageTitle = 'Ferramentas';
      this.pageDescription = 'Gerencie equipamentos usados nos exercicios.';
      return;
    }

    if (this.mode === 'muscles') {
      this.pageTitle = 'Musculos';
      this.pageDescription = 'Gerencie o catalogo de grupos musculares.';
      return;
    }

    this.pageTitle = 'Exercicios';
    this.pageDescription = 'Gerencie exercicios, tipo e ferramenta.';
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private handleError(error: unknown, fallback: string): void {
    const httpError = error as HttpErrorResponse;
    const backendMessage = httpError?.error?.error;
    let message = backendMessage || fallback;

    if (!backendMessage) {
      if (httpError?.status === 0) {
        message = 'Falha de conexao com o backend (status 0). Verifique se a API esta no ar e CORS liberado.';
      } else if (httpError?.status === 401) {
        message = 'Sessao expirada. Faca login novamente.';
      } else if (httpError?.status === 404) {
        message = 'Endpoint de catalogo nao encontrado (404). Reinicie o backend com as alteracoes novas.';
      } else if (httpError?.status) {
        message = `${fallback} (HTTP ${httpError.status})`;
      }
    }

    this.zone.run(() => {
      this.errorMessage = message;
      this.loading = false;
      this.saving = false;
      this.cdr.detectChanges();
    });
  }
}
