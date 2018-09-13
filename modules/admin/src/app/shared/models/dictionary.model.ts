
/**
 * #uncommented
 * @class
 * @extends
 */
 export class DictList<T> {

  /** #uncommented */
  private dict: Dict<T> = {};

  /** #uncommented */
  private list: T[] = [];

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  add(key: string, e: T): void {
    if (key in this.dict) {
      console.log("Key ", key + ": ", e, " already in dict.");
      return;
    }
    this.dict[key] = e;
    this.list.push(e);
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  has(key: string): boolean {
    if (key in this.dict) {
      return true;
    }
    return false;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  get(key: string): T {
    return this.dict[key];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  asList(): T[] {
    return this.list;
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  remove(key: string): void {
    if (!(key in this.dict)) {
      return;
    }
    let index: number = this.list.indexOf(this.dict[key], 0);
    if (index > -1) {
       this.list.splice(index, 1);
    }
    delete this.dict[key];
  }

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  clear(): void {
    this.dict = null;
    this.list = null;
  }
}

/**
 * #uncommented
 * @class
 */
export class Dict<T> {
    [key: string]: T;
}
