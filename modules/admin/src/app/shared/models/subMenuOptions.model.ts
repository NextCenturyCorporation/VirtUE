import { Item } from './item.model';

/**
 * @class
 * Used to specify a link to appear under each item's name, in a [[TextColumn]] in a [[GenericTableComponent]]
 * Most tables have a set of these options.
 *
 * Each option will have the text that is actually shown, a function that returns
 * true iff that option should show up based on current system state, and a function to
 * be called when the text is clicked.
 *
 * A list of these can be passed into any TextColumn constructor.
 */
export class SubMenuOptions {
  /**
   * @param text The string representing the option, shown to the user.
   * @param shouldAppear a function, taking some object as a parameter, which returns true or false based on whether this
   *                     option should show up. Often used to make an option disappear or change when a toggleable item is disabled.
   * @param action a function, potentially taking some object as a parameter, to be called upon click.
   */
  constructor(
    public text: string,
    public shouldAppear: (obj: any) => boolean,
    public action: (obj?: any) => void
  ) {}
}
