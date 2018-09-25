import { Item } from './item.model';

/**
 * @class
 * Used to specify a link to appear under each item's name, in the first column in a [[GenericTableComponent]]
 * Most tables have a set of these options.
 * dddd
 * Each option will have the text that is actually shown, a function that returns
 * true iff that option should show up based on current system state, and a function to
 * be called when the text is clicked.
 *
 * TODO will need to be changed to make tables generic.
 *
 * A list of these is passed into the GenericTable class' constructor.
 */
export class SubMenuOptions {
  /**
   * @param text The string representing the option, shown to the user.
   * @param shouldAppear a function, taking an item as a parameter, which returns true or false based on whether this
   *                     option should show up. Often used to make an option disappear or change when an item is disabled.
   * @param action a function, potentially taking an Item as a parameter, to be called upon click.
   */
  constructor(
    public text: string,
    public shouldAppear: (item: Item) => boolean,
    public action: (item?: Item) => void
  ) {}
}
