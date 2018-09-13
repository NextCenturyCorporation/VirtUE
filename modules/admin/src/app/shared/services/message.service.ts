import { Injectable } from '@angular/core';

/**
 * #uncommented
 * @class
 * @extends
 */
@Injectable()
export class MessageService {
  
  /** #uncommented */
  messages: string[] = [];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  add(message: string): void {
    this.messages.push(message);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  clear(): void {
    this.messages = [];
  }
}
