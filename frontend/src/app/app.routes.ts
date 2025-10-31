import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
  // Public routes
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent)
  },
  {
    path: 'register/organization',
    loadComponent: () => import('./features/auth/register-organization/register-organization.component')
      .then(m => m.RegisterOrganizationComponent)
  },
  {
    path: 'register/employee',
    loadComponent: () => import('./features/auth/register-employee/register-employee.component')
      .then(m => m.RegisterEmployeeComponent)
  },
  // 🆕 NEW: Forgot Password Route
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component')
      .then(m => m.ForgotPasswordComponent)
  },



  // Bank Admin routes
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { role: 'BANK_ADMIN' },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/bank-admin/dashboard/dashboard.component')
          .then(m => m.DashboardComponent)
      },
      {
        path: 'organizations',
        loadComponent: () => import('./features/bank-admin/organizations/organization-list.component')
          .then(m => m.OrganizationListComponent)
      },
      {
        path: 'organizations/:id',
        loadComponent: () => import('./features/bank-admin/organizations/organization-verify.component')
          .then(m => m.OrganizationVerifyComponent)
      },
      {
        path: 'payment-requests',
        loadComponent: () => import('./features/bank-admin/payment-requests/admin-payment-list.component')
          .then(m => m.AdminPaymentListComponent)
      },
      {
        path: 'payment-requests/:id',
        loadComponent: () => import('./features/bank-admin/payment-requests/admin-payment-detail.component')
          .then(m => m.AdminPaymentDetailComponent)
      }
    ]
  },

  // Organization routes
  {
    path: 'organization',
    canActivate: [authGuard, roleGuard],
    data: { role: 'ORGANIZATION' },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/organization/dashboard/org-dashboard.component')
          .then(m => m.OrgDashboardComponent)
      },
      {
        path: 'employees',
        loadComponent: () => import('./features/organization/employees/employee-list.component')
          .then(m => m.EmployeeListComponent)
      },
      {
        path: 'employees/create',
        loadComponent: () => import('./features/organization/employees/employee-create.component')
          .then(m => m.EmployeeCreateComponent)
      },
      {
        path: 'employees/edit/:id',
        loadComponent: () => import('./features/organization/employees/employee-edit.component')
        .then(m => m.EmployeeEditComponent)
      },
      {
        path: 'employees/:id',
        loadComponent: () => import('./features/organization/employees/employee-detail.component')
          .then(m => m.EmployeeDetailComponent)
      },
      {
      path: 'salary-structure',
      loadComponent: () => import('./features/organization/salary-structure/salary-overview.component')
        .then(m => m.SalaryOverviewComponent)
    },
    {
      path: 'salary-structure/employee/:employeeId',
      loadComponent: () => import('./features/organization/salary-structure/salary-structure-list.component')
        .then(m => m.SalaryStructureListComponent)
    },
    {
      path: 'salary-structure/create/:employeeId',
      loadComponent: () => import('./features/organization/salary-structure/salary-structure-create.component')
        .then(m => m.SalaryStructureCreateComponent)
    },
    {
      path: 'salary-structure/edit/:id',
      loadComponent: () => import('./features/organization/salary-structure/salary-structure-edit.component')
        .then(m => m.SalaryStructureEditComponent)
    },
      {
        path: 'payment-requests',
        loadComponent: () => import('./features/organization/payment-requests/payment-request-list.component')
          .then(m => m.PaymentRequestListComponent)
      },
      {
        path: 'payment-requests/create',
        loadComponent: () => import('./features/organization/payment-requests/payment-request-create.component')
          .then(m => m.PaymentRequestCreateComponent)
      },
      {
        path: 'payment-requests/:id',
        loadComponent: () => import('./features/organization/payment-requests/payment-request-detail.component')
          .then(m => m.PaymentRequestDetailComponent)
      },
      {
        path: 'vendors',
        loadComponent: () => import('./features/organization/vendors/vendor-list.component')
          .then(m => m.VendorListComponent)
      },
      {
        path: 'vendors/create',
        loadComponent: () => import('./features/organization/vendors/vendor-create.component')
          .then(m => m.VendorCreateComponent)
      },
      {
        path: 'vendors/:id',
        loadComponent: () => import('./features/organization/vendors/vendor-detail.component')
        .then(m => m.VendorDetailComponent)
      },
      {
        path: 'concerns',
        loadComponent: () => import('./features/organization/concerns/org-concern-list.component')
          .then(m => m.OrgConcernListComponent)
      },
      {
        path: 'concerns/:id',
        loadComponent: () => import('./features/organization/concerns/org-concern-detail.component')
          .then(m => m.OrgConcernDetailComponent)
      },
      {
        path: 'reports',
        loadComponent: () => import('./features/organization/reports/reports.component')
          .then(m => m.ReportsComponent)
      }
    ]
  },

  // Employee routes
  {
    path: 'employee',
    canActivate: [authGuard, roleGuard],
    data: { role: 'EMPLOYEE' },
    children: [
      {
        path: 'dashboard',
        loadComponent: () => import('./features/employee/dashboard/emp-dashboard.component')
          .then(m => m.EmpDashboardComponent)
      },
      {
        path: 'profile',
        loadComponent: () => import('./features/employee/profile/profile.component')
          .then(m => m.ProfileComponent)
      },
      {
        path: 'salary-history',
        loadComponent: () => import('./features/employee/salary-history/salary-history.component')
          .then(m => m.SalaryHistoryComponent)
      },
      {
        path: 'concerns',
        loadComponent: () => import('./features/employee/concerns/emp-concern-list.component')
          .then(m => m.EmpConcernListComponent)
      },
      {
        path: 'concerns/create',
        loadComponent: () => import('./features/employee/concerns/emp-concern-create.component')
          .then(m => m.EmpConcernCreateComponent)
      },
      {
      path: 'concerns/:id',
      loadComponent: () => import('./features/employee/concerns/emp-concern-detail.component')
        .then(m => m.EmpConcernDetailComponent)
    }
    ]
  },

  // Default routes
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '**', redirectTo: '/login' }
];