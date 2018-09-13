import { Pipe, PipeTransform } from '@angular/core';

/**
 * #uncommented
 * @class
 * @extends
 *
 * Appears to not be used. Appears to simply filter the
 * This will probably get burninated along with countFilter, but if not, note that 'value' is the list to be filtered.
 */
@Pipe({
  name: 'filter'
})
export class JsonFilterPipe implements PipeTransform {

  /**
   * #uncommented
   * @param
   *
   * @return
   */
  transform(value: any, filterString: string, propName: string) {

    if (value.length === 0 || filterString === '') {
      return value;
    }
    const resultArray = [];

    for (const item of value) {
      if (item[propName].toLowerCase().match(filterString)) {
        resultArray.push(item);
      }
    }

    return resultArray;
  }
}
