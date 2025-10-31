import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'  // Makes it available app-wide
})
export class StorageService {

  // Save data to localStorage
  setItem(key: string, value: any): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (error) {
      console.error('Error saving to localStorage', error);
    }
  }

  // Get data from localStorage
  getItem(key: string): any {
    try {
      const item = localStorage.getItem(key);
      return item ? JSON.parse(item) : null;
    } catch (error) {
      console.error('Error reading from localStorage', error);
      return null;
    }
  }

  // Remove item from localStorage
  removeItem(key: string): void {
    localStorage.removeItem(key);
  }

  // Clear all localStorage
  clear(): void {
    localStorage.clear();
  }

  // Token management
  saveToken(token: string): void {
    this.setItem('token', token);
  }

  getToken(): string | null {
    return this.getItem('token');
  }

  removeToken(): void {
    this.removeItem('token');
  }

  // User data management
  saveUser(user: any): void {
    this.setItem('user', user);
  }

  getUser(): any {
    return this.getItem('user');
  }

  removeUser(): void {
    this.removeItem('user');
  }
}