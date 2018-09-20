import { Injectable } from '@angular/core';

@Injectable()
export class MessageService {
  messages: string[] = [];

  /**
   * Record a message in the universal message list
   * @param message the string to be logged
   */
  add(message: string): void {
    this.messages.push(message);
  }

  /**
   * empties the list of messages
   */
  clear(): void {
    this.messages = [];
  }
}
