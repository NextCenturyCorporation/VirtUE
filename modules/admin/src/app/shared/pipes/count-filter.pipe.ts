import { Pipe, PipeTransform } from '@angular/core';

/**
 * #uncommented
 * @class
 * @implements
 *
 * This appears to never be used.
 * Used to print the number of items that match a given filter.
 * This will probably get burninated, but if not, note that 'value' is the list to be filtered.
 */
@Pipe({
  name: 'count'
})
export class CountFilterPipe implements PipeTransform {

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  transform(value: any, filterString: string, propName: string): number {

    if (value.length === 0 || filterString === '') {
      return value.length;
    }
    const resultArray = [];

    for (const item of value) {
      if (item[propName].match(filterString)) {
        resultArray.push(item);
      }
    }

    return resultArray.length;
  }

}
